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

        // Fill unused slots with an invisible item to block clicking.
        var invisibleItem = Registries.ITEM.get(Identifier.of("teapot_cosmetics", "invisible_filler"));
        GuiElementBuilder placeholder = GuiElementBuilder.from(invisibleItem.getDefaultStack())
            .setName(Text.literal(" "))
            .hideDefaultTooltip();

        // 9x3 chest has 27 slots
        for (int i = 0; i < 27; i++) {
            if (i == 10) {
                this.setSlotRedirect(10, new Slot(craftingInv, 0, 0, 0));
            } else if (i == 12) {
                this.setSlotRedirect(12, new Slot(craftingInv, 1, 0, 0));
            } else if (i == 15) {
                this.setSlotRedirect(15, new Slot(craftingInv, 2, 0, 0));
            } else {
                this.setSlot(i, placeholder);
            }
        }
    }
}
