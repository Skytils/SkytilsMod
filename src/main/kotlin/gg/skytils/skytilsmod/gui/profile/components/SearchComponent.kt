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

import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import gg.essential.universal.UKeyboard
import gg.essential.vigilance.utils.onLeftClick

class SearchComponent(startingValue: String = "") : UIRoundedRectangle(5f) {
    private var string: State<String> = BasicState(startingValue)

    fun bindValue(newState: State<String>) = apply {
        string = newState
        textbox.setText(string.get())
    }

    private val icon by UIImage.ofResourceCached("/vigilance/search.png").constrain {
        x = 2.pixels
        y = CenterConstraint()
        height = 16.pixels
        width = AspectConstraint()
    } childOf this

    private val textbox by UITextInput("Username").constrain {
        x = SiblingConstraint(2f)
        y = CenterConstraint()
        width = FillConstraint() - 9.pixels
    } childOf this

    init {
        textbox.onLeftClick { event ->
            event.stopPropagation()

            grabWindowFocus()
        }.onFocus {
            (this as UITextInput).setActive(true)
        }.onFocusLost {
            (this as UITextInput).run {
                setActive(false)
                string.set(getText())
            }

        }.onKeyType { _, keyCode ->
            if (keyCode == UKeyboard.KEY_ENTER || keyCode == UKeyboard.KEY_ESCAPE) {
                releaseWindowFocus()
                textbox.setActive(false)
            }
        }
        textbox.setText(startingValue)
    }
}