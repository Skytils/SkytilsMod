package skytils.skytilsmod.mixins;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.tileentity.TileEntityChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.features.impl.dungeons.solvers.ThreeWeirdosSolver;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;

@Mixin(TileEntityChestRenderer.class)
public class MixinTileEntityChestRenderer {

    @Inject(method = "renderTileEntityAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelChest;renderAll()V", shift = At.Shift.BEFORE))
    private void setChestColor(TileEntityChest te, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        if (te.getPos().equals(ThreeWeirdosSolver.riddleChest)) {
            Color colour = new Color(255, 0, 0, 255);
            GlStateManager.color((float)colour.getRed()/255, (float)colour.getGreen()/255, (float)colour.getBlue()/255);
            GlStateManager.disableTexture2D();
        }
    }

    @Inject(method = "renderTileEntityAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelChest;renderAll()V", shift = At.Shift.AFTER))
    private void setChestColorPost(TileEntityChest te, double x, double y, double z, float partialTicks, int destroyStage, CallbackInfo ci) {
        GlStateManager.enableTexture2D();
    }

}
