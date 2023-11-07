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

package gg.skytils.skytilsmod.gui.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.USound
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.gui.settings.SettingComponent
import gg.essential.vigilance.utils.onLeftClick

/**
 * Based on Vigilance under LGPL 3.0 license
 * Modified to add an indeterminate state
 * https://github.com/Sk1erLLC/Vigilance/blob/master/LICENSE
 * @author Sk1er LLC
 */
class MultiCheckboxComponent(initialValue: Boolean?) : SettingComponent() {
    var checked: Boolean? = initialValue
        set(value) {
            changeValue(value)
            field = value
            updateImage()
        }

    private val checkmark = UIImage.ofResourceCached("/vigilance/check.png").constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = 16.pixels()
        height = 12.pixels()
        color = getSettingColor().toConstraint()
    } childOf this

    private val indeterminate = UIBlock().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        width = 16.pixels()
        height = 2.pixels()
        color = getSettingColor().toConstraint()
    }.childOf(this).apply { hide(instantly = true) }

    init {
        constrain {
            width = 20.pixels()
            height = AspectConstraint()
        }

        effect(getOutlineEffect())

        if (checked == null) {
            indeterminate.unhide()
        }

        if (checked == false) {
            checkmark.hide(instantly = true)
        }

        onLeftClick {
            USound.playButtonPress()
            checked = checked == false
        }
    }

    private fun updateImage() {
        removeEffect<OutlineEffect>()
        effect(getOutlineEffect())

        when (checked) {
            null -> { // Indeterminate
                checkmark.hide()
                indeterminate.unhide()
            }
            true -> { // Enabled
                indeterminate.hide()
                checkmark.unhide()
            }
            else -> { // Disabled
                checkmark.hide()
                indeterminate.hide()
            }
        }
    }

    private fun getOutlineEffect() = OutlineEffect(getSettingColor(), 1f)

    private fun getSettingColor() =
        if (checked != false) VigilancePalette.getAccent() else VigilancePalette.getBrightDivider()


    fun setState(newState: Boolean?) {
        if (newState != checked) {
            checked = newState
        }
    }

    fun toggle() {
        setState(checked == false)
    }
}
