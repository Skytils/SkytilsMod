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

package gg.skytils.skytilsmod.features.impl.trackers.impl

import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.screen.GuiContainerPostDrawSlotEvent
import gg.skytils.event.impl.screen.ScreenOpenEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod._event.MainThreadPacketReceiveEvent
import gg.skytils.skytilsmod.features.impl.trackers.Tracker
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.*
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.Reader
import java.io.Writer

object DupeTracker : Tracker("duped_items"), EventSubscriber {
    val dupedUUIDs = hashSetOf<IdentifiableItem>()
    val dupeChecking = hashMapOf<IdentifiableItem, Int>()
    var inAuctionBrowser = false

    override fun setup() {
        register(::onWindowChange)
        register(::onPacket)
        register(::onSlotDraw)
    }

    fun onWindowChange(event: ScreenOpenEvent) {
        if (event.screen is GuiContainer) return
        inAuctionBrowser = false
        dupeChecking.clear()
        printDevMessage("Cleared dupe check", "dupecheck")
        printDevMessage("Cleared auction", "dupecheck")
        if (event.screen is GuiChat && DevTools.getToggle("dupecheck")) dupedUUIDs.clear()
    }

    fun onPacket(event: MainThreadPacketReceiveEvent<*>) {
        when (val packet = event.packet) {
            is S01PacketJoinGame -> {
                inAuctionBrowser = false
                dupeChecking.clear()
                printDevMessage("Cleared dupe check", "dupecheck")
                printDevMessage("Cleared auction", "dupecheck")
            }

            is S2DPacketOpenWindow -> {
                inAuctionBrowser =
                    Utils.inSkyblock && packet.slotCount == 54 && packet.windowTitle.unformattedText.run {
                        this == "Auctions Browser" || startsWith(
                            "Auctions: \""
                        )
                    }
                dupeChecking.clear()
                printDevMessage("Cleared dupe check", "dupecheck")
                printDevMessage("Is auction $inAuctionBrowser", "dupecheck")
            }

            is S2EPacketCloseWindow -> {
                inAuctionBrowser = false
                dupeChecking.clear()
                printDevMessage("Cleared dupe check", "dupecheck")
                printDevMessage("Cleared auction", "dupecheck")
            }

            is S2FPacketSetSlot -> {
                if (!inAuctionBrowser || !Skytils.config.dupeTracker) return
                val windowId = packet.func_149175_c()
                if (windowId != 0 && windowId == mc.thePlayer?.openContainer?.windowId) {
                    val item = packet.func_149174_e()
                    val slotId = packet.func_149173_d()
                    if (slotId < 54 && item?.stackSize == 1) {
                        val uuid = item.getUUID() ?: return
                        val itemId = ItemUtil.getSkyBlockItemID(item) ?: return
                        val idItem = IdentifiableItem(itemId, uuid)
                        val prev = dupeChecking.putIfAbsent(idItem, slotId) ?: return
                        if (prev != slotId) {
                            printDevMessage("Dupe set ${item.displayName}, $idItem $slotId", "dupecheck")
                            dupedUUIDs.add(idItem)
                            dirty = true
                        }
                    }
                }
            }

            is S30PacketWindowItems -> {
                if (!inAuctionBrowser || !Skytils.config.dupeTracker) return
                val windowId = packet.func_148911_c()
                if (windowId != 0 && windowId == mc.thePlayer?.openContainer?.windowId) {
                    for ((i, stack) in packet.itemStacks.withIndex()) {
                        if (i < 54 && stack?.stackSize == 1) {
                            val uuid = stack.getUUID() ?: continue
                            val itemId = ItemUtil.getSkyBlockItemID(stack) ?: continue
                            val idItem = IdentifiableItem(itemId, uuid)
                            val prev = dupeChecking.putIfAbsent(idItem, i) ?: continue
                            if (prev != i) {
                                printDevMessage("Dupe window ${stack.displayName}, $idItem $i", "dupecheck")
                                dupedUUIDs.add(idItem)
                                dirty = true
                            }
                        }
                    }
                }
            }
        }
    }

    fun onSlotDraw(event: GuiContainerPostDrawSlotEvent) {
        if (!Utils.inSkyblock || !Skytils.config.dupeTracker) return
        val stack = event.slot.stack
        val uuid = stack.getUUID() ?: return
        val itemId = ItemUtil.getSkyBlockItemID(stack) ?: return
        val idItem = IdentifiableItem(itemId, uuid)
        if (dupedUUIDs.contains(idItem)) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(0f, 0f, 299f)
            event.slot highlight Skytils.config.dupeTrackerOverlayColor
            GlStateManager.popMatrix()
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inSkyblock) return
        val extraAttrib = ItemUtil.getExtraAttributes(event.itemStack) ?: return
        if (Skytils.config.dupeTracker) {
            val uuid = extraAttrib.getString("uuid")
            val itemId = ItemUtil.getSkyBlockItemID(extraAttrib) ?: return
            val idItem = IdentifiableItem(itemId, uuid)
            if (dupedUUIDs.contains(idItem)) {
                event.toolTip.add("§c§lDUPED ITEM")
            }
        }
        when (val origin = extraAttrib.getString("originTag")) {
            "" -> return
            "ITEM_STASH" -> event.toolTip.add("§c§lStashed item: possibly duped")
            "ITEM_COMMAND", "ITEM_MENU" -> event.toolTip.add("§c§lSpawned by admin lol")
            else -> if (Skytils.config.showOrigin) event.toolTip.add(
                "§7§lOrigin: ${
                    origin.splitToWords()
                }"
            )
        }
    }

    override fun resetLoot() {
        dupedUUIDs.clear()
    }

    override fun read(reader: Reader) {
        dupedUUIDs.addAll(json.decodeFromString<List<IdentifiableItem>>(reader.readText()))
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(dupedUUIDs))
    }

    override fun setDefault(writer: Writer) {
        writer.write("[]")
    }

    private fun ItemStack?.getUUID(): String? = ItemUtil.getExtraAttributes(this)?.getString("uuid")?.ifEmpty { null }

/*    init {
        fixedRateTimer(name = "Skytils-FetchDupeData", period = 7 * 60 * 1000L) {
            if (Utils.inSkyblock && Skytils.config.dupeTracker) {
                Skytils.IO.launch {
                    client.get("https://${Skytils.domain}/api/auctions/dupeditems").body<List<IdentifiableItem>>()
                        .apply {
                            Utils.checkThreadAndQueue {
                                dupedUUIDs.addAll(this)
                            }
                        }
                }
            }
        }
    }*/
}

@Serializable
data class IdentifiableItem(
    val itemId: String,
    val uuid: String
)
