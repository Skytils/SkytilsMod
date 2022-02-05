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

package skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.constraints.ColorConstraint
import gg.essential.elementa.state.State
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.dungeons.DungeonClass
import skytils.skytilsmod.utils.SkillUtils

class DungeonClassComponent(
    var image: ItemComponent,
    var color: ColorConstraint,
    val dungeonClass: DungeonClass,
    userState: State<Member?>
) : XPComponent(
    image,
    colorConstraint = color
) {
    val skillXP = userState.map { user ->
        user?.dungeons?.classExperiences?.get(dungeonClass)
    }
    val skillLevel = skillXP.map { xp ->
        SkillUtils.calcXpWithOverflowAndProgress(
            xp ?: 0.0,
            SkillUtils.dungeoneeringXp.size,
            SkillUtils.dungeoneeringXp.values
        )
    }
    val progress = skillLevel.map { (level, _, percent) ->
        if (level == SkillUtils.dungeoneeringXp.size) return@map 1f
        (percent % 1).toFloat()
    }

    init {
        super.bindText(skillLevel.map { level ->
            "${
                dungeonClass.className
            } ${level.first}"
        })
        super.bindPercent(progress)
        super.bindOverflow(skillLevel.map { level -> level.second.toLong() })
    }
}