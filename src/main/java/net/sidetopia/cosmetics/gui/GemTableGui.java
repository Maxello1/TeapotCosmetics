package net.sidetopia.cosmetics.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;

import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Style;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class GemTableGui extends SimpleGui {

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
            // Leave slots 1, 3, and 6 open (Row 1)
            // Slot 1 = row 1, col 2
            // Slot 3 = row 1, col 4
            // Slot 6 = row 1, col 7
            if (i != 1 && i != 3 && i != 6) {
                this.setSlot(i, placeholder);
            }
        }
    }
}
