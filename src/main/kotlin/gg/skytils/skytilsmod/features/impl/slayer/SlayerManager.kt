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

package gg.skytils.skytilsmod.features.impl.slayer

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.features.impl.slayer.slayers.Slayer
import gg.skytils.skytilsmod.features.impl.slayer.slayers.ThrowingSlayer
import gg.skytils.skytilsmod.features.impl.slayer.slayers.impl.*
import gg.skytils.skytilsmod.utils.ScoreboardUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.printDevMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.entity.passive.EntityWolf
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.Executors

object SlayerManager : CoroutineScope {
    override val coroutineContext = Executors.newSingleThreadExecutor().asCoroutineDispatcher() + SupervisorJob()

    var lastTickHasSlayerText = false
    var hasSlayerText = false

    var slayer: Slayer<*>? = null
    val slayerEntity: Entity?
        get() = slayer?.entity

    fun processSlayerEntity(entity: Entity) {
        slayer = try {
            when (entity) {
                is EntityZombie -> RevenantSlayer(entity)
                is EntitySpider -> TarantulaSlayer(entity)
                is EntityWolf -> SvenSlayer(entity)
                is EntityEnderman -> SeraphSlayer(entity)
                is EntityBlaze -> DemonlordSlayer(entity)
                else -> null
            }
        } catch (e: IllegalStateException) {
            null
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!Utils.inSkyblock) return
        if (event.phase != TickEvent.Phase.START || mc.theWorld == null || mc.thePlayer == null) return
        lastTickHasSlayerText = hasSlayerText
        hasSlayerText = ScoreboardUtil.sidebarLines.any { it == "Slay the boss!" }
        slayer?.tick(event)
    }

    @SubscribeEvent
    fun onEntityJoinWorld(event: EntityJoinWorldEvent) {
        if (!Utils.inSkyblock) return
        (slayer as? ThrowingSlayer)?.run { entityJoinWorld(event) }
        if (!hasSlayerText) return
        if (slayer != null) {
            printDevMessage("boss not null", "slayerspam", "seraphspam")
            return
        }
        processSlayerEntity(event.entity)
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        (slayer as? ThrowingSlayer)?.blockChange(event)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        slayer = null
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.ReceiveEvent) {
        if (!Utils.inSkyblock) return
        when (event.packet) {
            is S1CPacketEntityMetadata -> (slayer as? ThrowingSlayer)?.entityMetadata(event.packet)
        }
    }

    fun getTier(name: String): String {
        return ScoreboardUtil.sidebarLines.find { it.startsWith(name) }?.substringAfter(name)?.drop(1) ?: ""
    }
}