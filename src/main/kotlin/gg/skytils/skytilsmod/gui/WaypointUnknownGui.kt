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

import gg.essential.api.EssentialAPI
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.gui.settings.DropDown
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.Waypoint
import gg.skytils.skytilsmod.features.impl.handlers.WaypointCategory
import gg.skytils.skytilsmod.features.impl.handlers.Waypoints
import gg.skytils.skytilsmod.gui.components.HelpComponent
import gg.skytils.skytilsmod.gui.components.MultiCheckboxComponent
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.SkyblockIsland
import gg.skytils.skytilsmod.utils.childContainers
import java.awt.Color

class WaypointUnknownGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {

    companion object {
        private const val CATEGORY_INNER_VERTICAL_PADDING = 7.5
    }

    private val scrollComponent: ScrollComponent

    private val islandDropdown: DropDown

    private val entries = HashMap<UIContainer, Entry>()
    private val categoryContainers = HashMap<UIContainer, Category>()

    init {
        scrollComponent = ScrollComponent(
            innerPadding = 4f,
        ).childOf(window).constrain {
            x = CenterConstraint()
            y = 15.percent()
            width = 90.percent()
            height = 70.percent() + 2.pixels()
        }

        islandDropdown = DropDown(
            SkyblockIsland.entries.indexOfFirst {
                SBInfo.mode == it.mode
            }.coerceAtLeast(0),
            SkyblockIsland.entries.mapNotNull { if (it == SkyblockIsland.Unknown) null else it.displayName }
        )
            .childOf(window)
            .constrain {
                x = 5.pixels(true)
                y = 5.percent()
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

        SimpleButton("Back").childOf(bottomButtons).constrain {
            x = 0.pixels()
            y = 0.pixels()
        }.onLeftClick {
            mc.displayGuiScreen(null)
        }

        SimpleButton("Move to Selected Island").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            exportSelectedWaypoints()
        }

        HelpComponent(
            window,
            "This menu is used to move waypoints without a known island to the correct island."
        )

        SimpleButton("Select All").childOf(window).constrain {
            x = SiblingConstraint(5f)
            y = 5.pixels(alignOpposite = true)
        }.onLeftClick {
            if (entries.values.all {
                    it.selected.checked!!
                }) {
                entries.values.forEach {
                    it.selected.setState(false)
                }
            } else {
                entries.values.forEach {
                    it.selected.setState(true)
                }
            }
        }

        loadWaypoints()
    }

    private fun exportSelectedWaypoints() {
        val island = SkyblockIsland.entries[islandDropdown.getValue()]

        val newCategories = HashSet<WaypointCategory>()
        val remainingUnknownCategories = HashSet<WaypointCategory>()

        categoryContainers.forEach { entry ->
            entry.value.container.childContainers.partition {
                entries[it]?.selected?.checked == true
            }.let { (selected, unselected) ->
                if (selected.isNotEmpty())
                    newCategories.add(
                        WaypointCategory(
                            name = entry.value.name,
                            waypoints = selected.mapNotNullTo(HashSet()) { entries[it]?.waypoint },
                            isExpanded = true,
                            island = island
                        )
                    )

                if (unselected.isNotEmpty())
                    remainingUnknownCategories.add(
                        WaypointCategory(
                            name = entry.value.name,
                            waypoints = unselected.mapNotNullTo(HashSet()) { entries[it]?.waypoint },
                            isExpanded = true,
                            island = SkyblockIsland.Unknown
                        )
                    )
            }
        }

        Waypoints.categories.removeAll { it.island == SkyblockIsland.Unknown }
        Waypoints.categories.addAll(newCategories)
        Waypoints.categories.addAll(remainingUnknownCategories)

        PersistentSave.markDirty<Waypoints>()

        val count = newCategories.sumOf { it.waypoints.size }
        EssentialAPI.getNotifications()
            .push(
                "Waypoints Moved",
                "$count waypoints were moved to ${island.displayName}!",
                2.5f
            )

        loadWaypoints()
    }

    private fun loadWaypoints() {
        entries.clear()
        categoryContainers.clear()
        scrollComponent.clearChildren()
        Waypoints.categories.filter {
            it.island == SkyblockIsland.Unknown
        }.forEach {
            val category = addNewCategory(it.name ?: "Uncategorized")
            it.waypoints.sortedBy { w -> "${w.name} ${w.pos} ${w.enabled}" }.forEach { waypoint ->
                addNewWaypoint(category, waypoint)
            }
            updateCheckbox(category)
        }
    }

    private fun addNewCategory(
        name: String = "",
        enabled: Boolean = true
    ): Category {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 90.percent()
            height = ChildBasedRangeConstraint() + (CATEGORY_INNER_VERTICAL_PADDING * 2).pixels()
        }.effect(OutlineEffect(Color(255, 255, 255, 100), 1f))

        val selectedComponent = MultiCheckboxComponent(enabled).childOf(container).constrain {
            x = 7.5.pixels()
            y = CATEGORY_INNER_VERTICAL_PADDING.pixels()
        }.apply {
            onValueChange { newValue: Any? ->
                if (newValue != null) {
                    // When the category is checked or unchecked, all child waypoints will follow this change.
                    this.parent.childContainers.forEach {
                        it.childrenOfType<MultiCheckboxComponent>().firstOrNull()?.setState(newValue as Boolean)
                    }
                }
            }
        }

        UIText(name.ifEmpty { "Unnamed Category" }).childOf(container).constrain {
            x = CenterConstraint()
            y = CATEGORY_INNER_VERTICAL_PADDING.pixels()
            height = 12.pixels()
        }

        val categoryObj = Category(container, selectedComponent, name)

        categoryContainers[container] = categoryObj
        return categoryObj
    }

    private fun addNewWaypoint(
        category: Category,
        waypoint: Waypoint
    ) {
        val container = UIContainer().childOf(category.container).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 90.percent()
            height = ChildBasedMaxSizeConstraint() + 2.pixels()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val selected = MultiCheckboxComponent(waypoint.enabled).childOf(container).constrain {
            x = 7.5.pixels()
            y = CenterConstraint()
        }.apply {
            onValueChange {
                Window.enqueueRenderOperation {
                    // Update the checkbox *after* the `checked` state is updated.
                    updateCheckbox(category)
                }
            }
        }

        val nameComponent = UIText(waypoint.name).childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        }

        // Position the x, y, and z labels so that they are centered at the middle of the ScrollComponent
        val coordinates = UIContainer().childOf(container).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = ChildBasedMaxSizeConstraint()
        }

        val xComponent = UIText(waypoint.pos.x.toString()).childOf(coordinates).constrain {
            x = 0.pixels()
            y = CenterConstraint()
            color = ConstantColorConstraint(Color.LIGHT_GRAY)
        }

        val yComponent = UIText(waypoint.pos.y.toString()).childOf(coordinates).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            color = ConstantColorConstraint(Color.LIGHT_GRAY)
        }

        val zComponent = UIText(waypoint.pos.z.toString()).childOf(coordinates).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            color = ConstantColorConstraint(Color.LIGHT_GRAY)
        }

        entries[container] = Entry(category, selected, nameComponent, xComponent, yComponent, zComponent, waypoint)
    }

    private fun updateCheckbox(category: Category) {
        val anySelected = category.container.childContainers.any {
            entries[it]?.selected?.checked == true
        }
        val allSelected = anySelected && category.container.childContainers.all {
            entries[it]?.selected?.checked == true
        }
        category.selected.setState(
            // If all checkboxes are checked, set the state to on.
            // If some but not all of the checkboxes are checked, set the state to an indeterminate state.
            // If none are checked, uncheck the checkbox.
            if (allSelected) true else if (anySelected) null else false
        )
    }

    override fun onScreenClose() {
        super.onScreenClose()
        Skytils.displayScreen = WaypointsGui()
    }

    private data class Category(
        val container: UIContainer,
        val selected: MultiCheckboxComponent,
        val name: String,
    )

    private data class Entry(
        val category: Category,
        val selected: MultiCheckboxComponent,
        val name: UIText,
        val x: UIText,
        val y: UIText,
        val z: UIText,
        val waypoint: Waypoint
    )
}
