package skytils.skytilsmod.utils.toasts;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Taken from Skyblockcatia under MIT License
 * Modified
 * https://github.com/SteveKunG/SkyBlockcatia/blob/1.8.9/LICENSE.md
 * @author SteveKunG
 */
public class GuiToast extends Gui
{
    protected final Minecraft mc;
    private final GuiToast.ToastInstance<?>[] visible = new GuiToast.ToastInstance[5];
    private final Deque<IToast> toastsQueue = new ArrayDeque<>();

    public GuiToast(Minecraft mc)
    {
        this.mc = mc;
    }

    public void drawToast(ScaledResolution resolution)
    {
        {
            RenderHelper.disableStandardItemLighting();

            for (int i = 0; i < this.visible.length; ++i)
            {
                GuiToast.ToastInstance<?> toastinstance = this.visible[i];

                if (toastinstance != null && toastinstance.render(resolution.getScaledWidth(), i))
                {
                    this.visible[i] = null;
                }
                if (this.visible[i] == null && !this.toastsQueue.isEmpty())
                {
                    this.visible[i] = new GuiToast.ToastInstance(this.toastsQueue.removeFirst());
                }
            }
        }
    }

    public static void drawLongItemName(GuiToast toastGui, long delta, long firstDrawTime, long maxDrawTime, FloatBuffer buffer, String itemName, boolean shadow)
    {
        long minDraw = (long)(maxDrawTime * 0.1D);
        long maxDraw = maxDrawTime + 500L;
        long backwardDraw = (long)(maxDrawTime * 0.5D);
        long textSpeed = 1500L + (long)(maxDrawTime * 0.1D);
        int x = 30;
        int textWidth = toastGui.mc.fontRendererObj.getStringWidth(itemName);
        int maxSize = textWidth - 135;
        long timeElapsed = delta - firstDrawTime - minDraw;
        long timeElapsed2 = maxDraw - delta - backwardDraw;
        int maxTextLength = 125;

        if (textWidth > maxSize && textWidth > maxTextLength)
        {
            if (timeElapsed > 0)
            {
                x = Math.max((int) (-textWidth * timeElapsed / textSpeed + x), -maxSize + 16);
            }

            int backward = Math.max(Math.min((int) -(textWidth * timeElapsed2 / textSpeed), 30), -maxSize + 16);

            if (timeElapsed > timeElapsed2)
            {
                x = backward;
            }
        }

        ScaledResolution res = new ScaledResolution(toastGui.mc);
        double height = res.getScaledHeight();
        double scale = res.getScaleFactor();
        float[] trans = new float[16];

        buffer.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buffer);
        buffer.get(trans);
        float xpos = trans[12];

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) ((xpos + 29) * scale), (int) ((height - 196) * scale), (int) (126 * scale), (int) (195 * scale));

        if (shadow)
        {
            toastGui.mc.fontRendererObj.drawStringWithShadow(itemName, x, 18, 0xFFFFFF);
        }
        else
        {
            toastGui.mc.fontRendererObj.drawString(itemName, x, 18, 0xFFFFFF);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Nullable
    public <T extends IToast> T getToast(Class<? extends T> clazz, Object obj)
    {
        for (GuiToast.ToastInstance<?> ins : this.visible)
        {
            if (ins != null && clazz.isAssignableFrom(ins.getToast().getClass()) && ins.getToast().getType().equals(obj))
            {
                return (T)ins.getToast();
            }
        }
        for (IToast toast : this.toastsQueue)
        {
            if (clazz.isAssignableFrom(toast.getClass()) && toast.getType().equals(obj))
            {
                return (T)toast;
            }
        }
        return null;
    }

    public void clear()
    {
        Arrays.fill(this.visible, null);
        this.toastsQueue.clear();
    }

    public boolean add(IToast toast)
    {
        return this.toastsQueue.add(toast);
    }

    public static void renderItem(ItemStack itemStack, int x, int y) {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(itemStack, x, y);
    }

    public static void renderTexture(ResourceLocation texture, int x, int y) {
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
    }

    class ToastInstance<T extends IToast>
    {
        private final T toast;
        private long animationTime;
        private long visibleTime;
        private IToast.Visibility visibility;

        private ToastInstance(T toast)
        {
            this.animationTime = -1L;
            this.visibleTime = -1L;
            this.visibility = IToast.Visibility.SHOW;
            this.toast = toast;
        }

        public T getToast()
        {
            return this.toast;
        }

        private float getVisibility(long delta)
        {
            float f = MathHelper.clamp_float((delta - this.animationTime) / 600.0F, 0.0F, 1.0F);
            f = f * f;
            return this.visibility == IToast.Visibility.HIDE ? 1.0F - f : f;
        }

        public boolean render(int x, int z)
        {
            long i = Minecraft.getSystemTime();

            if (this.animationTime == -1L)
            {
                this.animationTime = i;
            }
            if (this.visibility == IToast.Visibility.SHOW && i - this.animationTime <= 600L)
            {
                this.visibleTime = i;
            }

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.translate(x - 160.0F * this.getVisibility(i), z * 32, 500 + z);
            IToast.Visibility itoast$visibility = this.toast.draw(GuiToast.this, i - this.visibleTime);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();

            if (itoast$visibility != this.visibility)
            {
                this.animationTime = i - (int)((1.0F - this.getVisibility(i)) * 600.0F);
                this.visibility = itoast$visibility;
            }
            return this.visibility == IToast.Visibility.HIDE && i - this.animationTime > 600L;
        }
    }
}
