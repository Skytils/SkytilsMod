/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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

package sharttils.sharttilsmod.features.impl.handlers

import com.google.gson.JsonObject
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import sharttils.hylin.skyblock.dungeons.DungeonClass
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.Sharttils.Companion.mc
import sharttils.sharttilsmod.core.PersistentSave
import sharttils.sharttilsmod.core.structure.FloatPair
import sharttils.sharttilsmod.core.structure.GuiElement
import sharttils.sharttilsmod.events.impl.SetActionBarEvent
import sharttils.sharttilsmod.listeners.DungeonListener
import sharttils.sharttilsmod.utils.Utils
import sharttils.sharttilsmod.utils.graphics.ScreenRenderer
import sharttils.sharttilsmod.utils.graphics.SmartFontRenderer
import sharttils.sharttilsmod.utils.graphics.colors.CommonColors
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlin.math.floor
import kotlin.time.ExperimentalTime

class CooldownTracker : PersistentSave(File(Sharttils.modDir, "cooldowntracker.json")) {

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        cooldownReduction = 0.0
        cooldowns.clear()
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onActionBar(event: SetActionBarEvent) {
        if (!Utils.inSkyblock || !Sharttils.config.itemCooldownDisplay) return
        event.apply {
            if (message.contains("§b-") && message.contains(" Mana (§6")) {
                val itemId = message.substringAfter(" Mana (§6").substringBefore("§b)")
                val itemCooldown = itemCooldowns[itemId] ?: return
                cooldowns.computeIfAbsent(itemId) {
                    System.currentTimeMillis() + ((100 - cooldownReduction) / 100 * (itemCooldown) * 1000).toLong()
                }
            }
        }
    }

    init {
        CooldownDisplayElement()
    }

    class CooldownDisplayElement : GuiElement("Item Cooldown Display", FloatPair(10, 10)) {

        @OptIn(ExperimentalTime::class)
        override fun render() {
            if (Utils.inSkyblock && toggled) {
                cooldowns.entries.removeAll {
                    it.value <= System.currentTimeMillis()
                }
                for ((i, entry) in (cooldowns.entries).withIndex()) {
                    val elapsed = (entry.value - System.currentTimeMillis()) / 1000.0
                    ScreenRenderer.fontRenderer.drawString(
                        "${entry.key.replace("_", " ")}: ${"%.1f".format(elapsed)}s",
                        0f,
                        (ScreenRenderer.fontRenderer.FONT_HEIGHT * i).toFloat(),
                        CommonColors.ORANGE,
                        SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                        SmartFontRenderer.TextShadow.NORMAL
                    )
                }
            }
        }

        override fun demoRender() {
            ScreenRenderer.fontRenderer.drawString(
                "Ice Spray: 5s",
                0f,
                0f,
                CommonColors.ORANGE,
                SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                SmartFontRenderer.TextShadow.NORMAL
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("Ice Spray: 5s")

        override val toggled: Boolean
            get() = Sharttils.config.itemCooldownDisplay

        init {
            Sharttils.guiManager.registerElement(this)
        }
    }

    override fun read(reader: InputStreamReader) {
        itemCooldowns.clear()
        val obj = gson.fromJson(reader, JsonObject::class.java)
        for ((key, value) in obj.entrySet()) {
            itemCooldowns[key] = value.asDouble
        }
    }

    override fun write(writer: OutputStreamWriter) {
        val obj = JsonObject()
        for ((key, value) in itemCooldowns) {
            obj.addProperty(key, value)
        }
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        gson.toJson(JsonObject(), writer)
    }

    companion object {
        var cooldownReduction = 0.0
        val itemCooldowns = mutableMapOf<String, Double>()
        val cooldowns = mutableMapOf<String, Long>()

        fun updateCooldownReduction() {
            val mages = DungeonListener.team.values.filter { it.dungeonClass == DungeonClass.MAGE }
            val self = mages.find { it.playerName == mc.session.username } ?: return
            val soloMage = mages.size == 1
            cooldownReduction = ((if (soloMage) 50 else 25) + floor(self.classLevel / 2.0))
            println("Mage ${self.classLevel}, they are ${if (soloMage) "a" else "not a"} solo mage with cooldown reduction ${cooldownReduction}.")
        }
    }

    data class CooldownThing(var name: String, var seconds: Double, var mageBypass: Boolean)
}