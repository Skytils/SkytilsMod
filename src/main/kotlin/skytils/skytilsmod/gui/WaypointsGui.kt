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
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.gui.settings.CheckboxComponent
import gg.essential.vigilance.gui.settings.DropDown
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.util.BlockPos
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.features.impl.handlers.Waypoint
import skytils.skytilsmod.features.impl.handlers.Waypoints
import skytils.skytilsmod.gui.components.SimpleButton
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.SkyblockIsland
import java.awt.Color

class WaypointsGui : WindowScreen(newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent

    private val islandDropdown: DropDown

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

        islandDropdown = DropDown(SkyblockIsland.values().indexOfFirst {
            SBInfo.mode == it.mode
        }.run { if (this == -1) 0 else this }, SkyblockIsland.values().map { it.formattedName }).childOf(window)
            .constrain {
                x = basicXConstraint { it.parent.getRight() - it.getWidth() - 5f }
                y = 5.percent()
            }.also {
                it.onValueChange { s ->
                    loadWaypointsForSelection(s)
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
                Skytils.displayScreen = WaypointShareGui()
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
                Waypoints.waypoints.add(
                    Waypoint(
                        entry.name.getText(),
                        BlockPos(
                            entry.x.getText().toInt(),
                            entry.y.getText().toInt(),
                            entry.z.getText().toInt()
                        ),
                        current,
                        entry.enabled.checked
                    )
                )
            }
            PersistentSave.markDirty<Waypoints>()
        }
        entries.clear()
        scrollComponent.clearChildren()
        if (!isClosing) {
            val island = SkyblockIsland.values()[selection]
            Waypoints.waypoints.filter {
                it.island == island
            }.forEach {
                addNewWaypoint(it.name, it.pos, it.enabled)
            }
        }
    }

    private fun addNewWaypoint(name: String = "", pos: BlockPos = mc.thePlayer.position, enabled: Boolean = true) {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = 9.5.percent()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val enabled = CheckboxComponent(enabled).childOf(container).constrain {
            x = 7.5.pixels()
            y = CenterConstraint()
        }

        val nameComponent = (UITextInput("Waypoint Name").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 30.percent()
        }.onLeftClick {
            grabWindowFocus()
        } as UITextInput).also {
            it.setText(name)
        }

        val xComponent = (UITextInput("X").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.onLeftClick {
            grabWindowFocus()
        } as UITextInput).also {
            it.setText(pos.x.toString())
            it.onKeyType { _, _ ->
                it.setText(it.getText().filter { c -> c.isDigit() || c == '-' })
            }
        }

        val yComponent = (UITextInput("Y").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.onLeftClick {
            grabWindowFocus()
        } as UITextInput).also {
            it.setText(pos.y.toString())
            it.onKeyType { _, _ ->
                it.setText(it.getText().filter { c -> c.isDigit() || c == '-' })
            }
        }

        val zComponent = (UITextInput("Z").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.onLeftClick {
            grabWindowFocus()
        } as UITextInput).also {
            it.setText(pos.z.toString())
            it.onKeyType { _, _ ->
                it.setText(it.getText().filter { c -> c.isDigit() || c == '-' })
            }
        }

        SimpleButton("Remove").childOf(container).constrain {
            x = 85.percent()
            y = CenterConstraint()
            height = 75.percent()
        }.onLeftClick {
            scrollComponent.removeChild(container)
            entries.remove(container)
        }

        entries[container] = Entry(enabled, nameComponent, xComponent, yComponent, zComponent)
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
        val z: UITextInput
    )
}