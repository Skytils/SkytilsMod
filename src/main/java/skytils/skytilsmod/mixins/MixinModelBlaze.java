package skytils.skytilsmod.mixins;

import net.minecraft.client.model.ModelBlaze;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;

import java.awt.*;

import static skytils.skytilsmod.features.impl.dungeons.solvers.BlazeSolver.blazeMode;
import static skytils.skytilsmod.features.impl.dungeons.solvers.BlazeSolver.orderedBlazes;

@Mixin(ModelBlaze.class)
public class MixinModelBlaze {
    @Inject(method = "render", at = @At(value = "HEAD"))
    private void changeBlazeColor(Entity entity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale, CallbackInfo ci) {
        if (orderedBlazes.size() == 0) return;
        if (blazeMode <= 0) {
            if (entity.isEntityEqual(orderedBlazes.get(0).blaze)) {
                Color colour = new Color(255, 0, 0, 200);
                GlStateManager.color((float) colour.getRed() / 255, (float) colour.getGreen() / 255, (float) colour.getBlue() / 255);
            } else if (Skytils.config.showNextBlaze && orderedBlazes.size() > 1 && entity.isEntityEqual(orderedBlazes.get(1).blaze) && blazeMode != 0) {
                Color colour = new Color(255, 255, 0, 200);
                GlStateManager.color((float) colour.getRed() / 255, (float) colour.getGreen() / 255, (float) colour.getBlue() / 255);
            }
        }
        if (blazeMode >= 0) {
            if (entity.isEntityEqual(orderedBlazes.get(orderedBlazes.size() - 1).blaze)) {
                Color colour = new Color(0, 255, 0, 200);
                GlStateManager.color((float) colour.getRed() / 255, (float) colour.getGreen() / 255, (float) colour.getBlue() / 255);
            } else if (Skytils.config.showNextBlaze && orderedBlazes.size() > 1 && entity.isEntityEqual(orderedBlazes.get(orderedBlazes.size() - 2).blaze) && blazeMode != 0) {
                Color colour = new Color(255, 255, 0, 200);
                GlStateManager.color((float) colour.getRed() / 255, (float) colour.getGreen() / 255, (float) colour.getBlue() / 255);
            }
        }
    }
}
