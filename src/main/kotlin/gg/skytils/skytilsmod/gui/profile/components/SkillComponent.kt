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

package gg.skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.state.State
import gg.skytils.skytilsmod.utils.SkillUtils
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.Skills
import kotlin.reflect.KProperty

class SkillComponent(
    var image: ItemComponent,
    var color: ColorConstraint,
    val skillField: KProperty<Float?>,
    userState: State<Member?>
) : XPComponent(
    image,
    colorConstraint = color
) {
    val skillCap = userState.map { user ->
        kotlin.runCatching {
            if (skillField == Skills::farmingXP) {
                return@runCatching SkillUtils.maxSkillLevels["farming"]!! + (user?.jacob?.perks?.farmingLevelCap ?: 0)
            } else {
                return@runCatching SkillUtils.maxSkillLevels[skillField.name.substringBefore("XP")]
            }
        }.getOrNull() ?: 50
    }
    val skillXP = userState.map { user ->
        user?.skills?.let { skills -> skillField.getter.call(skills) } ?: 0f
    }
    val skillLevel = (skillXP.zip(skillCap)).map { (xp, cap) ->
        if (skillField == Skills::runecraftingXP) {
            SkillUtils.calcXpWithOverflowAndProgress(xp.toDouble(), cap, SkillUtils.runeXp.values)
        } else {
            SkillUtils.calcXpWithOverflowAndProgress(xp.toDouble(), cap, SkillUtils.skillXp.values)
        }
    }
    val progress = skillLevel.map { (level, _, percent) ->
        if (level == skillCap.get()) return@map 1f
        (percent % 1).toFloat()
    }

    init {
        super.bindText(skillLevel.map { level ->
            "${
                skillField.name.lowercase().substringBefore("xp").replaceFirstChar { it.uppercase() }
            } ${level.first}"
        })
        super.bindPercent(progress)
        super.bindOverflow(skillLevel.map { level -> level.second.toLong() })
    }
}