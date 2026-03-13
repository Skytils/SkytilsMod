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

import gg.essential.lib.caffeine.cache.Cache
import gg.essential.universal.ChatColor
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.gui.settings.CheckboxComponent
import gg.skytils.hypixel.types.skyblock.Pet
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtList
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text
import net.minecraft.util.*
import net.minecraft.util.math.*
import org.objectweb.asm.tree.MethodInsnNode
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.notExists
import kotlin.math.floor


object Utils {

    val inSkyblock: Boolean
        get() = SBInfo.skyblockState.getUntracked()

    val inDungeons: Boolean
        get() = SBInfo.dungeonsState.getUntracked()

    val isOnHypixel: Boolean
        get() = SBInfo.hypixelState.getUntracked()

    @JvmField
    var shouldBypassVolume = false

    fun getBlocksWithinRangeAtSameY(center: BlockPos, radius: Int, y: Int): Iterable<BlockPos> {
        val corner1 = BlockPos(center.x - radius, y, center.z - radius)
        val corner2 = BlockPos(center.x + radius, y, center.z + radius)
        return BlockPos.iterate(corner1, corner2)
    }

    /**
     * Taken from SkyblockAddons under MIT License
     * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
     * @author BiscuitDevelopment
     */
    fun playLoudSound(sound: String?, pitch: Double) {
        shouldBypassVolume = true
        mc.player!!.playSound(SoundEvent.of(Identifier.of(sound)), 1f, pitch.toFloat())
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

    fun checkThreadAndQueue(run: () -> Unit) {
        if (!mc.isOnThread) {
            mc.submit(run)
        } else run()
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
        val java = Path(System.getProperty("java.home"))
            .resolve("bin")
            .resolve(if (os != null && os.lowercase().startsWith("windows")) "java.exe" else "java")

        if (java.notExists()) {
            throw IOException("Unable to find suitable java runtime at $java")
        }

        return java.toAbsolutePath().toString()
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
}

inline val Box.minVec: Vec3d
    get() = Vec3d(minX, minY, minZ)
inline val Box.maxVec: Vec3d
    get() = Vec3d(maxX, maxY, maxZ)

fun Box.isPosInside(pos: BlockPos): Boolean {
    return pos.x > this.minX && pos.x < this.maxX && pos.y > this.minY && pos.y < this.maxY && pos.z > this.minZ && pos.z < this.maxZ
}

fun Vigilant.openGUI(): Job = Skytils.launch {
    Skytils.displayScreen = this@openGUI.gui()
}

fun Text.map(action: Text.() -> Unit) {
    action(this)
    siblings.forEach { it.map(action) }
}

fun Entity.getXZDistSq(other: Entity): Double {
    val xDelta = this.x - other.x
    val zDelta = this.z - other.z
    return xDelta * xDelta + zDelta * zDelta
}

fun Entity.getXZDistSq(pos: BlockPos): Double {
    val xDelta = this.x - pos.x
    val zDelta = this.z - pos.z
    return xDelta * xDelta + zDelta * zDelta
}

val Entity.hasMoved
    get() = this.x != this.prevX || this.y != this.prevY || this.z != this.prevZ

fun CheckboxComponent.toggle() {
    this.mouseClick(this.getLeft().toDouble(), this.getTop().toDouble(), 0)
}

fun CheckboxComponent.setState(checked: Boolean) {
    if (this.checked != checked) this.toggle()
}

fun BlockPos?.toVec3() = if (this == null) null else Vec3d(this)

fun BlockPos.middleVec() = Vec3d(x + 0.5, y + 0.5, z + 0.5)

fun <T : Any> T?.ifNull(run: () -> Unit): T? {
    if (this == null) run()
    return this
}

fun <T : Any> Map<T, T>.getOrSelf(key: T): T = this.getOrDefault(key, key)

operator fun <K : Any, V : Any> Cache<K, V>.set(name: K, value: V) = put(name, value)

fun Any?.toStringIfTrue(bool: Boolean?): String = if (bool == true) toString() else ""

fun NbtList.asStringSet() = (0..size).mapTo(hashSetOf()) { getString(it) }


fun Vec3i.toBoundingBox() = Box(x.toDouble(), y.toDouble(), z.toDouble(), x + 1.0, y + 1.0, z + 1.0)

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

fun <E> List<E>.getLastOrNull(index: Int) = getOrNull(lastIndex - index)

fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null

operator fun Vec3d.plus(other: Vec3d): Vec3d = add(other)
operator fun Vec3d.minus(other: Vec3d): Vec3d = subtract(other)

operator fun Vec3d.times(scaleValue: Double): Vec3d = Vec3d(x * scaleValue, y * scaleValue, z * scaleValue)

fun Vec3d.squareDistanceTo(x: Double, y: Double, z: Double) =
     (x - x) * (x - x) + (y - y) * (y - y) + (z - z) * (z - z)

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