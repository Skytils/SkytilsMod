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
import gg.essential.universal.wrappers.UPlayer
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.Skytils.Companion.prefix
import gg.skytils.skytilsmod.Skytils.Companion.successPrefix
import gg.skytils.skytilsmod.commands.BaseCommand
import gg.skytils.skytilsmod.core.DataFetcher
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.core.UpdateChecker
import gg.skytils.skytilsmod.features.impl.dungeons.PartyFinderStats
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.Catlas
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
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
import gg.skytils.skytilsmod.utils.*
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.hypixel.modapi.packet.HypixelPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundLocationPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPingPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPlayerInfoPacket
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.ChatComponentText
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.set

object SkytilsCommand : BaseCommand("skytils", listOf("st")) {
    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            Skytils.displayScreen = OptionsGui()
            return
        }
        when (args[0].lowercase()) {
            "config" -> Skytils.config.openGUI()
            "localapi" -> {
                runCatching {
                    when (args.getOrNull(1)?.lowercase()) {
                        "on" -> LocalAPI.startServer()
                        "off" -> LocalAPI.stopServer()
                        else -> UChat.chat("$prefix §b/skytils localapi <on/off>")
                    }
                }.onFailure {
                    UChat.chat("$failPrefix §cThe LocalAPI server emitted an error: ${it.message}.")
                }.onSuccess {
                    UChat.chat("$successPrefix §bThe LocalAPI server has been modified.")
                }
            }

            "fetchur" -> player.addChatMessage(
                ChatComponentText(
                    "$prefix §bToday's Fetchur item is: §f" + MiningFeatures.fetchurItems.values.toTypedArray()
                        [(ZonedDateTime.now(ZoneId.of("America/New_York"))
                        .dayOfMonth) % MiningFeatures.fetchurItems.size]
                )
            )

            "stats" -> if (args.size == 1) {
                UChat.chat("$prefix §b/skytils stats <player>")
            } else {
                PartyFinderStats.printStats(args[1], false)
            }

            "griffin" -> if (args.size == 1) {
                UChat.chat("$prefix §b/skytils griffin <refresh>")
            } else {
                when (args[1].lowercase()) {
                    "refresh" -> {
                        GriffinBurrows.particleBurrows.clear()
                    }
                    "clearguess" -> {
                        GriffinBurrows.BurrowEstimation.guesses.clear()
                    }

                    else -> UChat.chat("$prefix §b/skytils griffin <refresh>")
                }
            }

            "resettracker" -> if (args.size == 1) {
                throw WrongUsageException("You need to specify one of [${Tracker.TRACKERS.joinToString(", ") { it.id }}]!")
            } else {
                (Tracker.getTrackerById(args[1]) ?: throw WrongUsageException(
                    "Invalid Tracker! You need to specify one of [${
                        Tracker.TRACKERS.joinToString(
                            ", "
                        ) { it.id }
                    }]!"
                )).doReset()
            }

            "reload" -> {
                if (args.size == 1) {
                    UChat.chat("$prefix §b/skytils reload <data/mayor/slayer>")
                    return
                } else {
                    when (args[1].lowercase()) {
                        "data" -> {
                            DataFetcher.reloadData()
                            DataFetcher.job?.invokeOnCompletion {
                                it?.run {
                                    UChat.chat("$failPrefix §cFailed to reload repository data due to a ${it::class.simpleName ?: "error"}: ${it.message}!")
                                }.ifNull {
                                    UChat.chat("$prefix §bRepository data has been §freloaded§b successfully.")
                                }
                            }
                        }

                        "mayor" -> {
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

                        "slayer" -> {
                            for (entity in mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                                mc.thePlayer,
                                mc.thePlayer.entityBoundingBox.expand(5.0, 3.0, 5.0)
                            )) {
                                if (entity is EntityArmorStand) continue
                                SlayerFeatures.processSlayerEntity(entity)
                            }
                        }

                        else -> UChat.chat("$prefix §b/skytils reload <data/mayor/slayer>")
                    }
                }
            }

            "help" -> if (args.size == 1) {
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
                return
            }

            "aliases", "alias", "editaliases", "commandaliases" -> Skytils.displayScreen = CommandAliasesGui()
            "editlocation", "editlocations", "location", "locations", "loc", "gui" ->
                Skytils.displayScreen = ElementaEditingGui()

            "oldgui" -> Skytils.displayScreen = VanillaEditingGui()

            "keyshortcuts", "shortcuts" -> Skytils.displayScreen = KeyShortcutsGui()
            "spam", "spamhider" -> Skytils.displayScreen = SpamHiderGui()
            "armorcolor", "armorcolour", "armourcolor", "armourcolour" -> ArmorColorCommand.processCommand(
                player,
                args.copyOfRange(1, args.size)
            )

            "swaphub" -> {
                if (Utils.inSkyblock) {
                    Skytils.sendMessageQueue.add("/warpforge")
                    Skytils.launch {
                        delay(2000)
                        Skytils.sendMessageQueue.add("/warp ${args.getOrNull(1) ?: "hub"}")
                    }
                }
            }

            "spiritleapnames" -> Skytils.displayScreen = SpiritLeapNamesGui()
            "dev" -> {
                if (args.size == 1) {
                    UChat.chat("$prefix §b/skytils dev <toggle>")
                    return
                } else {
                    DevTools.toggle(args[1])
                    player.addChatMessage(
                        ChatComponentText(
                            "$successPrefix §c${
                                args[1]
                            } §awas toggled to: §6${
                                if (DevTools.allToggle) "Overriden by all toggle to ${DevTools.allToggle}" else DevTools.getToggle(
                                    args[1]
                                )
                            }"
                        )
                    )
                }
            }

            "enchant" -> Skytils.displayScreen = EnchantNamesGui()
            "update" -> {
                try {
                    Skytils.IO.launch {
                        async { UpdateChecker.updateGetter.run() }.invokeOnCompletion {
                            if (UpdateChecker.updateGetter.updateObj == null) {
                                return@invokeOnCompletion UChat.chat("$prefix §cNo new update found.")
                            }
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
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }

            "updatenow" -> Skytils.displayScreen = UpdateGui(true)
            "updatelater" -> Skytils.displayScreen = UpdateGui(false)
            "ping" -> {
                Ping.invokedCommand = true
                Ping.sendPing()
            }

            "waypoint", "waypoints" -> Skytils.displayScreen = WaypointsGui()
            "notifications" -> Skytils.displayScreen = CustomNotificationsGui()
            "pv" -> {
                if (args.size == 1) {
                    Skytils.displayScreen =
                        ProfileGui(mc.thePlayer.uniqueID, UPlayer.getPlayer()?.displayNameString ?: "")
                } else {
                    // TODO Add some kind of message indicating progress
                    Skytils.IO.launch {
                        runCatching {
                            MojangUtil.getUUIDFromUsername(args[1])
                        }.onFailure {
                            UChat.chat("$failPrefix §cError finding player!")
                            it.printStackTrace()
                        }.getOrNull()?.let { uuid ->
                            Skytils.displayScreen = ProfileGui(uuid, args[1])
                        }
                    }
                }
            }

            "pricepaid" -> {
                val extraAttr = ItemUtil.getExtraAttributes(mc.thePlayer?.heldItem) ?: return
                PricePaid.prices[UUID.fromString(extraAttr.getString("uuid").ifEmpty { return })] =
                    args[1].toDoubleOrNull() ?: return
                PersistentSave.markDirty<PricePaid>()
            }

            "resetelement" -> {
                val element = Skytils.guiManager.getByName(args.drop(1).joinToString(" "))
                    ?: return UChat.chat("$failPrefix §cThat element was not found!")
                element.setPos(0.5f, 0.5f)
                element.scale = 1f
            }

            "catlas", "dungeonmap" -> {
                when (args.getOrNull(1)?.lowercase()) {
                    // Scans the dungeon
                    "scan" -> {
                        Catlas.reset()
                        DungeonScanner.scan()
                    }
                    // Copies room data or room core to clipboard
                    "roomdata" -> {
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
                    "mapdata" -> {
                        val data = MapUtils.getMapData()
                        if (data != null) {
                            GuiScreen.setClipboardString(data.colors.contentToString())
                            UChat.chat("$successPrefix §aCopied map data to clipboard.")
                        } else {
                            UChat.chat("$failPrefix §cMap data not found.")
                        }
                    }
                    "cheater" -> {
                        if (Skytils.deobfEnvironment) {
                            UChat.chat(DungeonInfo.uniqueRooms.sortedByDescending { it.mainRoom.data.type }.map { it.name })
                        }
                    }
                    else -> {
                        Skytils.displayScreen = CatlasConfig.gui()
                    }
                }
            }

            "hypixelpacket" -> {
                val packet = when (args.getOrNull(1)) {
                    "ping" -> ServerboundPingPacket()
                    "location" -> ServerboundLocationPacket()
                    "party_info" -> ServerboundPartyInfoPacket()
                    "player_info" -> ServerboundPlayerInfoPacket()
                    else -> return UChat.chat("$failPrefix §cPacket not found!")
                }

                UChat.chat("$successPrefix §aPacket created: $packet")
                Skytils.IO.launch {
                    runCatching {
                        packet.getResponse<HypixelPacket>(mc.netHandler)
                    }.onFailure {
                        UChat.chat("$failPrefix §cFailed to get packet response: ${it.message}")
                    }.onSuccess { response ->
                        UChat.chat("$successPrefix §aPacket response: $response")
                    }
                }
            }

            else -> UChat.chat("$failPrefix §cThis command doesn't exist!\n §cUse §f/skytils help§c for a full list of commands")
        }
    }
}