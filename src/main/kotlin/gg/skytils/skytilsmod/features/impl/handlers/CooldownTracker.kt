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

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.events.impl.SetActionBarEvent
import gg.skytils.skytilsmod.listeners.DungeonListener
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.hylin.skyblock.dungeons.DungeonClass
import java.io.File
import java.io.Reader
import java.io.Writer
import kotlin.math.floor

object CooldownTracker : PersistentSave(File(Skytils.modDir, "cooldowntracker.json")) {

    var cooldownReduction = 0.0
    val itemCooldowns = hashMapOf<String, Double>()
    val cooldowns = hashMapOf<String, Long>()

    fun updateCooldownReduction() {
        val mages = DungeonListener.team.values.filter { it.dungeonClass == DungeonClass.MAGE }
        val self = mages.find { it.playerName == mc.session.username } ?: return
        val soloMage = mages.size == 1
        cooldownReduction = ((if (soloMage) 50 else 25) + floor(self.classLevel / 2.0))
        println("Mage ${self.classLevel}, they are ${if (soloMage) "a" else "not a"} solo mage with cooldown reduction ${cooldownReduction}.")
    }

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

    class CooldownDisplayElement : GuiElement("Item Cooldown Display", x = 10, y = 10) {

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
                        textShadow
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
                textShadow
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

    override fun read(reader: Reader) {
        itemCooldowns.clear()
        itemCooldowns.putAll(json.decodeFromString<Map<String, Double>>(reader.readText()))
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(itemCooldowns))
    }

    override fun setDefault(writer: Writer) {
        writer.write("{}")
    }

    data class CooldownThing(var name: String, var seconds: Double, var mageBypass: Boolean)
}