/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.commands.impl

import gg.essential.universal.UChat
import gg.essential.universal.wrappers.UPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.Sharttils.Companion.mc
import sharttils.sharttilsmod.commands.BaseCommand
import sharttils.sharttilsmod.core.DataFetcher
import sharttils.sharttilsmod.core.PersistentSave
import sharttils.sharttilsmod.core.UpdateChecker
import sharttils.sharttilsmod.features.impl.events.GriffinBurrows
import sharttils.sharttilsmod.features.impl.handlers.MayorInfo
import sharttils.sharttilsmod.features.impl.mining.MiningFeatures
import sharttils.sharttilsmod.features.impl.misc.Ping
import sharttils.sharttilsmod.features.impl.misc.PricePaid
import sharttils.sharttilsmod.features.impl.misc.SlayerFeatures
import sharttils.sharttilsmod.features.impl.trackers.Tracker
import sharttils.sharttilsmod.gui.*
import sharttils.sharttilsmod.gui.profile.ProfileGui
import sharttils.sharttilsmod.utils.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.concurrent.thread
import kotlin.properties.Delegates

object SharttilsCommand : BaseCommand("sharttils", listOf("st")) {
    override fun processCommand(player: EntityPlayerSP, args: Array<String>) {
        if (args.isEmpty()) {
            Sharttils.displayScreen = OptionsGui()
            return
        }
        when (args[0].lowercase()) {
            "setkey" -> {
                if (args.size == 1) {
                    UChat.chat("§c§l[ERROR] §8» §cPlease provide your Hypixel API key!")
                    return
                }
                Sharttils.threadPool.submit {
                    val apiKey = args[1]
                    if (APIUtil.getJSONResponse("https://api.hypixel.net/key?key=$apiKey").get("success")
                            .asBoolean
                    ) {
                        Sharttils.config.apiKey = apiKey
                        Sharttils.hylinAPI.key = Sharttils.config.apiKey
                        Sharttils.config.markDirty()
                        UChat.chat("§a§l[SUCCESS] §8» §aYour Hypixel API key has been set to §f$apiKey§a.")
                    } else {
                        UChat.chat("§c§l[ERROR] §8» §cThe Hypixel API key you provided was §finvalid§c.")
                    }
                }
            }
            "config" -> Sharttils.config.openGUI()
            "fetchur" -> player.addChatMessage(
                ChatComponentText(
                    "§e§l[FETCHUR] §8» §eToday's Fetchur item is: §f" + MiningFeatures.fetchurItems.values.toTypedArray()
                            [(ZonedDateTime.now(ZoneId.of("America/New_York"))
                        .dayOfMonth - 1) % MiningFeatures.fetchurItems.size]
                )
            )
            "griffin" -> if (args.size == 1) {
                UChat.chat("/sharttils griffin <refresh>")
            } else {
                when (args[1].lowercase()) {
                    "refresh" -> {
                        GriffinBurrows.particleBurrows.values.removeAll { !it.dug }
                        GriffinBurrows.burrows.clear()
                        if (System.currentTimeMillis() - GriffinBurrows.lastManualRefresh <= 2500) {
                            UChat.chat("§cSlow down! Did not refresh your burrows to prevent a rate limit!")
                        } else {
                            GriffinBurrows.burrowRefreshTimer.reset()
                            GriffinBurrows.shouldRefreshBurrows = true
                            GriffinBurrows.lastManualRefresh = System.currentTimeMillis()
                        }
                    }
                    else -> UChat.chat("/sharttils griffin <refresh>")
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
                    UChat.chat("/sharttils reload <aliases/data/slayer>")
                    return
                } else {
                    when (args[1].lowercase()) {
                        "data" -> {
                            DataFetcher.reloadData()
                            UChat.chat("§b§l[RELOAD] §8» §bSharttils repository data has been §freloaded§b successfully.")
                        }
                        "mayor" -> {
                            MayorInfo.fetchMayorData()
                            MayorInfo.fetchJerryData()
                            UChat.chat("§b§l[RELOAD] §8» §bSharttils mayor data has been §freloaded§b successfully.")
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
                        else -> UChat.chat("/sharttils reload <aliases/data/slayer>")
                    }
                }
            }
            "help" -> if (args.size == 1) {
                player.addChatMessage(
                    ChatComponentText(
                        """
                            #§9➜ Sharttils Commands and Info
                            #  §2§l ❣ §7§oCommands marked with a §a§o✯ §7§orequire an §f§oAPI key§7§o to work correctly.
                            #  §2§l ❣ §7§oThe current mod version is §f§o${Sharttils.VERSION}§7§o.
                            # §9§l➜ Setup:
                            #  §3/sharttils §l➡ §bOpens the main mod GUI. §7(Alias: §f/st§7)
                            #  §3/sharttils config §l➡ §bOpens the configuration GUI.
                            #  §3/sharttils setkey §l➡ §bSets your Hypixel API key.
                            #  §3/sharttils help §l➡ §bShows this help menu.
                            #  §3/sharttils reload <data/mayor/slayer> §l➡ §bForces a refresh of data.
                            #  §3/sharttils update §l➡ §bChecks for updates in-game.
                            #  §3/sharttils editlocations §l➡ §bOpens the location editing GUI.
                            #  §3/sharttils aliases §l➡ §bOpens the command alias editing GUI.
                            #  §3/sharttils spamhider §l➡ §bOpens the command spam hider editing GUI.
                            #  §3/sharttils enchant §l➡ §bOpens a GUI allowing you to rename enchants.
                            #  §3/sharttils waypoints §l➡ §bOpens a GUI allowing you to modify waypoints.
                            # §9§l➜ Events:
                            #  §3/sharttils griffin refresh §l➡ §bForcefully refreshes Griffin Burrow waypoints. §a§o✯
                            #  §3/sharttils fetchur §l➡ §bShows the item that Fetchur wants.
                            #  §3/sharttils resettracker §l➡ §bResets the specified tracker.
                            # §9§l➜ Color and Glint
                            #  §3/armorcolor <set/clear/clearall> §l➡ §bChanges the color of an armor piece to the hexcode or decimal color. §7(Alias: §f/armourcolour§7)
                            #  §3/glintcustomize override <on/off/clear/clearall> §l➡ §bEnables or disables the enchantment glint on an item.
                            #  §3/glintcustomize color <set/clear/clearall> §l➡ §bChange the enchantment glint color for an item.
                            # §9§l➜ Miscellaneous:
                            #  §3/reparty §l➡ §bDisbands and re-invites everyone in your party. §7(Alias: §f/rp§7)
                            #  §3/sharttilscata <player> §l➡ §bShows information about a player's Catacombs statistics. §a§o✯
                            #  §3/sharttilsslayer <player> §l➡ §bShows information about a player's Slayer statistics. §a§o✯
                            #  §3/trackcooldown <length> <ability name> §l➡ §bTracks the cooldown of the specified ability.
                            #      §4Must have§c Item Cooldown Display§4 enabled to work.
                            #  §3/sharttilshollowwaypoint <set/remove/clear/help> <name> <x y z> §l➡ §bAllows to set waypoints while in the Crystal Hollows. §7(Alias: §f/sthw§7)"
                            #  §3/sharttilscalcxp <dungeons/skill/zombie_slayer/spider_slayer/wolf_slayer/enderman_slayer> <start level> <end level> §l➡ §bCalculates the xp between two levels
                            #  §3/sharttils pv <player> §l➡ §bOpens the profile viewer. §a§o✯
                            #  §3/sharttils pricepaid <price> §l➡ §bSets your currently held item to a given price.
                        """.trimMargin("#")
                    )
                )
                return
            }
            "aliases", "alias", "editaliases", "commandaliases" -> Sharttils.displayScreen = CommandAliasesGui()
            "editlocation", "editlocations", "location", "locations", "loc", "gui" -> Sharttils.displayScreen =
                LocationEditGui()
            "keyshortcuts", "shortcuts" -> Sharttils.displayScreen = KeyShortcutsGui()
            "spam", "spamhider" -> Sharttils.displayScreen = SpamHiderGui()
            "armorcolor", "armorcolour", "armourcolor", "armourcolour" -> ArmorColorCommand.processCommand(
                player,
                args.copyOfRange(1, args.size)
            )
            "swaphub" -> {
                if (Utils.inSkyblock) {
                    Sharttils.sendMessageQueue.add("/warpforge")
                    CoroutineScope(Dispatchers.Default).launch {
                        delay(2000)
                        Sharttils.sendMessageQueue.add("/warp ${args.getOrNull(1) ?: "hub"}")
                    }
                }
            }
            "spiritleapnames" -> Sharttils.displayScreen = SpiritLeapNamesGui()
            "dev" -> {
                if (args.size == 1) {
                    UChat.chat("/sharttils dev <toggle>")
                    return
                } else {
                    DevTools.toggle(args[1])
                    player.addChatMessage(
                        ChatComponentText(
                            "§c§lSharttils ➜ §c${
                                args[1]
                            } was toggled to: §6${
                                if (DevTools.allToggle) "Overriden by all toggle to ${DevTools.allToggle}" else DevTools.getToggle(
                                    args[1]
                                )
                            }"
                        )
                    )
                }
            }
            "enchant" -> Sharttils.displayScreen = EnchantNamesGui()
            "update" -> {
                try {
                    thread(block = UpdateChecker.updateGetter::run).join()
                    if (UpdateChecker.updateGetter.updateObj == null) {
                        return UChat.chat("§b§lSharttils §r§8➡ §cNo new update found")
                    }
                    val message = ChatComponentText(
                        "§b§lSharttils §r§8➜ §7Update for version ${
                            UpdateChecker.updateGetter.updateObj?.get("tag_name")?.asString
                        } is available! "
                    )
                    message.appendSibling(
                        ChatComponentText("§a[Update Now] ").setChatStyle(
                            ChatStyle().setChatClickEvent(
                                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sharttils updateNow")
                            ).setChatHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    ChatComponentText("§eUpdates and restarts your game")
                                )
                            )
                        )
                    )
                    message.appendSibling(
                        ChatComponentText("§b[Update Later] ").setChatStyle(
                            ChatStyle().setChatClickEvent(
                                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sharttils updateLater")
                            ).setChatHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    ChatComponentText("§eUpdates after you close your game")
                                )
                            )
                        )
                    )
                    return player.addChatMessage(message)
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }
            }
            "updatenow" -> Sharttils.displayScreen = UpdateGui(true)
            "updatelater" -> Sharttils.displayScreen = UpdateGui(false)
            "ping" -> {
                Ping.invokedCommand = true
                Ping.sendPing()
            }
            "waypoint", "waypoints" -> Sharttils.displayScreen = WaypointsGui()
            "notifications" -> Sharttils.displayScreen = CustomNotificationsGui()
            "pv" -> {
                if (args.size == 1) {
                    Sharttils.displayScreen =
                        ProfileGui(mc.thePlayer.uniqueID, UPlayer.getPlayer()?.displayNameString ?: "")
                } else {
                    // TODO Add some kind of message indicating progress
                    Sharttils.launch {
                        Sharttils.hylinAPI.getUUID(args[1]).whenComplete { uuid ->
                            Sharttils.displayScreen = ProfileGui(uuid, args[1])
                        }.catch { error ->
                            UChat.chat("§9§lSharttils ➜ §cError finding player!")
                            error.printStackTrace()
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
            else -> UChat.chat("§c§lSharttils ➜ §cThis command doesn't exist!\n §cUse §f/sharttils help§c for a full list of commands")
        }
    }
}