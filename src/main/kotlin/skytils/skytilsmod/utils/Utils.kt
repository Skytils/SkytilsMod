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
package skytils.skytilsmod.utils

import dev.falsehonesty.asmhelper.AsmHelper
import dev.falsehonesty.asmhelper.dsl.instructions.Descriptor
import gg.essential.lib.caffeine.cache.Cache
import gg.essential.universal.UResolution
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.gui.settings.CheckboxComponent
import net.minecraft.client.gui.ChatLine
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.event.HoverEvent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagList
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.*
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import org.objectweb.asm.tree.MethodInsnNode
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.asm.SkytilsTransformer
import skytils.skytilsmod.events.impl.MainReceivePacketEvent
import skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiNewChat
import skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import skytils.skytilsmod.utils.graphics.colors.ColorFactory.web
import skytils.skytilsmod.utils.graphics.colors.CustomColor
import skytils.skytilsmod.utils.graphics.colors.RainbowColor.Companion.fromString
import java.awt.Color
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.Future
import kotlin.math.floor


object Utils {

    val azooPuzzoo by lazy {
        File(Skytils.modDir, "azoopuzzoo").exists()
    }

    val breefingdog by lazy {
        File(Skytils.modDir, "breefingdog").exists()
    }

    @JvmField
    var noSychic = false

    @JvmField
    var skyblock = false

    val inSkyblock: Boolean
        get() = skyblock || SBInfo.mode == "SKYBLOCK"

    @JvmField
    var dungeons = false

    val inDungeons: Boolean
        get() = dungeons || SBInfo.mode == "dungeon"

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
            MinecraftForge.EVENT_BUS.post(MainReceivePacketEvent(mc.netHandler, ReceivePacketEvent.packet))
            MinecraftForge.EVENT_BUS.post(ClientChatReceivedEvent(packet.type, packet.chatComponent))
        }
    }

    fun timeFormat(seconds: Double): String {
        return if (seconds >= 60) {
            "${floor(seconds / 60).toInt()}m ${(seconds % 60).roundToPrecision(3)}s"
        } else {
            "${seconds.roundToPrecision(3)}s"
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

    fun getKeyDisplayStringSafe(keyCode: Int): String =
        runCatching { GameSettings.getKeyDisplayString(keyCode) }.getOrNull() ?: "Key $keyCode"
}

val AxisAlignedBB.minVec: Vec3
    get() = Vec3(minX, minY, minZ)
val AxisAlignedBB.maxVec: Vec3
    get() = Vec3(maxX, maxY, maxZ)

fun AxisAlignedBB.isPosInside(pos: BlockPos): Boolean {
    return pos.x > this.minX && pos.x < this.maxX && pos.y > this.minY && pos.y < this.maxY && pos.z > this.minZ && pos.z < this.maxZ
}

fun Vigilant.openGUI(): Future<*> = Skytils.threadPool.submit {
    Skytils.displayScreen = this.gui()
}

val EntityLivingBase.baseMaxHealth: Double
    get() = this.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue

fun GuiNewChat.getChatLine(mouseX: Int, mouseY: Int): ChatLine? {
    if (chatOpen && this is AccessorGuiNewChat) {
        val scaleFactor = UResolution.scaleFactor
        val extraOffset =
            if (ReflectionHelper.getFieldFor("club.sk1er.patcher.config.PatcherConfig", "chatPosition")
                    ?.getBoolean(null) == true
            ) 12 else 0
        val x = MathHelper.floor_float((mouseX / scaleFactor - 3).toFloat() / chatScale)
        val y = MathHelper.floor_float((mouseY / scaleFactor - 27 - extraOffset).toFloat() / chatScale)

        if (x >= 0 && y >= 0) {
            val l = this.lineCount.coerceAtMost(this.drawnChatLines.size)
            if (x <= MathHelper.floor_float(this.chatWidth.toFloat() / this.chatScale) && y < mc.fontRendererObj.FONT_HEIGHT * l + l) {
                val lineNum = y / mc.fontRendererObj.FONT_HEIGHT + this.scrollPos
                if (lineNum >= 0 && lineNum < this.drawnChatLines.size) {
                    return drawnChatLines[lineNum]
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

fun Entity.getRotationFor(pos: BlockPos): Pair<Float, Float> {
    val deltaX = pos.x - posX
    val deltaZ = pos.z - posZ
    val deltaY = pos.y - (posY + eyeHeight)

    val dist = MathHelper.sqrt_double(deltaX * deltaX + deltaZ * deltaZ).toDouble()
    val yaw = (MathHelper.atan2(deltaZ, deltaX) * 180.0 / Math.PI).toFloat() - 90.0f
    val pitch = (-(MathHelper.atan2(deltaY, dist) * 180.0 / Math.PI)).toFloat()
    return yaw to pitch
}

fun CheckboxComponent.toggle() {
    this.mouseClick(this.getLeft().toDouble(), this.getTop().toDouble(), 0)
}

fun CheckboxComponent.setState(checked: Boolean) {
    if (this.checked != checked) this.toggle()
}

fun BlockPos?.toVec3() = if (this == null) null else Vec3(this)

fun <T : Any> T?.ifNull(run: () -> Unit): T? {
    if (this == null) run()
    return this
}

val MethodInsnNode.descriptor: Descriptor
    get() = Descriptor(
        AsmHelper.remapper.remapClassName(this.owner),
        SkytilsTransformer.methodMaps.getOrSelf(AsmHelper.remapper.remapMethodName(this.owner, this.name, this.desc)),
        AsmHelper.remapper.remapDesc(this.desc)
    )

fun <T : Any> Map<T, T>.getOrSelf(key: T): T = this.getOrDefault(key, key)

val S2APacketParticles.x
    get() = this.xCoordinate

val S2APacketParticles.y
    get() = this.yCoordinate

val S2APacketParticles.z
    get() = this.zCoordinate

val S2APacketParticles.type: EnumParticleTypes
    get() = this.particleType

val S2APacketParticles.count
    get() = this.particleCount

val S2APacketParticles.speed
    get() = this.particleSpeed

operator fun <K, V> Cache<K, V>.set(name: K, value: V) = put(name, value)

fun Any?.toStringIfTrue(bool: Boolean?): String = if (bool == true) toString() else ""

fun NBTTagList.asStringSet() = (0..tagCount()).mapTo(hashSetOf()) { getStringTagAt(it) }


fun Vec3i.toBoundingBox() = AxisAlignedBB(x.toDouble(), y.toDouble(), z.toDouble(), x + 1.0, y + 1.0, z + 1.0)