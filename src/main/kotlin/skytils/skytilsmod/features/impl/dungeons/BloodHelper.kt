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

package skytils.skytilsmod.features.impl.dungeons

import gg.essential.universal.UMinecraft
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityZombie
import net.minecraft.init.Items
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.events.impl.PacketEvent
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.RenderUtil
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.printDevMessage

object BloodHelper {
    val watcherSkins = setOf(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNlYzQwMDA4ZTFjMzFjMTk4NGY0ZDY1MGFiYjM0MTBmMjAzNzExOWZkNjI0YWZjOTUzNTYzYjczNTE1YTA3NyJ9fX0K",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVjMWRjNDdhMDRjZTU3MDAxYThiNzI2ZjAxOGNkZWY0MGI3ZWE5ZDdiZDZkODM1Y2E0OTVhMGVmMTY5Zjg5MyJ9fX0K",
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmY2ZTFlN2VkMzY1ODZjMmQ5ODA1NzAwMmJjMWFkYzk4MWUyODg5ZjdiZDdiNWIzODUyYmM1NWNjNzgwMjIwNCJ9fX0K"
    )
    val watchers = mutableSetOf<EntityZombie>()
    val mobs = mutableMapOf<EntityArmorStand, BloodMob>()

    @SubscribeEvent
    fun render(event: RenderWorldLastEvent) {
        if (!Utils.inDungeons || DungeonTimer.bloodOpenTime == -1L || DungeonTimer.bloodClearTime != -1L || !Skytils.config.bloodHelper) return
        watchers.removeIf { it.isDead }
        mobs.also {
            it.filter { it.key.isDead && !it.value.e }
                .forEach { println(System.currentTimeMillis() - it.value.start); it.value.e = true }
        }.filter { !it.key.isDead && it.value.finalPos != null }.toList()
            .forEachIndexed { index, (_, mob) ->
                RenderUtil.drawOutlinedBoundingBox(
                    AxisAlignedBB(
                        mob.finalPos!!.xCoord - 0.4,
                        mob.finalPos!!.yCoord + 1,
                        mob.finalPos!!.zCoord - 0.4,
                        mob.finalPos!!.xCoord + 0.4,
                        mob.finalPos!!.yCoord + 3,
                        mob.finalPos!!.zCoord + 0.4
                    ),
                    Skytils.config.bloodHelperColor,
                    1f,
                    event.partialTicks
                )
                RenderUtil.renderWaypointText(
                    "${index + 1}",
                    mob.finalPos!!.xCoord,
                    mob.finalPos!!.yCoord + 2,
                    mob.finalPos!!.zCoord,
                    event.partialTicks
                )
            }
    }

    @SubscribeEvent
    fun onJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityZombie) return
        (event.entity as EntityZombie).apply {
            TickTask(1) {
                val helmet = getEquipmentInSlot(4)
                if (helmet == null || helmet.item != Items.skull) return@TickTask
                val texture = ItemUtil.getSkullTexture(helmet)
                if (texture == null || (watcherSkins.contains(texture))) return@TickTask
                printDevMessage("found watcher", "blood")
                watchers.add(this)
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        watchers.clear()
        mobs.clear()
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.ReceiveEvent) {
        if (DungeonTimer.bloodOpenTime == -1L || DungeonTimer.bloodClearTime != -1L || watchers.isEmpty()) return
        if (event.packet !is S14PacketEntity.S17PacketEntityLookMove) return
        val entity = event.packet.getEntity(UMinecraft.getWorld()) ?: return
        if (entity !is EntityArmorStand || entity.getEquipmentInSlot(4)?.item != Items.skull) return
        mobs[entity]?.let { mob ->
            mob.deltas.add(
                Vec3(
                    event.packet.func_149062_c() / 32.0,
                    event.packet.func_149061_d() / 32.0,
                    event.packet.func_149064_e() / 32.0
                )
            )
            if (mob.deltas.size == mob.limit) {
                if (mob.firstWave) {
                    mob.finalPos = entity.positionVector.add(mob.calcDelta(9.6))
                } else {
                    mob.finalPos = entity.positionVector.add(mob.calcDelta(10.4))
                }
            }
        } ?: run {
            if (watchers.any { it.getDistanceSqToEntity(entity) > 64.0 }) return
            printDevMessage("blood mob close enough to watcher", "blood")
            mobs[entity] = BloodMob(
                mobs.size < 4,
                start = System.currentTimeMillis()
            )
        }
    }

    class BloodMob(
        val firstWave: Boolean = false,
        val deltas: MutableList<Vec3> = mutableListOf(),
        var finalPos: Vec3? = null,
        val start: Long,
        var e: Boolean = false
    ) {
        val limit: Int = 3

        private fun calcSpeed() =
            deltas.take(limit).reduce { acc, vec3 -> acc.add(vec3) }

        fun calcDelta(scale: Double) = calcSpeed().normalize().run {
            Vec3(
                xCoord * scale,
                yCoord * scale,
                zCoord * scale
            )
        }
    }
}