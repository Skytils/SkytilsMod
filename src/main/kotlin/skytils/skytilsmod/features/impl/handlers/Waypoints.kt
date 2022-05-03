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

import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import kotlinx.serialization.*
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.utils.*
import java.awt.Color
import java.io.File
import java.io.Reader
import java.io.Writer

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

    override fun read(reader: Reader) {
        waypoints.clear()
        // TODO: check to see if it will throw an exception if any waypoint is blazing fortress
        waypoints.addAll(json.decodeFromString<List<Waypoint>>(reader.readText()))
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(waypoints))
    }

    override fun setDefault(writer: Writer) {
        writer.write("[]")
    }

    companion object {
        @JvmField
        val waypoints = HashSet<Waypoint>()
    }
}

@Serializable
data class Waypoint @OptIn(ExperimentalSerializationApi::class) constructor(
    var name: String,
    @Serializable(with = BlockPosObjectSerializer::class)
    var pos: BlockPos,
    @Serializable(with = SkyblockIsland.ModeSerializer::class)
    var island: SkyblockIsland,
    @EncodeDefault
    var enabled: Boolean = true,
    @Serializable(with = IntColorSerializer::class)
    @EncodeDefault
    var color: Color = Color.RED,
    @EncodeDefault
    val addedAt: Long = System.currentTimeMillis()
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
