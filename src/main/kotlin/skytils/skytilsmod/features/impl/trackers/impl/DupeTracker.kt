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

package skytils.skytilsmod.features.impl.trackers.impl

import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.*
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.impl.GuiContainerEvent
import skytils.skytilsmod.events.impl.MainReceivePacketEvent
import skytils.skytilsmod.features.impl.trackers.Tracker
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.RenderUtil.highlight
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import kotlin.concurrent.fixedRateTimer

object DupeTracker : Tracker("duped_items") {
    val dupedUUIDs = hashSetOf<String>()
    val dirtyUUIDs = hashSetOf<String>()
    val dupeChecking = hashMapOf<String, Int>()
    var inAuctionBrowser = false


    @SubscribeEvent
    fun onWindowChange(event: GuiOpenEvent) {
        if (event.gui is GuiContainer) return
        inAuctionBrowser = false
        dupeChecking.clear()
        printDevMessage("Cleared dupe check", "dupecheck")
        printDevMessage("Cleared auction", "dupecheck")
        if (event.gui is GuiChat && DevTools.getToggle("dupecheck")) dupedUUIDs.clear()
    }

    @SubscribeEvent
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
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
                        val prev = dupeChecking.putIfAbsent(uuid, slotId) ?: return
                        if (prev != slotId) {
                            printDevMessage("Dupe set ${item.displayName}, $uuid $slotId", "dupecheck")
                            dupedUUIDs.add(uuid)
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
                            val prev = dupeChecking.putIfAbsent(uuid, i) ?: continue
                            if (prev != i) {
                                printDevMessage("Dupe window ${stack.displayName}, $uuid $i", "dupecheck")
                                dupedUUIDs.add(uuid)
                                dirty = true
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onSlotDraw(event: GuiContainerEvent.DrawSlotEvent.Post) {
        if (!Utils.inSkyblock || !Skytils.config.dupeTracker) return
        val uuid = event.slot.stack.getUUID() ?: return
        if (dupedUUIDs.contains(uuid) || dirtyUUIDs.contains(uuid)) {
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
            if (dupedUUIDs.contains(uuid)) {
                event.toolTip.add("§c§lDUPED ITEM")
            }
            if (dirtyUUIDs.contains(uuid)) {
                event.toolTip.add("§c§lDIRTY ITEM")
            }
        }
        val origin = extraAttrib.getString("originTag")
        when (origin) {
            "ITEM_STASH" -> event.toolTip.add("§c§lStashed item: possibly duped")
            "ITEM_COMMAND", "ITEM_MENU" -> event.toolTip.add("§c§lSpawned by admin lol")
            else -> if (Skytils.config.showOrigin) event.toolTip.add("§7§lOrigin: $origin")
        }
    }

    override fun resetLoot() {
        dupedUUIDs.clear()
        dirtyUUIDs.clear()
    }

    override fun read(reader: InputStreamReader) {
        dupedUUIDs.addAll(gson.fromJson<List<String>>(reader, List::class.java))
    }

    override fun write(writer: OutputStreamWriter) {
        gson.toJson(dupedUUIDs, Set::class.java, writer)
    }

    override fun setDefault(writer: OutputStreamWriter) {
        writer.write("[]")
    }

    private fun ItemStack?.getUUID(): String? = ItemUtil.getExtraAttributes(this)?.getString("uuid")?.ifEmpty { null }

    init {
        fixedRateTimer(name = "Skytils-FetchDupeData", period = 7 * 60 * 1000L) {
            if (Utils.skyblock && Skytils.config.dupeTracker) {
                APIUtil.getArrayResponse("https://${Skytils.domain}/api/auctions/dupeditems").apply {
                    Utils.checkThreadAndQueue {
                        dupedUUIDs.addAll(map { it.asString })
                    }
                }
                if (Skytils.config.markDirtyItems) {
                    APIUtil.getArrayResponse("https://${Skytils.domain}/api/auctions/dirtyitems").apply {
                        Utils.checkThreadAndQueue {
                            dirtyUUIDs.addAll(map { it.asString })
                        }
                    }
                }
            }
        }
    }
}
