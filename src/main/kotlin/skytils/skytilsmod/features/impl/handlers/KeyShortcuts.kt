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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import gg.essential.universal.UKeyboard
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.utils.Utils
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class KeyShortcuts : PersistentSave(File(Skytils.modDir, "keyshortcuts.json")) {
    @SubscribeEvent
    fun onInput(event: InputEvent) {
        if (!Utils.inSkyblock || shortcuts.isEmpty()) return
        val key =
            when {
                event is KeyInputEvent && Keyboard.getEventKeyState() -> Keyboard.getEventKey()
                event is InputEvent.MouseInputEvent && Mouse.getEventButtonState() -> Mouse.getEventButton() - 100
                else -> return
            }
        val modifiers = Modifiers.getBitfield(Modifiers.getPressed())
        for (s in shortcuts) {
            if (s.keyCode == 0) continue
            if (s.keyCode == key && s.modifiers == modifiers) {
                if (s.message.startsWith("/") && ClientCommandHandler.instance.executeCommand(
                        mc.thePlayer,
                        s.message
                    ) != 0
                ) continue
                Skytils.sendMessageQueue.add(s.message)
            }
        }
    }

    override fun read(reader: InputStreamReader) {
        shortcuts.clear()
        when (val data = gson.fromJson(reader, JsonElement::class.java)) {
            is JsonObject -> {
                data.entrySet().mapTo(shortcuts) { (cmd, keyCode) ->
                    KeybindShortcut(cmd, keyCode.asInt, 0)
                }
            }
            is JsonArray -> {
                data.mapTo(shortcuts) {
                    it as JsonObject
                    KeybindShortcut(it["message"].asString, it["keyCode"].asInt, it["modifiers"].asInt)
                }
            }
        }
    }

    override fun write(writer: OutputStreamWriter) {
        val arr = JsonArray()
        for (s in shortcuts) {
            val obj = JsonObject()
            obj.addProperty("message", s.message)
            obj.addProperty("keyCode", s.keyCode)
            obj.addProperty("modifiers", s.modifiers)
            arr.add(obj)
        }
        gson.toJson(arr, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        gson.toJson(JsonArray(), writer)
    }

    companion object {
        val shortcuts = HashSet<KeybindShortcut>()
    }


    data class KeybindShortcut(val message: String, val keyCode: Int, val modifiers: Int) {
        constructor(message: String, keyCode: Int, modifiers: List<Modifiers>) : this(
            message,
            keyCode,
            Modifiers.getBitfield(modifiers)
        )
    }

    enum class Modifiers(val shortName: String, val pressed: () -> Boolean) {
        CONTROL("Ctrl", { UKeyboard.isCtrlKeyDown() }),
        ALT("Alt", { UKeyboard.isAltKeyDown() }),
        SHIFT("Sft", { UKeyboard.isShiftKeyDown() });

        val bitValue by lazy {
            1 shl ordinal
        }

        fun inBitfield(field: Int) = (field and bitValue) == bitValue

        companion object {
            fun getPressed() = values().filter { it.pressed() }
            fun getBitfield(modifiers: List<Modifiers>): Int {
                var bits = 0
                for (modifier in modifiers) {
                    bits = bits or modifier.bitValue
                }
                return bits
            }

            fun fromBitfield(field: Int) = values().filter { it.inBitfield(field) }

            fun fromUCraftBitfield(modifiers: UKeyboard.Modifiers) = getBitfield(fromUCraft(modifiers))

            fun fromUCraft(modifiers: UKeyboard.Modifiers) = modifiers.run {
                mutableListOf<Modifiers>().apply {
                    if (isCtrl) add(CONTROL)
                    if (isAlt) add(ALT)
                    if (isShift) add(SHIFT)
                }
            }
        }
    }
}