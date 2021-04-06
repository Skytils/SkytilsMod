/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.gui.commandaliases.elements;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

/**
 * Taken from ChatShortcuts under MIT License
 * https://github.com/P0keDev/ChatShortcuts/blob/master/LICENSE
 * @author P0keDev
 */
public class ScrollingCommandAliasesList extends GuiScrollingList {

    private final Minecraft mc;
    private final ArrayList<AliasListEntry> aliases;

    public ScrollingCommandAliasesList(Minecraft mc, int width, int height, int top, int bottom, int left, int entryHeight,
                                       int screenWidth, int screenHeight) {
        super(mc, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
        this.aliases = new ArrayList<>();
        this.mc = mc;
    }

    public void addAlias(int id, GuiTextField key, GuiTextField message, GuiButton removeButton) {
        aliases.add(new AliasListEntry(id, key, message, removeButton));
    }

    public void removeAlias(int id) {
        for(AliasListEntry e : aliases) {
            if(e.id == id) {
                aliases.remove(e);
                return;
            }
        }
    }

    public List<AliasListEntry> getAliases(){
        return aliases;
    }

    public void keyTyped(char typedChar, int keyCode) {
        for(AliasListEntry e : aliases) {
            e.key.textboxKeyTyped(typedChar, keyCode);
            e.message.textboxKeyTyped(typedChar, keyCode);
        }
    }

    public void updateScreen() {
        for(AliasListEntry e : aliases) {
            e.key.updateCursorCounter();
            e.message.updateCursorCounter();
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for(AliasListEntry e : aliases) {
            e.key.mouseClicked(mouseX, mouseY, mouseButton);
            e.message.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    private void resetButtons() {
        for(AliasListEntry e : aliases) {
            e.removeButton.visible = false;
        }
    }

    @Override
    protected int getSize() {
        return aliases.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) { }

    @Override
    protected boolean isSelected(int index) {
        return false;
    }

    @Override
    protected void drawBackground() { }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        resetButtons();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        AliasListEntry alias = aliases.get(slotIdx);
        boolean visible = slotTop >= top && slotTop + slotHeight <= bottom;
        alias.removeButton.visible = visible;

        if(visible) {
            alias.key.yPosition = slotTop;
            alias.message.yPosition = slotTop;
            alias.removeButton.yPosition = slotTop;

            alias.key.drawTextBox();
            alias.message.drawTextBox();

            mc.fontRendererObj.drawString(":", alias.key.xPosition + alias.key.width + 10, slotTop + 5, 0xFFFFFF);
        }

    }

    @Override
    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2) { }

    public static class AliasListEntry {

        private final GuiTextField key;
        private final GuiTextField message;
        private final GuiButton removeButton;
        private final int id;

        public AliasListEntry(int id, GuiTextField key, GuiTextField message, GuiButton removeButton) {
            this.id = id;
            this.key = key;
            this.message = message;
            this.removeButton = removeButton;
        }

        public String getKey() {
            return key.getText();
        }

        public String getMessage() {
            return message.getText();
        }
    }

}