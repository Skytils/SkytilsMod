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

package gg.skytils.skytilsmod.features.impl.mining

import gg.essential.universal.UChat
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.utils.MCClickEventAction
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.HypixelPacketEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.colors.ColorFactory
import gg.skytils.skytilsws.client.WSClient
import gg.skytils.skytilsws.shared.packet.C2SPacketCHWaypoint
import gg.skytils.skytilsws.shared.packet.C2SPacketCHWaypointsSubscribe
import gg.skytils.skytilsws.shared.structs.CHWaypointType
import kotlinx.coroutines.launch
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.jvm.optionals.getOrNull

object CHWaypoints {
    var lastTPLoc: BlockPos? = null
    var waypoints = hashMapOf<String, BlockPos>()
    var waypointDelayTicks = 0
    private val SBE_DSM_PATTERN =
        Regex("\\\$(?:SBECHWP\\b|DSMCHWP):(?<stringLocation>.*?)@-(?<x>-?\\d+),(?<y>-?\\d+),(?<z>-?\\d+)")
    private val xyzPattern =
        Regex(".*?(?<user>[a-zA-Z0-9_]{3,16}):.*?(?<x>[0-9]{1,3}),? (?:y: )?(?<y>[0-9]{1,3}),? (?:z: )?(?<z>[0-9]{1,3}).*?")
    private val xzPattern =
        Regex(".*(?<user>[a-zA-Z0-9_]{3,16}):.* (?<x>[0-9]{1,3}),? (?<z>[0-9]{1,3}).*")
    val chWaypointsList = hashMapOf<String, CHInstance>()
    class CHInstance {
        val waypoints = hashMapOf<CHWaypointType, BlockPos>()
    }


    @SubscribeEvent
    fun onHypixelPacket(event: HypixelPacketEvent.ReceiveEvent) {
        if (event.packet is ClientboundLocationPacket) {
            if (event.packet.mode.getOrNull() == SkyblockIsland.CrystalHollows.mode) {
                Skytils.IO.launch {
                    WSClient.sendPacket(C2SPacketCHWaypointsSubscribe(event.packet.serverName))
                }
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.crystalHollowDeathWaypoint && event.packet is S08PacketPlayerPosLook && mc.thePlayer != null) {
            lastTPLoc = mc.thePlayer.position
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte()) return
        val unformatted = event.message.unformattedText.stripControlCodes()
        if (Skytils.config.hollowChatCoords && SBInfo.mode == SkyblockIsland.CrystalHollows.mode) {
            xyzPattern.find(unformatted)?.groups?.let {
                waypointChatMessage(it["x"]!!.value, it["y"]!!.value, it["z"]!!.value)
                return
            }
            xzPattern.find(unformatted)?.groups?.let {
                waypointChatMessage(it["x"]!!.value, "100", it["z"]!!.value)
                return
            }

            /**
             * Checks for the format used in DSM and SBE
             * $DSMCHWP:Mines of Divan@-673,117,426 ✔
             * $SBECHWP:Khazad-dûm@-292,63,281 ✔
             * $asdf:Khazad-dûm@-292,63,281 ❌
             * $SBECHWP:Khazad-dûm@asdf,asdf,asdf ❌
             */
            val cleaned = SBE_DSM_PATTERN.find(unformatted)
            if (cleaned != null) {
                val stringLocation = cleaned.groups["stringLocation"]!!.value
                val x = cleaned.groups["x"]!!.value
                val y = cleaned.groups["y"]!!.value
                val z = cleaned.groups["z"]!!.value
                CrystalHollowsMap.Locations.entries.find { it.cleanName == stringLocation }
                    ?.takeIf { !it.loc.exists() }?.let { loc ->
                        /**
                         * Sends the waypoints message except it suggests which one should be used based on
                         * the name contained in the message and converts it to the internally used names for the waypoints.
                         */
                        UMessage("§3Skytils > §eFound coordinates in a chat message, click a button to set a waypoint.\n")
                            .append(
                                UTextComponent("§f${loc.displayName} ")
                                    .setClick(
                                        MCClickEventAction.RUN_COMMAND,
                                        "/skytilshollowwaypoint set $x $y $z ${loc.id}"
                                    )
                                    .setHoverText("§eSet waypoint for ${loc.displayName}")
                            )
                            .append(
                                UTextComponent("§e[Custom]")
                                    .setClick(
                                        MCClickEventAction.SUGGEST_COMMAND,
                                        "/skytilshollowwaypoint set $x $y $z name_here"
                                    )
                                    .setHoverText("§eSet custom waypoint")
                            ).chat()
                    }
            }
        }
        if ((Skytils.config.crystalHollowWaypoints || Skytils.config.crystalHollowMapPlaces) && Skytils.config.kingYolkarWaypoint && SBInfo.mode == SkyblockIsland.CrystalHollows.mode
            && mc.thePlayer != null && unformatted.startsWith("[NPC] King Yolkar:")
        ) {
            val yolkar = CrystalHollowsMap.Locations.KingYolkar
            yolkar.loc.set()
            yolkar.sendThroughWS()
        }
        if (unformatted.startsWith("You died") || unformatted.startsWith("☠ You were killed")) {
            waypointDelayTicks =
                50 //this is to make sure the scoreboard has time to update and nothing moves halfway across the map
            if (Skytils.config.crystalHollowDeathWaypoint && SBInfo.mode == SkyblockIsland.CrystalHollows.mode && lastTPLoc != null) {
                UChat.chat(
                    UTextComponent("$prefix §eClick to set a death waypoint at ${lastTPLoc!!.x} ${lastTPLoc!!.y} ${lastTPLoc!!.z}").setClick(
                        MCClickEventAction.RUN_COMMAND,
                        "/sthw set ${lastTPLoc!!.x} ${lastTPLoc!!.y} ${lastTPLoc!!.z} Last Death"
                    )
                )
            }
        } else if (unformatted.startsWith("Warp")) {
            waypointDelayTicks = 50
        }
    }

    private fun waypointChatMessage(x: String, y: String, z: String) {
        val message = UMessage(
            "$prefix §eFound coordinates in a chat message, click a button to set a waypoint.\n"
        )
        for (loc in CrystalHollowsMap.Locations.entries) {
            if (loc.loc.exists()) continue
            message.append(
                UTextComponent("${loc.displayName.substring(0, 2)}[${loc.displayName}] ")
                    .setClick(MCClickEventAction.SUGGEST_COMMAND, "/sthw set $x $y $z ${loc.id}")
                    .setHoverText("§eSet waypoint for ${loc.cleanName}")
            )
        }
        message.append(
            UTextComponent("§e[Custom]").setClick(
                MCClickEventAction.SUGGEST_COMMAND,
                "/sthw set $x $y $z Name"
            ).setHoverText("§eSet waypoint for custom location")
        )
        message.chat()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Utils.inSkyblock) return
        val matrixStack = UMatrixStack()
        if (Skytils.config.crystalHollowWaypoints && SBInfo.mode == SkyblockIsland.CrystalHollows.mode) {
            GlStateManager.disableDepth()
            for (loc in CrystalHollowsMap.Locations.entries) {
                loc.loc.drawWaypoint(loc.cleanName, event.partialTicks, matrixStack)
            }
            RenderUtil.renderWaypointText("Crystal Nucleus", 513.5, 107.0, 513.5, event.partialTicks, matrixStack)
            for ((key, value) in waypoints)
                RenderUtil.renderWaypointText(key, value, event.partialTicks, matrixStack)
            GlStateManager.enableDepth()
        }
    }

    @SubscribeEvent
    fun onRenderLivingPre(event: RenderLivingEvent.Pre<EntityLivingBase?>) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.crystalHollowWaypoints &&
            event.entity is EntityOtherPlayerMP &&
            event.entity.name == "Team Treasurite" &&
            mc.thePlayer.canEntityBeSeen(event.entity) &&
            event.entity.baseMaxHealth == if (MayorInfo.allPerks.contains("DOUBLE MOBS HP!!!")) 2_000_000.0 else 1_000_000.0
        ) {
            val corleone = CrystalHollowsMap.Locations.Corleone
            if (!corleone.loc.exists()) {
                corleone.loc.set()
                corleone.sendThroughWS()
            } else corleone.loc.set()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW) // priority low so it always runs after sbinfo is updated
    fun onTick(event: ClientTickEvent) {
        if (!Utils.inSkyblock || event.phase != TickEvent.Phase.START) return
        if ((Skytils.config.crystalHollowWaypoints || Skytils.config.crystalHollowMapPlaces) && SBInfo.mode == SkyblockIsland.CrystalHollows.mode
            && waypointDelayTicks == 0 && mc.thePlayer != null
        ) {
            CrystalHollowsMap.Locations.cleanNameToLocation[SBInfo.location]?.let {
                if (!it.loc.exists()) {
                    it.loc.set()
                    it.sendThroughWS()
                } else it.loc.set()
            }
        } else if (waypointDelayTicks > 0)
            waypointDelayTicks--
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onWorldChange(event: WorldEvent.Unload) {
        val instance = chWaypointsList.getOrPut(SBInfo.server ?: "") { CHInstance() }
        CrystalHollowsMap.Locations.entries.forEach {
            if (it.loc.exists()) {
                instance.waypoints[it.packetType] = BlockPos(it.loc.locX!!, it.loc.locY!!, it.loc.locZ!!)
            }
            it.loc.reset()
        }
        waypoints.clear()
    }


    class CrystalHollowsMap : GuiElement(name = "Crystal Hollows Map", x = 0, y = 0) {
        val mapLocation = ResourceLocation("skytils", "crystalhollowsmap.png")

        enum class Locations(val displayName: String, val id: String, val color: Int, val packetType: CHWaypointType, val size: Int = 50) {
            LostPrecursorCity("§fLost Precursor City", "internal_city", ColorFactory.WHITE.rgb, CHWaypointType.LostPrecursorCity),
            JungleTemple("§aJungle Temple", "internal_temple", ColorFactory.GREEN.rgb, CHWaypointType.JungleTemple),
            GoblinQueensDen("§eGoblin Queen's Den", "internal_den", ColorFactory.YELLOW.rgb, CHWaypointType.GoblinQueensDen),
            MinesOfDivan("§9Mines of Divan", "internal_mines", ColorFactory.BLUE.rgb, CHWaypointType.MinesOfDivan),
            KingYolkar("§6King Yolkar", "internal_king", ColorFactory.ORANGE.rgb, CHWaypointType.KingYolkar,25),
            KhazadDum("§cKhazad-dûm", "internal_bal", ColorFactory.RED.rgb, CHWaypointType.KhazadDum),
            FairyGrotto("§dFairy Grotto", "internal_fairy", ColorFactory.PINK.rgb, CHWaypointType.FairyGrotto, 26),
            Corleone("§bCorleone", "internal_corleone", ColorFactory.AQUA.rgb, CHWaypointType.Corleone, 26);

            val loc = LocationObject()
            val cleanName = displayName.stripControlCodes()

            companion object {
                val cleanNameToLocation = entries.associateBy { it.cleanName }
            }

            fun sendThroughWS() {
                if (loc.exists()) {
                    Skytils.IO.launch {
                        WSClient.sendPacket(C2SPacketCHWaypoint(serverId = SBInfo.server ?: "", serverTime = mc.theWorld.worldTime, packetType, loc.locX!!.toInt(), loc.locY!!.toInt(), loc.locZ!!.toInt()))
                    }
                }
            }
        }

        override fun render() {
            if (!toggled || SBInfo.mode != SkyblockIsland.CrystalHollows.mode || mc.thePlayer == null) return
            val stack = UMatrixStack()
            UMatrixStack.Compat.runLegacyMethod(stack) {
                stack.scale(0.1, 0.1, 1.0)
                UGraphics.disableLighting()
                stack.runWithGlobalState {
                    RenderUtil.renderTexture(mapLocation, 0, 0, 624, 624, false)
                    if (Skytils.config.crystalHollowMapPlaces) {
                        Locations.entries.forEach {
                            it.loc.drawOnMap(it.size, it.color)
                        }
                    }
                }
                val x = (mc.thePlayer.posX - 202).coerceIn(0.0, 624.0)
                val y = (mc.thePlayer.posZ - 202).coerceIn(0.0, 624.0)

                // player marker code
                val wr = UGraphics.getFromTessellator()
                mc.textureManager.bindTexture(ResourceLocation("textures/map/map_icons.png"))

                stack.push()
                stack.translate(x, y, 0.0)

                // Rotate about the center to match the player's yaw
                stack.rotate((mc.thePlayer.rotationYawHead + 180f) % 360f, 0f, 0f, 1f)
                stack.scale(1.5f, 1.5f, 1.5f)
                stack.translate(-0.125f, 0.125f, 0.0f)
                UGraphics.color4f(1f, 1f, 1f, 1f)
                UGraphics.enableAlpha()
                val d1 = 0.0
                val d2 = 0.25
                wr.beginWithActiveShader(UGraphics.DrawMode.QUADS, DefaultVertexFormats.POSITION_TEX)
                wr.pos(stack, -8.0, -8.0, 100.0).tex(d1, d1).endVertex()
                wr.pos(stack, -8.0, 8.0, 100.0).tex(d1, d2).endVertex()
                wr.pos(stack, 8.0, 8.0, 100.0).tex(d2, d2).endVertex()
                wr.pos(stack, 8.0, -8.0, 100.0).tex(d2, d1).endVertex()
                wr.drawDirect()
                stack.pop()
            }
        }

        override fun demoRender() {
            UGraphics.disableLighting()
            RenderUtil.renderTexture(mapLocation, 0, 0, 62, 62, false)
        }

        override val toggled: Boolean
            get() = Skytils.config.crystalHollowMap
        override val height: Int
            get() = 62 // should be 62.4 but oh well
        override val width: Int
            get() = 62

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    init {
        CrystalHollowsMap()
    }

    class LocationObject {
        var locX: Double? = null
        var locY: Double? = null
        var locZ: Double? = null
        private var locMinX: Double = 1100.0
        private var locMinY: Double = 1100.0
        private var locMinZ: Double = 1100.0
        private var locMaxX: Double = -100.0
        private var locMaxY: Double = -100.0
        private var locMaxZ: Double = -100.0

        fun reset() {
            locX = null
            locY = null
            locZ = null
            locMinX = 1100.0
            locMinY = 1100.0
            locMinZ = 1100.0
            locMaxX = -100.0
            locMaxY = -100.0
            locMaxZ = -100.0
        }

        fun set() {
            locMinX = (mc.thePlayer.posX - 200).coerceIn(0.0, 624.0).coerceAtMost(locMinX)
            locMinY = mc.thePlayer.posY.coerceIn(0.0, 256.0).coerceAtMost(locMinY)
            locMinZ = (mc.thePlayer.posZ - 200).coerceIn(0.0, 624.0).coerceAtMost(locMinZ)
            locMaxX = (mc.thePlayer.posX - 200).coerceIn(0.0, 624.0).coerceAtLeast(locMaxX)
            locMaxY = mc.thePlayer.posY.coerceIn(0.0, 256.0).coerceAtLeast(locMaxY)
            locMaxZ = (mc.thePlayer.posZ - 200).coerceIn(0.0, 624.0).coerceAtLeast(locMaxZ)
            locX = (locMinX + locMaxX) / 2
            locY = (locMinY + locMaxY) / 2
            locZ = (locMinZ + locMaxZ) / 2
        }

        fun exists(): Boolean {
            return locX != null && locY != null && locZ != null
        }

        fun drawWaypoint(text: String, partialTicks: Float, matrixStack: UMatrixStack) {
            if (exists())
                RenderUtil.renderWaypointText(text, locX!! + 200, locY!!, locZ!! + 200, partialTicks, matrixStack)
        }

        fun drawOnMap(size: Int, color: Int) {
            if (exists())
                RenderUtil.drawRect(locX!! - size, locZ!! - size, locX!! + size, locZ!! + size, color)
        }

        override fun toString(): String {
            return String.format("%.0f", locX?.plus(200)) + " " + String.format(
                "%.0f",
                locY
            ) + " " + String.format(
                "%.0f",
                locZ?.plus(200)
            )
        }
    }
}