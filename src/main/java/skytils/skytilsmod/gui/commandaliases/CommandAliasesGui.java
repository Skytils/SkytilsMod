package skytils.skytilsmod.gui.commandaliases;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import skytils.skytilsmod.features.impl.handlers.CommandAliases;
import skytils.skytilsmod.gui.commandaliases.elements.CleanButton;
import skytils.skytilsmod.gui.commandaliases.elements.ScrollingCommandAliasesList;

import java.awt.*;
import java.io.IOException;
import java.util.Map.Entry;

/**
 * Taken from ChatShortcuts under MIT License
 * https://github.com/P0keDev/ChatShortcuts/blob/master/LICENSE
 * @author P0keDev
 */
public class CommandAliasesGui extends GuiScreen {

    ScrollingCommandAliasesList ScrollingCommandAliasesList;
    private int id;

    @Override
    public void initGui() {
        id = 0;
        ScrollingCommandAliasesList = new ScrollingCommandAliasesList(mc, width, height - 80, 20, height - 60, 0, 25, width, height);

        buttonList.clear();

        for(Entry<String, String> e : CommandAliases.aliases.entrySet()) {
            addAlias(e.getKey(), e.getValue());
        }

        buttonList.add(new CleanButton(9000, width/2 - 220, height - 40, "Save & Exit"));
        buttonList.add(new CleanButton(9001, width/2 + 20, height - 40, "Add Alias"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.id < 1000) {
            ScrollingCommandAliasesList.removeAlias(button.id);
            buttonList.remove(button);
        }

        if(button.id == 9000) {
            mc.thePlayer.closeScreen();
        }
        if(button.id == 9001) {
            addAlias();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, this.width, this.height, new Color(117, 115, 115, 25).getRGB(), new Color(0,0, 0,200).getRGB());
        ScrollingCommandAliasesList.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        ScrollingCommandAliasesList.keyTyped(typedChar, keyCode);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        ScrollingCommandAliasesList.updateScreen();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        ScrollingCommandAliasesList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        CommandAliases.aliases.clear();
        for(ScrollingCommandAliasesList.AliasListEntry e : ScrollingCommandAliasesList.getAliases()) {
            if(!e.getKey().isEmpty()) {
                CommandAliases.aliases.put(e.getKey(), e.getMessage());
            }
        }
        CommandAliases.saveAliases();
    }

    private void addAlias() {
        addAlias("", "");
    }

    private void addAlias(String key, String message) {
        GuiTextField keyField = new GuiTextField(1000 + id, fontRendererObj, width/2 - 220, 0, 100, 20);
        keyField.setText(key);
        GuiTextField messageField = new GuiTextField(2000 + id, fontRendererObj, width/2 - 100, 0, 270, 20);
        messageField.setMaxStringLength(255);
        messageField.setText(message);
        GuiButton removeButton = new CleanButton(id, width/2 + 175, 0, 50, 20, "Remove");

        buttonList.add(removeButton);
        ScrollingCommandAliasesList.addAlias(id, keyField, messageField, removeButton);

        id++;
    }

}