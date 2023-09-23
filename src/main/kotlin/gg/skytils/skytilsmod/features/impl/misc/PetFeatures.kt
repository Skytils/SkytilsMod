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
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.events.impl.CheckRenderEntityEvent
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent.SendEvent
import gg.skytils.skytilsmod.events.impl.SendChatMessageEvent
import gg.skytils.skytilsmod.utils.ItemUtil.getItemLore
import gg.skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.setHoverText
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.event.ClickEvent
import net.minecraft.inventory.ContainerChest
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PetFeatures {
    val petItems = HashMap<String, Boolean>()
    private val SUMMON_PATTERN = Regex("§r§aYou summoned your §r(?<pet>.+)§r§a!§r")
    private val AUTOPET_PATTERN =
        Regex("§cAutopet §eequipped your §7\\[Lvl (?<level>\\d+)] (?<pet>.+)§e! §a§lVIEW RULE§r")
    private var lastPetConfirmation: Long = 0
    private var lastPetLockNotif: Long = 0
    var lastPet: String? = null

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
        if (Skytils.config.highlightActivePet && (SBInfo.lastOpenContainerName?.startsWith("Pets") == true) && event.slot.hasStack && event.slot.slotNumber in 10..43) {
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

    @SubscribeEvent
    fun onSendPacket(event: SendEvent) {
        if (!Utils.inSkyblock) return
        if (Skytils.config.petItemConfirmation && (event.packet is C02PacketUseEntity || event.packet is C08PacketPlayerBlockPlacement)) {
            val item = mc.thePlayer.heldItem ?: return
            val itemId = getSkyBlockItemID(item) ?: return
            if (itemId !in petItems) {
                val isPetItem =
                    (itemId.contains("PET_ITEM") && !itemId.endsWith("_DROP")) || itemId.endsWith("CARROT_CANDY") || itemId.startsWith(
                        "PET_SKIN_"
                    ) || getItemLore(item).asReversed().any {
                        it.contains("PET ITEM")
                    }
                petItems[itemId] = isPetItem
            }
            if (petItems[itemId] == true) {
                if (System.currentTimeMillis() - lastPetConfirmation > 5000) {
                    event.isCanceled = true
                    if (System.currentTimeMillis() - lastPetLockNotif > 10000) {
                        lastPetLockNotif = System.currentTimeMillis()
                        UChat.chat(
                            UTextComponent("$prefix §cSkytils stopped you from using that pet item! §6Click this message to disable the lock.").setHoverText(
                                "Click to disable the pet item lock for 5 seconds."
                            ).setClick(ClickEvent.Action.RUN_COMMAND, "/disableskytilspetitemlock")
                        )
                    }
                } else {
                    lastPetConfirmation = 0
                }
            }
        }
    }

    @SubscribeEvent
    fun onSendChatMessage(event: SendChatMessageEvent) {
        if (event.message == "/disableskytilspetitemlock" && !event.addToChat) {
            lastPetConfirmation = System.currentTimeMillis()
            UChat.chat("$prefix §aYou may now apply pet items for 5 seconds.")
            event.isCanceled = true
        }
    }
}