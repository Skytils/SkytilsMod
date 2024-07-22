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

package gg.skytils.skytilsmod.mixins.hooks.util

import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.TickEvent
import gg.skytils.event.impl.screen.ScreenOpenEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer

object MouseHelperHook : EventSubscriber {
    private var lastOpen = -1L
    private var hasScreen = false

    override fun setup() {
        register(::onGuiOpen)
        register(::onTick)
    }

    fun onGuiOpen(e: ScreenOpenEvent) {
        val oldGui = mc.currentScreen
        if (e.screen is GuiChest && (oldGui is GuiContainer || oldGui == null)) {
            lastOpen = System.currentTimeMillis()
        }
    }

    fun onTick(event: TickEvent) {
        hasScreen = mc.currentScreen != null
    }

    fun shouldResetMouseToCenter(): Boolean {
        return !(Skytils.config.preventCursorReset && System.currentTimeMillis() - lastOpen <= 150 && hasScreen)
    }
}