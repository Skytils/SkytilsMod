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
package skytils.skytilsmod.features.impl.handlers

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.hylin.extension.getString
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.utils.*
import java.awt.Color
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class Waypoints : PersistentSave(File(Skytils.modDir, "waypoints.json")) {

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (Utils.inSkyblock) {
            val matrixStack = UMatrixStack()
            categories.filter {
                it.island.mode == SBInfo.mode
            }.forEach { category ->
                category.waypoints.filter { it.enabled }.forEach {
                    it.draw(event.partialTicks, matrixStack)
                }
            }
        }
    }

    override fun read(reader: InputStreamReader) {
        importFromGenericJsonObject(gson.fromJson(reader, JsonElement::class.java))
    }

    override fun write(writer: OutputStreamWriter) {
        val parentObj = JsonObject()
        val categoriesList = JsonArray()

        categories.forEach { category ->
            val categoryObj = JsonObject()
            categoryObj.addProperty("name", category.name ?: "")
            categoryObj.addProperty("island", category.island.mode)
            categoryObj.addProperty("isExpanded", category.isExpanded)
            val waypointsList = JsonArray()
            category.waypoints.forEach {
                waypointsList.add(JsonObject().apply {
                    addProperty("name", it.name)
                    addProperty("x", it.pos.x)
                    addProperty("y", it.pos.y)
                    addProperty("z", it.pos.z)
                    addProperty("enabled", it.enabled)
                    addProperty("color", it.color.rgb)
                    addProperty("addedAt", it.addedAt)
                })
            }
            categoryObj.add("waypoints", waypointsList)
            categoriesList.add(categoryObj)
        }

        parentObj.add("categories", categoriesList)
        gson.toJson(parentObj, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        gson.toJson(JsonObject(), writer)
    }

    companion object {
        @JvmField
        val categories = HashSet<WaypointCategory>()

        private val sbeWaypointFormat =
            Regex("(?:\\/?crystalwaypoint parse )?(?<name>[a-zA-Z\\d]+)@(?<x>[-\\d]+),(?<y>[-\\d]+),(?<z>[-\\d]+)\\\\?n?")

        /**
         * Imports waypoints from either a [JsonArray] (old format) or a [JsonObject] (new format)
         * and adds them to the [categories] list.
         *
         * @param obj An instance of a [JsonArray] or a [JsonObject]
         * @return The number of waypoints that were added
         */
        fun importFromGenericJsonObject(obj: JsonElement): Int {
            var count = 0
            if (obj.isJsonObject) {
                obj as JsonObject
                // Newer save format including waypoint categories
                val categoriesList = obj["categories"].asJsonArray
                for (category in categoriesList) {
                    category as JsonObject
                    categories.add(
                        WaypointCategory(
                            category["name"].asString,
                            category["waypoints"].asJsonArray.map { e ->
                                count++
                                (e as JsonObject).asWaypoint()
                            }.toHashSet(),
                            category["isExpanded"]?.asBoolean ?: true,
                            SkyblockIsland.values().find { it.mode == category.getIsland() } ?: continue
                        )
                    )
                }
            } else if (obj.isJsonArray) {
                obj as JsonArray
                // Older save format without waypoint categories
                for (group in obj.groupBy { it.asJsonObject.getIsland() }) {
                    categories.add(
                        WaypointCategory(
                            name = null,
                            waypoints = group.value.map { e ->
                                count++
                                (e as JsonObject).asWaypoint()
                            }.toHashSet(),
                            isExpanded = true,
                            island = SkyblockIsland.values().find { it.mode == group.key } ?: continue
                        )
                    )
                }
            } else if (obj.isJsonPrimitive) {
                // This string might be in the SBE format, let's convert it so people don't complain
                val str = obj.asString
                if (str.contains("@")) {
                    val waypoints = sbeWaypointFormat.findAll(str.trim()).map {
                        Waypoint(
                            it.groups["name"]!!.value, BlockPos(
                                it.groups["x"]!!.value.toInt(),
                                it.groups["y"]!!.value.toInt(),
                                it.groups["z"]!!.value.toInt()
                            ), true, Color.RED, System.currentTimeMillis()
                        )
                    }
                    if (!waypoints.iterator().hasNext()) {
                        error("invalid JSON type")
                    }
                    categories.add(
                        WaypointCategory(
                            name = null,
                            waypoints = waypoints.toHashSet(),
                            isExpanded = true,
                            island = SkyblockIsland.values().find { it.mode == SBInfo.mode }
                                ?: SkyblockIsland.CrystalHollows
                        ))
                } else error("invalid JSON type")
            }
            return count
        }

        private fun JsonObject.asWaypoint(): Waypoint {
            return Waypoint(
                this["name"].asString,
                BlockPos(this["x"].asInt, this["y"].asInt, this["z"].asInt),
                this["enabled"].asBoolean,
                this["color"]?.let { Color(it.asInt, true) } ?: Color.RED,
                this["addedAt"]?.asLong ?: System.currentTimeMillis()
            )
        }

        private fun JsonObject.getIsland() = this.getString("island")
    }
}

/**
 * Represents a collection of waypoints, including a name, island, and a list of [Waypoint] objects.
 */
data class WaypointCategory(
    var name: String?,
    val waypoints: HashSet<Waypoint>,
    var isExpanded: Boolean = true,
    var island: SkyblockIsland
)

data class Waypoint(
    var name: String,
    var pos: BlockPos,
    var enabled: Boolean,
    val color: Color,
    val addedAt: Long
) {
    fun draw(partialTicks: Float, matrixStack: UMatrixStack) {
        val (viewerX, viewerY, viewerZ) = RenderUtil.getViewerPos(partialTicks)
        RenderUtil.drawFilledBoundingBox(
            matrixStack,
            pos.toBoundingBox().expandBlock().offset(-viewerX, -viewerY, -viewerZ),
            color.withAlpha(color.alpha.coerceAtMost(128)),
            1f
        )
        UGraphics.disableDepth()
        RenderUtil.renderWaypointText(
            name,
            pos.x + 0.5,
            pos.y + 1.0,
            pos.z + 0.5,
            partialTicks,
            matrixStack
        )
        UGraphics.enableDepth()
    }
}
