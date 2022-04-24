/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

import com.google.gson.*
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
import gg.essential.vigilance.gui.settings.CheckboxComponent
import gg.essential.vigilance.gui.settings.DropDown
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.util.BlockPos
import org.apache.commons.codec.binary.Base64
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.Waypoints
import skytils.skytilsmod.gui.components.HelpComponent
import skytils.skytilsmod.gui.components.SimpleButton
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.SkyblockIsland
import skytils.skytilsmod.utils.setState
import java.awt.Color

class WaypointShareGui : WindowScreen(ElementaVersion.V1, newGuiScale = 2) {

    companion object {
        private val gson: Gson = GsonBuilder().create()

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

        islandDropdown = DropDown(SkyblockIsland.values().indexOfFirst {
            SBInfo.mode == it.mode
        }.run { if (this == -1) 0 else this }, SkyblockIsland.values().map { it.formattedName }).childOf(window)
            .constrain {
                x = 5.pixels(true)
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

        HelpComponent(
            window,
            "This menu is used to share waypoints with other people. To import waypoints from another person, copy the *entire* string of text to your clipboard, and then click 'Import from Clipboard'. To send waypoints to someone else, make sure the ones you want to share are selected and then click 'Export Selected to Clipboard'. Make sure the very long string of text is not cut off by any character limits, or people will not be able to import your waypoints."
        )

        SimpleButton("Select All").childOf(window).constrain {
            x = SiblingConstraint(5f)
            y = 5.pixels(alignOpposite = true)
        }.onLeftClick {
            if (entries.values.all {
                    it.selected.checked
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

    private fun importFromClipboard() {
        runCatching {
            val decoded = Base64.decodeBase64(getClipboardString()).toString(Charsets.UTF_8)

            val obj = gson.fromJson(decoded, JsonElement::class.java)
            val count = Waypoints.importFromGenericJsonObject(obj)
            PersistentSave.markDirty<Waypoints>()
            loadWaypointsForSelection(islandDropdown.getValue())
            EssentialAPI.getNotifications()
                .push("Waypoints Imported", "Successfully imported $count waypoints!", 2.5f)
        }.onFailure {
            it.printStackTrace()
            EssentialAPI.getNotifications()
                .push("Error", "Failed to import waypoints, reason: ${it::class.simpleName}: ${it.message}")
        }
    }

    private fun exportSelectedWaypoints() {
        val island = SkyblockIsland.values()[islandDropdown.getValue()]

        val obj = exportToNewFormat(island)
        val count = obj["categories"].asJsonArray.sumOf { it.asJsonObject["waypoints"].asJsonArray.size() }
        setClipboardString(Base64.encodeBase64String(gson.toJson(obj).encodeToByteArray()))
        EssentialAPI.getNotifications()
            .push(
                "Waypoints Exported",
                "$count ${island.formattedName} waypoints were copied to your clipboard!",
                2.5f
            )
    }

    private fun exportToNewFormat(island: SkyblockIsland): JsonObject {
        val parentObj = JsonObject()
        val categoriesList = JsonArray()

        categoryContainers.forEach { (uiContainer, categoryObj) ->
            categoriesList.add(JsonObject().apply {
                addProperty("name", categoryObj.name)
                addProperty("island", island.mode)
                add("waypoints", JsonArray().apply {
                    uiContainer.childContainers.mapNotNull {
                        val e = entries[it] ?: return@mapNotNull null
                        if (e.selected.checked)
                            JsonObject().apply {
                                addProperty("name", e.name.getText())
                                addProperty("x", e.x.getText().toInt())
                                addProperty("y", e.y.getText().toInt())
                                addProperty("z", e.z.getText().toInt())
                                addProperty("enabled", true)
                                addProperty("color", Color.RED.rgb)
                            }
                        else null
                    }.forEach { add(it) }
                })
            })
        }
        parentObj.add("categories", categoriesList)
        return parentObj
    }

    private fun exportToOldFormat(island: SkyblockIsland): JsonArray {
        val arr = JsonArray()
        entries.values.filter {
            it.selected.checked
        }.forEach {
            runCatching {
                arr.add(JsonObject().apply {
                    addProperty("name", it.name.getText())
                    addProperty("x", it.x.getText().toInt())
                    addProperty("y", it.y.getText().toInt())
                    addProperty("z", it.z.getText().toInt())
                    addProperty("island", island.mode)
                    addProperty("enabled", true)
                    addProperty("color", Color.RED.rgb)
                })
            }.onFailure {
                it.printStackTrace()
            }
        }
        return arr
    }

    private fun loadWaypointsForSelection(selection: Int) {
        entries.clear()
        categoryContainers.clear()
        scrollComponent.clearChildren()
        val island = SkyblockIsland.values()[selection]
        Waypoints.categories.filter {
            it.island == island
        }.forEach {
            val category = addNewCategory(it.name ?: "Uncategorized")
            it.waypoints.sortedBy { w -> "${w.name} ${w.pos} ${w.enabled}" }.forEach { waypoint ->
                addNewWaypoint(category, waypoint.name, waypoint.pos, waypoint.enabled)
            }
        }
    }

    private fun addNewCategory(
        name: String = "",
        enabled: Boolean = true,
    ): Category {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 90.percent()
            height = ChildBasedRangeConstraint() + (CATEGORY_INNER_VERTICAL_PADDING * 2).pixels()
        }.effect(OutlineEffect(Color(255, 255, 255, 100), 1f))

        val selectedComponent = CheckboxComponent(enabled).childOf(container).constrain {
            x = 7.5.pixels()
            y = CATEGORY_INNER_VERTICAL_PADDING.pixels()
        }.apply {
            onValueChange { newValue: Any? ->
                val categoryObj = categoryContainers[container] ?: error("no category found for UIContainer")
                // If this value change was triggered while updating the checkbox, don't update the child checkboxes.
                // see `updateCheckbox()`
                if (!categoryObj.ignoreCheckboxValueChange) {
                    // When the category is checked or unchecked, all child waypoints will follow this change.
                    this.parent.childContainers.forEach {
                        it.childrenOfType<CheckboxComponent>().firstOrNull()?.setState(newValue as Boolean)
                    }
                }
            }
        }

        UIText(name).childOf(container).constrain {
            x = CenterConstraint()
            y = CATEGORY_INNER_VERTICAL_PADDING.pixels()
            height = 12.pixels()
        }

        categoryContainers[container] = Category(
            container,
            selectedComponent,
            name
        )

        return categoryContainers[container]!!
    }

    private fun addNewWaypoint(
        category: Category,
        name: String = "",
        pos: BlockPos = mc.thePlayer.position,
        selected: Boolean = false,
    ) {
        val container = UIContainer().childOf(category.container).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 90.percent()
            height = ChildBasedMaxSizeConstraint() + 2.pixels()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val selected = CheckboxComponent(selected).childOf(container).constrain {
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

        val nameComponent = UIText(name).childOf(container).constrain {
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

        val xComponent = UIText(pos.x.toString()).childOf(coordinates).constrain {
            x = 0.pixels()
            y = CenterConstraint()
            color = ConstantColorConstraint(Color.LIGHT_GRAY)
        }

        val yComponent = UIText(pos.y.toString()).childOf(coordinates).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            color = ConstantColorConstraint(Color.LIGHT_GRAY)
        }

        val zComponent = UIText(pos.z.toString()).childOf(coordinates).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            color = ConstantColorConstraint(Color.LIGHT_GRAY)
        }

        entries[container] = Entry(category, selected, nameComponent, xComponent, yComponent, zComponent)
    }

    private fun updateCheckbox(category: Category) {
        category.ignoreCheckboxValueChange = true
        category.selected.setState(
            category.container.childContainers.all {
                entries[it]?.selected?.checked == true
            }
        )
        category.ignoreCheckboxValueChange = false
    }


    override fun onScreenClose() {
        super.onScreenClose()
        Skytils.displayScreen = WaypointsGui()
    }

    private data class Category(
        val container: UIContainer,
        val selected: CheckboxComponent,
        val name: String,
        var ignoreCheckboxValueChange: Boolean = false
    )

    private data class Entry(
        val category: Category,
        val selected: CheckboxComponent,
        val name: UIText,
        val x: UIText,
        val y: UIText,
        val z: UIText
    )
}