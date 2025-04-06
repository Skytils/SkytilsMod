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

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsws.client.WSClient
import gg.skytils.skytilsws.shared.SkytilsWS
import gg.skytils.skytilsws.shared.packet.C2SPacketDungeonMimic
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MimicDetector {
    var mimicOpenTime = 0L
    var mimicPos: BlockPos? = null

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (Utils.inDungeons && event.old.block == Blocks.trapped_chest && event.update.block == Blocks.air) {
            mimicOpenTime = System.currentTimeMillis()
            mimicPos = event.pos
        }
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        if (!Utils.inDungeons) return
        val entity = event.entity as? EntityZombie ?: return
        if (entity.isChild && (0..3).all { entity.getCurrentArmor(it) == null }) {
            if (!ScoreCalculation.mimicKilled.get()) {
                ScoreCalculation.mimicKilled.set(true)
                if (Skytils.config.scoreCalculationAssist) {
                    Skytils.sendMessageQueue.add("/pc \$SKYTILS-DUNGEON-SCORE-MIMIC$")
                }
                WSClient.sendPacketAsync(C2SPacketDungeonMimic(SBInfo.server ?: return))
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
                        ?.getString("Id") == "ae55953f-605e-3c71-a813-310c028de150"
                }) {
                ScoreCalculation.mimicKilled.set(true)
                if (Skytils.config.scoreCalculationAssist) {
                    Skytils.sendMessageQueue.add("/pc \$SKYTILS-DUNGEON-SCORE-MIMIC$")
                }
                WSClient.sendPacketAsync(C2SPacketDungeonMimic(SBInfo.server ?: return))
            }
        }
    }
}
