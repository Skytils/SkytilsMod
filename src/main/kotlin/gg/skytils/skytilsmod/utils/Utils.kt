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
package gg.skytils.skytilsmod.utils

import dev.falsehonesty.asmhelper.AsmHelper
import dev.falsehonesty.asmhelper.dsl.instructions.Descriptor
import gg.essential.lib.caffeine.cache.Cache
import gg.essential.universal.ChatColor
import gg.essential.universal.UResolution
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.gui.settings.CheckboxComponent
import gg.skytils.hypixel.types.skyblock.Pet
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.asm.SkytilsTransformer
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent.ReceiveEvent
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiNewChat
import gg.skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import gg.skytils.skytilsmod.utils.graphics.colors.ColorFactory.web
import gg.skytils.skytilsmod.utils.graphics.colors.CustomColor
import gg.skytils.skytilsmod.utils.graphics.colors.CyclingTwoColorGradient
import gg.skytils.skytilsmod.utils.graphics.colors.RainbowColor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
import java.awt.Color
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.math.floor


object Utils {

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

    val isBSMod by lazy {
        val cal = Calendar.getInstance()
        return@lazy cal.get(Calendar.MONTH) == Calendar.APRIL && cal.get(Calendar.DAY_OF_MONTH) == 1
    }

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
        return if (string.startsWith("rainbow(")) {
            RainbowColor.fromString(string)
        } else if (string.startsWith("cyclingtwocolorgradient(")) {
            CyclingTwoColorGradient.fromString(string)
        } else try {
            getCustomColorFromColor(web(string))
        } catch (e: IllegalArgumentException) {
            try {
                CustomColor.fromInt(string.toInt())
            } catch (ignored: NumberFormatException) {
                throw e
            }
        }
    }

    fun colorFromString(string: String): Color {
        return try {
            web(string)
        } catch (e: IllegalArgumentException) {
            try {
                Color(string.toInt(), true)
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
            "F7", "M7" -> "Maxor"
            else -> null
        } ?: return false

        // Livid has a prefix in front of the name, so we check ends with to cover all the livids
        return bossName.endsWith(correctBoss)
    }

    fun getKeyDisplayStringSafe(keyCode: Int): String =
        runCatching { GameSettings.getKeyDisplayString(keyCode) }.getOrNull() ?: "Key $keyCode"
}

inline val AxisAlignedBB.minVec: Vec3
    get() = Vec3(minX, minY, minZ)
inline val AxisAlignedBB.maxVec: Vec3
    get() = Vec3(maxX, maxY, maxZ)

fun AxisAlignedBB.isPosInside(pos: BlockPos): Boolean {
    return pos.x > this.minX && pos.x < this.maxX && pos.y > this.minY && pos.y < this.maxY && pos.z > this.minZ && pos.z < this.maxZ
}

fun Vigilant.openGUI(): Job = Skytils.launch {
    Skytils.displayScreen = this@openGUI.gui()
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

fun IChatComponent.map(action: IChatComponent.() -> Unit) {
    action(this)
    siblings.forEach { it.map(action) }
}

fun Entity.getXZDistSq(other: Entity): Double {
    val xDelta = this.posX - other.posX
    val zDelta = this.posZ - other.posZ
    return xDelta * xDelta + zDelta * zDelta
}

fun Entity.getXZDistSq(pos: BlockPos): Double {
    val xDelta = this.posX - pos.x
    val zDelta = this.posZ - pos.z
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

fun BlockPos.middleVec() = Vec3(x + 0.5, y + 0.5, z + 0.5)

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

inline val S2APacketParticles.x
    get() = this.xCoordinate

inline val S2APacketParticles.y
    get() = this.yCoordinate

inline val S2APacketParticles.z
    get() = this.zCoordinate

inline val S2APacketParticles.type: EnumParticleTypes
    get() = this.particleType

inline val S2APacketParticles.count
    get() = this.particleCount

inline val S2APacketParticles.speed
    get() = this.particleSpeed

inline val S2APacketParticles.vec3
    get() = Vec3(x, y, z)

operator fun <K : Any, V : Any> Cache<K, V>.set(name: K, value: V) = put(name, value)

fun Any?.toStringIfTrue(bool: Boolean?): String = if (bool == true) toString() else ""

fun NBTTagList.asStringSet() = (0..tagCount()).mapTo(hashSetOf()) { getStringTagAt(it) }


fun Vec3i.toBoundingBox() = AxisAlignedBB(x.toDouble(), y.toDouble(), z.toDouble(), x + 1.0, y + 1.0, z + 1.0)

fun File.ensureFile() = (parentFile.exists() || parentFile.mkdirs()) && createNewFile()

fun MethodInsnNode.matches(owner: String?, name: String?, desc: String?): Boolean {
    return (owner == null || this.owner == owner) && (name == null || this.name == name) && (desc == null || this.desc == desc)
}

val gg.skytils.hypixel.types.player.Player.rank_prefix
    get() = when(rank) {
        "VIP" -> "§a[VIP]"
        "VIP_PLUS" -> "§a[VIP§6+§a]"
        "MVP" -> "§b[MVP]"
        "MVP_PLUS" -> "§b[MVP${ChatColor.valueOf(plus_color)}+§b]"
        "MVP_PLUS_PLUS" -> "${ChatColor.valueOf(mvp_plus_plus_color)}[MVP${ChatColor.valueOf(plus_color)}++${ChatColor.valueOf(mvp_plus_plus_color)}]"
        "HELPER" -> "§9[HELPER]"
        "MODERATOR" -> "§2[MOD]"
        "GAME_MASTER" -> "§2[GM]"
        "ADMIN" -> "§c[ADMIN]"
        "YOUTUBER" -> "§c[§fYOUTUBE§c]"
        else -> "§7"
    }

val gg.skytils.hypixel.types.player.Player.formattedName
    get() = "${rank_prefix}${" ".toStringIfTrue(rank != "NONE")}$display_name"

val Pet.isSpirit
    get() = type == "SPIRIT" && (tier == "LEGENDARY" || (heldItem == "PET_ITEM_TIER_BOOST" && tier == "EPIC"))

val <E> MutableMap<E, Boolean>.asSet: MutableSet<E>
    get() = Collections.newSetFromMap(this)

fun getSkytilsResource(path: String) = ResourceLocation("skytils", path)

fun <E> List<E>.getLastOrNull(index: Int) = getOrNull(lastIndex - index)

fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null

inline val Vec3.x
    get() = this.xCoord

inline val Vec3.y
    get() = this.yCoord

inline val Vec3.z
    get() = this.zCoord

operator fun Vec3.plus(other: Vec3): Vec3 = add(other)
operator fun Vec3.minus(other: Vec3): Vec3 = subtract(other)

operator fun Vec3.times(scaleValue: Double): Vec3 = Vec3(xCoord * scaleValue, yCoord * scaleValue, zCoord * scaleValue)

fun Vec3.squareDistanceTo(x: Double, y: Double, z: Double)=
     (x - xCoord) * (x - xCoord) + (y - yCoord) * (y - yCoord) + (z - zCoord) * (z - zCoord)

/**
 * @author Ilya
 * @link https://stackoverflow.com/a/56043547
 * Modified https://creativecommons.org/licenses/by-sa/4.0/
 */
fun <T> List<T>.elementPairs() = sequence {
    val arr = this@elementPairs
    for (i in 0..<arr.size - 1)
        for (j in i + 1..<arr.size)
            yield(arr[i] to arr[j])
}
