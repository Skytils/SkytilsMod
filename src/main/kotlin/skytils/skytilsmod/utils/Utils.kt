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

import gg.essential.vigilance.Vigilant
import io.netty.util.internal.ConcurrentSet
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.ObfuscationReflectionHelper
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.utils.graphics.colors.ColorFactory.web
import skytils.skytilsmod.utils.graphics.colors.CustomColor
import skytils.skytilsmod.utils.graphics.colors.RainbowColor.Companion.fromString
import java.awt.Color
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.Future
import kotlin.math.floor
import kotlin.math.roundToInt


object Utils {
    private val mc = Minecraft.getMinecraft()

    @JvmField
    var inSkyblock = false

    @JvmField
    var inDungeons = false

    @JvmField
    var shouldBypassVolume = false

    @JvmField
    var lastRenderedSkullStack: ItemStack? = null

    @JvmField
    var lastRenderedSkullEntity: EntityLivingBase? = null

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
            if (ScoreboardUtil.sidebarLines.any {
                    val sCleaned = ScoreboardUtil.cleanSB(it)
                    sCleaned.contains("The Catacombs") && !sCleaned.contains("Queue") || sCleaned.contains("Dungeon Cleared:")
                }) {
                inDungeons = true
                return
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

    private fun getCustomColorFromColor(color: Color) = CustomColor.fromInt(color.rgb)

    fun checkThreadAndQueue(run: () -> Unit) {
        if (!mc.isCallingFromMinecraftThread) {
            mc.addScheduledTask {
                run()
            }
        } else run()
    }

    /**
     * Cancels a chat packet and posts the chat event to the event bus if other mods need it
     * @param ReceivePacketEvent packet to cancel
     */
    fun cancelChatPacket(ReceivePacketEvent: ReceiveEvent) {
        if (ReceivePacketEvent.packet !is S02PacketChat) return
        ReceivePacketEvent.isCanceled = true
        val packet = ReceivePacketEvent.packet
        checkThreadAndQueue {
            MinecraftForge.EVENT_BUS.post(ClientChatReceivedEvent(packet.type, packet.chatComponent))
        }
    }

    fun timeFormat(seconds: Double): String {
        return if (seconds >= 60) {
            floor(seconds / 60).toInt().toString() + "m " + (seconds % 60).roundToInt() + "s"
        } else {
            seconds.roundToInt().toString() + "s"
        }
    }

    fun InputStream.readTextAndClose(charset: Charset = Charsets.UTF_8): String =
        this.bufferedReader(charset).use { it.readText() }

    /**
     * @link https://stackoverflow.com/a/47925649
     */
    @Throws(IOException::class)
    fun getJavaRuntime(): String {
        val os = System.getProperty("os.name")
        val java = "${System.getProperty("java.home")}${File.separator}bin${File.separator}${
            if (os != null && os.lowercase().startsWith("windows")) "java.exe" else "java"
        }"
        if (!File(java).isFile) {
            throw IOException("Unable to find suitable java runtime at $java")
        }
        return java
    }

    fun checkBossName(floor: String, bossName: String): Boolean {
        val correctBoss = when (floor) {
            "E" -> "The Watcher"
            "F1", "M1" -> "Bonzo"
            "F2", "M2" -> "Scarf"
            "F3", "M3" -> "The Professor"
            "F4", "M4" -> "Thorn"
            "F5", "M5" -> "Livid"
            "F6", "M6" -> "Sadan"
            "F7", "M7" -> "Necron"
            else -> null
        } ?: return false

        // Livid has a prefix in front of the name, so we check ends with to cover all the livids
        return bossName.endsWith(correctBoss)
    }

}

typealias ConcurrentHashSet<T> = ConcurrentSet<T>

val AxisAlignedBB.minVec: Vec3
    get() = Vec3(minX, minY, minZ)
val AxisAlignedBB.maxVec: Vec3
    get() = Vec3(maxX, maxY, maxZ)

fun Vigilant.openGUI(): Future<*> = Skytils.threadPool.submit {
    Skytils.displayScreen = this.gui()
}

val EntityLivingBase.baseMaxHealth: Double
    get() = this.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue