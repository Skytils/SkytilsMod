/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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
package gg.skytils.skytilsmod.mixins.hooks.renderer

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo.currentMayor
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.baseMaxHealth
import gg.skytils.skytilsmod.utils.equalsAnyOf
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.passive.EntityBat
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

fun preRenderBat(bat: EntityBat, partialTicks: Float, ci: CallbackInfo) {
    if (Utils.inDungeons && Skytils.config.biggerBatModels &&
        if (currentMayor == "Derpy") bat.baseMaxHealth.equalsAnyOf(200.0, 800.0) else bat.baseMaxHealth.equalsAnyOf(
            100.0,
            400.0
        )
    ) {
        GlStateManager.scale(3f, 3f, 3f)
    }
}