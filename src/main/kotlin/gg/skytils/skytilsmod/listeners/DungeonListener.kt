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

package gg.skytils.skytilsmod.listeners

import gg.essential.lib.caffeine.cache.Cache
import gg.essential.lib.caffeine.cache.Caffeine
import gg.essential.lib.caffeine.cache.Expiry
import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.commands.impl.RepartyCommand
import gg.skytils.skytilsmod.core.TickTask
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation
import gg.skytils.skytilsmod.features.impl.handlers.CooldownTracker
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.NumberUtil.addSuffix
import gg.skytils.skytilsmod.utils.NumberUtil.romanToDecimal
import kotlinx.coroutines.launch
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.hylin.skyblock.Pet
import skytils.hylin.skyblock.dungeons.DungeonClass

object DungeonListener {

    val team = hashMapOf<String, DungeonTeammate>()
    val deads = hashSetOf<DungeonTeammate>()
    val missingPuzzles = hashSetOf<String>()
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
    private val deathRegex = Regex("§r§c ☠ §r§7(?:You were |(?:§.)+(?<username>\\w+)§r).* and became a ghost§r§7\\.§r")
    private val reviveRegex = Regex("^§r§a ❣ §r§7(?:§.)+(?<username>\\w+)§r§a was revived")

    @SubscribeEvent
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inDungeons) return
        if (event.packet is S02PacketChat) {
            val text = event.packet.chatComponent.formattedText
            if (event.packet.chatComponent.unformattedText.startsWith("Starting in 1 second.")) {
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
                        UChat.chat("§c☠ §lDeaths:§r ${team.values.sumOf { it.deaths }}\n${
                            team.values.filter { it.deaths > 0 }.sortedByDescending { it.deaths }.joinToString("\n") {
                                "  §c☠ ${it.playerName}:§r ${it.deaths}"
                            }
                        }"
                        )
                    }
                }
                if (Skytils.config.autoRepartyOnDungeonEnd) {
                    RepartyCommand.processCommand(mc.thePlayer, emptyArray())
                }
            } else if (text.startsWith("§r§c ☠ ") && text.endsWith(" and became a ghost§r§7.§r")) {
                val match = deathRegex.find(text) ?: return
                val username = match.groups["username"]?.value ?: mc.thePlayer.name
                val teammate = team[username] ?: return
                markDead(teammate)
            } else if (text.startsWith("§r§a ❣ ")) {
                val match = reviveRegex.find(text) ?: return
                val username = match.groups["username"]!!.value
                val teammate = team[username] ?: return
                if (deads.remove(teammate)) {
                    teammate.dead = false
                }
            }
        }
    }

    init {
        TickTask(4, repeats = true) {
            if (!Utils.inDungeons) return@TickTask
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
        }
        TickTask(2, repeats = true) {
            if (Utils.inDungeons && (DungeonTimer.scoreShownAt == -1L || System.currentTimeMillis() - DungeonTimer.scoreShownAt < 1500)) {
                val tabEntries = TabListUtils.tabEntries
                for (teammate in team.values) {
                    if (tabEntries.size <= teammate.tabEntryIndex) continue
                    val entry = tabEntries[teammate.tabEntryIndex].second
                    if (!entry.contains(teammate.playerName)) continue
                    teammate.player = mc.theWorld.playerEntities.find {
                        it.name == teammate.playerName && it.uniqueID.version() == 4
                    }
                    teammate.dead = entry.endsWith("§r§cDEAD§r§f)§r")
                    if (teammate.dead) {
                        markDead(teammate)
                    } else {
                        deads.remove(teammate)
                    }
                }
            }
        }
    }

    fun markDead(teammate: DungeonTeammate) {
        if (deads.add(teammate)) {
            val time = System.currentTimeMillis()
            val lastDeath = teammate.lastMarkedDead
            // there's no way they die twice in less than half a second
            if (lastDeath != null && time - lastDeath <= 500) return
            teammate.lastMarkedDead = time
            teammate.deaths++
            val totalDeaths = team.values.sumOf { it.deaths }
            val isFirstDeath = totalDeaths == 1

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
                        "§bThis is §e${teammate.playerName}§b's §e${teammate.deaths.addSuffix()}§b death out of §e${totalDeaths}§b total tracked deaths.${
                            " §6(SPIRIT)".toStringIfTrue(
                                `silly~churl, billy~churl, silly~billy hilichurl`
                            )
                        }"
                    )
                }
            }
        }
    }

    fun markAllRevived() {
        deads.clear()
        team.values.forEach {
            it.dead = false
        }
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
                team[name] =
                    DungeonTeammate(
                        name,
                        DungeonClass.getClassFromName(
                            dungeonClass
                        ), classLevel,
                        pos
                    )
            } else {
                println("Parsed teammate $name with value EMPTY, $text")
                team[name] = DungeonTeammate(
                    name,
                    DungeonClass.EMPTY, 0,
                    pos
                )
            }
        }
        if (partyCount != team.size) {
            UChat.chat("$failPrefix §cSomething isn't right! I expected $partyCount members but only got ${team.size}")
        }
        if (team.values.any { it.dungeonClass == DungeonClass.EMPTY }) {
            UChat.chat("$failPrefix §cSomething isn't right! One or more of your party members has an empty class! Could the server be lagging?")
        }
        CooldownTracker.updateCooldownReduction()
        checkSpiritPet()
    }

    fun checkSpiritPet() {
        if (Skytils.hylinAPI.key.isNotEmpty()) {
            Skytils.IO.launch {
                runCatching {
                    for (teammate in team.values) {
                        val name = teammate.playerName
                        if (hutaoFans.getIfPresent(name) != null) continue
                        val uuid = teammate.player?.uniqueID ?: MojangUtil.getUUIDFromUsername(name) ?: continue
                        val profile = Skytils.hylinAPI.getLatestSkyblockProfileForMemberSync(
                            uuid
                        ) ?: continue
                        hutaoFans[name] = profile.pets.any(Pet::isSpirit)
                    }
                    printDevMessage(hutaoFans.asMap().toString(), "spiritpet")
                }.onFailure {
                    it.printStackTrace()
                }
            }
        }
    }

    data class DungeonTeammate(
        val playerName: String,
        val dungeonClass: DungeonClass,
        val classLevel: Int,
        val tabEntryIndex: Int
    ) {
        var player: EntityPlayer? = null
        var dead = false
        var deaths = 0
        var lastMarkedDead: Long? = null


        fun canRender() = player != null && player!!.health > 0 && !dead
    }
}
