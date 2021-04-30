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

object SkillUtils {

    val dungeoneeringXp = LinkedHashMap<Int, Long>()

    fun calcDungeonsClassLevelWithProgress(experience: Double): Double {
        var xp = experience
        var level = 0.0

        for (toRemove in dungeoneeringXp.values) {
            xp -= toRemove.toDouble()
            if (xp < 0) {
                return level + (1 - (xp * -1) / toRemove);
            }
            level++
        }

        return level.coerceAtMost(50.0)
    }
}