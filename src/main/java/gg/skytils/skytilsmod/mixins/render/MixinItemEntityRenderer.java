package gg.skytils.skytilsmod.mixins.render;

import gg.skytils.skytilsmod.core.Config;
import gg.skytils.skytilsmod.util.SBInfo;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public class MixinItemEntityRenderer {
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V"))
    public void render(ItemEntityRenderState itemEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (!SBInfo.INSTANCE.getSkyblockState().getUntracked()) return;

        float droppedItemScale = Config.INSTANCE.getDroppedItemScale();
        if (droppedItemScale != 1f) {
            matrixStack.scale(droppedItemScale, droppedItemScale, droppedItemScale);
        }
    }
}