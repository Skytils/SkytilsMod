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

package gg.skytils.skytilsmod.features.impl.misc

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.cheats.Nametags
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NametagsPlayers {

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!Skytils.config.nametags || !Skytils.config.nametagPlayers) return
        if (Utils.inDungeons) {
            for (teammate in DungeonListener.team.values) {
                val player = teammate.player ?: continue
                if (!teammate.canRender()) continue
                Nametags.renderNameTag(player, ColorUtils.stripColor(player.displayName.unformattedText))
            }
        }
    }

    @SubscribeEvent
    fun onRender3D(event: RenderLivingEvent.Pre<*>) {
        if (!Skytils.config.nametags || !Skytils.config.nametagPlayers) return
        val player = event.entity as? EntityPlayer ?: return
        if (!Utils.inDungeons) {
            Nametags.renderNameTag(player, ColorUtils.stripColor(player.displayName.unformattedText))
        }
    }
}