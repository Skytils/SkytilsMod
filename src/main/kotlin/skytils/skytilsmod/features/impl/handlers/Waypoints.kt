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
import com.google.gson.JsonObject
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.hylin.extension.getOptionalString
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
            waypoints.filter { it.enabled && it.island.mode == SBInfo.mode }.forEach {
                it.draw(event.partialTicks, matrixStack)
            }
        }
    }

    override fun read(reader: InputStreamReader) {
        waypoints.clear()
        val arr = gson.fromJson(reader, JsonArray::class.java)
        arr.mapNotNullTo(waypoints) { e ->
            e as JsonObject
            Waypoint(
                e.getOptionalString("category").ifEmpty { null },
                e["name"].asString,
                BlockPos(
                    e["x"].asInt,
                    e["y"].asInt,
                    e["z"].asInt
                ),
                SkyblockIsland.values().find {
                    it.mode == e["island"].asString
                } ?: return@mapNotNullTo null,
                e["enabled"].asBoolean,
                e["color"]?.let { Color(it.asInt, true) } ?: Color.RED,
                e["addedAt"]?.asLong ?: System.currentTimeMillis()
            )
        }
    }

    override fun write(writer: OutputStreamWriter) {
        val arr = JsonArray()
        waypoints.forEach {
            arr.add(JsonObject().apply {
                if(it.category != null)
                    addProperty("category", it.category)
                addProperty("name", it.name)
                addProperty("x", it.pos.x)
                addProperty("y", it.pos.y)
                addProperty("z", it.pos.z)
                addProperty("island", it.island.mode)
                addProperty("enabled", it.enabled)
                addProperty("color", it.color.rgb)
                addProperty("addedAt", it.addedAt)
            })
        }
        gson.toJson(arr, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        gson.toJson(JsonArray(), writer)
    }

    companion object {
        @JvmField
        val waypoints = HashSet<Waypoint>()
    }
}

data class Waypoint(
    var category: String?,
    var name: String,
    var pos: BlockPos,
    var island: SkyblockIsland,
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
