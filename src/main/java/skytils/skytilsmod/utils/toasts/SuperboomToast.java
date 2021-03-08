package skytils.skytilsmod.utils.toasts;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;

import java.nio.FloatBuffer;

public class SuperboomToast implements IToast<SuperboomToast> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("skytils:gui/toast.png");
    private static final ItemStack superBoom = new ItemStack(Item.getItemFromBlock(Blocks.tnt));
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);
    private final long maxDrawTime;

    public SuperboomToast() {
        this.maxDrawTime = Skytils.config.toastTime;
        superBoom.addEnchantment(Enchantment.unbreaking, 1);
    }

    @Override
    public Visibility draw(GuiToast toastGui, long delta) {
        toastGui.mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 160, 32, 160, 32);

        toastGui.mc.fontRendererObj.drawStringWithShadow("§9Superboom TNT", 30, 7, 16777215);

        GuiToast.drawSubline(toastGui, delta, 0L, this.maxDrawTime, this.buffer, "", false);
        RenderHelper.enableGUIStandardItemLighting();

        RenderUtil.renderItem(superBoom, 8, 8);

        GlStateManager.disableLighting();

        return delta >= this.maxDrawTime ? Visibility.HIDE : Visibility.SHOW;
    }
}
