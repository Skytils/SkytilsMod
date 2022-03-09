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
package skytils.skytilsmod.listeners

import gg.essential.universal.UChat
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.hylin.extension.getString
import skytils.hylin.mojang.AshconException
import skytils.hylin.request.HypixelAPIException
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.Pet
import skytils.hylin.skyblock.item.Inventory
import skytils.hylin.skyblock.item.InventoryItem
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.commands.impl.RepartyCommand
import skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiNewChat
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.MathUtil.floor
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.math.floor
import kotlin.time.Duration

class ChatListener {
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.isOnHypixel || event.type == 2.toByte()) return
        val formatted = event.message.formattedText
        val unformatted = formatted.stripControlCodes()
        if (unformatted.startsWith("Your new API key is ") && event.message.siblings.size >= 1) {
            val apiKey = event.message.siblings[0].chatStyle.chatClickEvent.value
            Skytils.config.apiKey = apiKey
            Skytils.hylinAPI.key = Skytils.config.apiKey
            Skytils.config.markDirty()
            UChat.chat("§aSkytils updated your set Hypixel API key to §2${apiKey}")
            return
        }
        if(Skytils.config.partyFinderStats) {
            val joinedd = dungeon_finder_join_pattern.matcher(unformatted)
            if (joinedd.find()) {
                val username = joinedd.group(1)
                if(username.equals(mc.thePlayer.name)) {
                    return
                }
                Skytils.threadPool.submit {
                    val uuid = try {
                        (Skytils.hylinAPI.getUUIDSync(username))
                    } catch (e: AshconException) {
                        UChat.chat("§cFailed to get UUID, reason: ${e.message}")
                        return@submit
                    }
                    val profile = try {
                        Skytils.hylinAPI.getLatestSkyblockProfileForMemberSync(uuid)
                    } catch (e: HypixelAPIException) {
                        UChat.chat("§cUnable to retrieve profile information: ${e.message}")
                        return@submit
                    }
                    if (profile != null) {
                        playerStats(username, uuid, profile)
                    } else {
                        UChat.chat("§cUnable to retrieve profile information.")
                    }
                }
            }
        }

        if (Skytils.config.autoReparty) {
            if (formatted.endsWith("§r§ehas disbanded the party!§r")) {
                val matcher = playerPattern.matcher(unformatted)
                if (matcher.find()) {
                    lastPartyDisbander = matcher.group(1)
                    println("Party disbanded by $lastPartyDisbander")
                    Skytils.threadPool.submit {
                        if (Skytils.config.autoRepartyTimeout == 0) return@submit
                        try {
                            println("waiting for timeout")
                            Thread.sleep(Skytils.config.autoRepartyTimeout * 1000L)
                            lastPartyDisbander = ""
                            println("cleared last party disbander")
                        } catch (_: Exception) {
                        }
                    }
                    return
                }
            }
            if (unformatted.contains("You have 60 seconds to accept") && lastPartyDisbander.isNotEmpty() && event.message.siblings.size >= 7) {
                val acceptMessage = event.message.siblings[6].chatStyle
                if (acceptMessage.chatHoverEvent.value.unformattedText.contains(lastPartyDisbander)) {
                    Skytils.sendMessageQueue.add("/p accept $lastPartyDisbander")
                    lastPartyDisbander = ""
                    return
                }
            }
        }

        // Await delimiter
        if (awaitingDelimiter && unformatted.startsWith("---")) {
            event.isCanceled = true
            awaitingDelimiter = false
            return
        }

        // Reparty command
        // Getting party
        if (RepartyCommand.gettingParty) {
            if (unformatted.contains("-----")) {
                when (RepartyCommand.Delimiter) {
                    0 -> {
                        println("Get Party Delimiter Cancelled")
                        RepartyCommand.Delimiter++
                        event.isCanceled = true
                        return
                    }
                    1 -> {
                        println("Done querying party")
                        RepartyCommand.gettingParty = false
                        RepartyCommand.Delimiter = 0
                        event.isCanceled = true
                        return
                    }
                }
            } else if (unformatted.startsWith("Party M") || unformatted.startsWith("Party Leader")) {
                val player = mc.thePlayer
                val partyStart = party_start_pattern.matcher(unformatted)
                val leader = leader_pattern.matcher(unformatted)
                val members = members_pattern.matcher(unformatted)
                if (partyStart.matches() && partyStart.group(1).toInt() == 1) {
                    player.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "You cannot reparty yourself."))
                    RepartyCommand.partyThread!!.interrupt()
                } else if (leader.matches() && leader.group(1) != player.name) {
                    player.addChatMessage(ChatComponentText(EnumChatFormatting.RED.toString() + "You are not party leader."))
                    RepartyCommand.partyThread!!.interrupt()
                } else {
                    while (members.find()) {
                        val partyMember = members.group(1)
                        if (partyMember != player.name) {
                            RepartyCommand.party.add(partyMember)
                            println(partyMember)
                        }
                    }
                }
                event.isCanceled = true
                return
            }
        }
        // Disbanding party
        if (RepartyCommand.disbanding) {
            if (unformatted.contains("-----")) {
                when (RepartyCommand.Delimiter) {
                    0 -> {
                        println("Disband Delimiter Cancelled")
                        RepartyCommand.Delimiter++
                        event.isCanceled = true
                        return
                    }
                    1 -> {
                        println("Done disbanding")
                        RepartyCommand.disbanding = false
                        RepartyCommand.Delimiter = 0
                        event.isCanceled = true
                        return
                    }
                }
            } else if (unformatted.endsWith("has disbanded the party!")) {
                event.isCanceled = true
                return
            }
        }
        // Inviting
        if (RepartyCommand.inviting) {
            if (unformatted.endsWith(" to the party! They have 60 seconds to accept.")) {
                val invitee = invitePattern.matcher(unformatted)
                if (invitee.find()) {
                    println("" + invitee.group(1) + ": " + RepartyCommand.repartyFailList.remove(invitee.group(1)))
                }
                tryRemoveLineAtIndex(1)
                awaitingDelimiter = true
                event.isCanceled = true
                println("Player Invited!")
                RepartyCommand.inviting = false
                return
            } else if (unformatted.contains("Couldn't find a player") || unformatted.contains("You cannot invite that player")) {
                tryRemoveLineAtIndex(1)
                event.isCanceled = true
                println("Player Invited!")
                RepartyCommand.inviting = false
                return
            }
        }
        // Fail Inviting
        if (RepartyCommand.failInviting) {
            if (unformatted.endsWith(" to the party! They have 60 seconds to accept.")) {
                val invitee = invitePattern.matcher(unformatted)
                if (invitee.find()) {
                    println("" + invitee.group(1) + ": " + RepartyCommand.repartyFailList.remove(invitee.group(1)))
                }
                tryRemoveLineAtIndex(1)
                event.isCanceled = true
                RepartyCommand.inviting = false
                return
            } else if (unformatted.contains("Couldn't find a player") || unformatted.contains("You cannot invite that player")) {
                tryRemoveLineAtIndex(1)
                event.isCanceled = true
                println("Player Invited!")
                RepartyCommand.inviting = false
                return
            }
        }
        if (Skytils.config.firstLaunch && unformatted == "Welcome to Hypixel SkyBlock!") {
            UChat.chat("§bThank you for downloading Skytils!")
            ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/skytils help")
            Skytils.config.firstLaunch = false
            Skytils.config.markDirty()
            Skytils.config.writeData()
        }
    }

    private fun tryRemoveLineAtIndex(index: Int) {
        val lines = (mc.ingameGUI.chatGUI as AccessorGuiNewChat).drawnChatLines
        if (lines.size > index) {
            lines.removeAt(index)
        }
    }

    companion object {
        private var lastPartyDisbander = ""
        private val invitePattern = Pattern.compile("(?:(?:\\[.+?] )?(?:\\w+) invited )(?:\\[.+?] )?(\\w+)")
        private val playerPattern = Pattern.compile("(?:\\[.+?] )?(\\w+)")
        private val party_start_pattern = Pattern.compile("^Party Members \\((\\d+)\\)$")
        private val leader_pattern = Pattern.compile("^Party Leader: (?:\\[.+?] )?(\\w+) ●$")
        private val members_pattern = Pattern.compile(" (?:\\[.+?] )?(\\w+) ●")
        private val dungeon_finder_join_pattern = Pattern.compile("^Dungeon Finder > (\\w+) joined the dungeon group! \\(([A-Z][a-z]+) Level (\\d+)\\)$")
        private var awaitingDelimiter = false
    }
    private fun playerStats(username: String, uuid: UUID, profileData: Member) {

        val playerResponse = try {
            Skytils.hylinAPI.getPlayerSync(uuid)
        } catch (e: HypixelAPIException) {
            UChat.chat("§cFailed to get dungeon stats: ${e.message}")
            return
        }

        try {
            val dungeonsData = profileData.dungeons

            val catacombsObj = dungeonsData.dungeons["catacombs"]
            if (catacombsObj?.experience == null) {
                UChat.chat("§c${username} has not entered The Catacombs!")
                return
            }
            val cataData = catacombsObj.normal!!
            val masterCataData = catacombsObj.master

            val cataLevel =
                SkillUtils.calcXpWithProgress(catacombsObj.experience ?: 0.0, SkillUtils.dungeoneeringXp.values)

            val secrets = playerResponse.achievements.getOrDefault("skyblock_treasure_hunter", 0)
            val name = playerResponse.rankPrefix + if(playerResponse.rank.toString() == "NONE") {""} else {" "} + playerResponse.player.getString("displayname")
            val component = UMessage("&2&l-----------------------------\n")
                .append(
                    UTextComponent("$name's §dStats: §7(Hover)\n").setHoverText("§7Click to run: /skytilscata $username").setClick(
                        ClickEvent.Action.RUN_COMMAND, "/skytilscata $username")
                )
                .append("§dCata: §b${NumberUtil.nf.format(floor(cataLevel))}\n\n")
            val armor = profileData.armor
            val armorArray:ArrayList<List<String>> = ArrayList()
            armor?.forEveryItem {
                val armorLore = it.asMinecraft.getTooltip(mc.thePlayer, false)
                armorArray.add(armorLore)
            }
            armorArray.reverse()
            for (armorPiece in armorArray) {
                val armorName = armorPiece[0]
                component.append(UTextComponent(armorName + "\n").setHoverText(buildString {
                    for (line in armorPiece) {
                        append(line + "\n")
                    }
                }))
            }
            val pets = profileData.pets
            var activePet: Pet? = null
            for (pet in pets) {
                if (pet.active) {
                    activePet = pet
                }
            }
            if (activePet != null) {
                var rarity = "§f"
                when(activePet.tier.toString().uppercase()) {
                    ItemRarity.UNCOMMON.rarityName -> rarity = "§a"
                    ItemRarity.RARE.rarityName -> rarity = "§9"
                    ItemRarity.EPIC.rarityName -> rarity = "§5"
                    ItemRarity.LEGENDARY.rarityName -> rarity = "§6"
                    ItemRarity.MYTHIC.rarityName -> rarity = "§d"
                }

                component.append(UTextComponent(rarity + activePet.type.lowercase().replace("_", " ")
                    .replaceFirstChar { it.uppercase() }
                        + "\n\n")
                    .setHoverText( if(activePet.heldItem == null) {"§cNo Pet Item"} else "§b" +
                            activePet.heldItem!!.lowercase().replace("pet_item_", "")
                                .replace("_", " ").replaceFirstChar { it.uppercase() }
                    )
                )
            } else {
                component.append("§cNo Pet Equiped!\n\n")
            }

            //Stonk etc. stuff
            val inventory = profileData.inventory
            if (inventory != null) {
                //FIX THIS MESS
                var hasStonk = false
                var hasBonzoStaff = false
                var hasJerryGun = false
                var hasNecronsBladeVariant = false
                var hasTerminator = false
                var hasJujuShortbow = false
                var hasSpoon = false
                var hasYetiSword = false
                var hasSpiritSceptre = false
                var hasFrozenSycthe = false
                var hasAots = false
                var hasGiantSword = false
                var hasShadowFury = false
                var hasFlowerOfTruth = false

                inventory.forEveryItem {
                    val itemName = it.asMinecraft.displayName
                    if (itemName.contains("Stonk")) {
                        hasStonk = true
                    } else if (itemName.contains("Jerry-chine Gun")) {
                        hasJerryGun = true
                    } else if (itemName.contains("Bonzo's Staff")) {
                        hasBonzoStaff = true
                    } else if (itemName.contains("Hyperion") || itemName.contains("Valkyrie") || itemName.contains("Astraea") || itemName.contains("Scylla")) {
                        hasNecronsBladeVariant = true
                    } else if (itemName.contains("Terminator")) {
                        hasTerminator = true
                    } else if (itemName.contains("Juju Shortbow")) {
                        hasJujuShortbow = true
                    } else if (itemName.contains("Midas Staff")) {
                        hasSpoon = true
                    } else if (itemName.contains("Yeti Sword")) {
                        hasYetiSword = true
                    } else if (itemName.contains("Spirit Sceptre")) {
                        hasSpiritSceptre = true
                    } else if (itemName.contains("Frozen Sycthe")) {
                        hasFrozenSycthe = true
                    } else if (itemName.contains("Axe of the Shredded")) {
                        hasAots = true
                    } else if (itemName.contains("Giant's Sword")) {
                        hasGiantSword = true
                    } else if (itemName.contains("Shadow Fury")) {
                        hasShadowFury = true
                    } else if (itemName.contains("Flower of Truth")) {
                        hasFlowerOfTruth = true
                    }
                }

                component.append(UTextComponent("§dItems: §7(Hover)\n\n").setHoverText(buildString {
                    //Archer
                    if(hasTerminator) {
                        append("§dTerminator: §a§l●\n")
                    } else if(hasJujuShortbow) {
                        append("§5Juju: §a§l●\n")
                    } else {
                        append("§dTerminator: §c§l●\n")
                    }
                    //Mage
                    if(hasNecronsBladeVariant) {
                        append("§dNecron's Blade: §a§l●\n")
                    } else if(hasSpoon) {
                        append("§6Midas Spoon: §a§l●\n")
                    } else if(hasYetiSword) {
                        append("§fYeti Sword: §a§l●\n")
                    } else if(hasSpiritSceptre) {
                        append("§9Spirit Sceptre: §a§l●\n")
                    } else if(hasFrozenSycthe) {
                        append("§bFrozen Sycthe: §a§l●\n")
                    } else {
                        append("§dNecron's Blade: §c§l●\n")
                    }
                    //Berserk
                    if(hasGiantSword) {
                        append("§2Giant's Sword: §a§l●\n")
                    } else if(hasAots) {
                        append("§aAots: §a§l●\n")
                    } else if(hasShadowFury) {
                        append("§8Shadow Fury: §a§l●\n")
                    } else if(hasFlowerOfTruth) {
                        append("§cFot: §a§l●\n")
                    } else {
                        append("§2Giant's Sword: §c§l●\n")
                    }

                    if(hasStonk) { append("§6Stonk: §a§l●\n") } else { append("§6Stonk: §c§l●\n") }
                    if(hasBonzoStaff) { append("§9Bonzo Staff: §a§l●\n") } else { append("§9Bonzo Staff: §c§l●\n") }
                    if(hasJerryGun) { append("§eJerry Gun: §a§l●\n") } else { append("§eJerry Gun: §c§l●\n") }
                }))
            } else {
                component.append("§cInventory Api disabled!\n\n")
            }
            val completionObj = cataData.completions
            val highestFloor = cataData.highestCompletion

            if (completionObj != null && highestFloor != null) {
                component.append(UTextComponent("§aFloor Completions: §7(Hover)\n").setHoverText(buildString {
                    for (i in 0..highestFloor) {
                        append("§2§l●§a ")
                        append(if (i == 0) "Entrance: " else "Floor $i: ")
                        append("§6")
                        append(completionObj[i])
                        append(if (i < highestFloor) "\n" else "")
                    }
                }))

                val fastestSPlusTimes = cataData.fastestTimeSPlus
                if (fastestSPlusTimes != null) {
                    component.append(
                        UTextComponent("§aFastest §6S+ §aCompletions: §7(Hover)\n\n").setHoverText(
                            buildString {
                                for (i in 0..highestFloor) {
                                    append("§2§l●§a ")
                                    append(if (i == 0) "Entrance: " else "Floor $i: ")
                                    append("§6")
                                    append(fastestSPlusTimes[i]?.timeFormat() ?: "§cNo S+ Completion")
                                    append(if (i < highestFloor) "\n" else "")
                                }
                            }
                        )
                    )
                }
            }

            if (masterCataData?.completions != null) {
                val masterCompletionObj = masterCataData.completions
                val highestMasterFloor = masterCataData.highestCompletion

                if (masterCompletionObj != null && highestMasterFloor != null) {

                    component.append(UTextComponent("§l§4MM §cFloor Completions: §7(Hover)\n").setHoverText(buildString {
                        for (i in 1..highestMasterFloor) {
                            append("§4§l●§c ")
                            append("Floor $i: ")
                            append("§6")
                            append(if (i in masterCompletionObj) masterCompletionObj[i] else "§cDNF")
                            append(if (i < highestMasterFloor) "\n" else "")
                        }
                    }))

                    val masterFastestSPlus = UTextComponent("§l§4MM §cFastest §6S+ §cCompletions: §7(Hover)\n\n")

                    if (masterCataData.fastestTimeSPlus != null) {
                        val masterFastestSPlusTimes = masterCataData.fastestTimeSPlus!!
                        masterFastestSPlus.setHoverText(buildString {
                            for (i in 1..highestMasterFloor) {
                                append("§4§l●§c ")
                                append("Floor $i: ")
                                append("§6")
                                append(masterFastestSPlusTimes[i]?.timeFormat() ?: "§cNo S+ Completion")
                                append(if (i < highestMasterFloor) "\n" else "")
                            }
                        })
                    } else {
                        masterFastestSPlus.setHoverText(
                            "§cNo S+ Completions"
                        )
                    }
                    component.append(masterFastestSPlus)
                }
            }

            component
                .append("§aTotal Secrets Found: §l§6${NumberUtil.nf.format(secrets)}\n\n")
                .append(UTextComponent("§c§l[KICK]\n").setHoverText("§cClick to Kick $name.").setClick(ClickEvent.Action.SUGGEST_COMMAND, "/p kick $username"))
                .append("&2&l-----------------------------")
                .mutable()
                .chat()
        } catch (e: Throwable) {
            UChat.chat("§cCatacombs XP Lookup Failed: ${e.message ?: e::class.simpleName}")
            e.printStackTrace()
        }
    }
    private fun Duration.timeFormat() = toComponents { minutes, seconds, _ ->
        buildString {
            if (minutes > 0) {
                append(minutes)
                append(':')
            }
            append("%02d".format(seconds))
        }
    }
}
