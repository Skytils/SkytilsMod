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
package gg.skytils.skytilsmod.mixins.hooks.gui

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.events.impl.SetActionBarEvent
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import gg.skytils.skytilsmod.features.impl.misc.ItemFeatures
import gg.skytils.skytilsmod.utils.RenderUtil.renderRarity
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.equalsAnyOf
import net.minecraft.entity.player.EntityPlayer
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo


var recordPlaying: String? = null
var recordPlayingUpFor: Int = 0
var recordIsPlaying: Boolean = false

fun onSetActionBar(message: String, isPlaying: Boolean, ci: CallbackInfo): Boolean {
    val event = SetActionBarEvent(message, isPlaying)
    if (event.postAndCatch()) {
        ci.cancel()
        return false
    }
    if (message != event.message || isPlaying != event.isPlaying) {
        ci.cancel()
        recordPlaying = event.message
        recordPlayingUpFor = 60
        recordIsPlaying = event.isPlaying
        return true
    }
    return false
}

fun renderRarityOnHotbar(
    index: Int,
    xPos: Int,
    yPos: Int,
    partialTicks: Float,
    player: EntityPlayer,
    ci: CallbackInfo
) {
    if (Utils.inSkyblock && Skytils.config.showItemRarity) {
        ItemFeatures.hotbarRarityCache[index]?.let { renderRarity(xPos, yPos, it) }
    }
}

fun onWorldBorder(f: Float): Float =
    if (Skytils.config.worldborderFix && DungeonFeatures.hasBossSpawned && DungeonFeatures.dungeonFloor.equalsAnyOf(
            "F2",
            "M2",
            "F3",
            "M3"
        )
    ) 0f else f