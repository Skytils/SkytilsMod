/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers

import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.entity.LivingEntityDeathEvent
import gg.skytils.event.impl.world.BlockStateUpdateEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

object MimicDetector : EventSubscriber {
    var mimicOpenTime = 0L
    var mimicPos: BlockPos? = null

    override fun setup() {
        register(::onBlockChange)
        register(::onEntityDeath)
    }

    fun onBlockChange(event: BlockStateUpdateEvent) {
        if (Utils.inDungeons && event.old.block == Blocks.trapped_chest && event.update.block == Blocks.air) {
            mimicOpenTime = System.currentTimeMillis()
            mimicPos = event.pos
        }
    }

    fun onEntityDeath(event: LivingEntityDeathEvent) {
        if (!Utils.inDungeons) return
        val entity = event.entity as? EntityZombie ?: return
        if (entity.isChild && (0..3).all { entity.getCurrentArmor(it) == null }) {
            if (!ScoreCalculation.mimicKilled.get()) {
                ScoreCalculation.mimicKilled.set(true)
                if (Skytils.config.scoreCalculationAssist) {
                    Skytils.sendMessageQueue.add("/pc \$SKYTILS-DUNGEON-SCORE-MIMIC$")
                }
            }
        }
    }

    fun checkMimicDead() {
        if (ScoreCalculation.mimicKilled.get()) return
        if (mimicOpenTime == 0L) return
        if (System.currentTimeMillis() - mimicOpenTime < 750) return
        if (mc.thePlayer.getDistanceSq(mimicPos) < 400) {
            if (mc.theWorld.loadedEntityList.none {
                    it is EntityZombie && it.isChild && it.getCurrentArmor(3)
                        ?.getSubCompound("SkullOwner", false)
                        ?.getString("Id") == "bcb486a4-0cb5-35db-93f0-039fbdde03f0"
                }) {
                ScoreCalculation.mimicKilled.set(true)
                if (Skytils.config.scoreCalculationAssist) {
                    Skytils.sendMessageQueue.add("/pc \$SKYTILS-DUNGEON-SCORE-MIMIC$")
                }
            }
        }
    }
}