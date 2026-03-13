package gg.skytils.skytilsmod.commands.impl

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.gui.OptionsGui
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands

@Commands
object SkytilsCommand {

    @Command("skytils|st")
    fun main() {
        Skytils.displayScreen = OptionsGui()
    }

    @Command("skytils|st config")
    fun config() {
        Skytils.displayScreen = Config.gui()
    }

    @Command("skytils|st keyshortcuts|shortcuts")
    fun openShortcuts() {
        Skytils.displayScreen = CatlasConfig.gui()
    }
}
