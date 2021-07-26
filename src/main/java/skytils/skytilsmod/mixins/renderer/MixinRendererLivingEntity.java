/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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

package skytils.skytilsmod.mixins.renderer;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.features.impl.misc.SlayerFeatures;
import skytils.skytilsmod.utils.RenderUtilKt;
import skytils.skytilsmod.utils.Utils;

import java.util.Objects;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> extends Render<T> {
    protected MixinRendererLivingEntity(RenderManager renderManager) {
        super(renderManager);
    }

    @Inject(method = "getColorMultiplier", at = @At("HEAD"), cancellable = true)
    private void setColorMultiplier(T entity, float lightBrightness, float partialTickTime, CallbackInfoReturnable<Integer> cir) {
        if (Skytils.config.recolorSeraphBoss && Utils.inSkyblock && entity instanceof EntityEnderman) {
            EntityEnderman e = (EntityEnderman) entity;
            if (SlayerFeatures.Companion.getSlayerEntity() != e) return;
            e.hurtTime = 0;

            if (SlayerFeatures.Companion.getYangGlyphEntity() != null || SlayerFeatures.Companion.getYangGlyph() != null) {
                cir.setReturnValue(RenderUtilKt.withAlpha(Skytils.config.seraphBeaconPhaseColor, 169));
            } else if (Objects.requireNonNull(SlayerFeatures.Companion.getSlayerNameEntity()).getCustomNameTag().endsWith("Hits")) {
                cir.setReturnValue(RenderUtilKt.withAlpha(Skytils.config.seraphHitsPhaseColor, 169));
            } else cir.setReturnValue(RenderUtilKt.withAlpha(Skytils.config.seraphNormalPhaseColor, 169));
        }
    }
}
