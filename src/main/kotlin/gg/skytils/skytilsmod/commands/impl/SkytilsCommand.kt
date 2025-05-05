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
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.Skytils.Companion.successPrefix
import gg.skytils.skytilsmod.commands.SkytilsCommandSender
import gg.skytils.skytilsmod.core.DataFetcher
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.core.UpdateChecker
import gg.skytils.skytilsmod.features.impl.dungeons.PartyFinderStats
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.Catlas
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.Room
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.RoomState
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonInfo
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonScanner
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.MapUtils
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.ScanUtils
import gg.skytils.skytilsmod.features.impl.events.GriffinBurrows
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.features.impl.mining.MiningFeatures
import gg.skytils.skytilsmod.features.impl.misc.Ping
import gg.skytils.skytilsmod.features.impl.misc.PricePaid
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.trackers.Tracker
import gg.skytils.skytilsmod.gui.OptionsGui
import gg.skytils.skytilsmod.gui.editing.ElementaEditingGui
import gg.skytils.skytilsmod.gui.editing.VanillaEditingGui
import gg.skytils.skytilsmod.gui.features.*
import gg.skytils.skytilsmod.gui.profile.ProfileGui
import gg.skytils.skytilsmod.gui.updater.UpdateGui
import gg.skytils.skytilsmod.gui.waypoints.WaypointsGui
import gg.skytils.skytilsmod.listeners.ServerPayloadInterceptor.getResponse
import gg.skytils.skytilsmod.localapi.LocalAPI
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorHypixelPacketRegistry
import gg.skytils.skytilsmod.utils.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.ClientboundHypixelPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundVersionedPacket
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.item.EntityArmorStand
import org.incendo.cloud.annotation.specifier.Greedy
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.Commands
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Commands
object SkytilsCommand {

    @Command("skytils|st")
    fun openMenuScreen() {
        Skytils.displayScreen = OptionsGui()
    }

    @Command("skytils|st config")
    fun openConfigScreen() {
        Skytils.config.openGUI()
    }

    @Command("skytils|st localapi <state>")
    fun localAPI(
        @Argument("state", description = "The state to set the local API to (on/off)")
        state: Boolean
    ) {
        runCatching {
            if (state) LocalAPI.stopServer()
            else LocalAPI.stopServer()
        }.onFailure {
            UChat.chat("$failPrefix §cThe LocalAPI server emitted an error: ${it.message}.")
        }.onSuccess {
            UChat.chat("$successPrefix §bThe LocalAPI server has been modified.")
        }
    }

    @Command("skytils|st fetchur")
    fun fetchur() {
        UChat.chat("$prefix §bToday's Fetchur item is: §f" + MiningFeatures.fetchurItems.values.toTypedArray()
            [(ZonedDateTime.now(ZoneId.of("America/New_York"))
            .dayOfMonth) % MiningFeatures.fetchurItems.size])
    }

    @Command("skytils|st stats [name]")
    fun stats(
        @Argument("name", description = "The name of the player to get stats for")
        name: String?
    ) {
        PartyFinderStats.printStats(name ?: mc.thePlayer!!.name, false)
    }

    @Command("skytils|st griffin refresh")
    fun refreshGriffinBurrows() {
        GriffinBurrows.particleBurrows.clear()
    }

    @Command("skytils|st griffin clearguess")
    fun clearGriffinGuess() {
        GriffinBurrows.BurrowEstimation.guesses.clear()
    }

    @Command("skytils|st resettracker <tracker>")
    fun resetTracker(
        @Greedy
        @Argument("tracker", description = "The tracker to reset", suggestions = "skytilstrackers")
        tracker: String
    ) {
        val trackerObj = Tracker.getTrackerById(tracker) ?: throw WrongUsageException(
            "Invalid Tracker! You need to specify one of [${Tracker.TRACKERS.joinToString(", ") { it.id }}]!"
        )
        trackerObj.doReset()
    }

    @Suggestions("skytilstrackers")
    fun trackerSuggestions(ctx: CommandContext<SkytilsCommandSender>, input: String): Iterable<String> {
        return Tracker.TRACKERS.map { it.id }.filter { it.startsWith(input) }
    }

    @Command("skytils|st reload data")
    fun reloadData() {
        DataFetcher.reloadData()
        DataFetcher.job?.invokeOnCompletion {
            it?.run {
                UChat.chat("$failPrefix §cFailed to reload repository data due to a ${it::class.simpleName ?: "error"}: ${it.message}!")
            }.ifNull {
                UChat.chat("$prefix §bRepository data has been §freloaded§b successfully.")
            }
        }
    }

    @Command("skytils|st reload mayor")
    fun reloadMayor() {
        Skytils.IO.async {
            MayorInfo.fetchMayorData()
            MayorInfo.fetchJerryData()
        }.invokeOnCompletion {
            it?.run {
                UChat.chat("$failPrefix §cFailed to reload mayor data due to a ${it::class.simpleName ?: "error"}: ${it.message}!")
            }.ifNull {
                UChat.chat("$prefix §bMayor data has been §freloaded§b successfully.")
            }
        }
    }

    @Command("skytils|st reload slayer")
    fun reloadSlayer() {
        for (entity in mc.theWorld.getEntitiesWithinAABBExcludingEntity(
            mc.thePlayer,
            mc.thePlayer.entityBoundingBox.expand(5.0, 3.0, 5.0)
        )) {
            if (entity is EntityArmorStand) continue
            SlayerFeatures.processSlayerEntity(entity)
        }
    }

    @Command("skytils|st help")
    fun helpCommand() {
        UChat.chat(
            """
                #§9➜ Skytils Commands and Info
                #  §2§l ❣ §7§oThe current mod version is §f§o${Skytils.VERSION}§7§o.
                # §9§l➜ Setup:
                #  §3/skytils §l➡ §bOpens the main mod GUI. §7(Alias: §f/st§7)
                #  §3/skytils config §l➡ §bOpens the configuration GUI.
                #  §3/skytils help §l➡ §bShows this help menu.
                #  §3/skytils reload <data/mayor/slayer> §l➡ §bForces a refresh of data.
                #  §3/skytils update §l➡ §bChecks for updates in-game.
                #  §3/skytils editlocations §l➡ §bOpens the location editing GUI. This uses a new GUI and the previous GUI can be accessed using §3/skytils oldgui§b.
                #  §3/skytils resetelement <name> §l➡ §bResets the size of an element and sets its position to the middle of the screen..
                #  §3/skytils aliases §l➡ §bOpens the command alias editing GUI.
                #  §3/skytils shortcuts §l➡ §bOpens the command Key Shortcut editing GUI.
                #  §3/skytils spamhider §l➡ §bOpens the command spam hider editing GUI.
                #  §3/skytils enchant §l➡ §bOpens a GUI allowing you to rename enchantments.
                #  §3/skytils waypoints §l➡ §bOpens a GUI allowing you to modify waypoints.
                #  §3/skytils localapi <on/off> §l➡ §bTurns the Skytils LocalAPI on and off. Used for Web Waypoint editor.
                #  §3/skytils spiritleapnames §l➡ §bOpens a GUI allowing you to customize the spirit leap menu.
                #  §3/skytils notifications §l➡ §bOpens a GUI allowing you to modify chat notifications.
                #  §3/skytils catlas §l➡ §bOpens a GUI allowing you to modify dungeon map settings.
                # §9§l➜ Events:
                #  §3/skytils griffin refresh §l➡ §bClears currently marked griffin burrows.
                #  §3/skytils fetchur §l➡ §bShows the item that Fetchur wants.
                #  §3/skytils resettracker §l➡ §bResets the specified tracker.
                # §9§l➜ Color and Glint
                #  §3/armorcolor <set/clear/clearall> §l➡ §bChanges the color of an armor piece to the hexcode or decimal color. §7(Alias: §f/armourcolour§7)
                #  §3/glintcustomize override <on/off/clear/clearall> §l➡ §bEnables or disables the enchantment glint on an item.
                #  §3/glintcustomize color <set/clear/clearall> §l➡ §bChange the enchantment glint color for an item.
                # §9§l➜ Miscellaneous:
                #  §3/reparty §l➡ §bDisbands and re-invites everyone in your party. §7(Alias: §f/rp§7)
                #  §3/skytils stats <player> §l➡ §bShows the Dungeon statistics of a player (Identical to the Party Finder Stats feature).
                #  §3/skytilscata <player> §l➡ §bShows information about a player's Catacombs statistics.
                #  §3/skytilsslayer <player> §l➡ §bShows information about a player's Slayer statistics.
                #  §3/trackcooldown <length> <ability name> §l➡ §bTracks the cooldown of the specified ability.
                #      §4Must have§c Item Cooldown Display§4 enabled to work.
                #  §3/sthw <set/remove/clear/help> <x y z> <name> §l➡ §bAllows to set waypoints while in the Crystal Hollows. §7(Alias: §f/sthw§7)"
                #  §3/skytilscalcxp <dungeons/skill/zombie_slayer/spider_slayer/wolf_slayer/enderman_slayer> <start level> <end level> §l➡ §bCalculates the xp between two levels
                #  §3/skytils pv <player> §l➡ §bOpens the profile viewer.
                #  §3/skytils pricepaid <price> §l➡ §bSets your currently held item to a given price.
                #  §3/skytils ping §l➡ §bChecks your current ping to the server.
            """.trimMargin("#")
        )
    }

    @Command("skytils|st aliases|alias|editaliases|commandaliases")
    fun openAliases() {
        Skytils.displayScreen = CommandAliasesGui()
    }

    @Command("skytils|st editlocations|editloc|locations|loc|gui")
    fun openLocations() {
        Skytils.displayScreen = ElementaEditingGui()
    }

    @Command("skytils|st oldgui")
    fun openOldGui() {
        Skytils.displayScreen = VanillaEditingGui()
    }

    @Command("skytils|st keyshortcuts|shortcuts")
    fun openKeyShortcuts() {
        Skytils.displayScreen = KeyShortcutsGui()
    }

    @Command("skytils|st spamhider|spam")
    fun openSpamHider() {
        Skytils.displayScreen = SpamHiderGui()
    }

    @Command("skytils|st armorcolor")
    fun armorColorCommand() {
        UChat.chat("This command is no longer available. Use /skytils:armorcolor instead.")
    }

    @Command("skytils|st spiritleapnames")
    fun spiritLeapNamesCommand() {
        Skytils.displayScreen = SpiritLeapNamesGui()
    }

    @Command("skytils|st dev <toggle>")
    fun devCommand(
        @Argument("toggle", description = "The toggle to set")
        toggle: String
    ) {
        DevTools.toggle(toggle)
        UChat.chat(
            "$successPrefix §c${
                toggle
            } §awas toggled to: §6${
                if (DevTools.allToggle) "Overriden by all toggle to true." else DevTools.getToggle(
                    toggle
                )
            }"
        )
    }

    @Command("skytils|st enchant")
    fun enchantCommand() {
        Skytils.displayScreen = EnchantNamesGui()
    }

    @Command("skytils|st update")
    fun updateCommand() {
        Skytils.IO.launch {
            runCatching {
                UpdateChecker.updateGetter.run()
            }.onFailure {
                it.printStackTrace()
                UChat.chat("$failPrefix §cFailed to check for updates: ${it.message}")
            }.onSuccess {
                if (UpdateChecker.updateGetter.updateObj == null) {
                    UChat.chat("$prefix §cNo new update found.")
                } else {
                    val message = UMessage(
                        "$prefix §7Update for version ${
                            UpdateChecker.updateGetter.updateObj!!.tagName
                        } is available! ",
                        UTextComponent("§a[Update Now] ").setClick(
                            MCClickEventAction.RUN_COMMAND,
                            "/skytils updateNow"
                        ).setHoverText("§eUpdates and restarts your game"),
                        UTextComponent("§b[Update Later] ").setClick(
                            MCClickEventAction.RUN_COMMAND,
                            "/skytils updateLater"
                        ).setHoverText("§eUpdates after you close your game")
                    )
                    message.chat()
                }
            }
        }
    }

    @Command("skytils|st updateNow")
    fun updateNowCommand() {
        Skytils.displayScreen = UpdateGui(true)
    }

    @Command("skytils|st updateLater")
    fun updateLaterCommand() {
        Skytils.displayScreen = UpdateGui(false)
    }

    @Command("skytils|st ping")
    fun pingCommand() {
        Ping.invokedCommand = true
        Ping.sendPing()
    }

    @Command("skytils|st waypoint|waypoints")
    fun waypointCommand() {
        Skytils.displayScreen = WaypointsGui()
    }

    @Command("skytils|st notifications")
    fun notificationsCommand() {
        Skytils.displayScreen = CustomNotificationsGui()
    }

    @Command("skytils|st pv|profileviewer [name]")
    fun profileViewerCommand(
        @Argument("name", description = "The name of the player to view")
        name: String?
    ) {
        if (name == null) {
            Skytils.displayScreen = ProfileGui(mc.thePlayer!!.uniqueID, mc.thePlayer!!.name)
        } else {
            // TODO Add some kind of message indicating progress
            Skytils.IO.launch {
                runCatching {
                    MojangUtil.getUUIDFromUsername(name)
                }.onFailure {
                    UChat.chat("$failPrefix §cError finding player!")
                    it.printStackTrace()
                }.getOrNull()?.let { uuid ->
                    Skytils.displayScreen = ProfileGui(uuid, name)
                }
            }
        }
    }

    @Command("skytils|st pricepaid <amount>")
    fun pricePaidCommand(
        @Argument("amount", description = "The amount to set the price to")
        amount: Double
    ) {
        val extraAttr = ItemUtil.getExtraAttributes(mc.thePlayer?.heldItem) ?: return
        PricePaid.prices[UUID.fromString(extraAttr.getString("uuid").ifEmpty { return })] =
            amount
        PersistentSave.markDirty<PricePaid>()
    }

    @Command("skytils|st resetelement <name>")
    fun resetElement(
        @Greedy
        @Argument("name", description = "The name of the element to reset", suggestions = "skytilsguielements")
        name: String
    ) {
        val element = Skytils.guiManager.getByName(name) ?: return UChat.chat("$failPrefix §cThat element was not found!")
        element.setPos(0.5f, 0.5f)
        element.scale = 1f
    }

    @Suggestions("skytilsguielements")
    fun elementSuggestions(ctx: CommandContext<SkytilsCommandSender>, input: String): Iterable<String> {
        return Skytils.guiManager.searchElements(input).map { it.name }
    }

    @Command("skytils|st dungeonmap|catlas")
    fun dungeonMapCommand() {
        Skytils.displayScreen = CatlasConfig.gui()
    }

    @Command("skytils|st dungeonmap|catlas scan")
    fun dungeonMapScanCommand() {
        // Scans the dungeon
        Catlas.reset()
        DungeonScanner.scan()
    }

    @Command("skytils|st dungeonmap|catlas roomdata")
    fun dungeonMapRoomDataCommand() {
        // Copies room data or room core to clipboard
        val pos = ScanUtils.getRoomCenter(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
        val data = ScanUtils.getRoomData(pos.first, pos.second)
        if (data != null) {
            GuiScreen.setClipboardString(data.toString())
            UChat.chat("$successPrefix §aCopied room data to clipboard.")
        } else {
            GuiScreen.setClipboardString(ScanUtils.getCore(pos.first, pos.second).toString())
            UChat.chat("$successPrefix §aExisting room data not found. Copied room core to clipboard.")
        }
    }

    @Command("skytils|st dungeonmap|catlas mapdata")
    fun dungeonMapMapDataCommand() {
        // Copies map data to clipboard
        val data = MapUtils.getMapData()
        if (data != null) {
            GuiScreen.setClipboardString(data.colors.contentToString())
            UChat.chat("$successPrefix §aCopied map data to clipboard.")
        } else {
            UChat.chat("$failPrefix §cMap data not found.")
        }
    }

    @Command("skytils|st dungeonmap|catlas cheater")
    fun dungeonMapCheaterCommand() {
        if (Skytils.deobfEnvironment) {
            UChat.chat(DungeonInfo.uniqueRooms.entries.sortedByDescending { it.value.mainRoom.data.type }.map { it.key })
        }
    }

    @Command("skytils|st dungeonmap|catlas cheaterpre")
    fun dungeonMapCheaterPreCommand() {
        if (Skytils.deobfEnvironment) {
            DungeonInfo.dungeonList.forEach {
                if (it.state > RoomState.PREVISITED) {
                    it.state = RoomState.PREVISITED
                    (it as? Room)?.uniqueRoom?.state = RoomState.PREVISITED
                }
            }
        }
    }

    @Command("skytils|st hypixelpacket list")
    fun hypixelPacketListCommand() {
        val registry = HypixelModAPI.getInstance().registry
        UChat.chat("$successPrefix §eAvailable types: ${registry.identifiers.joinToString(", ")}")
    }

    @Command("skytils|st hypixelpacket send <id>")
    fun hypixelPacketSendCommand(
        @Argument("id", description = "The type of packet to send")
        id: String
    ) {
        val registry = HypixelModAPI.getInstance().registry
        if (!registry.isRegistered(id)) {
            UChat.chat("$failPrefix §cPacket not found!")
        } else {
            registry as AccessorHypixelPacketRegistry
            val packetClass = registry.classToIdentifier.entries.find { it.value == id && ServerboundVersionedPacket::class.java.isAssignableFrom(it.key) }
                ?: return UChat.chat("$failPrefix §cPacket not found!")
            val packet = packetClass.key.newInstance() as ServerboundVersionedPacket
            UChat.chat("$successPrefix §aPacket created: $packet")
            Skytils.IO.launch {
                runCatching {
                    packet.getResponse<ClientboundHypixelPacket>()
                }.onFailure {
                    UChat.chat("$failPrefix §cFailed to get packet response: ${it.message}")
                }.onSuccess { response ->
                    UChat.chat("$successPrefix §aPacket response: $response")
                }
            }
        }
    }
}