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
package gg.skytils.skytilsmod.commands.impl

import gg.essential.universal.UChat
import gg.essential.universal.utils.MCClickEventAction
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.successPrefix
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.ItemCycle
import gg.skytils.skytilsmod.features.impl.handlers.ItemCycle.getIdentifier
import gg.skytils.skytilsmod.gui.itemcycle.ItemCycleGui
import gg.skytils.skytilsmod.utils.SkyblockIsland
import gg.skytils.skytilsmod.utils.setHoverText
import net.minecraft.command.WrongUsageException
import net.minecraft.item.ItemStack
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands
import org.incendo.cloud.annotations.Default
import java.util.*

@Commands
object ItemCycleCommand {

    private fun getHeldItemIdentifier(): ItemCycle.Cycle.ItemIdentifier {
        val heldItemStack: ItemStack = mc.thePlayer?.heldItem
            ?: throw WrongUsageException("§cYou must be holding an item.")
        return heldItemStack.getIdentifier()
            ?: throw WrongUsageException("§cCould not get identifier for the held item.")
    }

    private fun getCycle(id: UUID): ItemCycle.Cycle {
        return ItemCycle.cycles[id]
            ?: throw WrongUsageException("§cNo cycle found with ID: $id")
    }

    @Command("skytilsitemcycle|stic")
    fun openGui() {
        Skytils.displayScreen = ItemCycleGui()
    }

    @Command("skytilsitemcycle|stic create [name]")
    fun createCycle(
        @Argument(value = "name", description = "Optional name for the cycle") name: String?
    ) {
        val startItemIdentifier = getHeldItemIdentifier()
        val cycleId = UUID.randomUUID()
        val cycleName = name ?: cycleId.toString()

        val newCycle = ItemCycle.Cycle(cycleId, cycleName, hashSetOf(), startItemIdentifier)
        ItemCycle.cycles[cycleId] = newCycle
        PersistentSave.markDirty<ItemCycle>()

        UChat.chat(
            UTextComponent("$successPrefix §fCreated new cycle '$cycleName' (§e${cycleId}§f).")
                .appendSibling(
                    UTextComponent(" §7§o[Click to Add Condition]")
                        .setHoverText("§eOpens command suggestion")
                        .setClick(MCClickEventAction.SUGGEST_COMMAND, "/stic condition $cycleId add ")
                )
        )
    }

    @Command("skytilsitemcycle|stic target <id>")
    fun setTarget(
        @Argument("id", description = "The UUID of the cycle") id: UUID
    ) {
        val cycle = getCycle(id) // Throws if cycle not found
        val targetItemIdentifier = getHeldItemIdentifier() // Throws if no item held

        cycle.swapTo = targetItemIdentifier
        PersistentSave.markDirty<ItemCycle>()

        UChat.chat(
            UTextComponent("$successPrefix §fSet target item for cycle '${cycle.name}' (§e${id}§f).")
                .appendSibling(
                    UTextComponent(" §7§o[Click to Edit Conditions]")
                        .setHoverText("§eOpens command suggestion")
                        .setClick(MCClickEventAction.SUGGEST_COMMAND, "/stic condition $id ")
                )
        )
    }

    @Command("skytilsitemcycle|stic delete <id>")
    fun deleteCycle(
        @Argument("id", description = "The UUID of the cycle to delete") id: UUID
    ) {
        if (ItemCycle.cycles.remove(id) != null) {
            PersistentSave.markDirty<ItemCycle>()
            UChat.chat("$successPrefix §fRemoved cycle with ID §e$id§f.")
        } else {
            UChat.chat("$failPrefix §cNo cycle found with ID §e$id§c.")
        }
    }

    @Command("skytilsitemcycle|stic condition <id> clear")
    fun conditionClear(
        @Argument("id", description = "The UUID of the cycle") id: UUID
    ) {
        val cycle = getCycle(id)
        if (cycle.conditions.isEmpty()) {
            UChat.chat("$failPrefix §cCycle '${cycle.name}' already has no conditions.")
            return
        }
        cycle.conditions.clear()
        PersistentSave.markDirty<ItemCycle>()
        UChat.chat("$successPrefix §fCleared all conditions for cycle '${cycle.name}' (§e$id§f).")
    }

    @Command("skytilsitemcycle|stic condition <id> remove <conditionId>")
    fun conditionRemove(
        @Argument("id", description = "The UUID of the cycle") id: UUID,
        @Argument("conditionId", description = "The UUID of the condition to remove") conditionId: UUID
    ) {
        val cycle = getCycle(id)
        val removed = cycle.conditions.removeIf { it.uuid == conditionId }

        if (removed) {
            PersistentSave.markDirty<ItemCycle>()
            UChat.chat("$successPrefix §fRemoved condition §e$conditionId§f from cycle '${cycle.name}'.")
        } else {
            UChat.chat("$failPrefix §cNo condition with ID §e$conditionId§c found in cycle '${cycle.name}'.")
        }
    }

    @Command("skytilsitemcycle|stic condition <id> add island <islandModes> [negated]")
    fun conditionAddIsland(
        @Argument("id") id: UUID,
        @Argument("islandModes", description = "Comma-separated list of SkyblockIsland internal names (e.g., dynamic,combat_1)") modes: String,
        @Argument(value = "negated", description = "Whether the condition should be inverted") @Default("false") negated: Boolean
    ) {
        val cycle = getCycle(id)
        // Parse the comma-separated string into a set of SkyblockIsland objects
        val islands: HashSet<SkyblockIsland> = modes.split(',')
            .mapNotNullTo(hashSetOf()) { modeName ->
                SkyblockIsland.byMode[modeName.trim()] // Find island by internal name/mode
                    ?: run {
                        UChat.chat("$failPrefix §cUnknown island mode: '$modeName'. Skipping.")
                        null // Silently skip unknown islands, or throw error
                    }
            }

        if (islands.isEmpty()) {
            throw WrongUsageException("§cNo valid islands specified in '$modes'. See SkyblockIsland internal names.")
        }

        val condition = ItemCycle.Cycle.Condition.IslandCondition(islands, negated)
        cycle.conditions.add(condition)
        PersistentSave.markDirty<ItemCycle>()

        val negationText = if (negated) " (Not Active)" else ""
        UChat.chat(UTextComponent("$successPrefix §fAdded Island condition${negationText} for islands [${islands.joinToString { it.name }}] to cycle '${cycle.name}'.")
            .appendSibling(
                UTextComponent(" §7§o[Click to Remove]")
                    .setHoverText("§cUUID: ${condition.uuid}")
                    .setClick(MCClickEventAction.SUGGEST_COMMAND, "/stic condition $id remove ${condition.uuid}")
            ))
    }

    @Command("skytilsitemcycle|stic condition <id> add click <button> <type> [negated]")
    fun conditionAddClick(
        @Argument("id") id: UUID,
        @Argument("button", description = "Mouse button ID (0=Left, 1=Right, 2=Middle, ...)") button: Int,
        @Argument("type", description = "Minecraft Click Type") type: Int,
        @Argument(value = "negated") @Default("false") negated: Boolean
    ) {
        val cycle = getCycle(id)
        val condition = ItemCycle.Cycle.Condition.ClickCondition(button, type, negated)
        cycle.conditions.add(condition)
        PersistentSave.markDirty<ItemCycle>()

        val negationText = if (negated) " (Not Active)" else ""
        val typeDesc = when(type) { 0 -> "Press"; 1 -> "Release"; 2 -> "Held"; else -> "Unknown Type"}
        UChat.chat(
            UTextComponent("$successPrefix §fAdded Click condition${negationText} (Button $button, Type $typeDesc) to cycle '${cycle.name}'.")
            .appendSibling(
                UTextComponent(" §7§o[Click to Remove]")
                    .setHoverText("§cUUID: ${condition.uuid}")
                    .setClick(MCClickEventAction.SUGGEST_COMMAND, "/stic condition $id remove ${condition.uuid}")
            )
        )
    }

    @Command("skytilsitemcycle|stic condition <id> add item [negated]")
    fun conditionAddItem(
        @Argument("id") id: UUID,
        @Argument(value = "negated") @Default("false") negated: Boolean
    ) {
        val cycle = getCycle(id)
        val itemIdentifier = getHeldItemIdentifier() // Throws if no item held

        val condition = ItemCycle.Cycle.Condition.ItemCondition(itemIdentifier, negated)
        cycle.conditions.add(condition)
        PersistentSave.markDirty<ItemCycle>()

        val negationText = if (negated) " (Not Held)" else " (Held)"
        UChat.chat(
            UTextComponent("$successPrefix §fAdded Item condition${negationText} for item '${itemIdentifier.id}' to cycle '${cycle.name}'.")
                .appendSibling(
                    UTextComponent(" §7§o[Click to Remove]")
                        .setHoverText("§cUUID: ${condition.uuid}")
                        .setClick(MCClickEventAction.SUGGEST_COMMAND, "/stic condition $id remove ${condition.uuid}")
                )
        )
    }
}