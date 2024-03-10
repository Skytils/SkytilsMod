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

package gg.skytils.skytilsmod.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UMinecraft
import gg.essential.universal.wrappers.UPlayer
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.gui.components.HelpComponent
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.util.ResourceLocation
import java.awt.Color

class SuperSecretGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent
    val catSound: PositionedSoundRecord = PositionedSoundRecord.create(
        ResourceLocation("records.cat"),
        UPlayer.getPosX().toFloat(),
        UPlayer.getPosY().toFloat(),
        UPlayer.getPosZ().toFloat()
    )

    init {
        SuperSecretSettings.add("chamberofsecrets")
        SuperSecretSettings.dirty = true
        UMinecraft.getMinecraft().soundHandler.playSound(catSound)
        UIText("Shhhhhhh.. It's a secret...").childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            height = 14.pixels()
        }

        scrollComponent = ScrollComponent(
            innerPadding = 4f,
        ).childOf(window).constrain {
            x = CenterConstraint()
            y = 15.percent()
            width = 90.percent()
            height = 70.percent() + 2.pixels()
        }

        val bottomButtons = UIContainer().childOf(window).constrain {
            x = CenterConstraint()
            y = 90.percent()
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        SimpleButton("Save and Exit").childOf(bottomButtons).constrain {
            x = 0.pixels()
            y = 0.pixels()
        }.onLeftClick {
            mc.displayGuiScreen(null)
        }

        SimpleButton("Add Secret").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            addNewSetting()
        }

        HelpComponent(window, "How did you get here?")

        for (setting in SuperSecretSettings.settings) {
            addNewSetting(setting)
        }
    }

    private fun addNewSetting(setting: String = "") {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = 9.5.percent()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val setting = UITextInput("Secret").childOf(container).constrain {
            x = 5.pixels()
            y = CenterConstraint()
            width = 70.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(setting)
        }

        SimpleButton("Remove").childOf(container).constrain {
            x = 85.percent()
            y = CenterConstraint()
            height = 75.percent()
        }.onLeftClick {
            scrollComponent.removeChild(container)
        }
    }

    override fun onScreenClose() {
        super.onScreenClose()
        SuperSecretSettings.clear()

        for (container in scrollComponent.allChildren) {
            val text = container.childrenOfType<UITextInput>()
            val setting = (text.find { it.placeholder == "Secret" }
                ?: throw IllegalStateException("${container.componentName} does not have the secret UITextInput! Available children ${container.children.map { it.componentName }}")).getText()
            if (setting.isEmpty()) continue
            SuperSecretSettings.add(setting)
        }

        SuperSecretSettings.save()
        mc.soundHandler.stopSound(catSound)
    }
}