package gg.skytils.skytilsmod.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.Window
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.unstable.layoutdsl.Alignment
import gg.essential.elementa.unstable.layoutdsl.Arrangement
import gg.essential.elementa.unstable.layoutdsl.Modifier
import gg.essential.elementa.unstable.layoutdsl.alignVertical
import gg.essential.elementa.unstable.layoutdsl.box
import gg.essential.elementa.unstable.layoutdsl.column
import gg.essential.elementa.unstable.layoutdsl.fillHeight
import gg.essential.elementa.unstable.layoutdsl.fillRemainingHeight
import gg.essential.elementa.unstable.layoutdsl.fillRemainingWidth
import gg.essential.elementa.unstable.layoutdsl.fillWidth
import gg.essential.elementa.unstable.layoutdsl.layoutAsBox
import gg.essential.elementa.unstable.layoutdsl.onLeftClick
import gg.essential.elementa.unstable.layoutdsl.row
import gg.essential.elementa.unstable.layoutdsl.scrollable
import gg.essential.elementa.unstable.state.v2.add
import gg.essential.elementa.unstable.state.v2.mutableListStateOf
import gg.essential.elementa.unstable.state.v2.remove
import gg.essential.elementa.unstable.state.v2.set
import gg.essential.elementa.unstable.state.v2.stateOf
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.CommandAliases
import gg.skytils.skytilsmod.features.impl.handlers.KeyShortcuts
import gg.skytils.skytilsmod.gui.layout.button
import gg.skytils.skytilsmod.gui.layout.text
import net.minecraft.util.StringHelper
import kotlin.code

class CommandAliasesScreen : WindowScreen(ElementaVersion.V10) {
    private val aliases = mutableListStateOf(*CommandAliases.aliases.entries.map { "/${it.key}" to "/${it.value}" }.toTypedArray())

    init {
        window.layoutAsBox {
            column(Modifier.fillHeight(padding = 20f).fillWidth(), horizontalAlignment = Alignment.Center) {
                text("Command Aliases", Modifier.alignVertical(Alignment.Start(20f)))
                //scroller
                scrollable(Modifier.fillRemainingHeight().fillWidth(), vertical = true) {
                    column(Modifier.fillWidth(), verticalArrangement = Arrangement.spacedBy(5f)) {
                        forEach(aliases) { alias ->
                            val index = aliases.getUntracked().indexOf(alias)
                            if (index < 0 ) return@forEach
                            row(Modifier.fillWidth(0.8f), horizontalArrangement = Arrangement.spacedBy(5f)) {
                                row(Modifier.fillRemainingWidth(), horizontalArrangement = Arrangement.spacedBy(5f)) {
                                    UITextInput("Alias")(Modifier.fillWidth(0.5f).onLeftClick { grabWindowFocus() }).also { input ->
                                        // Setting text directly is too early as the parent isn't initialized yet
                                        Window.enqueueRenderOperation {
                                            input.setText(alias.first)
                                        }
                                        input.onKeyType { _, _ ->
                                            val filtered = input.getText().filter { StringHelper.isValidChar(it.code) }.take(256)
                                            input.setText(filtered)
                                            aliases.set(index, filtered to alias.second)
                                        }
                                    }
                                    UITextInput("Executed Command")(Modifier.fillWidth(0.5f).onLeftClick { grabWindowFocus() }).also { input ->
                                        // Setting text directly is too early as the parent isn't initialized yet
                                        Window.enqueueRenderOperation {
                                            input.setText(alias.second)
                                        }
                                        input.onKeyType { _, _ ->
                                            val filtered = input.getText().filter { StringHelper.isValidChar(it.code) }.take(256)
                                            input.setText(filtered)
                                            aliases.set(index, alias.first to filtered)
                                        }
                                    }
                                }
                                button("Remove") { aliases.remove(alias) }
                            }
                        }
                    }
                }
                row(horizontalArrangement = Arrangement.spacedBy(5f)) {
                    button(stateOf("Save and Exit")) {
                        close()
                    }
                    button(stateOf("Add Shortcut")) {
                        aliases.add("" to "")
                    }
                }
            }
        }
    }

    override fun onScreenClose() {
        super.onScreenClose()

        val newAliases = aliases.getUntracked().mapNotNull { (alias, command) ->
            (alias.trim('/') to command.trim('/')).takeIf { alias.isNotEmpty() && command.isNotEmpty() }
        }.toMap()

        val dispatcher = CommandAliases.currentDispatcher
        if (dispatcher != null) {
            val removed = CommandAliases.aliases.keys - newAliases.keys
            val added = newAliases.keys - CommandAliases.aliases.keys
            CommandAliases.removeAliasesFromDispatcher(removed, dispatcher)
            CommandAliases.injectAliasesIntoDispatcher(added, dispatcher)
        }

        CommandAliases.aliases.run {
            clear()
            putAll(newAliases)
        }

        PersistentSave.markDirty<CommandAliases>()
    }
}