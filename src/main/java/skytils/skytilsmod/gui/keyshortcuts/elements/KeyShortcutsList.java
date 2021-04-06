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

package skytils.skytilsmod.gui.keyshortcuts.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.ArrayList;
import java.util.List;

/**
 * Adopted from ChatShortcuts under MIT License
 * https://github.com/P0keDev/ChatShortcuts/blob/master/LICENSE
 * @author P0keDev
 */
public class KeyShortcutsList extends GuiScrollingList {

    private final Minecraft mc;
    private final ArrayList<KeyShortcutEntry> keyShortcuts;
    public KeyShortcutEntry clickedShortcut;

    public KeyShortcutsList(Minecraft mc, int width, int height, int top, int bottom, int left, int entryHeight,
                            int screenWidth, int screenHeight) {
        super(mc, width, height, top, bottom, left, entryHeight, screenWidth, screenHeight);
        this.keyShortcuts = new ArrayList<>();
        this.mc = mc;
    }

    public void addShortcut(int id, int keyCode, GuiTextField command, GuiButton keyButton, GuiButton removeButton) {
        keyShortcuts.add(new KeyShortcutEntry(id, keyCode, command, keyButton, removeButton));
    }

    public void removeShortcut(int id) {
        for(KeyShortcutEntry e : keyShortcuts) {
            if(e.id == id) {
                keyShortcuts.remove(e);
                return;
            }
        }
    }

    public List<KeyShortcutEntry> getKeyShortcuts(){
        return keyShortcuts;
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (this.clickedShortcut != null) {
            if (keyCode == 1) {
                this.clickedShortcut.keyCode = 0;
            } else if (keyCode != 0) {
                this.clickedShortcut.keyCode = keyCode;
            }
            else if (typedChar > 0) {
                this.clickedShortcut.keyCode = typedChar + 256;
            }
            this.clickedShortcut = null;
        } else {
            for(KeyShortcutEntry e : keyShortcuts) {
                e.commandField.textboxKeyTyped(typedChar, keyCode);
            }
        }
    }

    public void updateScreen() {
        for(KeyShortcutEntry e : keyShortcuts) {
            e.commandField.updateCursorCounter();
            e.keyButton.displayString = GameSettings.getKeyDisplayString(e.keyCode);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (clickedShortcut != null) {
            clickedShortcut.keyCode = -100 + mouseButton;
            clickedShortcut = null;
        } else {
            for(KeyShortcutEntry e : keyShortcuts) {
                e.commandField.mouseClicked(mouseX, mouseY, mouseButton);
                if (e.keyButton.mousePressed(mc, mouseX, mouseY)) {
                    this.clickedShortcut = e;
                }
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    private void resetButtons() {
        for(KeyShortcutEntry e : keyShortcuts) {
            e.keyButton.visible = false;
            e.removeButton.visible = false;
        }
    }

    @Override
    protected int getSize() {
        return keyShortcuts.size();
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
        KeyShortcutEntry ks = keyShortcuts.get(slotIdx);
        boolean visible = slotTop >= top && slotTop + slotHeight <= bottom;
        ks.keyButton.visible = visible;
        ks.removeButton.visible = visible;

        if(visible) {
            ks.commandField.yPosition = slotTop;
            ks.keyButton.yPosition = slotTop;
            ks.removeButton.yPosition = slotTop;

            ks.commandField.drawTextBox();

            ks.keyButton.displayString = GameSettings.getKeyDisplayString(ks.keyCode);

            boolean pressed = this.clickedShortcut == ks;
            boolean reused = false;
            if (ks.keyCode != 0) {
                for (KeyBinding keybinding : this.mc.gameSettings.keyBindings) {
                    if (keybinding.getKeyCode() == ks.keyCode) {
                        reused = true;
                        break;
                    }
                }
                if (!reused) {
                    for (KeyShortcutEntry entry : this.keyShortcuts) {
                        if (entry.keyCode != 0 && entry != ks && entry.keyCode == ks.keyCode) {
                            reused = true;
                            break;
                        }
                    }
                }
            }

            if (pressed) {
                ks.keyButton.displayString = EnumChatFormatting.WHITE + "> " + EnumChatFormatting.YELLOW + ks.keyButton.displayString + EnumChatFormatting.WHITE + " <";
            } else if (reused) {
                ks.keyButton.displayString = EnumChatFormatting.RED + ks.keyButton.displayString;
            }

            ks.keyButton.drawButton(this.mc, this.mouseX, this.mouseY);


            mc.fontRendererObj.drawString(":", ks.commandField.xPosition + ks.commandField.width + 6, slotTop + 5, 0xFFFFFF);
        }

    }

    @Override
    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2) { }

    public static class KeyShortcutEntry {

        private final GuiTextField commandField;
        private final GuiButton keyButton;
        private final GuiButton removeButton;
        private final int id;
        private int keyCode;

        public KeyShortcutEntry(int id, int keyCode, GuiTextField commandField, GuiButton keyButton, GuiButton removeButton) {
            this.id = id;
            this.keyCode = keyCode;
            this.commandField = commandField;
            this.keyButton = keyButton;
            this.removeButton = removeButton;
        }

        public String getCommand() {
            return commandField.getText();
        }

        public int getKeyCode() {
            return keyCode;
        }

    }

}