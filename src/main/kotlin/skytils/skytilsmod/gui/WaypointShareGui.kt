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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import gg.essential.api.EssentialAPI
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
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
import org.apache.commons.codec.binary.Base64
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.Waypoint
import skytils.skytilsmod.features.impl.handlers.Waypoints
import skytils.skytilsmod.gui.components.SimpleButton
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.SkyblockIsland
import skytils.skytilsmod.utils.setState
import java.awt.Color

class WaypointShareGui : WindowScreen(newGuiScale = 2) {
    
    companion object {
        val gson: Gson = GsonBuilder().create()
    }

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

        SimpleButton("Back").childOf(bottomButtons).constrain {
            x = 0.pixels()
            y = 0.pixels()
        }.onLeftClick {
            if (mc.thePlayer != null) mc.thePlayer.closeScreen() else mc.displayGuiScreen(null)
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

        SimpleButton("Select All").childOf(window).constrain {
            x = 5.pixels()
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

            val arr = gson.fromJson(decoded, JsonArray::class.java)
            val results = arr.mapNotNull { e ->
                e as JsonObject
                Waypoint(
                    e["name"].asString,
                    BlockPos(
                        e["x"].asInt,
                        e["y"].asInt,
                        e["z"].asInt
                    ),
                    SkyblockIsland.values().find {
                        it.mode == e["island"].asString
                    } ?: return@mapNotNull null,
                    e["enabled"].asBoolean
                )
            }
            Waypoints.waypoints.addAll(results)
            PersistentSave.markDirty<Waypoints>()
            loadWaypointsForSelection(islandDropdown.getValue())
            EssentialAPI.getNotifications()
                .push("Waypoints Imported", "Successfully imported ${results.size} waypoints!", 2.5f)
        }.onFailure {
            it.printStackTrace()
            EssentialAPI.getNotifications()
                .push("Error", "Failed to import waypoints, reason: ${it::class.simpleName}: ${it.message}")
        }
    }

    private fun exportSelectedWaypoints() {
        val island = SkyblockIsland.values()[islandDropdown.getValue()]

        val arr = JsonArray()
        entries.values.filter {
            it.selected.checked
        }.forEach {
            arr.add(JsonObject().apply {
                addProperty("name", it.name.getText())
                addProperty("x", it.x.getText().toInt())
                addProperty("y", it.y.getText().toInt())
                addProperty("z", it.z.getText().toInt())
                addProperty("island", island.mode)
                addProperty("enabled", true)
            })
        }
        setClipboardString(Base64.encodeBase64String(gson.toJson(arr).encodeToByteArray()))
        EssentialAPI.getNotifications()
            .push(
                "Waypoints Exported",
                "${arr.size()} ${island.formattedName} waypoints were copied to your clipboard!",
                2.5f
            )
    }

    private fun loadWaypointsForSelection(selection: Int) {
        entries.clear()
        scrollComponent.clearChildren()
        val island = SkyblockIsland.values()[selection]
        Waypoints.waypoints.filter {
            it.island == island
        }.forEach {
            addNewWaypoint(it.name, it.pos, it.enabled)
        }
    }

    private fun addNewWaypoint(name: String = "", pos: BlockPos = mc.thePlayer.position, selected: Boolean = false) {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = 9.5.percent()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val selected = CheckboxComponent(selected).childOf(container).constrain {
            x = 7.5.pixels()
            y = CenterConstraint()
        }

        val nameComponent = UIText(name).childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        }

        val xComponent = UIText(pos.x.toString()).childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        }

        val yComponent = UIText(pos.y.toString()).childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        }

        val zComponent = UIText(pos.z.toString()).childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        }

        entries[container] = Entry(selected, nameComponent, xComponent, yComponent, zComponent)
    }


    override fun onScreenClose() {
        super.onScreenClose()
        Skytils.displayScreen = WaypointsGui()
    }

    private data class Entry(
        val selected: CheckboxComponent,
        val name: UIText,
        val x: UIText,
        val y: UIText,
        val z: UIText
    )
}