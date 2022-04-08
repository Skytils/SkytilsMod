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

package sharttils.sharttilsmod.features.impl.dungeons

import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.item.ItemMap
import net.minecraft.world.storage.MapData
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.Sharttils.Companion.mc
import sharttils.sharttilsmod.core.structure.FloatPair
import sharttils.sharttilsmod.core.structure.GuiElement
import sharttils.sharttilsmod.utils.SBInfo
import sharttils.sharttilsmod.utils.SkyblockIsland
import sharttils.sharttilsmod.utils.graphics.SmartFontRenderer
import java.awt.Color

class DungeonMap {

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        mapData = null
    }

    class TrashDungeonMap : GuiElement(name = "Dungeon Map", fp = FloatPair(0, 0)) {

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
            get() = Sharttils.config.dungeonTrashMap
        override val height: Int
            get() = 128
        override val width: Int
            get() = 128

        init {
            Sharttils.guiManager.registerElement(this)
        }
    }

    init {
        TrashDungeonMap()
    }

    companion object {
        var mapData: MapData? = null
    }
}