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
package skytils.skytilsmod.mixins.hooks.renderer

import skytils.skytilsmod.features.impl.misc.SlayerFeatures.Companion.slayerEntity
import skytils.skytilsmod.features.impl.misc.SlayerFeatures.Companion.yangGlyphEntity
import skytils.skytilsmod.features.impl.misc.SlayerFeatures.Companion.yangGlyph
import skytils.skytilsmod.features.impl.misc.SlayerFeatures.Companion.slayerNameEntity
import skytils.skytilsmod.Skytils
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.Entity
import skytils.skytilsmod.utils.*

fun setColorMultiplier(
    entity: Entity,
    lightBrightness: Float,
    partialTickTime: Float,
    cir: CallbackInfoReturnable<Int>
) {
    if (Skytils.config.recolorSeraphBoss && Utils.inSkyblock && entity is EntityEnderman) {
        if (slayerEntity != entity) return
        entity.hurtTime = 0
        if (yangGlyphEntity != null || yangGlyph != null) {
            cir.setReturnValue(Skytils.config.seraphBeaconPhaseColor.withAlpha(169))
        } else if (slayerNameEntity != null && (slayerNameEntity!!.customNameTag.dropLastWhile { it == 's' }
                .endsWith(" Hit"))
        ) {
            cir.setReturnValue(Skytils.config.seraphHitsPhaseColor.withAlpha(169))
        } else cir.setReturnValue(Skytils.config.seraphNormalPhaseColor.withAlpha(169))
    }
}