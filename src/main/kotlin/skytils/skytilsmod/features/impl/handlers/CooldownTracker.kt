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
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.SetActionBarEvent
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import kotlin.math.floor
import kotlin.time.ExperimentalTime

class CooldownTracker : PersistentSave(File(Skytils.modDir, "cooldowntracker.json")) {

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        cooldownReduction = 0.0
        cooldowns.clear()
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onActionBar(event: SetActionBarEvent) {
        if (!Utils.inSkyblock || !Skytils.config.itemCooldownDisplay) return
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
                        "${entry.key.replace("_", " ")}: ${elapsed.roundToPrecision(1)}s",
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
            get() = Skytils.config.itemCooldownDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

    override fun read(reader: FileReader) {
        itemCooldowns.clear()
        val obj = gson.fromJson(reader, JsonObject::class.java)
        for ((key, value) in obj.entrySet()) {
            itemCooldowns[key] = value.asDouble
        }
    }

    override fun write(writer: FileWriter) {
        val obj = JsonObject()
        for ((key, value) in itemCooldowns) {
            obj.addProperty(key, value)
        }
        gson.toJson(obj, writer)
    }

    override fun setDefault(writer: FileWriter) {
        gson.toJson(JsonObject(), writer)
    }

    companion object {
        var cooldownReduction = 0.0
        val itemCooldowns = mutableMapOf<String, Double>()
        val cooldowns = mutableMapOf<String, Long>()

        fun updateCooldownReduction() {
            val mages = DungeonListener.team.filter { it.dungeonClass == DungeonListener.DungeonClass.MAGE }
            val self = mages.find { it.playerName == mc.session.username } ?: return
            val soloMage = mages.size == 1
            cooldownReduction = ((if (soloMage) 50 else 25) + floor(self.classLevel / 2.0))
            println("Mage ${self.classLevel}, they are ${if (soloMage) "a" else "not a"} solo mage with cooldown reduction ${cooldownReduction}.")
        }
    }

}