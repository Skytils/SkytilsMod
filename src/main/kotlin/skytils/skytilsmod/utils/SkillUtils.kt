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

object SkillUtils {
    val maxSkillLevels = LinkedHashMap<String, Int>()
    val skillXp = LinkedHashMap<Int, Long>()
    val runeXp = LinkedHashMap<Int, Long>()
    val dungeoneeringXp = LinkedHashMap<Int, Long>()
    val slayerXp = LinkedHashMap<String, LinkedHashMap<Int, Long>>()

    fun calcXpWithProgress(experience: Double, values: Collection<Long>): Double {
        var xp = experience
        var level = 0.0

        for (toRemove in values) {
            xp -= toRemove
            if (xp < 0) {
                return level + (1 - (xp * -1) / toRemove)
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

    fun calcXpWithOverflowAndProgress(experience: Double, cap: Int, values: Collection<Long>): Triple<Int, Double, Double> {
        val overflow = calcXpWithOverflow(experience, cap, values)
        return Triple(overflow.first, overflow.second, calcXpWithProgress(experience, values))
    }

    fun findNextLevel(experience: Double, values: Map<Int, Long>?): Int {
        if (values == null) return -1
        values.onEach { entry ->
            if (experience < entry.value) {
                return@findNextLevel entry.key - 1
            }
        }
        return values.size
    }
}