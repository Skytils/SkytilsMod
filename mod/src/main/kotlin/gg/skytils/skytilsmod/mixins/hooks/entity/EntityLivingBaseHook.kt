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
package gg.skytils.skytilsmod.mixins.hooks.entity

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.features.impl.dungeons.WitherKingDragons
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion
import net.minecraft.util.EnumParticleTypes
import net.minecraft.world.World
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.awt.Color
import kotlin.random.Random

class EntityLivingBaseHook(val entity: EntityLivingBase) {

    var overrideDisplayName: String? = null
    var colorMultiplier: Color? = null
    var masterDragonType: WitherKingDragons? = null

    fun onNewDisplayName(s: String) {
        if (!Utils.inSkyblock) return
        if (overrideDisplayName != null)
            overrideDisplayName = s
    }

    val isBreefing by lazy {
        entity.name == "Breefing" && (SuperSecretSettings.breefingDog || Random.nextInt(
            100
        ) < 3)
    }

    val isSmol by lazy {
        Utils.inSkyblock && entity is EntityPlayer && (SuperSecretSettings.smolPeople || isBreefing)
    }

    fun modifyPotionActive(potionId: Int, cir: CallbackInfoReturnable<Boolean>) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.disableNightVision && potionId == Potion.nightVision.id && entity is EntityPlayerSP) {
            cir.returnValue = false
        }
    }

    fun removeDeathParticle(
        world: World,
        particleType: EnumParticleTypes,
        xCoord: Double,
        yCoord: Double,
        zCoord: Double,
        xOffset: Double,
        yOffset: Double,
        zOffset: Double,
        p_175688_14_: IntArray
    ): Boolean {
        return !Skytils.config.hideDeathParticles || !Utils.inSkyblock || particleType != EnumParticleTypes.EXPLOSION_NORMAL
    }

    fun isChild(cir: CallbackInfoReturnable<Boolean>) {
        cir.returnValue = isSmol
    }
}