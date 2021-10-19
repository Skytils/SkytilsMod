/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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
package skytils.skytilsmod.features.impl.handlers

import com.google.gson.JsonObject
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.utils.Utils
import java.io.*

class KeyShortcuts : PersistentSave(File(Skytils.modDir, "keyshortcuts.json")) {
    @SubscribeEvent
    fun onInput(event: InputEvent) {
        if (!Utils.inSkyblock) return
        for ((message, code) in shortcuts) {
            if (code == 0) continue
            val isDown =
                if (code > 0) event is KeyInputEvent && Keyboard.getEventKeyState() && Keyboard.getEventKey() == code else event is InputEvent.MouseInputEvent && Mouse.getEventButtonState() && Mouse.getEventButton() == code + 100
            if (isDown) {
                if (message.startsWith("/") && ClientCommandHandler.instance.executeCommand(
                        mc.thePlayer,
                        message
                    ) != 0
                ) break
                Skytils.sendMessageQueue.add(message)
            }
        }
    }

    override fun read(reader: InputStreamReader) {
        shortcuts.clear()
        val data = gson.fromJson(reader, JsonObject::class.java)
        for ((key, value) in data.entrySet()) {
            shortcuts[key] = value.asInt
        }
    }

    override fun write(writer: OutputStreamWriter) {
        val obj = JsonObject()
        for ((key, value) in shortcuts) {
            obj.addProperty(key, value)
        }
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        gson.toJson(JsonObject(), writer)
    }

    companion object {
        val shortcuts = HashMap<String, Int>()
    }
}