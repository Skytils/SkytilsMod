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

package skytils.skytilsmod.features.impl.misc

import net.minecraft.entity.item.EntityFallingBlock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.impl.CheckRenderEntityEvent
import skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import skytils.skytilsmod.utils.Utils

object RandomStuff {
    @SubscribeEvent
    fun onCheckRenderEntity(event: CheckRenderEntityEvent<*>) {
        if (!Skytils.config.randomStuff || !Utils.inSkyblock) return
        if (event.entity is EntityFallingBlock && DungeonTimer.phase2ClearTime == -1L && DungeonTimer.phase1ClearTime != -1L) {
            event.isCanceled = true
            mc.theWorld?.removeEntity(event.entity)
        }
    }
}