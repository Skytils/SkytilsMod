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
package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.events.impl.CheckRenderEntityEvent
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.utils.ItemUtil.getItemLore
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PetFeatures {
    private val SUMMON_PATTERN = Regex("§r§aYou summoned your §r(?<pet>.+)§r§a!§r")
    private val AUTOPET_PATTERN =
        Regex("§cAutopet §eequipped your §7\\[Lvl (?<level>\\d+)] (?<pet>.+)§e! §a§lVIEW RULE§r")
    var lastPet: String? = null


    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
        if (!Utils.inSkyblock) return
        if (event.entity is EntityArmorStand) {
            val entity = event.entity
            val name = entity.customNameTag
            if (Skytils.config.hidePetNametags && name.startsWith("§8[§7Lv") && name.contains(
                    "'s "
                ) && !name.contains("❤") && !name.contains("Hit")
            ) {
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type.toInt() == 2) return
        val message = event.message.formattedText
        if (message.startsWith("§r§aYou despawned your §r§")) {
            lastPet = null
        } else if (message.startsWith("§r§aYou summoned your §r")) {
            SUMMON_PATTERN.find(message)?.groups?.get("pet")?.value?.stripControlCodes().let {
                if (it == null) UChat.chat("$failPrefix §cSkytils failed to capture equipped pet.")
                else lastPet = it
            }
        } else if (message.startsWith("§cAutopet §eequipped your §7[Lvl ")) {
            AUTOPET_PATTERN.find(message)?.groups?.get("pet")?.value?.stripControlCodes().let {
                if (it == null) UChat.chat("$failPrefix §cSkytils failed to capture equipped pet.")
                else lastPet = it
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    fun onDraw(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inSkyblock || event.container !is ContainerChest) return
        if (Skytils.config.highlightActivePet && (SBInfo.lastOpenContainerName?.endsWith(") Pets") == true || SBInfo.lastOpenContainerName == "Pets") && event.slot.hasStack && event.slot.slotNumber in 10..43) {
            val item = event.slot.stack
            for (line in getItemLore(item)) {
                if (line.startsWith("§7§cClick to despawn")) {
                    GlStateManager.translate(0f, 0f, 3f)
                    event.slot highlight Skytils.config.activePetColor
                    GlStateManager.translate(0f, 0f, -3f)
                    break
                }
            }
        }
    }
}