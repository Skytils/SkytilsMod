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
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.Skytils.Companion.successPrefix
import gg.skytils.skytilsmod.features.impl.mining.CHWaypoints
import gg.skytils.skytilsmod.utils.append
import gg.skytils.skytilsmod.utils.setHoverText
import net.minecraft.event.ClickEvent
import net.minecraft.util.BlockPos
import net.minecraft.util.IChatComponent
import org.incendo.cloud.annotation.specifier.Quoted
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands

@Commands
object HollowWaypointCommand {
    private fun checkEnabled() {
        if (!Skytils.config.crystalHollowWaypoints) {
            UChat.chat("$prefix §cCrystal Hollows Waypoints were disabled, but running this command enabled them for you.")
            Skytils.config.crystalHollowWaypoints = true
            Skytils.config.markDirty()
        }
    }

    @Command("skytilshollowwaypoint|sthw help")
    fun showHelp() {
        checkEnabled()
        UChat.chat(
            "$prefix §e/sthw ➔ Shows all waypoints\n" +
                    "§e/sthw set \"name\" ➔ Sets waypoint at current location\n" +
                    "§e/sthw set \"name\" x y z ➔ Sets waypoint at specified location\n" +
                    "§e/sthw remove \"name\" ➔ Remove the specified waypoint\n" +
                    "§e/sthw clear ➔ Removes all waypoints"
        )
    }

    @Command("skytilshollowwaypoint|sthw")
    fun showSummary() {
        checkEnabled()
        val message = UMessage("$prefix §eWaypoints:\n")
        for (loc in CHWaypoints.CrystalHollowsMap.Locations.entries) {
            if (!loc.loc.exists()) continue
            message.append("${loc.displayName} ")
            message.append(copyMessage("${loc.cleanName}: ${loc.loc}"))
            message.append(removeMessage(loc.id))
        }
        for ((key, value) in CHWaypoints.waypoints) {
            message.append("§e$key ")
            message.append(copyMessage("$key: ${value.x} ${value.y} ${value.z}"))
            message.append(removeMessage(key))
        }
        message.append("§eFor more info do /sthw help")
        message.chat()
    }

    @Command("skytilshollowwaypoint|sthw set|add <name>")
    fun setWaypoint(
        @Quoted
        @Argument("name")
        name: String
    ) {
        checkEnabled()
        val loc = CHWaypoints.CrystalHollowsMap.Locations.entries.find { it.id == name }?.loc
        if (loc != null) {
            loc.locX = (mc.thePlayer.posX - 200).coerceIn(0.0, 624.0)
            loc.locY = mc.thePlayer.posY
            loc.locZ = (mc.thePlayer.posZ - 200).coerceIn(0.0, 624.0)
            UChat.chat("$successPrefix §aSuccessfully set location waypoint $name to your current location.")
        } else {
            CHWaypoints.waypoints[name] = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            UChat.chat("$successPrefix §aSuccessfully set waypoint $name to your current location.")
        }
    }

    @Command("skytilshollowwaypoint|sthw set|add <name> <x> <y> <z>")
    fun setWaypoint(
        @Quoted
        @Argument("name")
        name: String,
        @Argument("x")
        x: Double,
        @Argument("y")
        y: Double,
        @Argument("z")
        z: Double,
    ) {
        checkEnabled()
        val loc = CHWaypoints.CrystalHollowsMap.Locations.entries.find { it.id == name }?.loc
        if (loc != null) {
            loc.locX = (x - 200).coerceIn(0.0, 624.0)
            loc.locY = y
            loc.locZ = (z - 200).coerceIn(0.0, 624.0)
            UChat.chat("$successPrefix §aSuccessfully set location waypoint $name to $x $y $z.")
        } else {
            CHWaypoints.waypoints[name] = BlockPos(x, y, z)
            UChat.chat("$successPrefix §aSuccessfully set waypoint $name to $x $y $z.")
        }
    }

    @Command("skytilshollowwaypoint|sthw remove|delete <name>")
    fun removeWaypoint(
        @Quoted
        @Argument("name")
        name: String
    ) {
        checkEnabled()
        if (CHWaypoints.CrystalHollowsMap.Locations.entries
                .find { it.id == name }?.loc?.reset() != null
        )
            UChat.chat("$successPrefix §aSuccessfully removed waypoint §e${name}§a!")
        else if (CHWaypoints.waypoints.remove(name) != null)
            UChat.chat("$successPrefix §aSuccessfully removed waypoint §e$name§a.")
        else
            UChat.chat("$failPrefix §cWaypoint §e$name§c does not exist")
    }

    @Command("skytilshollowwaypoint|sthw clear")
    fun clearWaypoints() {
        checkEnabled()
        CHWaypoints.CrystalHollowsMap.Locations.entries.forEach { it.loc.reset() }
        CHWaypoints.waypoints.clear()
        UChat.chat("$successPrefix §aSuccessfully cleared all waypoints.")
    }

    private fun copyMessage(text: String): IChatComponent {
        return UTextComponent("§9[Copy] ").apply {
            setHoverText("§9Copy the coordinates in chat box.")
            clickAction = ClickEvent.Action.SUGGEST_COMMAND
            clickValue = text
        }
    }

    private fun removeMessage(id: String): IChatComponent {
        return UTextComponent("§c[Remove]\n").apply {
            setHoverText("§cRemove the waypoint.")
            clickAction = ClickEvent.Action.RUN_COMMAND
            clickValue = "/sthw remove $id"
        }
    }
}
