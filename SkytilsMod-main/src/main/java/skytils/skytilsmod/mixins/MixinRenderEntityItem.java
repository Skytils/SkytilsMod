package skytils.skytilsmod.mixins;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(RenderEntityItem.class)
public class MixinRenderEntityItem {
    @Inject(method = "doRender(Lnet/minecraft/entity/item/EntityItem;DDDFF)V", at = @At(value = "INVOKE", target = "net/minecraft/client/renderer/GlStateManager.pushMatrix()V", shift = At.Shift.AFTER, ordinal = 1))
    public void scaleItemDrop(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (!Utils.inSkyblock) return;
        float scale = Skytils.config.itemDropScale / 100f;
        GlStateManager.scale(scale, scale, scale);
    }
}
