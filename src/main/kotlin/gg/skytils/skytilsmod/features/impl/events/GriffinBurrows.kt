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

import com.google.common.collect.EvictingQueue
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import gg.essential.universal.UMatrixStack
import gg.essential.universal.UMinecraft
import gg.essential.universal.wrappers.UPlayer
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.utils.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color
import java.time.Duration
import java.time.Instant
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object GriffinBurrows {
    val particleBurrows = hashMapOf<BlockPos, ParticleBurrow>()
    var lastDugParticleBurrow: BlockPos? = null
    val recentlyDugParticleBurrows: EvictingQueue<BlockPos> = EvictingQueue.create(5)

    var hasSpadeInHotbar = false

    object BurrowEstimation {
        val arrows = mutableMapOf<Arrow, Instant>()
        val guesses = mutableMapOf<BurrowGuess, Instant>()
        fun getDistanceFromPitch(pitch: Double) =
            2805 * pitch - 1375

        val grassData by lazy {
            this::class.java.getResource("/assets/skytils/grassdata.txt")!!.readBytes()
        }

        class Arrow(val directionVector: Vec3, val pos: Vec3)
    }


    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        hasSpadeInHotbar = mc.thePlayer != null && Utils.inSkyblock && (0..7).any {
            mc.thePlayer.inventory.getStackInSlot(it).isSpade
        }
        if (!Skytils.config.burrowEstimation) return
        BurrowEstimation.guesses.entries.removeIf { (_, instant) ->
            Duration.between(instant, Instant.now()).toMinutes() > 2
        }
        BurrowEstimation.arrows.entries.removeIf { (_, instant) ->
            Duration.between(instant, Instant.now()).toMillis() > 30_000L
        }
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type == 2.toByte()) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (Skytils.config.showGriffinBurrows &&
            (unformatted.startsWith("You died") || unformatted.startsWith("☠ You were killed") ||
                    unformatted.startsWith("You dug out a Griffin Burrow! (") ||
                    unformatted == "You finished the Griffin burrow chain! (4/4)")
        ) {
            if (lastDugParticleBurrow != null) {
                val particleBurrow =
                    particleBurrows[lastDugParticleBurrow] ?: return
                recentlyDugParticleBurrows.add(lastDugParticleBurrow)
                particleBurrows.remove(particleBurrow.blockPos)
                printDevMessage("Removed $particleBurrow", "griffin")
                lastDugParticleBurrow = null
            }
        }
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.SendEvent) {
        if (!Utils.inSkyblock || !Skytils.config.showGriffinBurrows || mc.theWorld == null || mc.thePlayer == null) return
        val pos =
            when {
                event.packet is C07PacketPlayerDigging && event.packet.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK -> {
                    event.packet.position
                }
                event.packet is C08PacketPlayerBlockPlacement && event.packet.stack != null -> event.packet.position
                else -> return
            }
        if (mc.thePlayer.heldItem?.isSpade != true || mc.theWorld.getBlockState(pos).block !== Blocks.grass) return
        particleBurrows[pos]?.blockPos?.let {
            printDevMessage("Clicked on $it", "griffin")
            lastDugParticleBurrow = it
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (Skytils.config.showGriffinBurrows) {
            val matrixStack = UMatrixStack()
            for (pb in particleBurrows.values) {
                if (pb.hasEnchant && pb.hasFootstep && pb.type.get() != -1) {
                    pb.drawWaypoint(event.partialTicks, matrixStack)
                }
            }
            if (Skytils.config.burrowEstimation) {
                for (bg in BurrowEstimation.guesses.keys) {
                    bg.drawWaypoint(event.partialTicks, matrixStack)
                }
                for (arrow in BurrowEstimation.arrows.keys) {
                    RenderUtil.drawCircle(
                        matrixStack,
                        arrow.pos.x,
                        arrow.pos.y + 0.2,
                        arrow.pos.z,
                        event.partialTicks,
                        5.0,
                        100,
                        255,
                        128,
                        0,
                    )
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Unload) {
        particleBurrows.clear()
        recentlyDugParticleBurrows.clear()
    }

    @SubscribeEvent
    fun onReceivePacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inSkyblock) return
        when (event.packet) {
            is S2APacketParticles -> {
                if (Skytils.config.showGriffinBurrows && hasSpadeInHotbar) {
                    if (SBInfo.mode != SkyblockIsland.Hub.mode) return
                    event.packet.apply {
                        val type = ParticleType.getParticleType(this) ?: return
                        val pos = BlockPos(x, y, z).down()
                        if (recentlyDugParticleBurrows.contains(pos)) return
                        BurrowEstimation.guesses.keys.associateWith { guess ->
                            pos.distanceSq(
                                guess.x.toDouble(),
                                guess.y.toDouble(),
                                guess.z.toDouble()
                            )
                        }.minByOrNull { it.value }?.let { (guess, distance) ->
                            printDevMessage("Nearest guess is $distance blocks away", "griffin", "griffinguess")
                            if (distance <= 625) {
                                BurrowEstimation.guesses.remove(guess)
                            }
                        }
                        val burrow = particleBurrows.getOrPut(pos) {
                            ParticleBurrow(pos, hasFootstep = false, hasEnchant = false)
                        }
                        when (type) {
                            ParticleType.FOOTSTEP -> burrow.hasFootstep = true
                            ParticleType.ENCHANT -> burrow.hasEnchant = true
                            ParticleType.EMPTY -> burrow.type.set(0)
                            ParticleType.MOB -> burrow.type.set(1)
                            ParticleType.TREASURE -> burrow.type.set(2)
                        }
                    }
                }
            }
            is S04PacketEntityEquipment -> {
                if (!Skytils.config.burrowEstimation) return
                val entity = mc.theWorld?.getEntityByID(event.packet.entityID)
                (entity as? EntityArmorStand)?.let { armorStand ->
                    if (event.packet.itemStack?.item != Items.arrow) return
                    if (armorStand.getDistanceSq(mc.thePlayer?.position) >= 27) return
                    printDevMessage("Found armor stand with arrow", "griffin", "griffinguess")
                    val yaw = Math.toRadians(armorStand.rotationYaw.toDouble())
                    val lookVec = Vec3(
                        -sin(yaw),
                        0.0,
                        cos(yaw)
                    )
                    val offset = Vec3(-sin(yaw + PI/2), 0.0, cos(yaw + PI/2)) * 0.9
                    val origin = armorStand.positionVector.add(offset)
                    BurrowEstimation.arrows.put(BurrowEstimation.Arrow(lookVec, origin), Instant.now())
                }
            }
            is S29PacketSoundEffect -> {
                if (!Skytils.config.burrowEstimation) return
                if (event.packet.soundName != "note.harp") return
                val (arrow, distance) = BurrowEstimation.arrows.keys
                    .associateWith { arrow ->
                        arrow.pos.squareDistanceTo(event.packet.x, event.packet.y, event.packet.z)
                    }.minByOrNull { it.value } ?: return
                printDevMessage("Nearest arrow is $distance blocks away ${arrow.pos}", "griffin", "griffinguess")
                if (distance > 25) return
                val guessPos = arrow.pos.add(
                    arrow.directionVector * BurrowEstimation.getDistanceFromPitch(event.packet.pitch.toDouble())
                )

                var y: Int
                var x = guessPos.x.toInt()
                var z = guessPos.z.toInt()
                // offset of 300 blocks for both x and z
                // x ranges from 195 to -281
                // z ranges from 207 to -233
                do {
                    y = BurrowEstimation.grassData.getOrNull((x++ % 507) * 507 + (z++ % 495))?.toInt() ?: 0
                } while (y < 2)
                val guess = BurrowGuess(guessPos.x.toInt(), y, guessPos.z.toInt())
                BurrowEstimation.arrows.remove(arrow)
                BurrowEstimation.guesses[guess] = Instant.now()
            }
        }
    }

    abstract class Diggable {
        abstract val x: Int
        abstract val y: Int
        abstract val z: Int
        val blockPos: BlockPos by lazy {
            BlockPos(x, y, z)
        }

        protected abstract val waypointText: State<String>
        protected abstract val color: State<Color>
        fun drawWaypoint(partialTicks: Float, matrixStack: UMatrixStack) {
            val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(partialTicks)
            val renderX = this.x - viewerX
            val renderY = this.y - viewerY
            val renderZ = this.z - viewerZ
            val distSq = renderX * renderX + renderY * renderY + renderZ * renderZ
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                matrixStack,
                AxisAlignedBB(renderX, renderY, renderZ, renderX + 1, renderY + 1, renderZ + 1).expandBlock(),
                this.color.get(),
                (0.1f + 0.005f * distSq.toFloat()).coerceAtLeast(0.2f)
            )
            GlStateManager.disableTexture2D()
            if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(renderX, renderY + 1, renderZ, this.color.get().rgb, 1.0f, partialTicks)
            RenderUtil.renderWaypointText(
                waypointText.get(),
                x + 0.5,
                y + 5.0,
                z + 0.5,
                partialTicks,
                matrixStack
            )
            GlStateManager.disableLighting()
            GlStateManager.enableTexture2D()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    data class BurrowGuess(
        override val x: Int,
        override val y: Int,
        override val z: Int
    ) : Diggable() {
        override val waypointText = BasicState("§aBurrow §6(Guess)")
        override val color = BasicState(Color.ORANGE)
    }

    data class ParticleBurrow(
        override val x: Int,
        override val y: Int,
        override val z: Int,
        var hasFootstep: Boolean,
        var hasEnchant: Boolean
    ) : Diggable() {
        constructor(vec3: Vec3i, hasFootstep: Boolean, hasEnchant: Boolean) : this(
            vec3.x,
            vec3.y,
            vec3.z,
            hasFootstep,
            hasEnchant
        )

        val type = BasicState(-1)

        override val waypointText = type.map {
            "${
                when (it) {
                    0 -> "§aStart"
                    1 -> "§cMob"
                    2 -> "§6Treasure"
                    else -> "§7Unknown"
                }
            } §a(Particle)"
        }
        override val color = type.map {
            when (it) {
                0 -> Skytils.config.emptyBurrowColor
                1 -> Skytils.config.mobBurrowColor
                2 -> Skytils.config.treasureBurrowColor
                else -> Color.WHITE
            }
        }
    }

    private val ItemStack?.isSpade
        get() = ItemUtil.getSkyBlockItemID(this) == "ANCESTRAL_SPADE"

    private enum class ParticleType(val check: S2APacketParticles.() -> Boolean) {
        EMPTY({
            type == EnumParticleTypes.CRIT_MAGIC && count == 4 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f
        }),
        MOB({
            type == EnumParticleTypes.CRIT && count == 3 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f

        }),
        TREASURE({
            type == EnumParticleTypes.DRIP_LAVA && count == 2 && speed == 0.01f && xOffset == 0.35f && yOffset == 0.1f && zOffset == 0.35f
        }),
        FOOTSTEP({
            type == EnumParticleTypes.FOOTSTEP && count == 1 && speed == 0.0f && xOffset == 0.05f && yOffset == 0.0f && zOffset == 0.05f
        }),
        ENCHANT({
            type == EnumParticleTypes.ENCHANTMENT_TABLE && count == 5 && speed == 0.05f && xOffset == 0.5f && yOffset == 0.4f && zOffset == 0.5f
        });

        companion object {
            fun getParticleType(packet: S2APacketParticles): ParticleType? {
                if (!packet.isLongDistance) return null
                return entries.find {
                    it.check(packet)
                }
            }
        }
    }
}