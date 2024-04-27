/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.SkyblockIsland
import gg.skytils.skytilsmod.utils.Utils
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.io.File
import java.io.Reader
import java.io.Writer
import java.util.*

object ItemCycle : PersistentSave(File(Skytils.modDir, "itemcycle.json")) {

    val cycles = hashMapOf<UUID, Cycle>()
    private val itemLocations = hashMapOf<Cycle.ItemIdentifier, Int>()

    override fun read(reader: Reader) {
        cycles.clear()
        cycles.putAll(json.decodeFromString<Map<@Contextual UUID, Cycle>>(reader.readText()))
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString<Map<@Contextual UUID, Cycle>>(cycles))
    }

    override fun setDefault(writer: Writer) {
        writer.write(json.encodeToString(emptyMap<@Contextual UUID, Cycle>()))
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || cycles.isEmpty() || !Utils.inSkyblock || mc.thePlayer == null) return

        itemLocations.clear()
        for (slot in mc.thePlayer.inventoryContainer.inventorySlots) {
            val item = slot.stack?.getIdentifier() ?: continue

            itemLocations[item] = slot.slotNumber
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!Utils.inSkyblock || cycles.isEmpty() || event.clickType == 2 || event.container != mc.thePlayer.inventoryContainer) return

        if (event.slotId !in 36..44) return

        val clickedItem = event.slot?.stack?.getIdentifier()

        val cycle = cycles.values.find { cycle ->
            cycle.conditions.all { cond -> cond.check(event, clickedItem) }
        } ?: return

        val swapTo = itemLocations[cycle.swapTo] ?: return

        mc.playerController.windowClick(event.container.windowId, swapTo, event.slotId - 36, 2, mc.thePlayer)

        event.isCanceled = true
    }

    fun ItemStack?.getIdentifier() = ItemUtil.getExtraAttributes(this).let {
        if (it?.hasKey("uuid") == true) {
            Cycle.ItemIdentifier(it.getString("uuid"), Cycle.ItemIdentifier.Type.SKYBLOCK_UUID)
        } else {
            val sbId = ItemUtil.getSkyBlockItemID(it)
            when {
                sbId != null -> {
                    Cycle.ItemIdentifier(sbId, Cycle.ItemIdentifier.Type.SKYBLOCK_ID)
                }

                this != null -> {
                    Cycle.ItemIdentifier(this.unlocalizedName, Cycle.ItemIdentifier.Type.VANILLA_ID)
                }

                else -> null
            }
        }
    }

    @Serializable
    data class Cycle(
        val uuid: @Contextual UUID, var name: String, val conditions: MutableSet<Condition>, var swapTo: ItemIdentifier
    ) {
        @Serializable
        data class ItemIdentifier(
            val id: String, val type: Type
        ) {
            enum class Type {
                SKYBLOCK_ID, SKYBLOCK_UUID, VANILLA_ID
            }
        }

        @Serializable
        sealed class Condition(val uuid: @Contextual UUID = UUID.randomUUID()) {
            abstract fun check(event: GuiContainerEvent.SlotClickEvent, clickedItem: ItemIdentifier?): Boolean

            abstract fun displayText(): String

            @Serializable
            @SerialName("IslandCondition")
            class IslandCondition(var islands: Set<@Serializable(with = SkyblockIsland.ModeSerializer::class) SkyblockIsland>, var negated: Boolean = false) : Condition() {
                override fun check(event: GuiContainerEvent.SlotClickEvent, clickedItem: ItemIdentifier?): Boolean =
                    islands.any { SBInfo.mode == it.mode } == !negated

                override fun displayText(): String = "${if (negated) "Not " else ""}${islands.joinToString(", ")}"
            }

            @Serializable
            @SerialName("ClickCondition")
            class ClickCondition(var clickedButton: Int, var clickType: Int, var negated: Boolean = false) :
                Condition() {
                override fun check(event: GuiContainerEvent.SlotClickEvent, clickedItem: ItemIdentifier?): Boolean =
                    ((clickedButton == -1000 || event.clickedButton == clickedButton) && (clickType == -1000 || event.clickType == clickType)) == !negated

                override fun displayText(): String =
                    "${if (negated) "Not " else ""} button $clickedButton, type $clickType"
            }

            @Serializable
            @SerialName("ItemCondition")
            class ItemCondition(var item: ItemIdentifier, var negated: Boolean = false) : Condition() {
                override fun check(event: GuiContainerEvent.SlotClickEvent, clickedItem: ItemIdentifier?): Boolean {
                    return (clickedItem == item) == !negated
                }

                override fun displayText(): String = "${if (negated) "Not " else ""}${item.type}: ${item.id}"
            }

            @Serializable
            @SerialName("SlotCondition")
            class SlotCondition(var slotId: Int, var negated: Boolean = false) :
                Condition() {
                override fun check(event: GuiContainerEvent.SlotClickEvent, clickedItem: ItemIdentifier?): Boolean =
                    event.slotId == slotId == !negated

                override fun displayText(): String =
                    "${if (negated) "Not " else ""} slot $slotId"
            }
        }
    }
}