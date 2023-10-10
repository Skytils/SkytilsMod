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

import gg.skytils.skytilsmod.Skytils
import net.minecraft.client.Minecraft
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object MouseHelperHook {
    private var lastOpen = -1L
    private var guiWasNotNull = false
    @SubscribeEvent
    fun onGuiOpen(e: GuiOpenEvent) {
        val oldGui = Minecraft.getMinecraft().currentScreen
        if (e.gui is GuiChest && (oldGui is GuiContainer || oldGui == null)) {
            lastOpen = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        guiWasNotNull = Minecraft.getMinecraft().currentScreen != null
    }

    fun shouldResetMouseToCenter(): Boolean {
        return Skytils.config.preventCursorReset && System.currentTimeMillis() - lastOpen <= 150 && guiWasNotNull
    }
}