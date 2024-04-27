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
package gg.skytils.skytilsmod.events.impl

import gg.skytils.skytilsmod.events.SkytilsEvent
import net.minecraft.client.gui.FontRenderer
import net.minecraft.item.ItemStack

abstract class GuiRenderItemEvent : SkytilsEvent() {
    abstract class RenderOverlayEvent(
        open val fr: FontRenderer,
        open val stack: ItemStack?,
        open val x: Int,
        open val y: Int,
        open val text: String?
    ) : GuiRenderItemEvent() {
        data class Post(
            override val fr: FontRenderer,
            override val stack: ItemStack?,
            override val x: Int,
            override val y: Int,
            override val text: String?
        ) :
            RenderOverlayEvent(fr, stack, x, y, text)
    }
}