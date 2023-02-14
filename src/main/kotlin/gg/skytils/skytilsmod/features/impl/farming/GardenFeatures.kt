/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2023 Skytils
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

package gg.skytils.skytilsmod.features.impl.farming

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.ScoreboardUtil
import gg.skytils.skytilsmod.utils.SkyblockIsland
import net.minecraft.init.Blocks
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object GardenFeatures {
    private val cleanupRegex = Regex("^\\s*Cleanup: [\\d.]+%$")
    var isCleaningPlot = false
        private set
    val trashBlocks = setOf(Blocks.grass, Blocks.tallgrass, Blocks.red_flower, Blocks.yellow_flower)

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (SBInfo.mode != SkyblockIsland.TheGarden.mode || event.phase != TickEvent.Phase.START || mc.thePlayer == null) return

        isCleaningPlot = ScoreboardUtil.sidebarLines.any {
            it.matches(cleanupRegex)
        }.also {
            if (it != isCleaningPlot) {
                mc.renderGlobal.loadRenderers()
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        isCleaningPlot = false
    }
}