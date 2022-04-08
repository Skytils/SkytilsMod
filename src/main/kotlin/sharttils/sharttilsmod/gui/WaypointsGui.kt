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
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UKeyboard
import gg.essential.vigilance.gui.settings.CheckboxComponent
import gg.essential.vigilance.gui.settings.ColorComponent
import gg.essential.vigilance.gui.settings.DropDown
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.util.BlockPos
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.Sharttils.Companion.mc
import sharttils.sharttilsmod.core.PersistentSave
import sharttils.sharttilsmod.core.TickTask
import sharttils.sharttilsmod.features.impl.handlers.Waypoint
import sharttils.sharttilsmod.features.impl.handlers.Waypoints
import sharttils.sharttilsmod.gui.components.SimpleButton
import sharttils.sharttilsmod.utils.SBInfo
import sharttils.sharttilsmod.utils.SkyblockIsland
import sharttils.sharttilsmod.utils.toggle
import java.awt.Color

class WaypointsGui : WindowScreen(ElementaVersion.V1, newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent

    private val islandDropdown: DropDown
    private val sortingOrder: DropDown
    private val searchBar: UITextInput

    private val entries = HashMap<UIContainer, Entry>()

    init {
        scrollComponent = ScrollComponent(
            innerPadding = 4f,
        ).childOf(window).constrain {
            x = CenterConstraint()
            y = 15.percent()
            width = 90.percent()
            height = 70.percent() + 2.pixels()
        }

        val topButtons = UIContainer().childOf(window).constrain {
            x = 0.pixels()
            y = 5.percent()
            width = 100.percent()
            height = 50.pixels()
        }

        islandDropdown = DropDown(SkyblockIsland.values().indexOfFirst {
            SBInfo.mode == it.mode
        }.run { if (this == -1) 0 else this }, SkyblockIsland.values().map { it.formattedName }).childOf(topButtons)
            .constrain {
                x = 5.pixels(true)
            }.also {
                it.onValueChange { s ->
                    loadWaypointsForSelection(s)
                }
            }

        sortingOrder =
            DropDown(SortingOptions.lastSelected, SortingOptions.values().map { it.displayName }).childOf(topButtons)
                .constrain {
                    x = SiblingConstraint(10f, true)
                }.apply {
                    onValueChange {
                        SortingOptions.lastSelected = it
                        val sorter = SortingOptions.values()[it]
                        scrollComponent.sortChildren { c ->
                            c as UIContainer
                            val entry = entries[c] ?: error("no entry found for child")
                            return@sortChildren sorter.sortingBy(entry.toWaypoint(SkyblockIsland.PrivateIsland))
                        }
                    }
                }

        searchBar = UITextInput("Search").childOf(topButtons).constrain {
            x = 2.pixels()
            y = 2.pixels()
            width = 20.percent()
            height = 12.pixels()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            onKeyType { _, _ ->
                scrollComponent.allChildren.forEach { c ->
                    c as UIContainer
                    val entry = entries[c] ?: error("no entry found for child")
                    if (entry.name.getText().contains(this@apply.getText())) c.unhide()
                    else c.hide()
                }
            }
        }

        SimpleButton("Toggle Visible").childOf(topButtons).constrain {
            x = 2.pixels()
            y = 20.pixels()
            width = 100.pixels()
            height = 20.pixels()
        }.apply {
            onLeftClick {
                val valid = scrollComponent.allChildren.mapNotNull { c ->
                    c as UIContainer
                    val entry = entries[c] ?: error("no entry found for child")
                    if (entry.name.getText().contains(searchBar.getText())) return@mapNotNull entry
                    else return@mapNotNull null
                }
                if (valid.any { it.enabled.checked }) {
                    valid.forEach {
                        if (it.enabled.checked) it.enabled.toggle()
                    }
                } else {
                    valid.forEach {
                        if (!it.enabled.checked) it.enabled.toggle()
                    }
                }
            }
        }

        SimpleButton("Remove Visible").childOf(topButtons).constrain {
            x = 107.pixels()
            y = 20.pixels()
            width = 100.pixels()
            height = 20.pixels()
        }.apply {
            onLeftClick {
                scrollComponent.allChildren.forEach { c ->
                    c as UIContainer
                    val entry = entries[c] ?: error("no entry found for child")
                    if (entry.name.getText().contains(searchBar.getText())) {
                        scrollComponent.removeChild(c)
                        entries.remove(c)
                    }
                }
            }
        }

        UIText("Waypoints").childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            height = 14.pixels()
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

        SimpleButton("New Waypoints").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            addNewWaypoint()
        }

        SimpleButton("Share").childOf(window).constrain {
            x = 5.pixels()
            y = 5.pixels(alignOpposite = true)
        }.onLeftClick {
            mc.displayGuiScreen(null)
            TickTask(2) {
                Sharttils.displayScreen = WaypointShareGui()
            }
        }

        loadWaypointsForSelection(islandDropdown.getValue(), savePrev = false)
    }

    private fun loadWaypointsForSelection(selection: Int, savePrev: Boolean = true, isClosing: Boolean = false) {
        if (savePrev) {
            val current = SkyblockIsland.values().find {
                it.formattedName == islandDropdown.childrenOfType<UIText>()
                    .find { it.componentName == "currentSelectionText" }?.getText()
            } ?: error("previous selected island not found")
            Waypoints.waypoints.removeAll {
                it.island == current
            }
            for (entry in entries.values) {
                runCatching {
                    Waypoints.waypoints.add(
                        entry.toWaypoint(current)
                    )
                }.onFailure {
                    it.printStackTrace()
                }
            }
            PersistentSave.markDirty<Waypoints>()
        }
        entries.clear()
        scrollComponent.clearChildren()
        if (!isClosing) {
            val island = SkyblockIsland.values()[selection]
            Waypoints.waypoints.filter {
                it.island == island
            }.sortedBy { SortingOptions.values()[SortingOptions.lastSelected].sortingBy(it) }.forEach {
                addNewWaypoint(it.name, it.pos, it.enabled, it.color, it.addedAt)
            }
        }
    }

    private fun addNewWaypoint(
        name: String = "",
        pos: BlockPos = mc.thePlayer.position,
        enabled: Boolean = true,
        color: Color = Color.RED,
        addedAt: Long = System.currentTimeMillis()
    ) {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = ChildBasedRangeConstraint()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val enabled = CheckboxComponent(enabled).childOf(container).constrain {
            x = 7.5.pixels()
            y = CenterConstraint()
        }

        val nameComponent = UITextInput("Waypoint Name").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 30.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(name)
        }

        val xComponent = UITextInput("X").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(pos.x.toString())
        }

        val yComponent = UITextInput("Y").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(pos.y.toString())
        }

        val zComponent = UITextInput("Z").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(pos.z.toString())
        }

        val colorComponent = ColorComponent(color, true).childOf(container).constrain {
            x = SiblingConstraint(25f)
            y = CenterConstraint()
            width = CoerceAtLeastConstraint(AspectConstraint(), 10.percentOfWindow)
        }.apply {
            setColor(color)
            onValueChange { c ->
                setColor(c as Color)
            }
        }

        SimpleButton("Remove").childOf(container).constrain {
            x = 85.percent()
            y = CenterConstraint()
            height = 40.pixels
        }.onLeftClick {
            scrollComponent.removeChild(container)
            entries.remove(container)
        }

        nameComponent.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) xComponent.grabWindowFocus()
            }
        }

        xComponent.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) yComponent.grabWindowFocus()
                setText(getText().filter { c -> c.isDigit() || c == '-' })
            }
        }

        yComponent.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) zComponent.grabWindowFocus()
                setText(getText().filter { c -> c.isDigit() || c == '-' })
            }
        }

        zComponent.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) nameComponent.grabWindowFocus()
                setText(getText().filter { c -> c.isDigit() || c == '-' })
            }
        }

        entries[container] =
            Entry(enabled, nameComponent, xComponent, yComponent, zComponent, colorComponent, addedAt)
    }

    override fun onTick() {
        for ((container, entry) in entries) {
            if (entry.color.getHeight() == 20f) container.setHeight(9.5f.percent())
            else container.setHeight(30f.percent())
        }
        super.onTick()
    }

    override fun onScreenClose() {
        super.onScreenClose()
        loadWaypointsForSelection(-1, isClosing = true)
    }

    private data class Entry(
        val enabled: CheckboxComponent,
        val name: UITextInput,
        val x: UITextInput,
        val y: UITextInput,
        val z: UITextInput,
        val color: ColorComponent,
        val addedAt: Long
    ) {
        fun toWaypoint(island: SkyblockIsland) = Waypoint(
            name.getText(),
            BlockPos(
                x.getText().toInt(),
                y.getText().toInt(),
                z.getText().toInt()
            ),
            island,
            enabled.checked,
            color.getColor(),
            addedAt
        )
    }

    enum class SortingOptions(val displayName: String, val sortingBy: (Waypoint) -> String) {
        AZ("A-Z", { "${it.name} ${it.pos} ${it.enabled}" }),
        CLOSEST("Closest", { "${mc.thePlayer?.getDistanceSq(it.pos)} ${AZ.sortingBy(it)}" }),
        RECENT("Recent", { "${Long.MAX_VALUE - it.addedAt} ${AZ.sortingBy(it)}" });

        companion object {
            var lastSelected = 0
        }
    }
}
