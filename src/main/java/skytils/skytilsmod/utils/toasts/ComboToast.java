package skytils.skytilsmod.utils.toasts;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;

import java.nio.FloatBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComboToast implements IToast<ComboToast> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("skytils:gui/toast.png");
    private static final Pattern comboPattern = Pattern.compile("(§r§.§l)\\+(\\d+ Kill Combo) (§r§8.+)");
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
    private final long maxDrawTime;
    private String length;
    private String buff;
    private ResourceLocation buffTexture = new ResourceLocation("skytils:combo/comboFail.png");

    public ComboToast(String input) {
        this.maxDrawTime = Skytils.config.toastTime;
        Matcher comboMatcher = comboPattern.matcher(input);
        if (comboMatcher.find()) {
            this.length = comboMatcher.group(1) + comboMatcher.group(2);
            this.buff = comboMatcher.group(3);
        }
        if (input.contains("Magic Find")) {
            this.buffTexture = new ResourceLocation("skytils:combo/luck.png");
        } else if (input.contains("coins per kill")) {
            this.buffTexture = new ResourceLocation("skytils:combo/coin.png");
        } else if (input.contains("Combat Exp")) {
            this.buffTexture = new ResourceLocation("skytils:combo/combat.png");
        }
    }

    @Override
    public IToast.Visibility draw(GuiToast toastGui, long delta) {
        toastGui.mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);

        toastGui.mc.fontRendererObj.drawStringWithShadow(this.length, 30, 7, 16777215);

        GuiToast.drawSubline(toastGui, delta, 0L, this.maxDrawTime, this.buffer, this.buff, false);
        RenderHelper.enableGUIStandardItemLighting();

        RenderUtil.renderTexture(this.buffTexture, 8, 8);

        GlStateManager.disableLighting();

        return delta >= this.maxDrawTime ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
    }
}
