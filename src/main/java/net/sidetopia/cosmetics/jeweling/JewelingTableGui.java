package net.sidetopia.cosmetics.jeweling;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class JewelingTableGui extends SimpleGui {
    private static final int JEWEL_SLOT = 9;
    private static final int EQUIPMENT_SLOT = 11;
    private static final int RESULT_SLOT = 14;
    private static final double MAX_DISTANCE_SQUARED = 64.0D;

    private final ServerWorld world;
    private final BlockPos tablePos;
    private final SimpleInventory inputInventory = new SimpleInventory(2);
    private final SimpleInventory resultInventory = new SimpleInventory(1);
    private JewelingEvaluation evaluation = JewelingEvaluation.failure(
            JewelingStatus.EMPTY,
            Text.empty()
    );
    private JewelingStatus lastNotifiedStatus = JewelingStatus.EMPTY;
    private boolean updatingPreview;
    private boolean processingCraft;
    private boolean returnedInputs;

    public JewelingTableGui(
            ServerPlayerEntity player,
            ServerWorld world,
            BlockPos tablePos
    ) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.world = world;
        this.tablePos = tablePos.toImmutable();

        setTitle(Text.literal("\uE000\uE001").setStyle(
                Style.EMPTY
                        .withFont(Identifier.of("teapot_cosmetics", "default"))
                        .withColor(0xFFFFFF)
        ));

        GuiElementBuilder placeholder = GuiElementBuilder.from(Items.AIR.getDefaultStack());
        for (int index = 0; index < 27; index++) {
            setSlot(index, placeholder);
        }
        setSlotRedirect(JEWEL_SLOT, new JewelSlot(inputInventory, 0));
        setSlotRedirect(EQUIPMENT_SLOT, new EquipmentSlot(inputInventory, 1));
        setSlotRedirect(RESULT_SLOT, new PreviewSlot(resultInventory, 0));
        inputInventory.addListener(ignored -> updatePreview());
    }

    @Override
    public void afterOpen() {
        JewelingTableManager.register(this);
        updatePreview();
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        if (index == RESULT_SLOT) {
            handleResultClick(type);
            return false;
        }
        return true;
    }

    @Override
    public boolean onClick(
            int index,
            ClickType type,
            SlotActionType action,
            GuiElementInterface element
    ) {
        return false;
    }

    @Override
    public ItemStack quickMove(int index) {
        if (index == RESULT_SLOT) {
            handleResultClick(ClickType.MOUSE_LEFT_SHIFT);
            return ItemStack.EMPTY;
        }
        return super.quickMove(index);
    }

    @Override
    public void onTick() {
        if (!isTableStillUsable()) {
            close();
        }
    }

    @Override
    public void onClose() {
        returnInputsOnce();
        JewelingTableManager.unregister(this);
        super.onClose();
    }

    public void closeForReload() {
        if (!returnedInputs) {
            player.sendMessage(Text.literal(JewelingConfig.get().messages.dataReloaded), true);
        }
        if (isOpen()) {
            close();
        } else {
            returnInputsOnce();
            JewelingTableManager.unregister(this);
        }
    }

    private void updatePreview() {
        if (updatingPreview || processingCraft || returnedInputs) return;
        updatingPreview = true;
        try {
            resultInventory.setStack(0, ItemStack.EMPTY);
            evaluation = JewelingTableManager.service().evaluate(
                    player,
                    inputInventory.getStack(0),
                    inputInventory.getStack(1)
            );
            if (evaluation.ready()) {
                resultInventory.setStack(0, evaluation.output().copy());
            }
            notifyStatusChange(evaluation);
            if (screenHandler != null) {
                screenHandler.syncState();
            }
        } finally {
            updatingPreview = false;
        }
    }

    private void notifyStatusChange(JewelingEvaluation current) {
        if (current.status() == lastNotifiedStatus) return;
        lastNotifiedStatus = current.status();
        if (current.status() != JewelingStatus.EMPTY
                && current.status() != JewelingStatus.READY
                && !current.failureMessage().getString().isEmpty()) {
            player.sendMessage(current.failureMessage(), true);
        }
    }

    private void handleResultClick(ClickType type) {
        if (processingCraft || returnedInputs) return;
        boolean shift = type == ClickType.MOUSE_LEFT_SHIFT
                || type == ClickType.MOUSE_RIGHT_SHIFT;
        boolean cursorTransfer = type == ClickType.MOUSE_LEFT || type == ClickType.MOUSE_RIGHT;
        if (!shift && !cursorTransfer) return;

        JewelingEvaluation fresh = JewelingTableManager.service().evaluate(
                player,
                inputInventory.getStack(0),
                inputInventory.getStack(1)
        );
        ItemStack preview = resultInventory.getStack(0);
        if (!fresh.ready() || !ItemStack.areEqual(fresh.output(), preview)) {
            updatePreview();
            return;
        }

        int destinationSlot = -1;
        if (shift) {
            destinationSlot = player.getInventory().getEmptySlot();
            if (destinationSlot < 0) {
                player.sendMessage(
                        Text.literal(JewelingConfig.get().messages.inventoryFull),
                        true
                );
                return;
            }
        } else if (!canPlaceOnCursor(fresh.output())) {
            return;
        }

        processingCraft = true;
        try {
            ItemStack output = fresh.output().copy();
            consumeInputs();
            resultInventory.setStack(0, ItemStack.EMPTY);

            if (shift) {
                player.getInventory().setStack(destinationSlot, output);
                player.getInventory().markDirty();
            } else {
                putOnCursor(output);
            }

            JewelingConfig config = JewelingConfig.get();
            if (config.grantKnowledgeBoundXp) {
                JewelingTableManager.service().knowledgeBound().grantSuccessXp(player);
            }
            playSuccessFeedback(config);
            player.sendMessage(Text.literal(config.messages.success), true);
        } finally {
            processingCraft = false;
            updatePreview();
        }
    }

    private boolean canPlaceOnCursor(ItemStack output) {
        ItemStack cursor = player.currentScreenHandler.getCursorStack();
        return cursor.isEmpty()
                || (ItemStack.areItemsAndComponentsEqual(cursor, output)
                && cursor.getCount() < cursor.getMaxCount());
    }

    private void putOnCursor(ItemStack output) {
        ItemStack cursor = player.currentScreenHandler.getCursorStack();
        if (cursor.isEmpty()) {
            player.currentScreenHandler.setCursorStack(output);
        } else {
            cursor.increment(output.getCount());
            player.currentScreenHandler.setCursorStack(cursor);
        }
    }

    private void consumeInputs() {
        ItemStack jewel = inputInventory.getStack(0);
        jewel.decrement(1);
        if (jewel.isEmpty()) {
            inputInventory.setStack(0, ItemStack.EMPTY);
        } else {
            inputInventory.markDirty();
        }
        inputInventory.setStack(1, ItemStack.EMPTY);
    }

    private void playSuccessFeedback(JewelingConfig config) {
        SoundEvent sound = Registries.SOUND_EVENT.get(config.successSoundId());
        if (sound != null) {
            world.playSound(
                    null,
                    tablePos,
                    sound,
                    SoundCategory.BLOCKS,
                    config.successSoundVolume,
                    config.successSoundPitch
            );
        }
        world.spawnParticles(
                ParticleTypes.ENCHANT,
                tablePos.getX() + 0.5D,
                tablePos.getY() + 1.0D,
                tablePos.getZ() + 0.5D,
                8,
                0.35D,
                0.25D,
                0.35D,
                0.02D
        );
    }

    private boolean isTableStillUsable() {
        if (returnedInputs
                || player.isDisconnected()
                || !player.isAlive()
                || player.getServerWorld() != world
                || !JewelingConfig.get().jewelingEnabled) {
            return false;
        }
        if (player.squaredDistanceTo(Vec3d.ofCenter(tablePos)) > MAX_DISTANCE_SQUARED) {
            return false;
        }
        return Registries.BLOCK.getId(world.getBlockState(tablePos).getBlock())
                .equals(JewelingConfig.get().gemTableBlockId());
    }

    private void returnInputsOnce() {
        if (returnedInputs) return;
        returnedInputs = true;
        updatingPreview = true;
        try {
            resultInventory.setStack(0, ItemStack.EMPTY);
            ItemStack jewel = inputInventory.getStack(0).copy();
            ItemStack equipment = inputInventory.getStack(1).copy();
            inputInventory.clear();
            returnStack(jewel);
            returnStack(equipment);
        } finally {
            updatingPreview = false;
        }
    }

    private void returnStack(ItemStack stack) {
        if (stack.isEmpty()) return;
        player.getInventory().insertStack(stack);
        if (!stack.isEmpty()) {
            player.dropItem(stack, false);
        }
    }

    private static final class JewelSlot extends Slot {
        private JewelSlot(Inventory inventory, int index) {
            super(inventory, index, 0, 0);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.isIn(JewelingTags.jewels())
                    && JewelingTableManager.recipes().hasRecipeFor(stack);
        }
    }

    private static final class EquipmentSlot extends Slot {
        private EquipmentSlot(Inventory inventory, int index) {
            super(inventory, index, 0, 0);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return stack.getCount() == 1
                    && stack.isIn(JewelingTags.jewelable())
                    && JewelingTableManager.recipes().matchesAnyEquipment(stack);
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }
    }

    private static final class PreviewSlot extends Slot {
        private PreviewSlot(Inventory inventory, int index) {
            super(inventory, index, 0, 0);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return false;
        }

        @Override
        public int getMaxItemCount() {
            return 1;
        }
    }
}
