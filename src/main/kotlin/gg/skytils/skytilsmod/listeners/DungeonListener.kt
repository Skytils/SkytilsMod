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
import gg.skytils.hypixel.types.skyblock.Pet
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.IO
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.commands.impl.RepartyCommand
import gg.skytils.skytilsmod.core.API
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.events.impl.MainReceivePacketEvent
import gg.skytils.skytilsmod.events.impl.skyblock.DungeonEvent
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer
import gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.DungeonMapPlayer
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.Room
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.RoomType
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.map.UniqueRoom
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.handlers.DungeonInfo
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.ScanUtils
import gg.skytils.skytilsmod.features.impl.handlers.CooldownTracker
import gg.skytils.skytilsmod.features.impl.handlers.SpiritLeap
import gg.skytils.skytilsmod.listeners.ServerPayloadInterceptor.getResponse
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorChatComponentText
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.NumberUtil.addSuffix
import gg.skytils.skytilsmod.utils.NumberUtil.romanToDecimal
import gg.skytils.skytilsws.client.WSClient
import gg.skytils.skytilsws.shared.packet.C2SPacketDungeonEnd
import gg.skytils.skytilsws.shared.packet.C2SPacketDungeonRoom
import gg.skytils.skytilsws.shared.packet.C2SPacketDungeonRoomSecret
import gg.skytils.skytilsws.shared.packet.C2SPacketDungeonStart
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentLinkedQueue

object DungeonListener {
    val team = hashMapOf<String, DungeonTeammate>()
    private val teamCached = hashMapOf<String, Pair<DungeonClass, Int>>()
    val deads = hashSetOf<DungeonTeammate>()
    val disconnected = hashSetOf<String>()
    val missingPuzzles = hashSetOf<String>()
    val completedPuzzles = hashSetOf<String>()
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
        Regex("§r(?:§.)+(?:\\[.+] )?(?<name>\\w+?)(?:§.)* (?:§r(?:§[\\da-fklmno]){1,2}.+ )?§r§f\\(§r§d(?:(?<class>Archer|Berserk|Healer|Mage|Tank) (?<lvl>\\w+)|§r§7EMPTY|§r§cDEAD)§r§f\\)§r")
    private val missingPuzzlePattern = Regex("§r (?<puzzle>.+): §r§7\\[§r§6§l✦§r§7] ?§r")
    private val deathRegex = Regex("§r§c ☠ §r§7(?:You were |(?:§.)+(?<username>\\w+)§r)(?<reason>.*) and became a ghost§r§7\\.§r")
    private val reconnectedRegex = Regex("§r§c ☠ §r§7(?:§.)+(?<username>\\w+) §r§7reconnected§r§7.§r")
    private val reviveRegex = Regex("^§r§a ❣ §r§7(?:§.)+(?<username>\\w+)§r§a was revived")
    private val secretsRegex = Regex("\\s*§7(?<secrets>\\d+)\\/(?<maxSecrets>\\d+) Secrets")
    private val keyPickupRegex = Regex("§r§e§lRIGHT CLICK §r§7on §r§7.+?§r§7 to open it\\. This key can only be used to open §r§a(?<num>\\d+)§r§7 door!§r")
    private val witherDoorOpenedRegex = Regex("^(?:\\[.+?] )?(?<name>\\w+) opened a WITHER door!$")
    private val terminalCompletedRegex = Regex("§r§.(?<username>\\w+)§r§a (?:activated|completed) a (?<type>device|terminal|lever)! \\(§r§c(?<completed>\\d)§r§a\\/(?<total>\\d)\\)§r")
    private const val bloodOpenedString = "§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r"
    val outboundRoomQueue = ConcurrentLinkedQueue<C2SPacketDungeonRoom>()
    var isSoloDungeon = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        team.clear()
        deads.clear()
        disconnected.clear()
        missingPuzzles.clear()
        completedPuzzles.clear()
        teamCached.clear()
        outboundRoomQueue.clear()
        isSoloDungeon = false
    }

    @SubscribeEvent
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inDungeons) return
        if (event.packet is S02PacketChat) {
            val text = event.packet.chatComponent.formattedText
            val unformatted = text.stripControlCodes()
            if (event.packet.type == 2.toByte()) {
                secretsRegex.find(text)?.destructured?.also { (secrets, maxSecrets) ->
                    val sec = secrets.toInt()
                    val max = maxSecrets.toInt().coerceAtLeast(sec)

                    DungeonFeatures.DungeonSecretDisplay.secrets = sec
                    DungeonFeatures.DungeonSecretDisplay.maxSecrets = max

                    run setFoundSecrets@ {
                        val tile = ScanUtils.getRoomFromPos(mc.thePlayer.position)
                        if (tile is Room && tile.data.name != "Unknown") {
                            val room = tile.uniqueRoom ?: return@setFoundSecrets
                            if (room.foundSecrets != sec) {
                                room.foundSecrets = sec
                                updateSecrets(room)

                                if (team.size > 1)
                                    WSClient.sendPacketAsync(C2SPacketDungeonRoomSecret(SBInfo.server ?: return@setFoundSecrets, room.mainRoom.data.name, sec))
                            }
                        }
                    }

                }.ifNull {
                    DungeonFeatures.DungeonSecretDisplay.secrets = -1
                    DungeonFeatures.DungeonSecretDisplay.maxSecrets = -1
                }
            } else {
                terminalCompletedRegex.find(text)?.let {
                    val completer = team[it.groups["username"]?.value]
                    val type = it.groups["type"]?.value

                    if (completer != null && type != null) {
                        when (type) {
                            "lever" -> completer.leversDone++
                            "terminal", "device" -> completer.terminalsDone++
                        }
                    }
                }
                if (text.stripControlCodes().trim() == "> EXTRA STATS <") {
                    if (team.size > 1) {
                        SBInfo.server?.let {
                            WSClient.sendPacketAsync(C2SPacketDungeonEnd(it))
                        }
                    }
                    if (Skytils.config.dungeonDeathCounter) {
                        tickTimer(6) {
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
                    if (Skytils.config.runBreakdown != 0) {
                        tickTimer(6) {
                            val output = team.map {
                                val secretsDone = "§aSecrets: §6${
                                    if (it.value.minimumSecretsDone == it.value.maximumSecretsDone) {
                                        "${it.value.minimumSecretsDone}"
                                    } else "${it.value.minimumSecretsDone}§a - §6${it.value.maximumSecretsDone}"
                                }"

                                val roomsDone = "§aRooms: §6${
                                    if (it.value.minimumRoomsDone == it.value.maximumRoomsDone) {
                                        "${it.value.minimumRoomsDone}"
                                    } else "${it.value.minimumRoomsDone}§a - §6${it.value.maximumRoomsDone}"
                                }"

                                //TODO: Maybe also save the rank color?
                                var output =
                                    "§6${it.key}§a | $secretsDone§a | $roomsDone§a | Deaths: §6${it.value.deaths}"

                                if (Skytils.config.runBreakdown == 2 && DungeonFeatures.dungeonFloorNumber == 7) {
                                    output += "§a | Terminals: §6${it.value.terminalsDone}§a | Levers: §6${it.value.leversDone}"
                                }

                                output
                            }

                            UChat.chat(output.joinToString("\n"))
                        }
                    }
                } else if (text.startsWith("§r§c ☠ ")) {
                    if (text.endsWith(" §r§7reconnected§r§7.§r")) {
                        val match = reconnectedRegex.find(text) ?: return
                        val username = match.groups["username"]?.value ?: return
                        disconnected.remove(username)
                    } else if (text.endsWith(" and became a ghost§r§7.§r")) {
                        val match = deathRegex.find(text) ?: return
                        val username = match.groups["username"]?.value ?: mc.thePlayer.name
                        val teammate = team[username] ?: return
                        markDead(teammate)

                        if (match.groups["reason"]?.value?.contains("disconnected") == true) {
                            disconnected.add(username)
                        }
                    }
                } else if (text.startsWith("§r§a ❣ ")) {
                    val match = reviveRegex.find(text) ?: return
                    val username = match.groups["username"]!!.value
                    val teammate = team[username] ?: return
                    markRevived(teammate)
                } else if (text == bloodOpenedString) {
                    SpiritLeap.doorOpener = null
                    DungeonInfo.keys--
                } else if (text == "§r§aStarting in 1 second.§r") {
                    Skytils.launch {
                        delay(2000)
                        if (DungeonTimer.dungeonStartTime != -1L && team.size > 1) {
                            val party = async {
                                ServerboundPartyInfoPacket().getResponse<ClientboundPartyInfoPacket>()
                            }
                            val partyMembers = party.await().members.ifEmpty { setOf(mc.thePlayer.uniqueID) }.mapTo(hashSetOf()) { it.toString() }
                            val entrance = DungeonInfo.uniqueRooms.first { it.mainRoom.data.type == RoomType.ENTRANCE }
                            async(WSClient.wsClient.coroutineContext) {
                                WSClient.sendPacketAsync(C2SPacketDungeonStart(
                                    serverId = SBInfo.server ?: return@async,
                                    floor = DungeonFeatures.dungeonFloor!!,
                                    members = partyMembers,
                                    startTime = DungeonTimer.dungeonStartTime,
                                    entranceLoc = entrance.mainRoom.z * entrance.mainRoom.x
                                ))
                                while (DungeonTimer.dungeonStartTime != -1L) {
                                    while (outboundRoomQueue.isNotEmpty()) {
                                        val packet = outboundRoomQueue.poll() ?: continue
                                        WSClient.sendPacketAsync(packet)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    witherDoorOpenedRegex.find(unformatted)?.destructured?.let { (name) ->
                        SpiritLeap.doorOpener = name

                        DungeonInfo.keys--
                    }

                    keyPickupRegex.find(text)?.destructured?.let { (num) ->
                        DungeonInfo.keys += num.toInt()
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onChatLow(event: ClientChatReceivedEvent) {
        if (Skytils.config.dungeonSecretDisplay && Utils.inDungeons && event.type == 2.toByte()) {
            if (event.message is AccessorChatComponentText) {
                (event.message as AccessorChatComponentText).text = (event.message as AccessorChatComponentText).text.replace(secretsRegex, "")
            }
            event.message.siblings.forEach {
                if (it !is AccessorChatComponentText) return@forEach
                it.text = it.text.replace(secretsRegex, "")
            }
        }
    }

    init {
        tickTimer(4, repeats = true) {
            if (!Utils.inDungeons) return@tickTimer
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
            if (missingPuzzles.size != localMissingPuzzles.size || !missingPuzzles.containsAll(localMissingPuzzles)) {
                val newPuzzles = localMissingPuzzles.filter { it !in missingPuzzles }
                val localCompletedPuzzles = missingPuzzles.filter { it !in localMissingPuzzles }
                val resetPuzzles = localMissingPuzzles.filter { it in completedPuzzles }

                resetPuzzles.forEach {
                    DungeonEvent.PuzzleEvent.Reset(it).postAndCatch()
                }
                newPuzzles.forEach {
                    DungeonEvent.PuzzleEvent.Discovered(it).postAndCatch()
                }
                localCompletedPuzzles.forEach {
                    DungeonEvent.PuzzleEvent.Completed(it).postAndCatch()
                }
                missingPuzzles.clear()
                missingPuzzles.addAll(localMissingPuzzles)
                completedPuzzles.clear()
                completedPuzzles.addAll(localCompletedPuzzles)
            }
        }
        tickTimer(2, repeats = true) {
            if (Utils.inDungeons && mc.thePlayer != null && mc.thePlayer.ticksExisted >= 100 && (DungeonTimer.scoreShownAt == -1L || System.currentTimeMillis() - DungeonTimer.scoreShownAt < 1500)) {
                val tabEntries = TabListUtils.tabEntries
                var partyCount: Int? = null
                if (tabEntries.isNotEmpty() && tabEntries[0].second.contains("§r§b§lParty §r§f(")) {
                    partyCount = partyCountPattern.find(tabEntries[0].second)?.groupValues?.get(1)?.toIntOrNull()
                    if (partyCount != null) {
                        // we can just keep disconnected players here i think
                        if (team.size != partyCount) {
                            println("Recomputing team as party size has changed ${team.size} -> $partyCount")
                            team.values.filter { it.dungeonClass != DungeonClass.EMPTY }.forEach {
                                teamCached[it.playerName] = it.dungeonClass to it.classLevel
                            }
                            team.clear()
                        } else if (team.size > 5) {
                            UChat.chat("$failPrefix §cSomething isn't right! I got more than 5 members. Expected $partyCount members but got ${team.size}")
                            println("Got more than 5 players on the team??")
                            team.values.filter { it.dungeonClass != DungeonClass.EMPTY }.forEach {
                                teamCached[it.playerName] = it.dungeonClass to it.classLevel
                            }
                            team.clear()
                        }
                    } else {
                        println("Couldn't get party count")
                    }
                } else {
                    println("Couldn't get party text")
                }

                if (partyCount != null && (team.isEmpty() || (DungeonTimer.dungeonStartTime != -1L && team.values.any { it.dungeonClass == DungeonClass.EMPTY  }))) {
                    printDevMessage("Parsing party", "dungeonlistener")
                    println("There are $partyCount members in this party")
                    for (i in 0..<5) {
                        val pos = 1 + i * 4
                        if (pos >= tabEntries.size) {
                            println("Tried to get index $pos but doesn't exist!")
                            break
                        }
                        val (entry, text) = tabEntries[pos]
                        val matcher = classPattern.find(text)
                        if (matcher == null) {
                            println("Skipping over entry $text due to it not matching")
                            continue
                        }
                        val name = matcher.groups["name"]!!.value
                        if (name in disconnected) {
                            println("Skipping over entry $name due to player being disconnected")
                            continue
                        }

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
                                    pos,
                                    entry.locationSkin
                                )
                        } else {
                            println("Parsed teammate $name with value EMPTY, $text")
                            if (name in teamCached) {
                                val cache = teamCached[name]!!
                                team[name] = DungeonTeammate(
                                    name,
                                    cache.first, cache.second,
                                    pos,
                                    entry.locationSkin
                                )
                                println("Got old teammate $name with value EMPTY, using values from cache instead ${cache}")
                            } else {
                                team[name] = DungeonTeammate(
                                    name,
                                    DungeonClass.EMPTY, 0,
                                    pos,
                                    entry.locationSkin
                                )
                            }
                        }

                        team[name]?.mapPlayer?.icon = "icon-${(i + (partyCount-1)) % (partyCount)}}"
                    }

                    if (partyCount != team.size) {
                        UChat.chat("$failPrefix §cSomething isn't right! I expected $partyCount members but got ${team.size}")
                    }

                    if (DungeonTimer.dungeonStartTime != -1L && System.currentTimeMillis() - DungeonTimer.dungeonStartTime >= 2000 && team.values.any { it.dungeonClass == DungeonClass.EMPTY }) {
                        UChat.chat("$failPrefix §cSomething isn't right! One or more of your party members has an empty class! Could the server be lagging?")
                    }

                    if (team.isNotEmpty()) {
                        CooldownTracker.updateCooldownReduction()
                        checkSpiritPet()
                    }
                } else {
                    val self = team[mc.thePlayer.name]
                    for (teammate in team.values) {
                        if (tabEntries.size <= teammate.tabEntryIndex) continue
                        val entry = tabEntries[teammate.tabEntryIndex].second
                        if (!entry.contains(teammate.playerName)) {
                            println("Expected ${teammate.playerName} at ${teammate.tabEntryIndex}, got $entry")
                            continue
                        }
                        teammate.player = mc.theWorld.playerEntities.find {
                            it.name == teammate.playerName && it.uniqueID.version() == 4
                        }
                        if (self?.dead != true) {
                            if (entry.endsWith("§r§cDEAD§r§f)§r")) markDead(teammate)
                            else markRevived(teammate)
                        }
                    }

                    val alives = team.values.filterNot {
                        it.dead || it == self || it in deads
                    }.sortedBy {
                        it.tabEntryIndex
                    }

                    alives.forEachIndexed { i, teammate ->
                        teammate.mapPlayer.icon = "icon-$i"
                        printDevMessage("Setting icon for ${teammate.playerName} to icon-$i", "dungeonlistener")
                    }
                    self?.mapPlayer?.icon = "icon-${alives.size}"
                }
            }
        }
    }

    fun markDead(teammate: DungeonTeammate) {
        if (deads.add(teammate)) {
            val time = System.currentTimeMillis()
            val lastDeath = teammate.lastLivingStateChange
            if (lastDeath != null && time - lastDeath <= 1000) return
            teammate.lastLivingStateChange = time
            teammate.dead = true
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
                tickTimer(1) {
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

    fun markRevived(teammate: DungeonTeammate) {
        if (deads.remove(teammate)) {
            teammate.dead = false
            teammate.lastLivingStateChange = System.currentTimeMillis()
        }
    }

    fun markAllRevived() {
        printDevMessage("${Skytils.prefix} §fdebug: marking all teammates as revived", "scorecalc")
        deads.clear()
        team.values.forEach {
            it.dead = false
        }
    }

    fun checkSpiritPet() {
        val teamCopy = team.values.toList()
        IO.launch {
            runCatching {
                for (teammate in teamCopy) {
                    val name = teammate.playerName
                    if (hutaoFans.getIfPresent(name) != null) continue
                    val uuid = teammate.player?.uniqueID ?: MojangUtil.getUUIDFromUsername(name) ?: continue
                    API.getSelectedSkyblockProfile(uuid)?.members?.get(uuid.nonDashedString())?.pets_data?.pets?.any(Pet::isSpirit)?.let {
                        hutaoFans[name] = it
                    }
                }
                printDevMessage(hutaoFans.asMap().toString(), "spiritpet")
            }.onFailure {
                it.printStackTrace()
            }
        }
    }

    fun updateSecrets(room: UniqueRoom) {
        if (room.mainRoom.data.secrets < 1 || (room.foundSecrets ?: -1) < 1) return

        val finders = team.filter { entry ->
            val location = entry.value.mapPlayer.getBlockPos()
            val playerRoom = ScanUtils.getRoomFromPos(location)?.uniqueRoom

            playerRoom != null && playerRoom.name != "Unknown" && room.mainRoom.data.name == playerRoom.name
        }

        if (finders.size >= 2) {
            finders.forEach {
                team[it.key]?.let { member ->
                    member.maximumSecretsDone++

                    if (room.foundSecrets == room.mainRoom.data.secrets) {
                        member.maximumRoomsDone++
                    }
                }
            }
        } else if (finders.size == 1) {
            finders.forEach {
                team[it.key]?.let { member ->
                    member.minimumSecretsDone++
                    member.maximumSecretsDone++

                    if (room.foundSecrets == room.mainRoom.data.secrets) {
                        member.minimumRoomsDone++
                        member.maximumRoomsDone++
                    }
                }
            }
        }
    }

    data class DungeonTeammate(
        val playerName: String,
        val dungeonClass: DungeonClass,
        val classLevel: Int,
        val tabEntryIndex: Int,
        val skin: ResourceLocation
    ) {
        var player: EntityPlayer? = null
            set(value) {
                field = value
                if (value != null) {
                    mapPlayer.setData(value)
                }
            }
        var dead = false
        var deaths = 0
        var minimumSecretsDone = 0
        var maximumSecretsDone = 0
        var minimumRoomsDone = 0
        var maximumRoomsDone = 0
        var terminalsDone = 0
        var leversDone = 0

        var lastLivingStateChange: Long? = null

        val mapPlayer = DungeonMapPlayer(this, skin)

        fun canRender() = player != null && player!!.health > 0 && !dead
    }
}
