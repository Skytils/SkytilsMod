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

package gg.skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.state.State
import gg.skytils.hypixel.types.skyblock.Member
import gg.skytils.skytilsmod.utils.SkillUtils
import gg.skytils.skytilsmod.utils.toTitleCase
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class SkillComponent(
    var image: ItemComponent,
    var color: ColorConstraint,
    val skillField: String,
    userState: State<Member?>
) : XPComponent(
    image,
    colorConstraint = color
) {
    val skillCap = userState.map { user ->
        kotlin.runCatching {
            if (skillField == "SKILL_FARMING") {
                return@runCatching SkillUtils.maxSkillLevels["farming"]!! + (user?.jacob?.perks?.get("farming_level_cap")?.jsonPrimitive?.intOrNull ?: 0)
            } else {
                return@runCatching SkillUtils.maxSkillLevels[skillField.substringAfter("SKILL_").lowercase()]
            }
        }.getOrNull() ?: 50
    }
    val skillXP = userState.map { user ->
        user?.player_data?.experience?.get(skillField) ?: 0.0
    }
    val skillLevel = (skillXP.zip(skillCap)).map { (xp, cap) ->
        if (skillField == "SKILL_RUNECRAFTING") {
            SkillUtils.calcXpWithOverflowAndProgress(xp, cap, SkillUtils.runeXp.values)
        } else {
            SkillUtils.calcXpWithOverflowAndProgress(xp, cap, SkillUtils.skillXp.values)
        }
    }
    val progress = skillLevel.map { (level, _, percent) ->
        if (level == skillCap.get()) return@map 1f
        (percent % 1).toFloat()
    }

    init {
        super.bindText(skillLevel.map { level ->
            "${
                skillField.substringAfter("SKILL_").toTitleCase()
            } ${level.first}"
        })
        super.bindPercent(progress)
        super.bindOverflow(skillLevel.map { level -> level.second.toLong() })
    }
}