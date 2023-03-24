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

package gg.skytils.skytilsmod.features.impl.dungeons

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.SkyblockIsland
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.item.ItemMap
import net.minecraft.world.storage.MapData
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object DungeonMap {
    var mapData: MapData? = null

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        mapData = null
    }

    class TrashDungeonMap : GuiElement(name = "Dungeon Map", x = 0, y = 0) {

        override fun render() {
            if (!toggled || SBInfo.mode != SkyblockIsland.Dungeon.mode || mc.thePlayer == null || mc.theWorld == null) return
            readMapData()
            if (mapData == null) return
            GlStateManager.color(1f, 1f, 1f, 1f)
            mc.entityRenderer.mapItemRenderer.renderMap(mapData, false)
        }

        override fun demoRender() {
            Gui.drawRect(0, 0, 128, 128, Color.RED.rgb)
            fr.drawString("Dungeon Map", 64f, 5f, alignment = SmartFontRenderer.TextAlignment.MIDDLE)
        }

        private fun readMapData() {
            val mapItem = mc.thePlayer.inventory.getStackInSlot(8)
            if (mapItem == null || mapItem.item !is ItemMap) return
            mapData = Items.filled_map.getMapData(mapItem, mc.theWorld)
        }

        override val toggled: Boolean
            get() = Skytils.config.dungeonTrashMap
        override val height: Int
            get() = 128
        override val width: Int
            get() = 128

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    init {
        TrashDungeonMap()
    }
}