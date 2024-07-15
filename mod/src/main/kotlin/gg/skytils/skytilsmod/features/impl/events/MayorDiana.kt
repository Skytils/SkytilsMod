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

package gg.skytils.skytilsmod.features.impl.events

import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.TickEvent
import gg.skytils.event.impl.play.WorldUnloadEvent
import gg.skytils.event.impl.render.LivingEntityPostRenderEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod._event.MainThreadPacketReceiveEvent
import gg.skytils.skytilsmod.utils.RenderUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.baseMaxHealth
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.awt.Color

object MayorDiana : EventSubscriber {

    private val gaiaConstructHits = HashMap<EntityIronGolem, Int>()

    override fun setup() {
        register(::onPacket, EventPriority.Highest)
        register(::onPostRenderEntity)
        register(::onTick)
        register(::onWorldChange)
    }

    fun onPacket(event: MainThreadPacketReceiveEvent<*>) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.trackGaiaHits && event.packet is S29PacketSoundEffect) {
            if (event.packet.volume == 0.8f && event.packet.soundName == "random.anvil_land") {
                val pos = BlockPos(event.packet.x, event.packet.y, event.packet.z)
                val golem = (mc.theWorld.loadedEntityList.filter {
                    it is EntityIronGolem && it.health > 0 && it.getDistanceSq(pos) <= 25 * 25
                }.minByOrNull { it.getDistanceSq(pos) } ?: return) as EntityIronGolem
                gaiaConstructHits.compute(golem) { _: EntityIronGolem, i: Int? -> (i ?: 0) + 1 }
            }
        }
    }

    fun onPostRenderEntity(event: LivingEntityPostRenderEvent) {
        if (!Utils.inSkyblock) return
        if (event.entity is EntityIronGolem) {
            with(event.entity as EntityIronGolem) {
                if (gaiaConstructHits.containsKey(this)) {
                    val percentageHp = health / baseMaxHealth
                    val neededHits = when {
                        percentageHp <= 0.33 -> 7
                        percentageHp <= 0.66 -> 6
                        else -> 5
                    }
                    val hits = gaiaConstructHits.getOrDefault(this, 0)
                    val matrixStack = UMatrixStack()
                    UGraphics.disableDepth()
                    RenderUtil.drawLabel(
                        Vec3(posX, posY + 2, posZ),
                        "Hits: $hits / $neededHits",
                        if (hits < neededHits) Color.RED else Color.GREEN,
                        RenderUtil.getPartialTicks(),
                        matrixStack
                    )
                    UGraphics.enableDepth()
                }
            }
        }
    }

    fun onTick(event: TickEvent) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.trackGaiaHits) for (golem in gaiaConstructHits.keys) {
            if (golem.hurtTime == 10) {
                gaiaConstructHits[golem] = 0
            }
        }
    }

    fun onWorldChange(event: WorldUnloadEvent) {
        gaiaConstructHits.clear()
    }
}