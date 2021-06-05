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

import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import kotlin.math.floor
import kotlin.time.ExperimentalTime

object CooldownTracker {

    var cooldownReduction = 0.0
    val itemCooldowns = mutableMapOf<String, Double>()
    val cooldowns = mutableMapOf<String, Long>()

    @SubscribeEvent
    fun onEvent(event: Event) {
        if (!Utils.inSkyblock) return
        event.apply {
            when (this) {
                is WorldEvent.Load -> {
                    cooldownReduction = 0.0
                    cooldowns.clear()
                }
                is TickEvent.ClientTickEvent -> {
                    if (phase != TickEvent.Phase.START) return
                    cooldowns.entries.removeIf {
                        it.value <= System.currentTimeMillis()
                    }
                }
                is PacketEvent.SendEvent -> {
                    when (packet) {
                        is C08PacketPlayerBlockPlacement -> {
                            val itemId = ItemUtil.getSkyBlockItemID(packet.stack) ?: return
                            val itemCooldown = getCooldownFromItem(packet.stack)
                            if (itemCooldown == 0.0) return
                            cooldowns.computeIfAbsent(itemId) {
                                System.currentTimeMillis() + ((1 - cooldownReduction) * itemCooldown * 1000).toLong()
                            }
                        }
                    }
                }
            }
        }
    }

    fun updateCooldownReduction() {
        val mages = DungeonListener.team.filter { it.dungeonClass == DungeonListener.DungeonClass.MAGE }
        val self = mages.find { it.playerName == mc.session.username } ?: return
        val soloMage = mages.size == 1
        cooldownReduction = ((if (soloMage) 50 else 25) + floor(self.classLevel / 2.0))
        println("Mage ${self.classLevel}, they are ${if (soloMage) "a" else "not a"} solo mage with cooldown reduction ${cooldownReduction}.")
    }

    fun getCooldownFromItem(item: ItemStack): Double {
        val itemId = ItemUtil.getSkyBlockItemID(item) ?: return 0.0
        if (itemCooldowns.containsKey(itemId)) return itemCooldowns[itemId]!!
        var foundRightClickAbility = false
        var cooldown: Double? = null
        for (line in ItemUtil.getItemLore(item)) {
            if (!foundRightClickAbility) {
                if (line.startsWith("§6Ability: ") && line.endsWith("§e§lRIGHT CLICK")) {
                    foundRightClickAbility = true
                }
            } else {
                if (line.startsWith("§8Cooldown: §a")) {
                    cooldown = line.substringAfter("§a").substringBefore("s").toDoubleOrNull()
                    break
                }
            }
        }
        if (cooldown != null) {
            itemCooldowns[itemId] = cooldown
        }
        return cooldown ?: 0.0
    }

    init {
        CooldownDisplayElement()
    }

    class CooldownDisplayElement : GuiElement("Item Cooldown Display", FloatPair(10, 10)) {

        @OptIn(ExperimentalTime::class)
        override fun render() {
            if (Utils.inSkyblock && toggled) {
                for ((i, entry) in (cooldowns.entries).withIndex()) {
                    val elapsed = (entry.value - System.currentTimeMillis()) / 1000.0
                    ScreenRenderer.fontRenderer.drawString(
                        "${entry.key.replace("_", " ")}: ${elapsed}s",
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
                "Ice Spray Wand: 5s",
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
            get() = ScreenRenderer.fontRenderer.getStringWidth("Ice Spray Wand: 5s")

        override val toggled: Boolean
            get() = Skytils.config.itemCooldownDisplay

        init {
            Skytils.guiManager.registerElement(this)
        }
    }

}