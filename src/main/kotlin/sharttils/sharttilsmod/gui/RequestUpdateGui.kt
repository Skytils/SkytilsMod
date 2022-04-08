/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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

package sharttils.sharttilsmod.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.markdown.MarkdownComponent
import net.minecraft.client.gui.GuiMainMenu
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.core.UpdateChecker
import sharttils.sharttilsmod.gui.components.SimpleButton

class RequestUpdateGui : WindowScreen(ElementaVersion.V1, newGuiScale = 2) {

    init {
        val updateObj = UpdateChecker.updateGetter.updateObj
        UIText("Sharttils ${updateObj?.get("tag_name")?.asString} is available!")
            .constrain {
                x = CenterConstraint()
                y = RelativeConstraint(0.1f)
            } childOf window
        UIText("You are currently on version ${Sharttils.VERSION}.")
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
            } childOf window
        val authorText =
            UIText("Uploaded by: ${UpdateChecker.updateAsset?.getAsJsonObject("uploader")?.get("login")?.asString}")
                .constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint()
                } childOf window
        val changelogWrapper = ScrollComponent()
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint(10f)
                height = basicHeightConstraint { window.getHeight() - 90 - authorText.getBottom() }
                width = RelativeConstraint(0.7f)
            } childOf window
        UpdateChecker.updateGetter.updateObj?.get("body")?.asString?.let { MarkdownComponent(it.replace("*", "")) }
            ?.constrain {
                height = RelativeConstraint()
                width = RelativeConstraint()
            }
            ?.childOf(changelogWrapper)
        SimpleButton("Update")
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint(5f)
                width = 100.pixels()
                height = 20.pixels()
            }.onMouseClick {
                Sharttils.displayScreen = UpdateGui(true)
            } childOf window
        SimpleButton("Update Later")
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint(5f)
                width = 100.pixels()
                height = 20.pixels()
            }.onMouseClick {
                Sharttils.displayScreen = UpdateGui(false)
            } childOf window
        SimpleButton("Main Menu")
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint(5f)
                width = 100.pixels()
                height = 20.pixels()
            }.onMouseClick {
                UpdateChecker.updateGetter.updateObj = null
                Sharttils.displayScreen = GuiMainMenu()
            } childOf window
    }
}