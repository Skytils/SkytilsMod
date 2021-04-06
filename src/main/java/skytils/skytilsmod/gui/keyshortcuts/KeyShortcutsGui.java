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

package skytils.skytilsmod.gui.keyshortcuts;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.settings.GameSettings;
import skytils.skytilsmod.features.impl.handlers.KeyShortcuts;
import skytils.skytilsmod.gui.commandaliases.elements.CleanButton;
import skytils.skytilsmod.gui.keyshortcuts.elements.KeyShortcutsList;

import java.awt.*;
import java.io.IOException;
import java.util.Map.Entry;

/**
 * Adopted from ChatShortcuts under MIT License
 * https://github.com/P0keDev/ChatShortcuts/blob/master/LICENSE
 * @author P0keDev
 */
public class KeyShortcutsGui extends GuiScreen {

    KeyShortcutsList KeyShortcutsList;
    private int id;

    @Override
    public void initGui() {
        id = 0;
        KeyShortcutsList = new KeyShortcutsList(mc, width, height - 80, 20, height - 60, 0, 25, width, height);

        buttonList.clear();

        for(Entry<String, Integer> e : KeyShortcuts.shortcuts.entrySet()) {
            addShortcut(e.getKey(), e.getValue());
        }


        buttonList.add(new CleanButton(9000, width/2 - 220, height - 40, "Save & Exit"));
        buttonList.add(new CleanButton(9001, width/2 + 20, height - 40, "Add Shortcut"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id < 1000) {
            KeyShortcutsList.removeShortcut(button.id);
            buttonList.remove(button);
        }

        if(button.id == 9000) {
            if (mc.thePlayer != null) mc.thePlayer.closeScreen();
            else mc.displayGuiScreen(null);
        }

        if(button.id == 9001) {
            addBlankShortcut();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, this.width, this.height, new Color(117, 115, 115, 25).getRGB(), new Color(0,0, 0,200).getRGB());
        KeyShortcutsList.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (KeyShortcutsList.clickedShortcut == null) super.keyTyped(typedChar, keyCode);
        KeyShortcutsList.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        KeyShortcutsList.updateScreen();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        KeyShortcutsList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        KeyShortcutsList.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        KeyShortcuts.shortcuts.clear();
        for(KeyShortcutsList.KeyShortcutEntry e : KeyShortcutsList.getKeyShortcuts()) {
            if(!e.getCommand().isEmpty()) {
                KeyShortcuts.shortcuts.put(e.getCommand(), e.getKeyCode());
            }
        }
        KeyShortcuts.saveShortcuts();
    }

    private void addBlankShortcut() {
        addShortcut("", 0);
    }

    private void addShortcut(String command, int keyCode) {
        GuiTextField keyField = new GuiTextField(1000 + id, fontRendererObj, width/2 - 220, 0, 270, 20);
        keyField.setText(command);
        keyField.setMaxStringLength(255);
        GuiButton keybindingButton = new GuiButton(id, width/2 + 65, 0, 100, 20, GameSettings.getKeyDisplayString(keyCode));
        GuiButton removeButton = new CleanButton(id, width/2 + 175, 0, 50, 20, "Remove");

        buttonList.add(removeButton);
        KeyShortcutsList.addShortcut(id, keyCode, keyField, keybindingButton, removeButton);

        id++;
    }

}