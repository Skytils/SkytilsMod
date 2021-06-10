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

package skytils.skytilsmod.gui

import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.markdown.MarkdownComponent
import net.minecraft.client.gui.GuiMainMenu
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.UpdateChecker
import skytils.skytilsmod.gui.components.SimpleButton

class RequestUpdateGui : WindowScreen() {

    init {
        val updateButton = SimpleButton("Update")
            .constrain {
                x = CenterConstraint()
                y = CenterConstraint().plus((window.getHeight() / 5).pixels())
                width = 100.pixels()
                height = 20.pixels()
            }.onMouseClick {
                Skytils.displayScreen = UpdateGui()
            } childOf window
        val mainMenuButton = SimpleButton("Main Menu")
            .constrain {
                x = CenterConstraint()
                y = CenterConstraint().plus((window.getHeight() / 3).pixels())
                width = 100.pixels()
                height = 20.pixels()
            }.onMouseClick {
                UpdateChecker.updateGetter.updateObj = null
                Skytils.displayScreen = GuiMainMenu()
            } childOf window
        val updateAvailableText =
            UIText("Skytils ${UpdateChecker.updateGetter.updateObj?.get("tag_name")?.asString} is available!")
                .constrain {
                    x = CenterConstraint()
                    y = CenterConstraint().minus((window.getHeight() / 3).pixels())
                        .minus((window.getHeight() / 16).pixels())
                } childOf window
        val versionText =
            UIText("You are currently on version ${Skytils.VERSION}.")
                .constrain {
                    x = CenterConstraint()
                    y = CenterConstraint().minus((window.getHeight() / 3).pixels())
                } childOf window
        val changelogWrapper = ScrollComponent()
            .constrain {
                x = CenterConstraint()
                y = CenterConstraint().minus((window.getHeight() / 15).pixels())
                height = ((window.getHeight() / 3 * 2) - (window.getHeight() / 4)).pixels()
                width = (window.getWidth() / 3 * 2).pixels()
            } childOf window
        val changelog =
            UpdateChecker.updateGetter.updateObj?.get("body")?.asString?.let { MarkdownComponent(it.replace("*", "")) }
                ?.constrain {
                    height = ((window.getHeight() / 3 * 2) - (window.getHeight() / 4)).pixels()
                    width = (window.getWidth() / 3 * 2).pixels()
                }
                ?.childOf(changelogWrapper)
    }
}