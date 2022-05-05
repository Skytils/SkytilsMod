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

import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import skytils.skytilsmod.core.Config
import skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiEditSign
import skytils.skytilsmod.utils.Utils

/**
 * This feature allows users to confirm sign popups in skyblock with the enter key instead of the done button or the escape key.
 */
class EnterToConfirmSignPopup {

    /**
     * EventListener for this feature.
     */
    @SubscribeEvent
    fun onGuiKey(event: GuiScreenEvent.KeyboardInputEvent.Post) {
        if (!Config.pressEnterToConfirmSignQuestion) return
        if (!Utils.inSkyblock) return
        val gui = event.gui as? GuiEditSign ?: return
        if (Keyboard.getEventKey() != Keyboard.KEY_RETURN) return
        val tile = (gui as AccessorGuiEditSign).tileSign
        if (tile.signText[1].unformattedText != "^^^^^^^^^^^^^^^") return
        gui.mc.displayGuiScreen(null)
    }

}
