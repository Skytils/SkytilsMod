/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.gui.waypoints

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
import gg.skytils.skytilsmod.features.impl.handlers.CategoryList
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
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class WaypointShareGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {

    companion object {
        private const val CATEGORY_INNER_VERTICAL_PADDING = 7.5
    }

    private val scrollComponent: ScrollComponent

    private val islandDropdown: DropDown
    private val versionDropdown: DropDown

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

        val hasUnknown = Waypoints.categories.any { it.island == SkyblockIsland.Unknown }
        islandDropdown = DropDown(
            SkyblockIsland.entries.indexOfFirst {
                SBInfo.mode == it.mode
            }.coerceAtLeast(0),
            SkyblockIsland.entries
                .mapNotNull { if (it == SkyblockIsland.Unknown && !hasUnknown) null else it.displayName }
        )
            .childOf(window)
            .constrain {
                x = 5.pixels(true)
                y = 5.percent()
            }.also {
                it.onValueChange { s ->
                    loadWaypointsForSelection(s)
                }
            }
        versionDropdown = DropDown(0, (1..2).map { "Format v${it}" }).childOf(window).constrain {
            x = 5.pixels(true)
            y = SiblingConstraint(5f)
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

        SimpleButton("Import from Clipboard").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            importFromClipboard()
        }

        SimpleButton("Export Selected to Clipboard").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            exportSelectedWaypoints()
        }

        SimpleButton("Import from File").childOf(bottomButtons).constrain {
            x = 0.pixels()
            y = SiblingConstraint(5f)
        }.onLeftClick {
            importFromFile()
        }

        SimpleButton("Export to File").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = CramSiblingConstraint()
        }.onLeftClick {
            exportToFile()
        }

        HelpComponent(
            window,
            "This menu is used to share waypoints with other people. To import waypoints from another person, copy the *entire* string of text to your clipboard, and then click 'Import from Clipboard'. To send waypoints to someone else, make sure the ones you want to share are selected and then click 'Export Selected to Clipboard'. Make sure the very long string of text is not cut off by any character limits, or people will not be able to import your waypoints."
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

        loadWaypointsForSelection(islandDropdown.getValue())
    }


    private fun importFromFile() {
        runCatching {
            val fileChooser = JFileChooser()
            fileChooser.fileFilter = FileNameExtensionFilter("Skytils Waypoints", "SkytilsWaypoints")
            fileChooser.dialogTitle = "Select a file to import waypoints from"
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile
                val data = Waypoints.getWaypointsFromFile(file)
                Waypoints.categories.addAll(data.categories)
                EssentialAPI.getNotifications().push(
                    "Waypoints Imported",
                    "Successfully imported ${data.categories.sumOf { it.waypoints.size }} waypoints!",
                    2.5f
                )

                PersistentSave.markDirty<Waypoints>()
                loadWaypointsForSelection(islandDropdown.getValue())
            }
        }.onFailure {
            it.printStackTrace()
            EssentialAPI.getNotifications()
                .push("Error", "Failed to import waypoints, reason: ${it::class.simpleName}: ${it.message}")
        }

    }

    private fun exportToFile() {
        runCatching {
            val version = versionDropdown.getValue() + 1
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "Select a file to export waypoints to"
            fileChooser.fileFilter = FileNameExtensionFilter("Skytils Waypoints", "SkytilsWaypoints")
            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                val file = fileChooser.selectedFile

                val island = SkyblockIsland.entries[islandDropdown.getValue()]

                // Convert the selected waypoints into an object that can be easily serialized
                val categories = categoryContainers.map { entry ->
                    WaypointCategory(
                        name = entry.value.name,
                        waypoints = entry.value.container.childContainers.mapNotNull {
                            if (entries[it]?.selected?.checked != true) null
                            else entries[it]?.waypoint
                        }.toHashSet(),
                        isExpanded = true,
                        island = island
                    )
                }.toSet()

                Waypoints.writeWaypointsToFile(CategoryList(categories), file.toPath(), version)

                val count = categories.sumOf { it.waypoints.size }
                EssentialAPI.getNotifications()
                    .push(
                        "Waypoints Exported",
                        "$count ${island.displayName} waypoints were saved!",
                        2.5f
                    )

            }
        }.onFailure {
            it.printStackTrace()
            EssentialAPI.getNotifications()
                .push("Error", "Failed to export waypoints, reason: ${it::class.simpleName}: ${it.message}")
        }
    }

    private fun importFromClipboard() {
        runCatching {
            val clipboard = getClipboardString()
            val categories = Waypoints.getWaypointsFromString(clipboard)
            Waypoints.categories.addAll(categories)
            EssentialAPI.getNotifications().push(
                "Waypoints Imported",
                "Successfully imported ${categories.sumOf { it.waypoints.size }} waypoints!",
                2.5f
            )

            PersistentSave.markDirty<Waypoints>()
            loadWaypointsForSelection(islandDropdown.getValue())
        }.onFailure {
            it.printStackTrace()
            EssentialAPI.getNotifications()
                .push("Error", "Failed to import waypoints, reason: ${it::class.simpleName}: ${it.message}")
        }
    }

    private fun exportSelectedWaypoints() {
        runCatching {
            val island = SkyblockIsland.entries[islandDropdown.getValue()]

            // Convert the selected waypoints into an object that can be easily serialized
            val categories = categoryContainers.map { entry ->
                WaypointCategory(
                    name = entry.value.name,
                    waypoints = entry.value.container.childContainers.mapNotNull {
                        if (entries[it]?.selected?.checked != true) null
                        else entries[it]?.waypoint
                    }.toHashSet(),
                    isExpanded = true,
                    island = island
                )
            }.toSet()

            setClipboardString(Waypoints.getStringFromWaypoints(categories, versionDropdown.getValue() + 1))

            val count = categories.sumOf { it.waypoints.size }
            EssentialAPI.getNotifications()
                .push(
                    "Waypoints Exported",
                    "$count ${island.displayName} waypoints were copied to your clipboard!",
                    2.5f
                )
        }.onFailure {
            it.printStackTrace()
            EssentialAPI.getNotifications()
                .push("Error", "Failed to export waypoints, reason: ${it::class.simpleName}: ${it.message}")
        }
    }

    private fun loadWaypointsForSelection(selection: Int) {
        entries.clear()
        categoryContainers.clear()
        scrollComponent.clearChildren()
        val island = SkyblockIsland.entries[selection]
        Waypoints.categories.filter {
            it.island == island
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