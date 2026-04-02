package gg.skytils.skytilsmod.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.RainbowColorConstraint
import gg.essential.elementa.constraints.RelativeWindowConstraint
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.provideDelegate
import gg.essential.elementa.unstable.layoutdsl.Arrangement
import gg.essential.elementa.unstable.layoutdsl.Modifier
import gg.essential.elementa.unstable.layoutdsl.box
import gg.essential.elementa.unstable.layoutdsl.childBasedHeight
import gg.essential.elementa.unstable.layoutdsl.childBasedSize
import gg.essential.elementa.unstable.layoutdsl.childBasedWidth
import gg.essential.elementa.unstable.layoutdsl.column
import gg.essential.elementa.unstable.layoutdsl.fillHeight
import gg.essential.elementa.unstable.layoutdsl.fillParent
import gg.essential.elementa.unstable.layoutdsl.fillRemainingHeight
import gg.essential.elementa.unstable.layoutdsl.fillWidth
import gg.essential.elementa.unstable.layoutdsl.layoutAsBox
import gg.essential.elementa.unstable.layoutdsl.layoutAsColumn
import gg.essential.elementa.unstable.layoutdsl.width
import gg.essential.universal.UDesktop
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.gui.layout.button
import gg.skytils.skytilsmod.gui.layout.text
import java.net.URI

class SkytilsScreen : WindowScreen(ElementaVersion.V10) {
    init {
        window.layoutAsBox {
            column(Modifier.fillParent()) {
                box(Modifier.fillHeight(0.075f))
                text("Skytils", shadow = false).animate {
                    color = RainbowColorConstraint()
                    textScale = RelativeWindowConstraint(0.025f)
                }
                column(Modifier.fillRemainingHeight().width(200f), verticalArrangement = Arrangement.spacedBy(5f)) {
                    button("Config", Modifier.fillWidth()) {
                        displayScreen(Config.gui())
                    }
                    button("Edit Key Shortcuts", Modifier.fillWidth()) {
                        displayScreen(KeyShortcutsScreen())
                    }
                    button("Command Aliases", Modifier.fillWidth()) {
                        displayScreen(CommandAliasesScreen())
                    }
                }
            }
        }
    }

    private val corner by UIContainer()
        .constrain {
            x = 3.pixels(alignOpposite = true)
            y = 3.pixels(alignOpposite = true)
        }.childOf(window)
        .layoutAsColumn(Modifier.childBasedHeight().fillWidth(0.1f)) {
            button("Discord", Modifier.fillWidth()) {
                UDesktop.browse(URI("https://discord.gg/skytils"))
            }
            button("GitHub", Modifier.fillWidth()) {
                UDesktop.browse(URI("https://github.com/Skytils/SkytilsMod"))
            }
        }
}