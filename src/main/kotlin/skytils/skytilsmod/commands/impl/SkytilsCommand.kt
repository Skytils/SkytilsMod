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
package skytils.skytilsmod.commands.impl

import gg.essential.api.EssentialAPI
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.commands.DisplayName
import gg.essential.api.commands.SubCommand
import gg.essential.api.utils.Multithreading
import gg.essential.universal.UChat
import gg.essential.universal.wrappers.message.UTextComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.minecraft.command.WrongUsageException
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatStyle
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.DataFetcher
import skytils.skytilsmod.core.UpdateChecker
import skytils.skytilsmod.features.impl.events.GriffinBurrows
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import skytils.skytilsmod.features.impl.mining.MiningFeatures
import skytils.skytilsmod.features.impl.misc.Ping
import skytils.skytilsmod.features.impl.misc.SlayerFeatures
import skytils.skytilsmod.features.impl.trackers.Tracker
import skytils.skytilsmod.gui.*
import skytils.skytilsmod.utils.APIUtil
import skytils.skytilsmod.utils.DevTools
import skytils.skytilsmod.utils.Utils
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.concurrent.thread


object SkytilsCommand : Command("skytils", false) {
    override val commandAliases: Set<Alias> = setOf(Alias("st"))

    @DefaultHandler
    fun handle() {
        EssentialAPI.getGuiUtil().openScreen(OptionsGui())
    }

    @SubCommand("config")
    fun openConfig() {
        EssentialAPI.getGuiUtil().openScreen(OptionsGui())
    }

    @SubCommand("setkey")
    fun setKey(@DisplayName("API Key") apiKey: String) {
        Multithreading.runAsync {
            if (APIUtil.getJSONResponse("https://api.hypixel.net/key?key=$apiKey").get("success")
                    .asBoolean
            ) {
                Skytils.config.apiKey = apiKey
                Skytils.hylinAPI.key = Skytils.config.apiKey
                Skytils.config.markDirty()
                EssentialAPI.getMinecraftUtil()
                    .sendMessage(UTextComponent("§a§l[SUCCESS] §8» §aYour Hypixel API key has been set to §f$apiKey§a."))
            } else {
                EssentialAPI.getMinecraftUtil()
                    .sendMessage(UTextComponent("§c§l[ERROR] §8» §cThe Hypixel API key you provided was §finvalid§c."))
            }
        }
    }

    @SubCommand("fetchur")
    fun fetchur() {
        EssentialAPI.getMinecraftUtil().sendMessage(
            UTextComponent(
                "§e§l[FETCHUR] §8» §eToday's Fetchur item is: §f" + MiningFeatures.fetchurItems.values.toTypedArray()[(ZonedDateTime.now(
                    ZoneId.of("America/New_York")
                ).dayOfMonth - 1) % MiningFeatures.fetchurItems.size]
            )
        )
    }

    @SubCommand("griffin")
    fun griffin(@DisplayName("refresh") refresh: String) {
        if (refresh == "refresh") {
            GriffinBurrows.particleBurrows.removeIf { pb -> !pb.dug }
            GriffinBurrows.burrows.clear()
            if (System.currentTimeMillis() - GriffinBurrows.lastManualRefresh <= 2500) {
                UChat.chat("§cSlow down! Did not refresh your burrows to prevent a rate limit!")
                GriffinBurrows.burrowRefreshTimer.reset()
                GriffinBurrows.shouldRefreshBurrows = true
                GriffinBurrows.lastManualRefresh = System.currentTimeMillis()
            }
        }
    }

    @SubCommand("resettracker")
    fun resetTracker(@DisplayName("tracker") tracker: String?) {
        if (tracker == null) {
            throw WrongUsageException("You need to specify one of [${Tracker.TRACKERS.joinToString(", ") { it.id }}]!")
        } else {
            (Tracker.getTrackerById(tracker) ?: throw WrongUsageException(
                "Invalid Tracker! You need to specify one of [${
                    Tracker.TRACKERS.joinToString(
                        ", "
                    ) { it.id }
                }]!"
            )).doReset()
        }
    }

    @SubCommand("reload")
    fun reload(@DisplayName("data type") data: String) {
        when (data.lowercase()) {
            "data" -> {
                DataFetcher.reloadData()
                EssentialAPI.getMinecraftUtil()
                    .sendMessage(UTextComponent("§b§l[RELOAD] §8» §bSkytils repository data has been §freloaded§b successfully."))
            }
            "mayor" -> {
                MayorInfo.fetchMayorData()
                MayorInfo.fetchJerryData()
                EssentialAPI.getMinecraftUtil()
                    .sendMessage(UTextComponent("§b§l[RELOAD] §8» §bSkytils mayor data has been §freloaded§b successfully."))
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
            else -> EssentialAPI.getMinecraftUtil().sendMessage(UTextComponent("/skytils reload <aliases/data/slayer>"))
        }
    }

    @SubCommand("help")
    fun help() {
        EssentialAPI.getMinecraftUtil().sendMessage(
            UTextComponent(
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
                            #  §3/skytils update §l➡ §bChecks for updates in-game.
                            #  §3/skytils editlocations §l➡ §bOpens the location editing GUI.
                            #  §3/skytils aliases §l➡ §bOpens the command alias editing GUI.
                            #  §3/skytils spamhider §l➡ §bOpens the command spam hider editing GUI.
                            #  §3/skytils enchant §l➡ §bOpens a GUI allowing you to rename enchants.
                            #  §3/skytils waypoints §l➡ §bOpens a GUI allowing you to modify waypoints.
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
                            #  §3/trackcooldown <length> <ability name> §l➡ §bTracks the cooldown of the specified ability.
                            #      §4Must have§c Item Cooldown Display§4 enabled to work.
                            #  §3/skytilshollowwaypoint <set/remove/clear/help> <name> <x y z> §l➡ §bAllows to set waypoints while in the Crystal Hollows. §7(Alias: §f/sthw§7)"
                        """.trimMargin("#")
            )
        )
    }

    @SubCommand("aliases", ["alias", "editaliases", "commandaliases"])
    fun aliases() {
        EssentialAPI.getGuiUtil().openScreen(CommandAliasesGui())
    }

    @SubCommand("editlocation", ["editlocations", "location", "locations", "loc", "gui"])
    fun editLocation() {
        EssentialAPI.getGuiUtil().openScreen(LocationEditGui())
    }

    @SubCommand("keyshortcuts", ["shortcuts"])
    fun shortcuts() {
        EssentialAPI.getGuiUtil().openScreen(KeyShortcutsGui())
    }

    @SubCommand("spamhider", ["spam"])
    fun spam() {
        EssentialAPI.getGuiUtil().openScreen(SpamHiderGui())
    }

    @SubCommand("armorcolor", ["armorcolour", "armourcolor", "armourcolour"])
    fun armorcolor(@DisplayName("<set/clearall/clear>") type: String, @DisplayName("(optional) hex color") hex: String?) {
        when (type.lowercase()) {
            "set" -> {
                if (hex == null) {
                    throw WrongUsageException("/skytils armorcolor set <hex color>")
                } else {
                    ArmorColorCommand.handleSet(hex)
                }
            }
            "clearall" -> {
                ArmorColorCommand.handleClearAll()
            }
            "clear" -> {
                ArmorColorCommand.handleClear()
            }
            else -> throw WrongUsageException("/skytils armorcolor <set/clearall/clear>")
        }
    }

    @SubCommand("swaphub")
    fun swapHub(@DisplayName("hub") hub: String?) {
        if (Utils.inSkyblock) {
            Skytils.sendMessageQueue.add("/warpforge")
            CoroutineScope(Dispatchers.Default).launch {
                delay(2000)
                Skytils.sendMessageQueue.add("/warp ${hub ?: "hub"}")
            }
        }
    }

    @SubCommand("spiritleapnames")
    fun spiritLeapNames() {
        EssentialAPI.getGuiUtil().openScreen(SpiritLeapNamesGui())
    }

    @SubCommand("dev")
    fun dev(@DisplayName("toggle") toggle: String) {
        DevTools.toggle(toggle)
        EssentialAPI.getMinecraftUtil().sendMessage(
            UTextComponent(
                "§c§lSkytils ➜ §c${
                    toggle
                } was toggled to: §6${
                    if (DevTools.allToggle) "Overriden by all toggle to ${DevTools.allToggle}" else DevTools.getToggle(
                        toggle
                    )
                }"
            )
        )
    }

    @SubCommand("enchant")
    fun enchant() {
        EssentialAPI.getGuiUtil().openScreen(EnchantNamesGui())
    }

    @SubCommand("update")
    fun update() {
        try {
            thread(block = UpdateChecker.updateGetter::run).join()
            if (UpdateChecker.updateGetter.updateObj == null) {
                return EssentialAPI.getMinecraftUtil()
                    .sendMessage(UTextComponent("§b§lSkytils §r§8➡ §cNo new update found"))
            }
            val message = UTextComponent(
                "§b§lSkytils §r§8➜ §7Update for version ${
                    UpdateChecker.updateGetter.updateObj?.get("tag_name")?.asString
                } is available! "
            )
            message.appendSibling(
                UTextComponent("§a[Update Now] ").setChatStyle(
                    ChatStyle().setChatClickEvent(
                        ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skytils updateNow")
                    ).setChatHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            UTextComponent("§eUpdates and restarts your game")
                        )
                    )
                )
            )
            message.appendSibling(
                UTextComponent("§b[Update Later] ").setChatStyle(
                    ChatStyle().setChatClickEvent(
                        ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skytils updateLater")
                    ).setChatHoverEvent(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            UTextComponent("§eUpdates after you close your game")
                        )
                    )
                )
            )
            return EssentialAPI.getMinecraftUtil().sendMessage(message)
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }

    @SubCommand("updatenow")
    fun updateNow() {
        EssentialAPI.getGuiUtil().openScreen(UpdateGui(true))
    }

    @SubCommand("updatelater")
    fun updateLater() {
        EssentialAPI.getGuiUtil().openScreen(UpdateGui(false))
    }

    @SubCommand("ping")
    fun ping() {
        Ping.invokedCommand = true
        Ping.sendPing()
    }

    @SubCommand("waypoints", ["waypoint"])
    fun waypoints() {
        EssentialAPI.getGuiUtil().openScreen(WaypointsGui())
    }


}