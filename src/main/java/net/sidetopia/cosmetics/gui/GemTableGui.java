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
        // \uE001 is our massive 256x256 custom GUI texture
        this.setTitle(Text.literal("\uE000\uE001").setStyle(Style.EMPTY.withFont(Identifier.of("teapot_cosmetics", "default")).withColor(0xFFFFFF)));

        // Fill unused slots with an invisible item to block clicking.
        var invisibleItem = Registries.ITEM.get(Identifier.of("teapot_cosmetics", "invisible_filler"));
        GuiElementBuilder placeholder = GuiElementBuilder.from(invisibleItem.getDefaultStack())
            .setName(Text.literal(" "))
            .hideDefaultTooltip();

        for (int i = 0; i < 27; i++) {
            // Leave slots 10, 12, and 15 open (based on the transparent squares in the GUI)
            // Slot 10 = row 2, col 2
            // Slot 12 = row 2, col 4
            // Slot 15 = row 2, col 7
            if (i != 10 && i != 12 && i != 15) {
                this.setSlot(i, placeholder);
            }
        }
    }
}
