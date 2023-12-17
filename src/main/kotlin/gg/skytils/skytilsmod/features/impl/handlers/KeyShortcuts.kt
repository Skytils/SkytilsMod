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
package gg.skytils.skytilsmod.features.impl.handlers

import gg.essential.universal.UKeyboard
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.utils.Utils
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.io.File
import java.io.Reader
import java.io.Writer

object KeyShortcuts : PersistentSave(File(Skytils.modDir, "keyshortcuts.json")) {
    val shortcuts = HashSet<KeybindShortcut>()

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

    override fun read(reader: Reader) {
        shortcuts.clear()
        when (val data = json.decodeFromString<JsonElement>(reader.readText())) {
            is JsonArray -> {
                shortcuts.addAll(json.decodeFromJsonElement<List<KeybindShortcut>>(data))
            }

            is JsonObject -> {
                json.decodeFromJsonElement<Map<String, Int>>(data).mapTo(shortcuts) { (cmd, keyCode) ->
                    KeybindShortcut(cmd, keyCode)
                }
            }

            else -> error("Invalid shortcuts file")
        }
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(shortcuts))
    }

    override fun setDefault(writer: Writer) {
        writer.write("[]")
    }

    @Serializable
    data class KeybindShortcut(val message: String, val keyCode: Int, val modifiers: Int = 0) {
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
            fun getPressed() = entries.filter { it.pressed() }
            fun getBitfield(modifiers: List<Modifiers>): Int {
                var bits = 0
                for (modifier in modifiers) {
                    bits = bits or modifier.bitValue
                }
                return bits
            }

            fun fromBitfield(field: Int) = entries.filter { it.inBitfield(field) }

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