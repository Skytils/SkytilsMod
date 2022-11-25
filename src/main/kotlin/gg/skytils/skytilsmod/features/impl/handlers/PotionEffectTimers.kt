/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

import gg.essential.universal.UChat
import gg.essential.universal.utils.MCClickEventAction
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.core.TickTask
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.events.impl.PacketEvent
import gg.skytils.skytilsmod.events.impl.SendChatMessageEvent
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.splitToWords
import gg.skytils.skytilsmod.utils.startsWithAny
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.init.Items
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.io.File
import java.io.Reader
import java.io.Writer

object PotionEffectTimers : PersistentSave(File(Skytils.modDir, "potionEffectTimers.json")) {
    val effectMenuTitle = Regex("^(?:\\((?<currPage>\\d+)\\/(?<lastPage>\\d+)\\) )?Active Effects\$")
    val duration =
        Regex("(?:§7Remaining: §f(?:(?<hours>\\d+):)?(?:(?<minutes>\\d+):)(?<seconds>\\d+)|(?<infinite>§cInfinite))")
    val potionEffectTimers = hashMapOf<String, PotionEffectTimer>()
    val notifications = hashMapOf<String, Long>()
    var shouldReadEffects = false
    var neededPage = 1

    @SubscribeEvent
    fun onPacket(event: PacketEvent.ReceiveEvent) {
        if (!Utils.inSkyblock || Utils.inDungeons) return
        when (event.packet) {
            is S02PacketChat -> {
                val message = event.packet.chatComponent.formattedText
                if (message.startsWithAny("§a§lBUFF! §f", "§r§aYou ate ")) {
                    TickTask(1) {
                        UMessage(
                            UTextComponent("${Skytils.prefix} §fYour potion effects have been updated! Click me to update your effect timers.").setClick(
                                MCClickEventAction.RUN_COMMAND,
                                "/skytilsupdatepotioneffects"
                            )
                        ).apply {
                            chatLineId = -693020
                        }.chat()
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!Utils.inSkyblock || Utils.inDungeons || event.phase != TickEvent.Phase.START) return

        potionEffectTimers.entries.removeAll { (id, effect) ->

            if (!effect.infinite && effect.duration == (notifications[id] ?: Long.MAX_VALUE)) {
                GuiManager.createTitle("§c${effect.potionId.splitToWords()} is about to wear off!", 20)
            }
            return@removeAll effect.tick()
        }
        markDirty<PotionEffectTimers>()
    }

    @SubscribeEvent
    fun onCommandRun(event: SendChatMessageEvent) {
        if (!event.addToChat && event.message == "/skytilsupdatepotioneffects") {
            event.isCanceled = true
            shouldReadEffects = true
            potionEffectTimers.clear()
            neededPage = 1
            Skytils.sendMessageQueue.add("/effects")
        }
    }

    @SubscribeEvent
    fun onDrawSlot(event: GuiContainerEvent.DrawSlotEvent.Post) {
        if (!shouldReadEffects || !event.chestName.endsWith("Active Effects")) return
        if (event.slot.inventory == mc.thePlayer.inventory) return
        val item = event.slot.stack ?: return
        val extraAttr = ItemUtil.getExtraAttributes(item)
        val sbId = ItemUtil.getSkyBlockItemID(extraAttr)
        if (sbId == "POTION" && extraAttr != null) {
            val potionId = extraAttr.getString("potion")
            val potionLevel = extraAttr.getInteger("potion_level")
            for (line in ItemUtil.getItemLore(item).asReversed()) {
                val match = duration.matchEntire(line) ?: continue
                val isInfinite = match.groups["infinite"]?.value != null
                val hours = match.groups["hours"]?.value?.toIntOrNull() ?: 0
                val minutes = match.groups["minutes"]?.value?.toIntOrNull() ?: 0
                val seconds = match.groups["seconds"]?.value?.toIntOrNull() ?: 0
                val durationTicks = if (isInfinite) Long.MAX_VALUE else (hours * 3600 + minutes * 60 + seconds) * 20L
                potionEffectTimers[potionId] = PotionEffectTimer(potionId, potionLevel, durationTicks, isInfinite)
                break
            }
        }
        if (event.slot.slotNumber == 53) {
            val currPage =
                effectMenuTitle.matchEntire(event.chestName)?.groups?.get("currPage")?.value?.toIntOrNull() ?: 1
            if (currPage != neededPage) return
            if (item.item == Items.arrow && item.displayName == "§aNext Page") {
                neededPage++
                TickTask(20) {
                    UChat.chat("${Skytils.prefix} §fMoving to the next page ${neededPage}...")
                    mc.playerController.windowClick(
                        event.container.windowId,
                        event.slot.slotNumber,
                        0,
                        4,
                        mc.thePlayer
                    )
                }
            } else {
                shouldReadEffects = false
                TickTask(20) {
                    mc.thePlayer.closeScreen()
                    UChat.chat("${Skytils.successPrefix} §aYour ${potionEffectTimers.size} potion effects have been updated!")
                }
            }
        }
    }

    override fun read(reader: Reader) {
        val save = json.decodeFromString<Save>(reader.readText())
        notifications.putAll(save.notifications)
        potionEffectTimers.putAll(save.potionEffectTimers)
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(Save(potionEffectTimers, notifications)))
    }

    override fun setDefault(writer: Writer) = write(writer)

    @Serializable
    data class Save(
        val potionEffectTimers: HashMap<String, PotionEffectTimer>,
        val notifications: HashMap<String, Long>
    )

    @Serializable
    data class PotionEffectTimer(
        val potionId: String,
        val potionLevel: Int,
        var duration: Long,
        val infinite: Boolean
    ) {
        fun tick(): Boolean {
            return !infinite && --duration < 0
        }
    }
}