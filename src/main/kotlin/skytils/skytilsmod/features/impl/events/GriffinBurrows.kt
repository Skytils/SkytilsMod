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

import gg.essential.universal.UChat
import gg.essential.universal.UResolution
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.apache.commons.lang3.time.StopWatch
import skytils.hylin.request.HypixelAPIException
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.DamageBlockEvent
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.awt.Color
import java.util.concurrent.Future
import kotlin.math.roundToInt

class GriffinBurrows {
    companion object {
        var burrows = arrayListOf<Burrow>()
        var dugBurrows = arrayListOf<BlockPos>()
        var lastDugBurrow: BlockPos? = null
        var particleBurrows = arrayListOf<ParticleBurrow>()
        var lastDugParticleBurrow: BlockPos? = null
        var burrowRefreshTimer = StopWatch()
        var shouldRefreshBurrows = false

        var hasSpadeInHotbar = false

        private val mc = Minecraft.getMinecraft()
        fun refreshBurrows(): Future<*> {
            return Skytils.threadPool.submit {
                try {
                    println("Finding burrows")
                    val uuid = mc.thePlayer.gameProfile.id
                    val apiKey = Skytils.config.apiKey
                    if (apiKey.isEmpty()) {
                        UChat.chat("§c§lYour API key is required in order to use the burrow feature. §cPlease set it with /api new or /st setkey <key>")
                        Skytils.config.showGriffinBurrows = false
                        return@submit
                    }
                    val burrowArray =
                        Skytils.hylinAPI.getLatestSkyblockProfileForMemberSync(uuid)
                    if (burrowArray == null) {
                        UChat.chat("§c§lUnable to find your Skyblock Profile!")
                        return@submit
                    }
                    val receivedBurrows = burrowArray.griffin.burrows.mapTo(ArrayList()) {
                        Burrow(it.x, it.y, it.z, it.type, it.tier, it.chain)
                    }

                    dugBurrows.removeIf { dug: BlockPos ->
                        receivedBurrows.none { burrow: Burrow -> burrow.blockPos == dug }
                    }
                    particleBurrows.removeIf { pb: ParticleBurrow? ->
                        receivedBurrows.any { rb: Burrow -> rb.blockPos == pb!!.blockPos }
                    }
                    val removedDupes = receivedBurrows.removeIf { burrow: Burrow ->
                        dugBurrows.contains(burrow.blockPos) || particleBurrows.any { pb: ParticleBurrow -> pb.dug && pb.blockPos == burrow.blockPos }
                    }
                    burrows.clear()
                    burrows.addAll(receivedBurrows)
                    particleBurrows.clear()
                    if (receivedBurrows.size == 0) {
                        if (!removedDupes) UChat.chat("§cSkytils failed to load griffin burrows. Try manually digging a burrow and switching hubs.") else UChat.chat(
                            "§cSkytils was unable to load fresh burrows. Please wait for the API refresh or switch hubs."
                        )
                    } else UChat.chat("§aSkytils loaded §2${receivedBurrows.size}§a burrows!")
                } catch (apiException: HypixelAPIException) {
                    UChat.chat("Failed to get burrows with reason: ${apiException.message}")
                }
            }
        }

        init {
            GriffinGuiElement()
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        val player = mc.thePlayer
        if (event.phase != TickEvent.Phase.START) return
        hasSpadeInHotbar = player != null && Utils.inSkyblock && (0..7).any {
            val hotbarItem = player.inventory.getStackInSlot(it) ?: return@any false
            return@any ItemUtil.getDisplayName(hotbarItem).contains("Ancestral Spade")
        }
        if (!Utils.inSkyblock || player == null || !Skytils.config.showGriffinBurrows || SBInfo.mode != SBInfo.SkyblockIsland.Hub.mode) return
        if (!burrowRefreshTimer.isStarted) burrowRefreshTimer.start()
        if ((burrowRefreshTimer.time >= 60_000L || shouldRefreshBurrows)) {
            burrowRefreshTimer.reset()
            shouldRefreshBurrows = false
            if (hasSpadeInHotbar) {
                player.addChatMessage(ChatComponentText(EnumChatFormatting.GREEN.toString() + "Looking for burrows..."))
                refreshBurrows()
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (Skytils.config.showGriffinBurrows &&
            (unformatted == "You died!" ||
                    unformatted.startsWith("You dug out a Griffin Burrow! (") ||
                    unformatted == "You finished the Griffin burrow chain! (4/4)")
        ) {
            if (lastDugBurrow != null) {
                dugBurrows.add(lastDugBurrow!!)
                burrows.removeIf { burrow: Burrow -> burrow.blockPos == lastDugBurrow }
                lastDugBurrow = null
            }
            if (lastDugParticleBurrow != null) {
                val particleBurrow =
                    particleBurrows.find { pb: ParticleBurrow? -> pb!!.blockPos == lastDugParticleBurrow }
                if (particleBurrow != null) {
                    particleBurrow.dug = true
                    dugBurrows.add(lastDugParticleBurrow!!)
                    particleBurrows.remove(particleBurrow)
                    lastDugParticleBurrow = null
                }
            }
        }
    }

    @SubscribeEvent
    fun onDamageBlock(event: DamageBlockEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return
        val blockState = mc.theWorld.getBlockState(event.pos)
        val item = mc.thePlayer.heldItem
        if (Utils.inSkyblock) {
            if (Skytils.config.showGriffinBurrows && item != null) {
                if (ItemUtil.getDisplayName(item).contains("Ancestral Spade") && blockState.block === Blocks.grass) {
                    if (burrows.any { burrow: Burrow -> burrow.blockPos == event.pos }) {
                        lastDugBurrow = event.pos
                    }
                    if (particleBurrows.any { pb: ParticleBurrow? -> pb!!.blockPos == event.pos }) {
                        lastDugParticleBurrow = event.pos
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return
        if (mc.theWorld == null || mc.thePlayer == null) return
        val blockState = mc.theWorld.getBlockState(event.pos)
        val item = mc.thePlayer.heldItem
        if (Utils.inSkyblock) {
            if (Skytils.config.showGriffinBurrows && item != null) {
                if (ItemUtil.getDisplayName(item).contains("Ancestral Spade") && blockState.block === Blocks.grass) {
                    if (burrows.any { burrow: Burrow -> burrow.blockPos == event.pos }) {
                        lastDugBurrow = event.pos
                    }
                    if (particleBurrows.any { pb: ParticleBurrow? -> pb!!.blockPos == event.pos }) {
                        lastDugParticleBurrow = event.pos
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (Skytils.config.showGriffinBurrows) {
            if (burrows.isNotEmpty()) {
                for (burrow in burrows.toTypedArray()) {
                    if (burrow != null) {
                        burrow.drawWaypoint(event.partialTicks)
                    }
                }
            }
            if (Skytils.config.particleBurrows && particleBurrows.isNotEmpty()) {
                for (pb in particleBurrows.toTypedArray()) {
                    if (pb != null && pb.hasEnchant && pb.hasFootstep && pb.type != -1) {
                        pb.drawWaypoint(event.partialTicks)
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        burrows.clear()
        particleBurrows.clear()
        shouldRefreshBurrows = true
    }

    class GriffinGuiElement : GuiElement("Griffin Timer", FloatPair(100, 10)) {
        override fun render() {
            if (SBInfo.mode != SBInfo.SkyblockIsland.Hub.mode) return
            val player = mc.thePlayer
            if (toggled && Utils.inSkyblock && player != null && hasSpadeInHotbar) {
                val diff = ((60_000L - burrowRefreshTimer.time) / 1000L).toFloat().roundToInt().toLong()
                val sr = UResolution
                val leftAlign = actualX < sr.scaledWidth / 2f
                val alignment = if (leftAlign) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    "Time until refresh: " + diff + "s",
                    if (leftAlign) 0f else actualWidth,
                    0f,
                    CommonColors.WHITE,
                    alignment,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override fun demoRender() {
            ScreenRenderer.fontRenderer.drawString(
                "Time until refresh: 10s",
                0f,
                0f,
                CommonColors.WHITE,
                TextAlignment.LEFT_RIGHT,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Time until refresh: 10s")
        override val toggled: Boolean
            get() = Skytils.config.showGriffinBurrows && Skytils.config.showGriffinCountdown

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceiveEvent) {
        if (!Utils.inSkyblock) return
        Utils.checkThreadAndQueue {
            if (Skytils.config.showGriffinBurrows && Skytils.config.particleBurrows && event.packet is S2APacketParticles) {
                if (SBInfo.mode != SBInfo.SkyblockIsland.Hub.mode) return@checkThreadAndQueue
                val packet = event.packet
                val type = packet.particleType
                val longDistance = packet.isLongDistance
                val count = packet.particleCount
                val speed = packet.particleSpeed
                val xOffset = packet.xOffset
                val yOffset = packet.yOffset
                val zOffset = packet.zOffset
                val x = packet.xCoordinate
                val y = packet.yCoordinate
                val z = packet.zCoordinate
                val pos = BlockPos(x, y, z).down()
                val footstepFilter =
                    type == EnumParticleTypes.FOOTSTEP && count == 1 && speed == 0.0f && xOffset == 0.05f && yOffset == 0.0f && zOffset == 0.05f
                val enchantFilter =
                    type == EnumParticleTypes.ENCHANTMENT_TABLE && count == 5 && speed == 0.05f && xOffset == 0.5f && yOffset == 0.4f && zOffset == 0.5f
                val startFilter =
                    type == EnumParticleTypes.CRIT_MAGIC && count == 4 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f
                val mobFilter =
                    type == EnumParticleTypes.CRIT && count == 3 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f
                val treasureFilter =
                    type == EnumParticleTypes.DRIP_LAVA && count == 2 && speed == 0.01f && xOffset == 0.35f && yOffset == 0.1f && zOffset == 0.35f
                if (longDistance && (footstepFilter || enchantFilter || startFilter || mobFilter || treasureFilter)) {
                    if (burrows.none { b: Burrow -> b.blockPos == pos } && dugBurrows.none { b: BlockPos -> b == pos }) {
                        for (existingBurrow in burrows) {
                            if (existingBurrow.blockPos.distanceSq(x, y, z) < 4) return@checkThreadAndQueue
                        }
                        var burrow = particleBurrows.find { b: ParticleBurrow -> b.blockPos == pos }
                        if (burrow == null) burrow =
                            ParticleBurrow(pos, hasFootstep = false, hasEnchant = false, type = -1)
                        if (!particleBurrows.contains(burrow)) particleBurrows.add(burrow)
                        if (!burrow.hasFootstep && footstepFilter) {
                            burrow.hasFootstep = true
                        } else if (!burrow.hasEnchant && enchantFilter) {
                            burrow.hasEnchant = true
                        } else if (burrow.type == -1 && type != EnumParticleTypes.FOOTSTEP && type != EnumParticleTypes.ENCHANTMENT_TABLE) {
                            when {
                                startFilter -> burrow.type = 0
                                mobFilter -> burrow.type = 1
                                treasureFilter -> burrow.type = 2
                            }
                        }
                    }
                    //System.out.println(String.format("%s %s %s particles with %s speed at %s, %s, %s, offset by %s %s %s", count, longDistance ? "long-distance" : "", type.getParticleName(), speed, x, y, z, xOffset, yOffset, zOffset));
                }
            }
        }
    }

    class ParticleBurrow(
        var x: Int,
        var y: Int,
        var z: Int,
        var hasFootstep: Boolean,
        var hasEnchant: Boolean,
        var type: Int
    ) {
        private var timestamp: Long = System.currentTimeMillis()
        var dug = false

        constructor(vec3: Vec3i, hasFootstep: Boolean, hasEnchant: Boolean, type: Int) : this(
            vec3.x,
            vec3.y,
            vec3.z,
            hasFootstep,
            hasEnchant,
            type
        )

        val blockPos: BlockPos
            get() = BlockPos(x, y, z)
        private val waypointText: String
            get() {
                var type = "Burrow"
                when (this.type) {
                    0 -> type = EnumChatFormatting.GREEN.toString() + "Start"
                    1 -> type = EnumChatFormatting.RED.toString() + "Mob"
                    2 -> type = EnumChatFormatting.GOLD.toString() + "Treasure"
                }
                return "$type §a(Particle)"
            }
        private val color: Color
            get() {
                return when (this.type) {
                    0 -> Skytils.config.emptyBurrowColor
                    1 -> Skytils.config.mobBurrowColor
                    2 -> Skytils.config.treasureBurrowColor
                    else -> Color.WHITE
                }
            }

        fun drawWaypoint(partialTicks: Float) {
            val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(partialTicks)
            val pos = blockPos
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            val distSq = x * x + y * y + z * z
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1),
                this.color,
                (0.1f + 0.005f * distSq.toFloat()).coerceAtLeast(0.2f)
            )
            GlStateManager.disableTexture2D()
            if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(x, y + 1, z, this.color.rgb, 1.0f, partialTicks)
            RenderUtil.renderWaypointText(
                waypointText,
                blockPos.x + 0.5,
                blockPos.y + 5.0,
                blockPos.z + 0.5,
                partialTicks
            )
            GlStateManager.disableLighting()
            GlStateManager.enableTexture2D()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }

    }

    class Burrow(
        var x: Int, var y: Int, var z: Int,
        /**
         * This variable seems to hold whether or not the burrow is the start/empty, a mob, or treasure
         */
        var type: Int,
        /**
         * This variable holds the Griffin used, -1 means no Griffin, 0 means Common, etc.
         */
        var tier: Int,
        /**
         * This variable appears to hold what order the burrow is in the chain
         */
        private var chain: Int
    ) {
        val blockPos: BlockPos
            get() = BlockPos(x, y, z)
        private val waypointText: String
            get() {
                var type = "Burrow"
                when (this.type) {
                    0 -> type =
                        if (chain == 0) EnumChatFormatting.GREEN.toString() + "Start" else EnumChatFormatting.WHITE.toString() + "Empty"
                    1 -> type = EnumChatFormatting.RED.toString() + "Mob"
                    2, 3 -> type = EnumChatFormatting.GOLD.toString() + "Treasure"
                }
                var closest: FastTravelLocations? = null
                var distance = mc.thePlayer.position.distanceSq(blockPos)
                for (warp in FastTravelLocations.values()) {
                    if (!warp.toggled) continue
                    val warpDistance = blockPos.distanceSq(warp.pos)
                    if (warpDistance < distance) {
                        distance = warpDistance
                        closest = warp
                    }
                }
                return "$type §bPosition: ${chain + 1}/4${
                    if (closest != null) " ${closest.nameWithColor}" else ""
                }"
            }

        private val color: Color
            get() {
                return when (this.type) {
                    0 -> Skytils.config.emptyBurrowColor
                    1 -> Skytils.config.mobBurrowColor
                    2, 3 -> Skytils.config.treasureBurrowColor
                    else -> Color.WHITE
                }
            }

        fun drawWaypoint(partialTicks: Float) {
            val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(partialTicks)
            val pos = blockPos
            val x = pos.x - viewerX
            val y = pos.y - viewerY
            val z = pos.z - viewerZ
            val distSq = x * x + y * y + z * z
            GlStateManager.disableDepth()
            GlStateManager.disableCull()
            RenderUtil.drawFilledBoundingBox(
                AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1),
                this.color,
                (0.1f + 0.005f * distSq.toFloat()).coerceAtLeast(0.2f)
            )
            GlStateManager.disableTexture2D()
            if (distSq > 5 * 5) RenderUtil.renderBeaconBeam(x, y + 1, z, this.color.rgb, 1.0f, partialTicks)
            RenderUtil.renderWaypointText(
                waypointText,
                blockPos.x + 0.5,
                blockPos.y + 5.0,
                blockPos.z + 0.5, partialTicks
            )
            GlStateManager.disableLighting()
            GlStateManager.enableTexture2D()
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    enum class FastTravelLocations(val pos: BlockPos) {
        CASTLE(BlockPos(-250, 130, 45)), CRYPTS(
            BlockPos(-162, 60, -100)
        ),
        DA(BlockPos(91, 74, 173)), HUB(
            BlockPos(-3, 70, -70)
        );

        val toggled: Boolean
            get() {
                if (this == CASTLE) return Skytils.config.burrowCastleFastTravel
                if (this == CRYPTS) return Skytils.config.burrowCryptsFastTravel
                if (this == DA) return Skytils.config.burrowDarkAuctionFastTravel
                if (this == HUB) return Skytils.config.burrowHubFastTravel
                return false
            }

        val nameWithColor: String
            get() = when (this) {
                CASTLE -> EnumChatFormatting.GRAY.toString() + "CASTLE"
                CRYPTS -> EnumChatFormatting.DARK_GREEN.toString() + "CRYPTS"
                DA -> EnumChatFormatting.DARK_PURPLE.toString() + "DA"
                HUB -> EnumChatFormatting.WHITE.toString() + "HUB"
            }
    }
}