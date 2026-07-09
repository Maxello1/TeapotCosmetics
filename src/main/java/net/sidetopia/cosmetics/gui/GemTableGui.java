package net.sidetopia.cosmetics.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;

import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.inventory.SimpleInventory;

public class GemTableGui extends SimpleGui {

    // Dedicated inventory for the 3 interactive slots
    private final SimpleInventory craftingInv = new SimpleInventory(3);

    public GemTableGui(ServerPlayerEntity player) {
        // 3 rows chest GUI
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        
        // Use the font trick: 
        // \uE000 is our negative space (-8 pixels) to align to the left edge of the chest GUI
        // \uE001 is our massive custom GUI texture
        this.setTitle(Text.literal("\uE000\uE001").setStyle(Style.EMPTY.withFont(Identifier.of("teapot_cosmetics", "default")).withColor(0xFFFFFF)));

        // Fill unused slots with an empty item to block clicking (SimpleGui handles the locking)
        GuiElementBuilder placeholder = GuiElementBuilder.from(Items.AIR.getDefaultStack());

        // 9x3 chest has 27 slots
        for (int i = 0; i < 27; i++) {
            if (i == 9) {
                this.setSlotRedirect(9, new Slot(craftingInv, 0, 0, 0));
            } else if (i == 11) {
                this.setSlotRedirect(11, new Slot(craftingInv, 1, 0, 0));
            } else if (i == 14) {
                this.setSlotRedirect(14, new Slot(craftingInv, 2, 0, 0));
            } else {
                this.setSlot(i, placeholder);
            }
        }
    }

    @Override
    public void onClose() {
        if (this.player != null) {
            for (int i = 0; i < craftingInv.size(); i++) {
                net.minecraft.item.ItemStack stack = craftingInv.getStack(i);
                if (!stack.isEmpty()) {
                    this.player.getInventory().insertStack(stack);
                    if (!stack.isEmpty()) {
                        this.player.dropItem(stack, false);
                    }
                    craftingInv.setStack(i, net.minecraft.item.ItemStack.EMPTY);
                }
            }
        }
        super.onClose();
    }
}
