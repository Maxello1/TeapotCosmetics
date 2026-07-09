package net.sidetopia.cosmetics.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GemTableGui extends SimpleGui {

    public GemTableGui(ServerPlayerEntity player) {
        // 3 rows chest GUI
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        
        // Use the font trick: 
        // \uE000 is our negative space (-8 pixels) to align to the left edge of the chest GUI
        // \uE001 is our massive 256x256 custom GUI texture
        this.setTitle(Text.literal("\uE000\uE001"));

        // Fill all 27 slots with a placeholder item to block clicking,
        // EXCEPT for the 3 slots we want the player to interact with.
        // We'll use Light Gray Glass Panes so they're easy to see for alignment testing.
        GuiElementBuilder placeholder = GuiElementBuilder.from(Items.LIGHT_GRAY_STAINED_GLASS_PANE)
            .setName(Text.literal(" "))
            .hideDefaultTooltip();

        for (int i = 0; i < 27; i++) {
            // Leave slots 10, 13, and 16 open (these are just guesses for the transparent squares)
            // Slot 10 = row 2, col 2
            // Slot 13 = row 2, col 5
            // Slot 16 = row 2, col 8
            if (i != 10 && i != 13 && i != 16) {
                this.setSlot(i, placeholder);
            }
        }
    }
}
