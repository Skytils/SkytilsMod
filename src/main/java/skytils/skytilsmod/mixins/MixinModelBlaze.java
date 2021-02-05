package skytils.skytilsmod.mixins;

import net.minecraft.client.model.ModelBlaze;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.features.impl.dungeons.solvers.BlazeSolver;

import java.awt.*;

@Mixin(ModelBlaze.class)
public class MixinModelBlaze {
    @Inject(method = "render", at = @At(value = "HEAD"))
    private void onRender(Entity entityIn, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale, CallbackInfo ci) {
       if (entityIn.isEntityEqual(BlazeSolver.lowestBlaze) && BlazeSolver.blazeMode <= 0) {
           Color colour = new Color(255, 0, 0, 200);
           GlStateManager.color((float)colour.getRed()/255, (float)colour.getGreen()/255, (float)colour.getBlue()/255);
       }
       if (entityIn.isEntityEqual(BlazeSolver.highestBlaze) && BlazeSolver.blazeMode >= 0) {
           Color colour = new Color(0, 255, 0, 200);
           GlStateManager.color((float)colour.getRed()/255, (float)colour.getGreen()/255, (float)colour.getBlue()/255);
       }
    }
}
