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

package gg.skytils.skytilsmod.features.impl.dungeons

import gg.essential.universal.UChat
import gg.essential.universal.wrappers.message.UMessage
import gg.essential.universal.wrappers.message.UTextComponent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.NumberUtil.toRoman
import gg.skytils.skytilsmod.utils.SkillUtils.level
import kotlinx.coroutines.launch
import net.minecraft.event.ClickEvent
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.Pet
import skytils.hylin.skyblock.dungeons.DungeonClass
import java.util.*
import kotlin.time.Duration

object PartyFinderStats {

    private val partyFinderRegex = Regex(
        "^Party Finder > (?<name>\\w+) joined the dungeon group! \\((?<class>${
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
                    MojangUtil.getUUIDFromUsername(username)
                } catch (e: MojangUtil.MojangException) {
                    e.printStackTrace()
                    UChat.chat("$failPrefix §cFailed to get UUID, reason: ${e.message}")
                    return@launch
                } ?: return@launch
                Skytils.hylinAPI.getLatestSkyblockProfileForMember(uuid).whenComplete { profile ->
                    profile?.run { playerStats(username, uuid, this) }
                }.catch { e ->
                    e.printStackTrace()
                    UChat.chat(
                        "$failPrefix §cUnable to retrieve profile information: ${
                            e.message
                        }"
                    )
                }
            }
        }
    }

    private fun playerStats(username: String, uuid: UUID, profileData: Member) {
        Skytils.hylinAPI.getPlayer(uuid).whenComplete { playerResponse ->
            try {
                profileData.dungeons.dungeons["catacombs"]?.let { catacombsObj ->
                    val cataData = catacombsObj.normal!!
                    val masterCataData = catacombsObj.master

                    val cataLevel =
                        SkillUtils.calcXpWithProgress(catacombsObj.experience ?: 0.0, SkillUtils.dungeoneeringXp.values)

                    val name = playerResponse.formattedName

                    val secrets = playerResponse.achievements.getOrDefault("skyblock_treasure_hunter", 0)
                    val component = UMessage("&2&m--------------------------------\n")
                        .append("$name §8» §dCata §9${NumberUtil.nf.format(cataLevel)} ")
                        .append(
                            UTextComponent("§7[Stats]\n\n")
                                .setHoverText("§7Click to run: /skytilscata $username")
                                .setClick(
                                    ClickEvent.Action.RUN_COMMAND, "/skytilscata $username"
                                )
                        )
                    profileData.armor?.items?.reversed()?.filterNotNull()?.forEach { armorPiece ->
                        val lore = armorPiece.asMinecraft.getTooltip(mc.thePlayer, false)
                        component.append(
                            UTextComponent("${armorPiece.asMinecraft.displayName}\n").setHoverText(
                                lore.joinToString(separator = "\n")
                            )
                        )
                    }

                    profileData.pets.find { it.active }?.let { activePet ->
                        val rarity = ItemRarity.valueOf(activePet.tier.name).baseColor
                        component.append(
                            UTextComponent(
                                "§7[Lvl ${activePet.level}] ${rarity}${
                                    activePet.type.splitToWords()
                                }"
                            )
                                .setHoverText(
                                    "§b" + (activePet.heldItem?.lowercase()
                                        ?.replace("pet_item_", "")
                                        ?.splitToWords() ?: "§cNo Pet Item")
                                )
                        )
                    } ?: component.append("§cNo Pet Equipped!")
                    profileData.pets.find(Pet::isSpirit)?.run {
                        component.append(" §7(§6Spirit§7)\n\n")
                    } ?: component.append(" §7(No Spirit)\n\n")

                    profileData.inventory?.items?.mapNotNull {
                        ItemUtil.getExtraAttributes(it?.asMinecraft)
                    }?.associateWith {
                        ItemUtil.getSkyBlockItemID(it)
                    }?.let { (extraAttribs, itemIds) ->
                        val items = buildSet {
                            //Archer
                            when {
                                itemIds.contains("TERMINATOR") -> add("§5Terminator")
                                itemIds.contains("JUJU_SHORTBOW") -> add("§5Juju")
                            }
                            //Mage
                            when {
                                extraAttribs.any {
                                    it.getTagList("ability_scroll", Constants.NBT.TAG_STRING).tagCount() == 3
                                } -> add("§dWither Impact")

                                itemIds.contains("MIDAS_STAFF") -> add("§6Midas Staff")
                                itemIds.contains("YETI_SWORD") -> add("§fYeti Sword")
                                itemIds.contains("BAT_WAND") -> add("§9Spirit Sceptre")
                                itemIds.contains("FROZEN_SCYTHE") -> add("§bFrozen Scythe")
                            }
                            //Berserk
                            when {
                                itemIds.contains("AXE_OF_THE_SHREDDED") -> add("§aAotS")
                                itemIds.contains("SHADOW_FURY") -> add("§8Shadow Fury")
                                itemIds.contains("FLOWER_OF_TRUTH") -> add("§cFoT")
                            }
                            //Miscellaneous
                            add(checkItemId(itemIds, "DARK_CLAYMORE", "§7Dark Claymore"))
                            add(checkItemId(itemIds, "GIANTS_SWORD", "§2Giant's Sword"))
                            add(checkItemId(itemIds, "ICE_SPRAY_WAND", "§bIce Spray"))
                            checkStonk(itemIds, extraAttribs)?.run { add(this) }
                            add(checkItemId(itemIds, "BONZO_STAFF", "§9Bonzo Staff"))
                            add(checkItemId(itemIds, "JERRY_STAFF", "§eJerry-chine"))

                            remove(null)
                        }
                        component.append(
                            UTextComponent("§dImportant Items: §7(Hover)\n\n").setHoverText(
                                items.joinToString(
                                    "§8, "
                                ).ifBlank { "§c§lNone" }
                            )
                        )
                    } ?: component.append("§cInventory API disabled!\n\n")

                    cataData.highestCompletion?.let { highestFloor ->
                        val completionObj = cataData.completions!!
                        component.append(UTextComponent("§aFloor Completions: §7(Hover)\n").setHoverText(buildString {
                            for (i in 0..highestFloor) {
                                append("§2§l●§a ")
                                append(if (i == 0) "Entrance: " else "Floor $i: ")
                                append("§e")
                                if (i in completionObj) {
                                    append(completionObj[i])
                                    append(" §7(§6S+ §e")
                                    append(cataData.fastestTimeSPlus?.get(i)?.timeFormat() ?: "§cNo Comp")
                                    append("§7)")
                                } else append("§cDNF")
                                if (i != highestFloor)
                                    append("\n")
                            }
                        }))
                    }

                    masterCataData?.highestCompletion?.let { highestFloor ->
                        val masterCompletionObj = masterCataData.completions!!
                        component.append(
                            UTextComponent("§l§4MM §cFloor Completions: §7(Hover)\n").setHoverText(
                                buildString {
                                    for (i in 1..highestFloor) {
                                        append("§c§l●§4 ")
                                        append("Floor $i: ")
                                        append("§e")
                                        if (i in masterCompletionObj) {
                                            append(masterCompletionObj[i])
                                            append(" §7(§6S+ §e")
                                            append(masterCataData.fastestTimeSPlus?.get(i)?.timeFormat() ?: "§cNo Comp")
                                            append("§7)")
                                        } else append("§cDNF")
                                        if (i != highestFloor)
                                            append("\n")
                                    }
                                })
                        )
                    }

                    component
                        .append("\n§aTotal Secrets Found: §l§e${NumberUtil.nf.format(secrets)}\n")
                        .append(
                            "§aBlood Mobs Killed: §l§e${
                                NumberUtil.nf.format(
                                    (profileData.stats?.get("kills_watcher_summon_undead") ?: 0) +
                                            (profileData.stats?.get("kills_master_watcher_summon_undead") ?: 0)
                                )
                            }\n\n"
                        )
                        .append(
                            UTextComponent("§c§l[KICK]\n").setHoverText("§cClick to kick ${name}§c.")
                                .setClick(ClickEvent.Action.SUGGEST_COMMAND, "/p kick $username")
                        )
                        .append("&2&m--------------------------------")
                        .chat()
                } ?: UChat.chat("$failPrefix §c$username has not entered The Catacombs!")
            } catch (e: Throwable) {
                UChat.chat("$failPrefix §cCatacombs XP Lookup Failed: ${e.message ?: e::class.simpleName}")
                e.printStackTrace()
            }
        }.catch { e ->
            e.printStackTrace()
            UChat.chat(
                "$failPrefix §cFailed to get dungeon stats: ${
                    e.message
                }"
            )
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

    private fun checkItemId(set: Set<String?>, itemId: String, itemName: String): String? {
        return if (set.contains(itemId)) itemName else null
    }

    private fun checkStonk(items: Set<String?>, tags: Set<NBTTagCompound?>): String? {
        val eff = tags.mapNotNull { it?.getCompoundTag("enchantments")?.getInteger("efficiency") }.maxOrNull() ?: 0
        return when {
            eff >= 7 -> "§6Efficiency ${eff.toRoman()}"
            items.contains("STONK") -> "§6Stonk"
            else -> null
        }
    }
}

private operator fun <K, V> Map<K, V>.component1(): Set<K> = keys
private operator fun <K, V> Map<K, V>.component2(): Set<V> = values.toMutableSet()
