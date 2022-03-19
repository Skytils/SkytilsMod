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

package skytils.skytilsmod.features.impl.dungeons

import gg.essential.universal.UChat
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import kotlinx.coroutines.launch
import net.minecraft.event.ClickEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.hylin.mojang.AshconException
import skytils.hylin.request.HypixelAPIException
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.Pet
import skytils.hylin.skyblock.dungeons.DungeonClass
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.utils.*
import java.util.*
import kotlin.time.Duration

object PartyFinderStats {

    private val partyFinderRegex = Regex(
        "^Dungeon Finder > (?<name>\\w+) joined the dungeon group! \\((?<class>${
            DungeonClass.values().joinToString("|") { it.className }
        }) Level (?<classLevel>\\d+)\\)$"
    )

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.isOnHypixel || event.type == 2.toByte()) return
        if (Skytils.config.partyFinderStats) {
            val match = partyFinderRegex.find(event.message.formattedText.stripControlCodes()) ?: return
            val username = match.groups["name"]?.value ?: return
            if (username == mc.thePlayer.name) return
            Skytils.launch {
                val uuid = try {
                    Skytils.hylinAPI.getUUIDSync(username)
                } catch (e: AshconException) {
                    UChat.chat("§cFailed to get UUID, reason: ${e.message}")
                    return@launch
                }
                val profile = try {
                    Skytils.hylinAPI.getLatestSkyblockProfileForMemberSync(uuid)
                } catch (e: HypixelAPIException) {
                    UChat.chat("§cUnable to retrieve profile information: ${e.message}")
                    return@launch
                }
                if (profile != null) {
                    playerStats(username, uuid, profile)
                } else {
                    UChat.chat("§cUnable to retrieve profile information.")
                }
            }
        }
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

            val name = playerResponse.formattedName

            val secrets = playerResponse.achievements.getOrDefault("skyblock_treasure_hunter", 0)
            val component = UMessage("&2&l-----------------------------\n")
                .append(
                    UTextComponent("${name}'s §dStats: §7(Hover)\n")
                        .setHoverText("§7Click to run: /skytilscata $username")
                        .setClick(
                            ClickEvent.Action.RUN_COMMAND, "/skytilscata $username"
                        )
                )
                .append("§dCata: §b${NumberUtil.nf.format(cataLevel)}\n\n")
            for (armorPiece in profileData.armor?.items?.reversed() ?: emptyList()) {
                if (armorPiece == null) continue
                val lore = armorPiece.asMinecraft.getTooltip(mc.thePlayer, false)
                component.append(
                    UTextComponent("${armorPiece.asMinecraft.displayName}\n").setHoverText(
                        lore.joinToString(separator = "\n")
                    )
                )
            }
            val activePet = profileData.pets.find { it.active }
            val spiritPet = profileData.pets.find(Pet::isSpirit)

            if (activePet != null) {
                val rarity = ItemRarity.valueOf(activePet.tier.name).baseColor
                component.append(
                    UTextComponent("${rarity}${activePet.type.replace("_", " ").toTitleCase()}")
                        .setHoverText(
                            "§b" + (activePet.heldItem?.lowercase()
                                ?.replace("pet_item_", "")
                                ?.split('_')?.joinToString(" ") { it.toTitleCase() } ?: "§cNo Pet Item")
                        )
                )
                if (spiritPet != null) {
                    component.append(" §7(Spirit)\n\n")
                } else {
                    component.append("\n\n")
                }
            } else {
                component.append("§cNo Pet Equipped!\n\n")
            }

            val inv =
                profileData.inventory?.items?.mapNotNull {
                    ItemUtil.getExtraAttributes(it?.asMinecraft)
                }?.associateWith {
                    ItemUtil.getSkyBlockItemID(it)
                }
            if (inv != null) {
                val extraAttribs = inv.keys
                val itemIds = inv.values.toMutableSet()
                component.append(UTextComponent("§dItems: §7(Hover)\n\n").setHoverText(buildString {
                    //Archer
                    when {
                        itemIds.contains("TERMINATOR") -> {
                            append("§dTerminator: §a§l●\n")
                        }
                        itemIds.contains("JUJU_SHORTBOW") -> {
                            append("§5Juju: §a§l●\n")
                        }
                        else -> {
                            append("§dTerminator: §c§l●\n")
                        }
                    }
                    //Mage
                    when {
                        extraAttribs.any {
                            it.getTagList("ability_scroll", Constants.NBT.TAG_STRING).tagCount() == 3
                        } -> {
                            append("§dWither Impact: §a§l●\n")
                        }
                        itemIds.contains("MIDAS_STAFF") -> {
                            append("§6Midas Staff: §a§l●\n")
                        }
                        itemIds.contains("YETI_SWORD") -> {
                            append("§fYeti Sword: §a§l●\n")
                        }
                        itemIds.contains("BAT_WAND") -> {
                            append("§9Spirit Sceptre: §a§l●\n")
                        }
                        itemIds.contains("FROZEN_SCYTHE") -> {
                            append("§bFrozen Scythe: §a§l●\n")
                        }
                        else -> {
                            append("§dWither Impact: §c§l●\n")
                        }
                    }
                    //Berserk
                    when {
                        itemIds.contains("AXE_OF_THE_SHREDDED") -> {
                            append("§aAotS: §a§l●\n")
                        }
                        itemIds.contains("SHADOW_FURY") -> {
                            append("§8Shadow Fury: §a§l●\n")
                        }
                        itemIds.contains("FLOWER_OF_TRUTH") -> {
                            append("§cFoT: §a§l●\n")
                        }
                        else -> {
                            append("§aAotS: §c§l●\n")
                        }
                    }
                    //Miscellaneous
                    append(checkItemId(itemIds, "DARK_CLAYMORE", "§7Dark Claymore"))
                    append(checkItemId(itemIds, "GIANTS_SWORD", "§2Giant's Sword"))
                    append(checkItemId(itemIds, "ICE_SPRAY_WAND", "§bIce Spray"))
                    append(checkItemId(itemIds, "STONK", "§6Stonk"))
                    append(checkItemId(itemIds, "BONZO_STAFF", "§9Bonzo Staff"))
                    append(checkItemId(itemIds, "JERRY_STAFF", "§eJerry-chine"))
                }))
            } else {
                component.append("§cInventory API disabled!\n\n")
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
                .append(
                    UTextComponent("§c§l[KICK]\n").setHoverText("§cClick to kick ${name}§c.")
                        .setClick(ClickEvent.Action.SUGGEST_COMMAND, "/p kick $username")
                )
                .append("&2&l-----------------------------")
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

    private fun checkItemId(set: Set<String?>, itemId: String, itemName: String): String {
        return "${itemName}: §${if (set.contains(itemId)) 'a' else 'c'}§l●\n"
    }
}