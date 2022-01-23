/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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
package skytils.skytilsmod.mixins.hooks.entity

import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.potion.Potion
import net.minecraft.util.EnumParticleTypes
import net.minecraft.world.World
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.stripControlCodes
import java.io.File
import kotlin.random.Random

class EntityLivingBaseHook(val entity: EntityLivingBase) {

    companion object {
        val smolPeople by lazy {
            File(Skytils.modDir, "smolpeople").exists()
        }

        private val dmgPattern = Regex("✧?(?<num>\\d+)[⚔+✧❤♞☄✷]*")
    }

    var overrideDisplayName: String? = null

    fun onNewDisplayName(s: String) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.commaDamage && entity is EntityArmorStand && entity.ticksExisted < 300 && s.isNotEmpty()) {
            val matched = dmgPattern.matchEntire(s.stripControlCodes())
            if (matched != null) {
                val dmg = matched.groups["num"]?.value?.toIntOrNull()
                if ((dmg ?: 0) >= 1000) {
                    overrideDisplayName = buildString {
                        var idx = 0
                        val reverse = s.reversed()
                        for ((i, c) in reverse.withIndex()) {
                            append(c)
                            if (c.isDigit()) {
                                val next = reverse.getOrNull(i + 1)
                                if (next != '§') {
                                    idx++
                                }
                                if (idx != 0 && idx % 3 == 0) {
                                    val remaining = reverse.substring(i + 1)
                                    if (remaining.withIndex().any { (i1, c1) ->
                                            c1.isDigit() && remaining.getOrNull(i1 + 1) != '§'
                                        }) {
                                        append(',')
                                        idx = 0
                                    }
                                }
                            }
                        }
                    }.reversed()
                    return
                }
            }
        }
        if (overrideDisplayName != null)
            overrideDisplayName = s
    }

    val isBreefing by lazy {
        entity.name == "Breefing" && (Utils.breefingdog || Random.nextInt(
            100
        ) < 3)
    }

    val isSmol by lazy {
        Utils.inSkyblock && entity is EntityPlayer && (smolPeople || isBreefing)
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