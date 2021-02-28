package skytils.skytilsmod.mixins;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiRenderItemEvent;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {

    @Shadow protected abstract void renderModel(IBakedModel model, int color);

    @Inject(method = "renderItemIntoGUI(Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"))
    private void renderRarity(ItemStack itemStack, int xPosition, int yPosition, CallbackInfo info)
    {
        if (Skytils.config.showItemRarity) {
            RenderUtil.renderRarity(itemStack, xPosition, yPosition);
        }
    }

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("RETURN"))
    private void renderItemOverlayPost(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new GuiRenderItemEvent.RenderOverlayEvent.Post(fr, stack, xPosition, yPosition, text));
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/GlStateManager.scale(FFF)V", shift = At.Shift.AFTER))
    private void renderItemPre(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        if (!Utils.inSkyblock) return;
        if (stack.getItem() == Items.skull) {
            double scale = Skytils.config.largerHeadScale / 100f;
            GlStateManager.scale(scale, scale, scale);
        }
    }
}
