/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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
package skytils.skytilsmod.commands

import kotlinx.coroutines.*
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.DataFetcher
import skytils.skytilsmod.core.UpdateChecker
import skytils.skytilsmod.features.impl.events.GriffinBurrows
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import skytils.skytilsmod.features.impl.mining.MiningFeatures
import skytils.skytilsmod.features.impl.misc.SlayerFeatures
import skytils.skytilsmod.features.impl.trackers.Tracker
import skytils.skytilsmod.gui.*
import skytils.skytilsmod.utils.APIUtil
import skytils.skytilsmod.utils.DevTools
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.openGUI
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.concurrent.thread

object SkytilsCommand : CommandBase() {

    override fun getCommandName(): String {
        return "skytils"
    }

    override fun getCommandAliases(): MutableList<String> {
        return mutableListOf("st")
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/$commandName"
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> {
        return emptyList()
    }

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        val player = sender as EntityPlayerSP
        if (args.isEmpty()) {
            Skytils.displayScreen = OptionsGui()
            return
        }
        when (args[0].lowercase()) {
            "setkey" -> {
                if (args.size == 1) {
                    player.addChatMessage(ChatComponentText("§c§l[ERROR] §8» §cPlease provide your Hypixel API key!"))
                    return
                }
                Skytils.threadPool.submit {
                    val apiKey = args[1]
                    if (APIUtil.getJSONResponse("https://api.hypixel.net/key?key=$apiKey").get("success")
                            .asBoolean
                    ) {
                        Skytils.config.apiKey = apiKey
                        Skytils.apiWrapper.key = Skytils.config.apiKey
                        Skytils.config.markDirty()
                        player.addChatMessage(ChatComponentText("§a§l[SUCCESS] §8» §aYour Hypixel API key has been set to §f$apiKey§a."))
                    } else {
                        player.addChatMessage(ChatComponentText("§c§l[ERROR] §8» §cThe Hypixel API key you provided was §finvalid§c."))
                    }
                }
            }
            "config" -> Skytils.config.openGUI()
            "fetchur" -> player.addChatMessage(
                ChatComponentText(
                    "§e§l[FETCHUR] §8» §eToday's Fetchur item is: §f" + MiningFeatures.fetchurItems.values.toTypedArray()
                            [(ZonedDateTime.now(ZoneId.of("America/New_York"))
                        .dayOfMonth - 1) % MiningFeatures.fetchurItems.size]
                )
            )
            "griffin" -> if (args.size == 1) {
                player.addChatMessage(ChatComponentText("/skytils griffin <refresh>"))
            } else {
                when (args[1].lowercase()) {
                    "refresh" -> {
                        GriffinBurrows.particleBurrows.removeIf { pb -> !pb.dug }
                        GriffinBurrows.burrows.clear()
                        GriffinBurrows.burrowRefreshTimer.reset()
                        GriffinBurrows.shouldRefreshBurrows = true
                    }
                    else -> player.addChatMessage(ChatComponentText("/skytils griffin <refresh>"))
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
                    player.addChatMessage(ChatComponentText("/skytils reload <aliases/data/slayer>"))
                    return
                } else {
                    when (args[1].lowercase()) {
                        "data" -> {
                            DataFetcher.reloadData()
                            player.addChatMessage(ChatComponentText("§b§l[RELOAD] §8» §bSkytils repository data has been §freloaded§b successfully."))
                        }
                        "mayor" -> {
                            MayorInfo.fetchMayorData()
                            MayorInfo.fetchJerryData()
                            player.addChatMessage(ChatComponentText("§b§l[RELOAD] §8» §bSkytils mayor data has been §freloaded§b successfully."))
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
                        else -> player.addChatMessage(ChatComponentText("/skytils reload <aliases/data/slayer>"))
                    }
                }
            }
            "help" -> if (args.size == 1) {
                player.addChatMessage(
                    ChatComponentText(
                        """
                            #§9➜ Skytils Commands and Info
                            #  §2§l ❣ §7§oCommands marked with a §a§o✯ §7§orequire an §f§oAPI key§7§o to work correctly.
                            #  §2§l ❣ §7§oThe current mod version is §f§o${Skytils.VERSION}§7§o.
                            # §9§l➜ Setup:
                            #  §3/skytils §l➡ §bOpens the main mod GUI. §7(Alias: §f/st§7)
                            #  §3/skytils config §l➡ §bOpens the configuration GUI.
                            #  §3/skytils setkey §l➡ §bSets your Hypixel API key.
                            #  §3/skytils help §l➡ §bShows this help menu.
                            #  §3/skytils reload <data/mayor/slayer> §l➡ §bForces a refresh of data.
                            #  §3/skytils editlocations §l➡ §bOpens the location editing GUI.
                            #  §3/skytils aliases §l➡ §bOpens the command alias editing GUI.
                            #  §3/skytils spamhider §l➡ §bOpens the command spam hider editing GUI.
                            #  §3/skytils enchant §l➡ §bOpens a GUI allowing you to rename enchants.
                            # §9§l➜ Events:
                            #  §3/skytils griffin refresh §l➡ §bForcefully refreshes Griffin Burrow waypoints. §a§o✯
                            #  §3/skytils fetchur §l➡ §bShows the item that Fetchur wants.
                            #  §3/skytils resettracker §l➡ §bResets the specified tracker.
                            # §9§l➜ Color and Glint
                            #  §3/armorcolor <set/clear/clearall> §l➡ §bChanges the color of an armor piece to the hexcode or decimal color. §7(Alias: §f/armourcolour§7)
                            #  §3/glintcustomize override <on/off/clear/clearall> §l➡ §bEnables or disables the enchantment glint on an item.
                            #  §3/glintcustomize color <set/clear/clearall> §l➡ §bChange the enchantment glint color for an item.
                            # §9§l➜ Miscellaneous:
                            #  §3/reparty §l➡ §bDisbands and re-invites everyone in your party. §7(Alias: §f/rp§7)
                            #  §3/skytilscata <player> §l➡ §bShows information about a player's Catacombs statistics. §a§o✯
                            #  §3/skytilsslayer <player> §l➡ §bShows information about a player's Slayer statistics. §a§o✯
                            #  §3/trackcooldown <length> <ability name> §l➡ §bTracks the cooldown of the specified ability. §4Must have§c Item Cooldown Display§4 enabled to work"
                            #  §3/skytilshollowwaypoint <set/remove/clear/help> <name> <x y z> §l➡ §bAllows to set waypoints while in the Crystal Hollows. §7(Alias: §f/sthw§7)"
                        """.trimMargin("#")
                    )
                )
                return
            }
            "aliases", "alias", "editaliases", "commandaliases" -> Skytils.displayScreen =
                CommandAliasesGui()
            "editlocation", "editlocations", "location", "locations", "loc", "gui" -> Skytils.displayScreen =
                LocationEditGui()
            "keyshortcuts", "shortcuts" -> Skytils.displayScreen = KeyShortcutsGui()
            "spam", "spamhider" -> Skytils.displayScreen = SpamHiderGui()
            "armorcolor", "armorcolour", "armourcolor", "armourcolour" -> ArmorColorCommand.processCommand(
                sender,
                args.copyOfRange(1, args.size)
            )
            "swaphub" -> {
                if (Utils.inSkyblock) {
                    Skytils.sendMessageQueue.add("/warpforge")
                    CoroutineScope(Dispatchers.Default).launch {
                        delay(2000)
                        Skytils.sendMessageQueue.add("/warp ${args.getOrNull(1) ?: "hub"}")
                    }
                }
            }
            "spiritleapnames" -> {
                Skytils.displayScreen = SpiritLeapNamesGui()
            }
            "dev" -> {
                if (args.size == 1) {
                    player.addChatMessage(ChatComponentText("/skytils dev <toggle>"))
                    return
                } else {
                    DevTools.toggle(args[1])
                    player.addChatMessage(
                        ChatComponentText(
                            "§c§lSkytils ➜ §c${
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
            "enchant" -> Skytils.displayScreen = EnchantNamesGui()
            "update" -> {
                try {
                    thread(block = UpdateChecker.updateGetter::run).join()
                    if (UpdateChecker.updateGetter.updateObj == null) {
                        return player.addChatMessage(ChatComponentText("§b§lSkytils §r§8➡ §cNo new update found"))
                    }
                    val message = ChatComponentText(
                        "§b§lSkytils §r§8➜ §7Update for version ${
                            UpdateChecker.updateGetter.updateObj?.get("tag_name")?.asString
                        } is available! "
                    )
                    message.appendSibling(
                        ChatComponentText("§a[Update Now] ").setChatStyle(
                            ChatStyle().setChatClickEvent(
                                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skytils updateNow")
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
                                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skytils updateLater")
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
            "updatenow" -> {
                Skytils.displayScreen = UpdateGui(true)
                return
            }
            "updatelater" -> {
                Skytils.displayScreen = UpdateGui(false)
                return
            }
            else -> player.addChatMessage(ChatComponentText("§c§lSkytils ➜ §cThis command doesn't exist!\n §cUse §f/skytils help§c for a full list of commands"))
        }
    }
}