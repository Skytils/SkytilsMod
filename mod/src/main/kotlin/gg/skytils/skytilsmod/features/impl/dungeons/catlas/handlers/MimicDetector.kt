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
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.multiplatform.EquipmentSlot
import gg.skytils.skytilsmod.utils.multiplatform.armorItems
import gg.skytils.skytilsws.client.WSClient
import gg.skytils.skytilsws.shared.packet.C2SPacketDungeonMimic
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.block.Blocks
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

object MimicDetector : EventSubscriber {
    var mimicOpenTime = 0L
    var mimicPos: BlockPos? = null
    val mimicSkullUUID: UUID = UUID.fromString("ae55953f-605e-3c71-a813-310c028de150")

    override fun setup() {
        register(::onBlockChange)
        register(::onEntityDeath)
    }

    fun onBlockChange(event: BlockStateUpdateEvent) {
        if (Utils.inDungeons && event.old.block == Blocks.TRAPPED_CHEST && event.update.block == Blocks.AIR) {
            mimicOpenTime = System.currentTimeMillis()
            mimicPos = event.pos
        }
    }

    fun onEntityDeath(event: LivingEntityDeathEvent) {
        if (!Utils.inDungeons) return
        val entity = event.entity as? ZombieEntity ?: return
        //#if MC==10809
        //$$ if (entity.isBaby && (0..3).all { entity.method_0_7157(it) == null }) {
        //#else
        if (entity.isBaby && entity.armorItems.all { it == ItemStack.EMPTY }) {
        //#endif
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
        if (mc.player!!.blockPos.getSquaredDistance(mimicPos) < 400) {
            if (mc.world!!.entities.none { entity ->
                    entity is ZombieEntity && entity.isBaby && entity.getEquippedStack(EquipmentSlot.HEAD)
                        //#if MC==10809
                        //$$ ?.getOrCreateSubNbt("SkullOwner", false)
                        //$$ ?.getString("Id")?.getOrNull() == "ae55953f-605e-3c71-a813-310c028de150"
                        //#else
                        .get(DataComponentTypes.PROFILE)?.gameProfile?.id == mimicSkullUUID
                        //#endif
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
