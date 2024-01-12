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

import skytils.hylin.skyblock.Pet
import skytils.hylin.skyblock.item.Tier
import java.util.*

object SkillUtils {
    val maxSkillLevels = LinkedHashMap<String, Int>()
    val skillXp = LinkedHashMap<Int, Long>()
    val runeXp = LinkedHashMap<Int, Long>()
    val hotmXp = LinkedHashMap<Int, Long>()
    val dungeoneeringXp = LinkedHashMap<Int, Long>()
    val slayerXp = LinkedHashMap<String, LinkedHashMap<Int, Long>>()
    val petRarityOffset: Map<String, Int> = mapOf(
        "COMMON" to 0,
        "UNCOMMON" to 6,
        "RARE" to 11,
        "EPIC" to 16,
        "LEGENDARY" to 20,
        "MYTHIC" to 20,
    )

    val petLevels = arrayOf(
        100,
        110,
        120,
        130,
        145,
        160,
        175,
        190,
        210,
        230,
        250,
        275,
        300,
        330,
        360,
        400,
        440,
        490,
        540,
        600,
        660,
        730,
        800,
        880,
        960,
        1050,
        1150,
        1260,
        1380,
        1510,
        1650,
        1800,
        1960,
        2130,
        2310,
        2500,
        2700,
        2920,
        3160,
        3420,
        3700,
        4000,
        4350,
        4750,
        5200,
        5700,
        6300,
        7000,
        7800,
        8700,
        9700,
        10800,
        12000,
        13300,
        14700,
        16200,
        17800,
        19500,
        21300,
        23200,
        25200,
        27400,
        29800,
        32400,
        35200,
        38200,
        41400,
        44800,
        48400,
        52200,
        56200,
        60400,
        64800,
        69400,
        74200,
        79200,
        84700,
        90700,
        97200,
        104200,
        111700,
        119700,
        128200,
        137200,
        146700,
        156700,
        167700,
        179700,
        192700,
        206700,
        221700,
        237700,
        254700,
        272700,
        291700,
        311700,
        333700,
        357700,
        383700,
        411700,
        441700,
        476700,
        516700,
        561700,
        611700,
        666700,
        726700,
        791700,
        861700,
        936700,
        1016700,
        1101700,
        1191700,
        1286700,
        1386700,
        1496700,
        1616700,
        1746700,
        1886700,
        0,
        5555,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700,
        1886700
    )

    fun calcXpWithProgress(experience: Number, values: Collection<Long>): Double {
        var xp = experience.toDouble()
        var level = 0.0

        for (toRemove in values) {
            xp -= toRemove
            if (xp < 0) {
                return level + (1 + (xp / toRemove))
            }
            level++
        }

        return level
    }

    fun calcXpWithOverflow(experience: Double, cap: Int, values: Collection<Long>): Pair<Int, Double> {
        var xp = experience
        var level = 0
        values.forEachIndexed { i, value ->
            if (xp - value < 0) return level to xp
            if (i >= cap) return level to xp
            xp -= value
            level++
        }
        return level to xp
    }

    fun calcXpWithOverflowAndProgress(
        experience: Double,
        cap: Int,
        values: Collection<Long>
    ): Triple<Int, Double, Double> {
        val overflow = calcXpWithOverflow(experience, cap, values)
        return Triple(overflow.first, overflow.second, calcXpWithProgress(experience, values))
    }

    val Pet.level: Int
        get() {
            val offset = petRarityOffset[tier.toString()]!!
            val maxLevel = if (type == "GOLDEN_DRAGON") 200 else 100
            val levels = petLevels.sliceArray(offset..<offset + maxLevel - 1)

            var xpRemaining = xp
            for ((i, xp) in levels.withIndex()) {
                if (xp > xpRemaining) return i - 1
                xpRemaining -= xp
            }
            return maxLevel
        }

    val gg.skytils.hypixel.types.skyblock.Pet.level: Int
        get () {
            val offset = petRarityOffset[tier] ?: 0
            val maxLevel = if (type == "GOLDEN_DRAGON") 200 else 100
            val levels = petLevels.sliceArray(offset..< offset + maxLevel - 1)

            levels.reduceIndexed { index, acc, i ->
                if (exp < acc) return index - 1
                println(acc + i)
                acc + i
            }
            return maxLevel
        }
}