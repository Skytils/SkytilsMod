package gg.skytils.skytilsmod.features.impl.handlers

import gg.essential.universal.UKeyboard
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.KeyboardInputEvent
import gg.skytils.event.impl.play.MouseInputEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.json
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.utils.SBInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.minecraft.client.MinecraftClient
import java.io.Reader
import java.io.Writer

object KeyShortcuts: EventSubscriber, PersistentSave(Skytils.modDir.resolve("keyshortcuts.json")) {
    val shortcuts = mutableSetOf<KeybindShortcut>()

    override fun setup() {
        register(::onMouseInput)
        register(::onKeyInput)
    }

    fun onMouseInput(event: MouseInputEvent) {
        if (!SBInfo.skyblockState.getUntracked() || shortcuts.isEmpty() || mc.currentScreen != null) return
        handleInput(event.button - 100)
    }

    fun onKeyInput(event: KeyboardInputEvent) {
        if (!SBInfo.skyblockState.getUntracked() || shortcuts.isEmpty() || mc.currentScreen != null) return
        handleInput(event.keyCode)
    }

    private fun handleInput(keyCode: Int) {
        val modifiers = Modifiers.getBitfield(Modifiers.getPressed())
        for (s in shortcuts) {
            if (s.keyCode == 0 || !s.enabled || s.keyCode == UKeyboard.KEY_NONE) continue
            if (s.keyCode == keyCode && s.modifiers == modifiers) {
                val command = s.message.drop(if (s.message.startsWith('/')) 1 else 0)
                MinecraftClient.getInstance().player?.networkHandler?.sendChatCommand(command)
                break // only run one shortcut per input
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

    @Serializable
    data class KeybindShortcut(val message: String, val keyCode: Int, val modifiers: Int = 0, val enabled: Boolean = true) {
        constructor(message: String, keyCode: Int, modifiers: List<Modifiers>, enabled: Boolean) : this(
            message,
            keyCode,
            Modifiers.getBitfield(modifiers),
            enabled
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
            fun getBitfield(modifiers: List<Modifiers>) =
                modifiers.fold(0) { acc, modifier ->
                    acc or modifier.bitValue
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