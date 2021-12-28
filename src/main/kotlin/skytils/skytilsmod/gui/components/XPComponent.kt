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

package skytils.skytilsmod.gui.components

import com.google.gson.JsonObject
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import gg.essential.elementa.utils.withAlpha
import gg.essential.vigilance.gui.VigilancePalette
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.Skills
import skytils.skytilsmod.utils.NumberUtil
import skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import skytils.skytilsmod.utils.SkillUtils
import java.awt.Color
import kotlin.reflect.KProperty

class XPComponent(
    var image: ItemComponent,
    var colorConstraint: ColorConstraint,
    val skillField: KProperty<Float?>,
    val userData: Member
) : UIComponent() {
    val skillCap: Int = kotlin.runCatching {
        if (skillField == Skills::farmingXP) {
            return@runCatching SkillUtils.maxSkillLevels["farming"]!! + (userData.jacob.perks.farmingLevelCap ?: 0)
        } else {
            return@runCatching SkillUtils.maxSkillLevels[skillField.name.substringBefore("XP")]
        }
    }.getOrDefault(50)!!
    private val skillXP: Float = skillField.getter.call(userData.skills) ?: 0f
    private val skillLevel = if (skillField == Skills::runecraftingXP) {
        SkillUtils.calcXpWithOverflowAndProgress(skillXP.toDouble(), skillCap, SkillUtils.runeXp.values)
    } else {
        SkillUtils.calcXpWithOverflowAndProgress(skillXP.toDouble(), skillCap, SkillUtils.skillXp.values)
    }
    private val percent = (skillLevel.third % 1).toFloat()

    private val background = UIRoundedRectangle(5f)
        .constrain {
            x = basicXConstraint { this@XPComponent.getHeight() / 2 + this@XPComponent.getLeft() }
            y = RelativeConstraint(0.5f)
            width = RelativeConstraint()
            height = RelativeConstraint(0.5f)
            color = Color.WHITE.withAlpha(40).toConstraint()
        } childOf this

    private val progress = UIRoundedRectangle(5f)
        .constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = if (skillLevel.first == skillCap) {
                RelativeConstraint()
            } else {
                basicWidthConstraint {
                    val hidden = imageContainer.getWidth() / 2
                    (background.getWidth() - hidden) * percent + hidden
                }
            }
            height = RelativeConstraint()
            color = colorConstraint
        } childOf background

    private val overflow = UIText(text = NumberUtil.format(skillLevel.second.toLong()))
        .constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf background


    private val shadow by GradientComponent(
        Color(0, 0, 0, 80),
        Color(0, 0, 0, 0),
        GradientComponent.GradientDirection.LEFT_TO_RIGHT
    )
        .constrain {
            x = RelativeConstraint(0.025f)
            y = 0.pixels()
            width = RelativeConstraint(0.1f)
            height = RelativeConstraint()
        } childOf background

    private val imageContainer = UIRoundedRectangle(5f)
        .constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = AspectConstraint()
            height = RelativeConstraint()
            color = colorConstraint
        } childOf this

    private val skillLevelContainer = UIContainer()
        .constrain {
            x = basicXConstraint { imageContainer.getRight() }
            y = 0.pixels()
            width = FillConstraint()
            height = RelativeConstraint(0.5f)
        } childOf this

    private val skillText =
        UIText(
            "${
                skillField.name.lowercase().substringBefore("xp").replaceFirstChar { it.uppercase() }
            } ${skillLevel.first}"
        )
            .constrain {
                x = 2.pixels()
                y = CenterConstraint()
            } childOf skillLevelContainer

    init {
        image
            .constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                height = RelativeConstraint(0.9f)
                width = RelativeConstraint(0.9f)
            } childOf imageContainer
    }
}

