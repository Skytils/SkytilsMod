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

import gg.essential.lib.caffeine.cache.Cache
import gg.essential.lib.caffeine.cache.Caffeine
import gg.essential.lib.caffeine.cache.Expiry
import gg.essential.universal.UChat
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import skytils.hylin.skyblock.item.Tier
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.commands.impl.RepartyCommand
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.events.impl.MainReceivePacketEvent
import skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import skytils.skytilsmod.features.impl.dungeons.ScoreCalculation
import skytils.skytilsmod.features.impl.handlers.CooldownTracker
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.NumberUtil.addSuffix
import skytils.skytilsmod.utils.NumberUtil.romanToDecimal

object DungeonListener {

    val team = HashSet<DungeonTeammate>()
    val deads = HashSet<DungeonTeammate>()
    val missingPuzzles = HashSet<String>()
    val hutaoFans: Cache<String, Boolean> = Caffeine.newBuilder()
        .weakKeys()
        .weakValues()
        .maximumSize(100L)
        .expireAfter(object : Expiry<String, Boolean> {
            val hour = 3600000000000L
            val tenMinutes = 600000000000L


            override fun expireAfterCreate(
                key: String, value: Boolean, currentTime: Long
            ): Long {
                return if (value) hour else tenMinutes
            }

            override fun expireAfterUpdate(
                key: String, value: Boolean, currentTime: Long, currentDuration: Long
            ): Long {
                return currentDuration
            }

            override fun expireAfterRead(
                key: String, value: Boolean, currentTime: Long, currentDuration: Long
            ): Long {
                return currentDuration
            }

        })
        .build()

    val partyCountPattern = Regex("§r {9}§r§b§lParty §r§f\\((?<count>[1-5])\\)§r")
    private val classPattern =
        Regex("§r(?:§.)+(?:\\[.+] )?(?<name>\\w+?)(?:§.)* (?:§r(?:§[\\da-fklmno]){1,2}.+ )?§r§f\\(§r§d(?:(?<class>Archer|Berserk|Healer|Mage|Tank) (?<lvl>\\w+)|§r§7EMPTY)§r§f\\)§r")
    private val missingPuzzlePattern = Regex("§r (?<puzzle>.+): §r§7\\[§r§6§l✦§r§7] ?§r")

    private var ticks = 0

    @SubscribeEvent
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inDungeons) return
        if (event.packet is S02PacketChat) {
            if (event.packet.chatComponent.unformattedText.startsWith("Dungeon starts in 1 second.")) {
                team.clear()
                deads.clear()
                missingPuzzles.clear()
                TickTask(40) {
                    getMembers()
                }
            } else if (event.packet.chatComponent.unformattedText.stripControlCodes()
                    .trim() == "> EXTRA STATS <"
            ) {
                if (Skytils.config.dungeonDeathCounter) {
                    TickTask(6) {
                        UChat.chat("§c☠ §lDeaths:§r ${team.sumOf { it.deaths }}\n${
                            team.filter { it.deaths > 0 }.sortedByDescending { it.deaths }.joinToString("\n") {
                                "  §c☠ ${it.playerName}:§r ${it.deaths}"
                            }
                        }"
                        )
                    }
                }
                if (Skytils.config.autoRepartyOnDungeonEnd) {
                    RepartyCommand.processCommand(mc.thePlayer, emptyArray())
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!Utils.inDungeons) return
        if (event.phase != TickEvent.Phase.START) return
        if (ticks % 4 == 0) {
            val localMissingPuzzles = TabListUtils.tabEntries.mapNotNull {
                val name = it.second
                if (name.contains("✦")) {
                    val matcher = missingPuzzlePattern.find(name)
                    if (matcher != null) {
                        val puzzleName = matcher.groups["puzzle"]!!.value
                        if (puzzleName != "???") {
                            return@mapNotNull puzzleName
                        }
                    }
                }
                return@mapNotNull null
            }
            missingPuzzles.clear()
            missingPuzzles.addAll(localMissingPuzzles)
            ticks = 0
        }
        if (ticks % 2 == 0) {
            if (DungeonTimer.scoreShownAt == -1L || System.currentTimeMillis() - DungeonTimer.scoreShownAt < 1500) {
                val tabEntries = TabListUtils.tabEntries
                for (teammate in team) {
                    if (tabEntries.size <= teammate.tabEntryIndex) continue
                    val entry = tabEntries[teammate.tabEntryIndex].second
                    if (!entry.contains(teammate.playerName)) continue
                    teammate.player = mc.theWorld.playerEntities.find {
                        it.name == teammate.playerName && it.uniqueID.version() == 4
                    }
                    teammate.dead = entry.endsWith("§r§cDEAD§r§f)§r")
                    if (teammate.dead) {
                        if (deads.add(teammate)) {
                            teammate.deaths++
                            val isFirstDeath = team.sumOf { it.deaths } == 1

                            @Suppress("LocalVariableName")
                            val `silly~churl, billy~churl, silly~billy hilichurl` = if (isFirstDeath) {
                                val hutaoIsCool = hutaoFans.getIfPresent(teammate.playerName) ?: false
                                ScoreCalculation.firstDeathHadSpirit.set(hutaoIsCool)
                                hutaoIsCool
                            } else false
                            printDevMessage(isFirstDeath.toString(), "spiritpet")
                            printDevMessage(ScoreCalculation.firstDeathHadSpirit.toString(), "spiritpet")
                            if (Skytils.config.dungeonDeathCounter) {
                                TickTask(1) {
                                    UChat.chat(
                                        "§bThis is §e${teammate.playerName}§b's §e${teammate.deaths.addSuffix()}§b death out of §e${team.sumOf { it.deaths }}§b total deaths.${
                                            " §6(SPIRIT)".toStringIfTrue(
                                                `silly~churl, billy~churl, silly~billy hilichurl`
                                            )
                                        }"
                                    )
                                }
                            }
                        }
                    } else {
                        deads.remove(teammate)
                    }
                }
            }
        }
        ticks++
    }

    private fun getMembers() {
        if (team.isNotEmpty() || !Utils.inDungeons) return
        val tabEntries = TabListUtils.tabEntries

        if (tabEntries.isEmpty() || !tabEntries[0].second.contains("§r§b§lParty §r§f(")) {
            TickTask(5) {
                getMembers()
            }
            return
        }

        val partyCount = partyCountPattern.find(tabEntries[0].second)?.groupValues?.get(1)?.toIntOrNull()
        if (partyCount == null) {
            println("Couldn't get party count")
            TickTask(5) {
                getMembers()
            }
            return
        }
        println("There are $partyCount members in this party")
        for (i in 0 until partyCount) {
            val pos = 1 + i * 4
            val text = tabEntries[pos].second
            val matcher = classPattern.find(text)
            if (matcher == null) {
                println("Skipping over entry $text due to it not matching")
                continue
            }
            val name = matcher.groups["name"]!!.value
            if (matcher.groups["class"] != null) {
                val dungeonClass = matcher.groups["class"]!!.value
                val classLevel = matcher.groups["lvl"]!!.value.romanToDecimal()
                println("Parsed teammate $name, they are a $dungeonClass $classLevel")
                team.add(
                    DungeonTeammate(
                        name,
                        DungeonClass.getClassFromName(
                            dungeonClass
                        ), classLevel,
                        pos
                    )
                )
            } else {
                println("Parsed teammate $name with value EMPTY, $text")
                team.add(
                    DungeonTeammate(
                        name,
                        DungeonClass.EMPTY, 0,
                        pos
                    )
                )
            }
        }
        if (partyCount != team.size) {
            UChat.chat("§9§lSkytils §8» §cSomething isn't right! I expected $partyCount members but only got ${team.size}")
        }
        if (team.any { it.dungeonClass == DungeonClass.EMPTY }) {
            UChat.chat("§9§lSkytils §8» §cSomething isn't right! One or more of your party members has an empty class! Could the server be lagging?")
        }
        CooldownTracker.updateCooldownReduction()
        checkSpiritPet()
    }

    fun checkSpiritPet() {
        if (Skytils.hylinAPI.key.isNotEmpty()) {
            Skytils.threadPool.submit {
                runCatching {
                    for (teammate in team) {
                        val name = teammate.playerName
                        if (hutaoFans.getIfPresent(name) != null) continue
                        val uuid = teammate.player?.uniqueID ?: Skytils.hylinAPI.getUUIDSync(
                            name
                        )
                        val profile = Skytils.hylinAPI.getLatestSkyblockProfileForMemberSync(
                            uuid
                        ) ?: continue
                        hutaoFans[name] = profile.pets.any {
                            it.type == "SPIRIT" && (it.tier == Tier.LEGENDARY || (it.heldItem == "PET_ITEM_TIER_BOOST" && it.tier == Tier.EPIC))
                        }
                    }
                    printDevMessage(hutaoFans.asMap().toString(), "spiritpet")
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    class DungeonTeammate(
        val playerName: String,
        val dungeonClass: DungeonClass,
        val classLevel: Int,
        val tabEntryIndex: Int
    ) {
        var player: EntityPlayer? = null
        var dead = false
        var deaths = 0


        fun canRender() = player != null && player!!.health > 0 && !dead
    }

    @Suppress("unused")
    enum class DungeonClass(val className: String) {
        ARCHER("Archer"),
        BERSERK("Berserk"),
        MAGE("Mage"),
        HEALER("Healer"),
        TANK("Tank"),
        EMPTY("Empty");

        companion object {
            fun getClassFromName(name: String): DungeonClass {
                return values().find { it.className.lowercase() == name.lowercase() }
                    ?: throw IllegalArgumentException("No class could be found for the name $name")
            }
        }
    }

}
