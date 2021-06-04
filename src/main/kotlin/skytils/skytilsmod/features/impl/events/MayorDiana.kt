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

package skytils.skytilsmod.features.impl.events

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.CheckRenderEntityEvent
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.mixins.accessors.AccessorMinecraft
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.baseMaxHealth
import java.awt.Color

class MayorDiana {

    private val gaiaConstructHits = HashMap<EntityIronGolem, Int>()


    @SubscribeEvent
    fun onEvent(event: Event) {
        if (!Utils.inSkyblock) return
        when (event) {
            is TickEvent.ClientTickEvent -> {
                if (event.phase == TickEvent.Phase.START && Skytils.config.trackGaiaHits) for (golem in gaiaConstructHits.keys) {
                    if (golem.hurtTime == 10) {
                        gaiaConstructHits[golem] = 0
                    }
                }
            }
            is PacketEvent.ReceiveEvent -> {
                if (Skytils.config.trackGaiaHits && event.packet is S29PacketSoundEffect) {
                    if (event.packet.volume == 0f) return
                    if (event.packet.volume == 0.8f && event.packet.soundName == "random.anvil_land") {
                        val pos = BlockPos(event.packet.x, event.packet.y, event.packet.z)
                        val golem = (mc.theWorld.loadedEntityList.filter {
                            it is EntityIronGolem && it.health > 0 && it.getDistanceSq(pos) <= 25 * 25
                        }.minByOrNull { it.getDistanceSq(pos) } ?: return) as EntityIronGolem
                        gaiaConstructHits.compute(golem) { _: EntityIronGolem, i: Int? -> (i ?: 0) + 1 }
                    }
                }
            }
            is RenderLivingEvent.Post<*> -> {
                if (event.entity is EntityIronGolem) {
                    with(event.entity) {
                        if (gaiaConstructHits.containsKey(this)) {
                            val percentageHp = health / baseMaxHealth
                            val neededHits = when {
                                percentageHp <= 0.33 -> 7
                                percentageHp <= 0.66 -> 6
                                else -> 5
                            }
                            val hits = gaiaConstructHits.getOrDefault(this, 0)
                            GlStateManager.disableDepth()
                            RenderUtil.draw3DString(
                                Vec3(posX, posY + 2, posZ),
                                "Hits: $hits / $neededHits",
                                if (hits < neededHits) Color.RED else Color.GREEN,
                                (mc as AccessorMinecraft).timer.renderPartialTicks
                            )
                            GlStateManager.enableDepth()
                        }
                    }
                }
            }
            is CheckRenderEntityEvent<*> -> {
                val entity = event.entity
                if (Skytils.config.removeLeftOverBleeds && mc.theWorld != null && entity is EntityArmorStand && entity.hasCustomName() && entity.displayName.formattedText.startsWith(
                        "§c☣ §fBleeds: §c"
                    ) && entity.ticksExisted >= 20
                ) {
                    val aabb = entity.entityBoundingBox.expand(2.0, 5.0, 2.0)
                    if (mc.theWorld.loadedEntityList.none {
                            it.displayName.formattedText.endsWith("§c❤") && it.displayName.formattedText.contains(
                                "Minotaur §"
                            ) && it.entityBoundingBox.intersectsWith(aabb)
                        }) {
                        event.isCanceled = true
                        mc.theWorld.removeEntity(entity)
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        gaiaConstructHits.clear()
    }
}