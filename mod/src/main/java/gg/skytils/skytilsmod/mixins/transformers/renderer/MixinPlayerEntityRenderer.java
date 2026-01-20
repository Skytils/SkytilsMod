/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package gg.skytils.skytilsmod.mixins.transformers.renderer;

//#if MC>12000
import gg.skytils.skytilsmod.utils.SuperSecretSettings;
import gg.skytils.skytilsmod.utils.Utils;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(PlayerEntityRenderer.class)
public class MixinPlayerEntityRenderer {
    private static final Random random = new Random();
    @Inject(method = "scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V", at = @At("HEAD"))
    private static void doScale(PlayerEntityRenderState playerEntityRenderState, MatrixStack matrixStack, CallbackInfo ci){
        if (isSmol(playerEntityRenderState)){
            matrixStack.scale(0.5f, 0.5f, 0.5f);
        }
    }

    private static boolean isSmol(PlayerEntityRenderState state){
        boolean isBreefing = state.name.equals("Breefing") && (SuperSecretSettings.breefingDog || random.nextInt(100) < 3);
        return Utils.INSTANCE.getInSkyblock() && (SuperSecretSettings.smolPeople || isBreefing);
    }
}
//#endif