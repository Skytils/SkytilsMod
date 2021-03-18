package skytils.skytilsmod.utils.toasts;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;

import java.nio.FloatBuffer;

public class ReviveStoneToast implements IToast<SuperboomToast> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("skytils:gui/toast.png");
    private static final ResourceLocation REVIVE = new ResourceLocation("skytils:toasts/revive.png");
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
    private final long maxDrawTime;

    public ReviveStoneToast() {
        this.maxDrawTime = Skytils.config.toastTime;
    }

    @Override
    public Visibility draw(GuiToast toastGui, long delta) {
        toastGui.mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);

        toastGui.mc.fontRendererObj.drawStringWithShadow("§6Revive Stone", 30, 7, 16777215);

        GuiToast.drawSubline(toastGui, delta, 0L, this.maxDrawTime, this.buffer, "", false);
        RenderHelper.enableGUIStandardItemLighting();

        RenderUtil.renderTexture(REVIVE, 8, 8);

        GlStateManager.disableLighting();

        return delta >= this.maxDrawTime ? Visibility.HIDE : Visibility.SHOW;
    }
}
