package skytils.skytilsmod.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.gui.commandaliases.CommandAliasesGui;
import skytils.skytilsmod.gui.commandaliases.elements.CleanButton;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.awt.*;
import java.io.IOException;

public class OptionsGui extends GuiScreen {

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new CleanButton(0, width / 2 - 100, this.height / 4 + 100, 200, 20, "Config"));
        buttonList.add(new CleanButton(1, width / 2 - 100, this.height / 4 + 125, 200, 20, "Edit Aliases"));
        buttonList.add(new CleanButton(2, width / 2 - 100, this.height / 4 + 150, 200, 20, "Edit Locations"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        SmartFontRenderer fr = ScreenRenderer.fontRenderer;

        drawGradientRect(0, 0, this.width, this.height, new Color(117, 115, 115, 25).getRGB(), new Color(0,0, 0,200).getRGB());

        float scale = 12.5f;
        GlStateManager.scale(scale, scale, 0);
        fr.drawString("Skytils", (this.width / 2f) / scale, (this.height / 4f - 75) / scale, CommonColors.RAINBOW, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
        GlStateManager.scale(1/scale, 1/scale, 0);

        for (GuiButton button : buttonList) {
            button.drawButton(mc, mouseX, mouseY);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                mc.displayGuiScreen(Skytils.config.gui());
                break;
            case 1:
                mc.displayGuiScreen(new CommandAliasesGui());
                break;
            case 2:
                mc.displayGuiScreen(new LocationEditGui());
                break;
        }
    }
}
