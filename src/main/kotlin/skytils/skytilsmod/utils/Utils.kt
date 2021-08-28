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

import gg.essential.universal.UResolution
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.gui.settings.CheckboxComponent
import io.netty.util.internal.ConcurrentSet
import net.minecraft.client.gui.ChatLine
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.client.settings.GameSettings
import net.minecraft.crash.CrashReportCategory
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.HoverEvent
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiNewChat
import skytils.skytilsmod.utils.graphics.colors.ColorFactory.web
import skytils.skytilsmod.utils.graphics.colors.CustomColor
import skytils.skytilsmod.utils.graphics.colors.RainbowColor.Companion.fromString
import java.awt.Color
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Future
import kotlin.math.floor
import kotlin.math.roundToInt


object Utils {
    @JvmField
    var noSychic = false

    @JvmField
    var inSkyblock = false

    @JvmField
    var inDungeons = false

    @JvmField
    var isOnHypixel = false

    @JvmField
    var shouldBypassVolume = false

    @JvmField
    var lastRenderedSkullStack: ItemStack? = null

    @JvmField
    var lastRenderedSkullEntity: EntityLivingBase? = null

    @JvmStatic
    var random = Random()

    fun getBlocksWithinRangeAtSameY(center: BlockPos, radius: Int, y: Int): Iterable<BlockPos> {
        val corner1 = BlockPos(center.x - radius, y, center.z - radius)
        val corner2 = BlockPos(center.x + radius, y, center.z + radius)
        return BlockPos.getAllInBox(corner1, corner2)
    }

    @JvmStatic
    fun isInTablist(player: EntityPlayer): Boolean {
        if (mc.isSingleplayer) {
            return true
        }
        return mc.netHandler.playerInfoMap.any { it.gameProfile.name.equals(player.name, ignoreCase = true) }
    }

    /**
     * Taken from SkyblockAddons under MIT License
     * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
     * @author BiscuitDevelopment
     */
    fun playLoudSound(sound: String?, pitch: Double) {
        shouldBypassVolume = true
        mc.thePlayer.playSound(sound, 1f, pitch.toFloat())
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
            mc.addScheduledTask(run)
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

    fun generateDebugInfo(crashReportCategory: CrashReportCategory) {
        crashReportCategory.addCrashSectionCallable("Skytils Debug Info") {
            val hasBetterFPS = runCatching {
                Class.forName("me.guichaguri.betterfps.BetterFpsHelper").getDeclaredField("VERSION").also { it.isAccessible = true }
                    .get(null) as String
            }.getOrDefault("NONE")

            return@addCrashSectionCallable """
                            # BetterFPS: ${hasBetterFPS != "NONE"} version: $hasBetterFPS; Disabled Startup Check: ${
                System.getProperty(
                    "skytils.skipStartChecks"
                ) != null
            }
                        """.trimMargin("#")
        }
    }

    fun getKeyDisplayStringSafe(keyCode: Int): String =
        runCatching { GameSettings.getKeyDisplayString(keyCode) }.getOrNull() ?: "Key $keyCode"
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

fun GuiNewChat.getChatLine(mouseX: Int, mouseY: Int): ChatLine? {
    if (this is AccessorGuiNewChat) {
        if (this.chatOpen) {
            val scaledresolution = UResolution
            val scaleFactor = scaledresolution.scaleFactor
            val chatScale = this.chatScale
            var xPos = mouseX / scaleFactor.toInt() - 3
            var yPos = mouseY / scaleFactor.toInt() - 27
            xPos = MathHelper.floor_float(xPos.toFloat() / chatScale)
            yPos = MathHelper.floor_float(yPos.toFloat() / chatScale)
            if (xPos >= 0 && yPos >= 0) {
                val lineCount: Int = this.lineCount.coerceAtMost(this.drawnChatLines.size)
                if (xPos <= MathHelper.floor_float(this.chatWidth.toFloat() / this.chatScale) && yPos < mc.fontRendererObj.FONT_HEIGHT * lineCount + lineCount) {
                    val lineNum: Int = yPos / mc.fontRendererObj.FONT_HEIGHT + this.scrollPos
                    if (lineNum >= 0 && lineNum < this.drawnChatLines.size) {
                        return drawnChatLines[lineNum]
                    }
                }
            }
        }
    }
    return null
}

fun UMessage.append(item: Any) = this.addTextComponent(item)
fun UTextComponent.setHoverText(text: String): UTextComponent {
    hoverAction = HoverEvent.Action.SHOW_TEXT
    hoverValue = text
    return this
}

fun Entity.getXZDistSq(other: Entity): Double {
    val xDelta = this.posX - other.posX
    val zDelta = this.posZ - other.posZ
    return xDelta * xDelta + zDelta * zDelta
}

val Entity.hasMoved
    get() = this.posX != this.prevPosX || this.posY != this.prevPosY || this.posZ != this.prevPosZ

fun CheckboxComponent.toggle() {
    this.mouseClick(this.getLeft().toDouble(), this.getTop().toDouble(), 0)
}

fun CheckboxComponent.setState(checked: Boolean) {
    if (this.checked != checked) this.toggle()
}