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
package skytils.skytilsmod.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Slot
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.utils.graphics.colors.ColorFactory.web
import skytils.skytilsmod.utils.graphics.colors.CustomColor
import skytils.skytilsmod.utils.graphics.colors.RainbowColor.Companion.fromString
import java.awt.Color
import java.util.*

object Utils {
    private val mc = Minecraft.getMinecraft()

    @JvmField
    var inSkyblock = false

    @JvmField
    var inDungeons = false

    @JvmField
    var shouldBypassVolume = false

    @JvmStatic
    var random = Random()

    @JvmStatic
    val isOnHypixel: Boolean
        get() {
            try {
                if (mc.theWorld != null && !mc.isSingleplayer) {
                    if (mc.thePlayer != null && mc.thePlayer.clientBrand != null) {
                        if (mc.thePlayer.clientBrand.lowercase().contains("hypixel")) return true
                    }
                    if (mc.currentServerData != null) return mc.currentServerData.serverIP.lowercase()
                        .contains("hypixel")
                }
                return false
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }

    /**
     * Taken from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    fun checkForSkyblock() {
        if (isOnHypixel) {
            val scoreboardObj = mc!!.theWorld.scoreboard.getObjectiveInDisplaySlot(1)
            if (scoreboardObj != null) {
                val scObjName = ScoreboardUtil.cleanSB(scoreboardObj.displayName)
                if (scObjName.contains("SKYBLOCK")) {
                    inSkyblock = true
                    return
                }
            }
        }
        inSkyblock = false
    }

    /**
     * Taken from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    fun checkForDungeons() {
        if (inSkyblock) {
            val scoreboard = ScoreboardUtil.sidebarLines
            for (s in scoreboard) {
                val sCleaned = ScoreboardUtil.cleanSB(s)
                if (sCleaned.contains("The Catacombs") && !sCleaned.contains("Queue") || sCleaned.contains("Dungeon Cleared:")) {
                    inDungeons = true
                    return
                }
            }
        }
        inDungeons = false
    }

    fun getSlotUnderMouse(gui: GuiContainer): Slot {
        return ObfuscationReflectionHelper.getPrivateValue(GuiContainer::class.java, gui, "theSlot", "field_147006_u")
    }

    fun getBlocksWithinRangeAtSameY(center: BlockPos, radius: Int, y: Int): Iterable<BlockPos> {
        val corner1 = BlockPos(center.x - radius, y, center.z - radius)
        val corner2 = BlockPos(center.x + radius, y, center.z + radius)
        return BlockPos.getAllInBox(corner1, corner2)
    }

    @JvmStatic
    fun isInTablist(player: EntityPlayer): Boolean {
        if (mc!!.isSingleplayer) {
            return true
        }
        for (pi in mc.netHandler.playerInfoMap) {
            if (pi.gameProfile.name.equals(player.name, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * Taken from SkyblockAddons under MIT License
     * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
     * @author BiscuitDevelopment
     */
    fun playLoudSound(sound: String?, pitch: Double) {
        shouldBypassVolume = true
        mc!!.thePlayer.playSound(sound, 1f, pitch.toFloat())
        shouldBypassVolume = false
    }

    /**
     * Checks if an object is equal to any of the other objects
     * @param object Object to compare
     * @param other Objects being compared
     * @return boolean
     */
    @JvmStatic
    fun equalsOneOf(`object`: Any?, vararg other: Any): Boolean {
        for (obj in other) {
            if (`object` == obj) return true
        }
        return false
    }

    fun customColorFromString(string: String?): CustomColor {
        if (string == null) throw NullPointerException("Argument cannot be null!")
        if (string.startsWith("rainbow(")) {
            return fromString(string)
        }
        return try {
            getCustomColorFromColor(web(string))
        } catch (e: IllegalArgumentException) {
            try {
                CustomColor.fromInt(string.toInt())
            } catch (ignored: NumberFormatException) {
                throw e
            }
        }
    }

    fun getCustomColorFromColor(color: Color): CustomColor {
        return CustomColor.fromInt(color.rgb)
    }

    /**
     * Cancels a chat packet and posts the chat event to the event bus if other mods need it
     * @param ReceivePacketEvent packet to cancel
     */
    fun cancelChatPacket(ReceivePacketEvent: ReceiveEvent) {
        if (ReceivePacketEvent.packet !is S02PacketChat) return
        ReceivePacketEvent.isCanceled = true
        val packet = ReceivePacketEvent.packet
        MinecraftForge.EVENT_BUS.post(ClientChatReceivedEvent(packet.type, packet.chatComponent))
    }
}