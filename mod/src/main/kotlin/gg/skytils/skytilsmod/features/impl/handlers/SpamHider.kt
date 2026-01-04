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
package gg.skytils.skytilsmod.features.impl.handlers

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.unstable.layoutdsl.LayoutScope
import gg.essential.elementa.unstable.layoutdsl.column
import gg.essential.universal.UChat
import gg.skytils.event.EventPriority
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.TickEvent
import gg.skytils.event.impl.play.ActionBarReceivedEvent
import gg.skytils.event.impl.play.ChatMessageReceivedEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod._event.PacketReceiveEvent
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.core.structure.v2.HudElement
import gg.skytils.skytilsmod.gui.layout.text
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiNewChat
import gg.skytils.skytilsmod.utils.RegexAsString
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.formattedText
import gg.skytils.skytilsmod.utils.startsWithAny
import gg.skytils.skytilsmod.utils.stripControlCodes
import gg.skytils.skytilsmod.utils.toast.*
import kotlinx.serialization.*
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.item.Items
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket
import net.minecraft.text.Text
import java.io.File
import java.io.Reader
import java.io.Writer

object SpamHider : EventSubscriber, PersistentSave(File(Skytils.modDir, "spamhider.json")) {

    val filters = hashSetOf<Filter>()
    val repoFilters = hashSetOf<Filter>()

    override fun read(reader: Reader) {
        filters.clear()
        val obj = json.decodeFromString<SpamHiderSave>(reader.readText())
        obj.default.forEach { (k, v) ->
            repoFilters.find { it.name == k }?.state = v.state
        }
        filters.addAll(obj.filter.values)
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(SpamHiderSave(
            default = repoFilters.associate { it.name to RepoFilterSave(it.state, it.name) },
            filter = filters.associateBy { it.name }
        )))
    }

    override fun setDefault(writer: Writer) {
        writer.write(
            json.encodeToString(
                SpamHiderSave(
                    default = emptyMap(),
                    filter = emptyMap()
                )
            )
        )
    }

    @Serializable
    data class Filter @OptIn(ExperimentalSerializationApi::class) constructor(
        var name: String,
        @EncodeDefault
        var state: Int = repoFilters.find { f -> f.name == name }?.state ?: 0,
        var skyblockOnly: Boolean = true,
        @Serializable(with = RegexAsString::class)
        @SerialName("pattern")
        var regex: Regex,
        var type: FilterType,
        var formatted: Boolean
    ) {
        fun check(input: String): Boolean {
            return this.type.method(input, regex)
        }
    }

    enum class FilterType(val type: String, val method: (String, Regex) -> Boolean) {
        STARTSWITH("startsWith", { first, second -> first.startsWith(second.pattern) }),
        CONTAINS("contains", { first, second -> first.contains(second) }),
        REGEX("regex", { first, second -> second.matchEntire(first) != null })
    }

    private var lastBlessingType = ""
    private var abilityUses = 0
    private var lastAbilityUsed = ""

    private val SEA_CREATURES = setOf(
        // Jerry Workshop
        "Frozen Steve fell into the pond long ago, never to resurface...until now!",
        "It's a snowman! He looks harmless",
        "The Grinch stole Jerry's Gifts...get them back!",
        // Spooky Fishing
        "Phew! It's only a Scarecrow.",
        "You hear trotting from beneath the waves, you caught a Nightmare.",
        "It must be a full moon, a Werewolf appears.",
        // Fishing Festival
        "A tiny fin emerges from the water, you've caught a Nurse Shark.",
        "You spot a fin as blue as the water it came from, it's a Blue Shark.",
        "A striped beast bounds from the depths, the wild Tiger Shark!",
        // Regular
        "A Squid appeared.",
        "You caught a Sea Walker.",
        "Pitch darkness reveals a Night Squid.",
        "You stumbled upon a Sea Guardian.",
        "It looks like you've disrupted the Sea Witch's brewing session. Watch out, she's furious!",
        "You reeled in a Sea Archer.",
        "The Monster of the Deep has emerged.",
        "Huh? A Catfish!",
        "Is this even a Fish? It's the Carrot King!",
        "Gross! A Sea Leech!",
        "You've discovered a Guardian Defender of the sea.",
        "You have awoken the Deep Sea Protector, prepare for a battle!",
    )

    private val LEGENDARY_SEA_CREATURES = setOf(
        "What is this creature!?",
        "The spirit of a long lost Phantom Fisher has come to haunt you.",
        "This can't be! The manifestation of death himself!",
        "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!",
        "The Water Hydra has come to test your strength.",
        "The Sea Emperor arises from the depths."
    )

    private var lastSpooked = 0L

    private val deadBush by lazy {
        Items.DEAD_BUSH
    }

    private val powderQueue = hashMapOf<String, Int>()
    private var powderQueueTicks = -1

    override fun setup() {
        register(::onActionBarDisplay)
        register(::onChatPacket, EventPriority.Lowest)
        register(::onTick)
        register(::handleChat)
    }

    fun onActionBarDisplay(event: ActionBarReceivedEvent) {
        if (!Utils.inSkyblock) return
        val message = event.message.string.stripControlCodes()
        val manaUsageMatcher = Regexs.MANAUSED.pattern.find(message)
        if (Skytils.config.manaUseHider != 0 && manaUsageMatcher != null) {
            val manaUsage = manaUsageMatcher.groups[1]?.value ?: return
            val spaced = " ".repeat(manaUsage.length)

            event.message = Text.literal(message.replace(manaUsage, spaced))
            if (Skytils.config.manaUseHider == 2) {
                if (lastAbilityUsed != manaUsage || abilityUses % 3 == 0) {
                    lastAbilityUsed = manaUsage
                    abilityUses = 1
                    newMessage(manaUsage)
                } else abilityUses++
            }
        }
    }

    private enum class Regexs(var pattern: Regex) {
        BLESSINGBUFF(Regex("(?<buff1>\\+[\\d,.%& \\+x]+) (?<symbol1>\\S{1,2})")),
        BLESSINGGRANT(Regex("(?:Also g|G)rant.{1,2} you (.*) (?:and|&) (.*)\\.")),
        BLESSINGNAME(Regex("Blessing of (?<blessing>\\w+)")),
        BUILDINGTOOLS(Regex("(§eZapped §a\\d+ §eblocks! §a§lUNDO§r)|(§r§eUnzapped §r§c\\d+ §r§eblocks away!§r)|(§r§cYou may not Grand Architect that many blocks! \\(\\d+/\\d+\\)§r)|(§r§cYou have \\(\\d+/\\d+\\) of what you're attempting to place!§r)|(§eYou built §a\\d+ §eblocks! §a§lUNDO§r)|(§r§eUndid latest Grand Architect use of §r§c\\d+ §r§eblocks!§r)")),
        MANAUSED(Regex("(§b-\\d+ Mana \\(§6.+§b\\))")),
        SPOOKED(
            Regex(
                "(§r§cYou died and lost [\\d,.]+ coins!§r)|(§r§dJust kidding! .+ §r§7spooked you!§r)|(§r§aAll your coins are fine, this was just a big mean spook :\\)§r)|(§r§c§lDO YOU REALLY WANT TO DELETE YOUR CURRENT PROFILE\\?§r)|(§r§cIt will delete in 10 seconds\\.\\.\\.§r)|(§r§c(?:[1-5]|\\.\\.\\.)§r)|(§r§7You just got spooked! .+ §r§7is the culprit!§r)|(§r§7False! .+ §r§7just §r§7spooked §r§7you!§r)|(§r§cYou had a blacklisted .+ §r§cin your inventory, we had to delete it! Sorry!§r)|(§r§aJK! Your items are fine\\. This was just a big spook :\\)§r)|(§r§[9-b]§l▬+§r)|(§r§eFriend request from §r§d\\[PIG§r§b\\+\\+\\+§r§d\\] Technoblade§r)|(§r§a§l\\[ACCEPT\\] §r§8- §r§c§l\\[DENY\\] §r§8- §r§7§l\\[IGNORE\\]§r)|(§r§7Nope! .+ §r§7just §r§7spooked §r§7you!§r)|(§r§aOnly kidding! We won't give you op ;\\)§r)|(§r§eYou are now op!§r)|(§r§aYour profile is fine! This was just an evil spook :\\)§r)|(§r§aYou're fine! Nothing changed with your guild status! :\\)§r)|(§r§cYou were kicked from your guild with reason '.+'§r)|(§r§aSorry, its just a spook bro\\. :\\)§r)"
            )
        ),
        POWDERCHEST(Regex("§r§aYou received §r§b\\+(?<amount>[\\d,]+) §r§a(?<type>Gemstone|Mithril) Powder\\.§r"))
        ;
    }

    fun onChatPacket(event: PacketReceiveEvent<*>) {
        val packet = event.packet
        if (Utils.inSkyblock && packet is EntityAnimationS2CPacket && packet.animationId == 0) {
            val entity = mc.world?.getEntityById(packet.entityId) ?: return
            if (entity !is OtherClientPlayerEntity) return
            if (entity.mainHandStack?.item != deadBush || entity.squaredDistanceTo(mc.player) > 4 * 4) return
            lastSpooked = System.currentTimeMillis()
        }
        if (packet !is GameMessageS2CPacket || packet.overlay) return
        val unformatted = packet.content?.string?.stripControlCodes() ?: return
        val formatted = packet.content?.formattedText ?: return

        // Profile
        if (formatted.startsWith("§aYou are playing on profile:")) {
            when (Skytils.config.profileHider) {
                1, 2 -> cancelChatPacket(event, Skytils.config.profileHider == 2)
            }
        }

        if (filters.any {
                checkFilter(it, formatted, unformatted, event)
            } || repoFilters.any { checkFilter(it, formatted, unformatted, event) }) return

        if (!Utils.inSkyblock) return
        try {
            if (Utils.inDungeons) {
                // Hide Mort Messages
                when {
                    unformatted.startsWith("[NPC] Mort") -> {
                        when (Skytils.config.hideMortMessages) {
                            1, 2 -> cancelChatPacket(event, Skytils.config.hideMortMessages == 2)
                        }
                    }

                    unformatted.startsWith("[BOSS]") && !unformatted.startsWith("[BOSS] The Watcher") -> {
                        when (Skytils.config.hideBossMessages) {
                            1, 2 -> cancelChatPacket(event, Skytils.config.hideBossMessages == 2)
                        }
                    }

                    Skytils.config.hideOruoMessages && unformatted.startsWith("[STATUE] Oruo the Omniscient: ") && !unformatted.contains(
                        "You should have listened"
                    ) && !unformatted.contains("Yikes") && !unformatted.contains("chose the wrong answer") && !unformatted.contains(
                        "thinks the answer is"
                    ) && !((unformatted.contains("answered Question #") || unformatted.contains("answered the final question")) && unformatted.endsWith(
                        "correctly!"
                    )) -> {
                        cancelChatPacket(event, false)
                    }
                }
            }
            if (unformatted.contains(":") || event.cancelled) return
            when {
                //Autopet hider
                unformatted.startsWith("Autopet equipped your") -> {
                    when (Skytils.config.hideAutopetMessages) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.hideAutopetMessages == 2)
                    }
                }
                // CantUseAbilityHider
                unformatted.startsWith("You cannot use abilities in this room!") -> {
                    when (Skytils.config.hideCantUseAbility) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.hideCantUseAbility == 2)
                    }
                }
                //No enemies nearby
                formatted.startsWith("§r§cThere are no enemies nearby!") -> {
                    when (Skytils.config.hideNoEnemiesNearby) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.hideNoEnemiesNearby == 2)
                    }
                }

                // Implosion
                formatted.contains("§r§7Your Implosion hit ") -> {
                    when (Skytils.config.implosionHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.implosionHider == 2)
                    }
                }

                // Midas Staff
                formatted.contains("§r§7Your Molten Wave hit ") -> {
                    when (Skytils.config.midasStaffHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.midasStaffHider == 2)
                    }
                }

                // Spirit Sceptre
                formatted.contains("§r§7Your Spirit Sceptre hit ") -> {
                    when (Skytils.config.spiritSceptreHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.spiritSceptreHider == 2)
                    }
                }

                // Giant Sword
                formatted.contains("§r§7Your Giant's Sword hit ") -> {
                    when (Skytils.config.giantSwordHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.giantSwordHider == 2)
                    }
                }

                // Livid Dagger
                formatted.contains("§r§7Your Livid Dagger hit") -> {
                    when (Skytils.config.lividHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.lividHider == 2)
                    }
                }

                // Ray of Hope
                formatted.startsWith("§r§7Your Ray of Hope hit") -> {
                    when (Skytils.config.hopeHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.hopeHider == 2)
                    }
                }

                // Mining Abilities
                (
                        (formatted.startsWith("§r§a§r§6") && formatted.endsWith("§r§ais now available!§r"))
                                || formatted.contains("§r§cYour Mining Speed Boost has expired!§r")
                                || formatted.contains("§r§aYou used your §r§6Mining Speed Boost §r§aPickaxe Ability!§r")
                        ) -> {
                    when (Skytils.config.miningAbilityHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.miningAbilityHider == 2)
                    }
                }


                // Blessings
                formatted.contains("§r§6§lDUNGEON BUFF!") -> {
                    when (Skytils.config.blessingHider) {
                        1 -> cancelChatPacket(event, false)
                        2 -> {
                            val blessingTypeMatcher = Regexs.BLESSINGNAME.pattern.find(unformatted)
                            if (blessingTypeMatcher != null) {
                                lastBlessingType = blessingTypeMatcher.groups["blessing"]?.value?.lowercase() ?: ""
                                cancelChatPacket(event, false)
                            }
                        }
                    }
                }

                Utils.inDungeons && unformatted.contains("rant") -> {
                    Regexs.BLESSINGGRANT.pattern.find(unformatted)?.let { match ->
                        when (Skytils.config.blessingHider) {
                            1 -> cancelChatPacket(event, false)
                            2 -> {
                                // TODO: account for new format
                                val buffs = match.groupValues.mapNotNull { blessingGroup ->
                                    Regexs.BLESSINGBUFF.pattern.matchEntire(blessingGroup)
                                }.map { blessingBuffMatch ->
                                    BlessingToast.BlessingBuff(
                                        blessingBuffMatch.groups["buff1"]?.value ?: return@let,
                                        blessingBuffMatch.groups["symbol1"]?.value ?: return@let
                                    )
                                }
                                if (lastBlessingType != "") {
                                    Utils.checkThreadAndQueue {
                                        GuiManager.addToast(BlessingToast(lastBlessingType, buffs))
                                    }
                                }
                                cancelChatPacket(event, false)
                            }
                        }
                    }
                }

                Utils.inDungeons && unformatted.contains("Blessing of ") -> {
                    when (Skytils.config.blessingHider) {
                        1, 2 -> cancelChatPacket(event, false)
                    }
                }

                // Keys
                // Wither
                formatted.contains("§r§8Wither Key") && Utils.inDungeons -> {
                    when (Skytils.config.witherKeyHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.witherKeyHider == 2)
                        3 -> {
                            cancelChatPacket(event, false)
                            GuiManager.addToast(
                                KeyToast(
                                    "wither",
                                    if (unformatted.contains("was picked up")) {
                                        ""
                                    } else {
                                        formatted.substring(0, formatted.indexOf("§r§f §r§ehas"))
                                    }
                                )
                            )
                        }
                    }
                }

                formatted.contains("§r§e§lRIGHT CLICK §r§7on §r§7a §r§8WITHER §r§7door§r§7 to open it.") -> {
                    when (Skytils.config.witherKeyHider) {
                        1, 2, 3 -> cancelChatPacket(event, false)
                    }
                }
                // Blood
                unformatted.contains("Blood Key") && Utils.inDungeons -> {
                    when (Skytils.config.bloodKeyHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.bloodKeyHider == 2)
                        3 -> {
                            cancelChatPacket(event, false)
                            GuiManager.addToast(
                                KeyToast(
                                    "blood",
                                    if (unformatted.contains("was picked up")) {
                                        ""
                                    } else {
                                        formatted.substring(0, formatted.indexOf("§r§f §r§ehas"))
                                    }
                                )
                            )
                        }
                    }
                }

                unformatted.contains("RIGHT CLICK on the BLOOD DOOR to open it.") -> {
                    when (Skytils.config.bloodKeyHider) {
                        1, 2, 3 -> cancelChatPacket(event, false)
                    }
                }

                // Superboom tnt
                formatted.contains("§r§9Superboom TNT") && Utils.inDungeons -> {
                    when (Skytils.config.superboomHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.superboomHider == 2)
                        3 -> {
                            cancelChatPacket(event, false)
                            val username = mc.player?.name?.string ?: return
                            if (formatted.contains(username)) {
                                GuiManager.addToast(SuperboomToast())
                            }
                        }
                    }
                }

                // Revive Stone
                formatted.contains("§r§6Revive Stone") && Utils.inDungeons -> {
                    when (Skytils.config.reviveStoneHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.reviveStoneHider == 2)
                        3 -> {
                            cancelChatPacket(event, false)
                            val username = mc.player?.name?.string ?: return
                            if (formatted.contains(username)) {
                                GuiManager.addToast(ReviveStoneToast())
                            }
                        }
                    }
                }

                // Combo
                unformatted.contains(" Combo") -> {
                    when (Skytils.config.comboHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.comboHider == 2)
                        3 -> {
                            if (unformatted.startsWith("Your Kill Combo has expired!")) {
                                GuiManager.addToast(ComboToast("§r§c§lCombo Failed!", "§r§cYou reached ${unformatted.filter { it.isDigit() }}"))
                            } else {
                                ComboToast.fromString(formatted)?.let { toast ->
                                    GuiManager.addToast(toast)
                                }
                            }
                            cancelChatPacket(event, false)
                        }
                    }
                }

                // Blessing enchant
                formatted.startsWith("§r§aYour Blessing enchant") -> {
                    when (Skytils.config.blessingEnchantHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.blessingEnchantHider == 2)
                    }
                }

                // Blessing bair
                formatted.startsWith("§r§aYour bait got you double") -> {
                    when (Skytils.config.blessedBaitHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.blessedBaitHider == 2)
                    }
                }

                // Sea creature catch
                formatted.startsWith("§r§a") && SEA_CREATURES.contains(unformatted) -> {
                    when (Skytils.config.scCatchHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.scCatchHider == 2)
                    }
                }

                // Legendary sea creature catch
                formatted.startsWith("§r§a") && LEGENDARY_SEA_CREATURES.contains(unformatted) -> {
                    when (Skytils.config.legendaryScCatchHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.legendaryScCatchHider == 2)
                    }
                }

                // Good catch
                unformatted.startsWith("GOOD CATCH! You found") -> {
                    when (Skytils.config.goodTreasureHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.goodTreasureHider == 2)
                    }
                }

                // Great catch
                unformatted.startsWith("GREAT CATCH! You found") -> {
                    when (Skytils.config.greatTreasureHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.greatTreasureHider == 2)
                    }
                }

                // Compact
                unformatted.startsWith("COMPACT! You found") -> {
                    when (Skytils.config.compactHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.compactHider == 2)
                    }
                }

                // Pristine
                unformatted.startsWith("PRISTINE! You found") -> {
                    when (Skytils.config.pristineHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.pristineHider == 2)
                    }
                }

                // Wind
                unformatted.contains("The wind has changed direction!") -> {
                    when (Skytils.config.windHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.windHider == 2)
                    }
                }

                // Blocks in the way
                unformatted.contains("There are blocks in the way") -> {
                    when (Skytils.config.inTheWayHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.inTheWayHider == 2)
                    }
                }

                // Cooldown
                unformatted.contains(" cooldown") -> {
                    when (Skytils.config.cooldownHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.cooldownHider == 2)
                    }
                }

                // Out of mana
                unformatted.contains("You do not have enough mana to do this!") || unformatted.startsWith("Not enough mana!") -> {
                    when (Skytils.config.manaMessages) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.manaMessages == 2)
                    }
                }

                //Hide Abilities
                (Utils.inDungeons && unformatted.contains("is now available!") && !unformatted.contains("Mining Speed Boost") && !unformatted.contains(
                    "Pickobulus"
                ) || unformatted.contains("is ready to use!") || unformatted.startsWith("Used") || unformatted.contains(
                    "Your Guided Sheep hit"
                ) || unformatted.contains("Your Thunderstorm hit") || unformatted.contains("Your Wish healed") || unformatted.contains(
                    "Your Throwing Axe hit"
                ) || unformatted.contains("Your Explosive Shot hit") || unformatted.contains("Your Seismic Wave hit")
                        ) -> {
                    when (Skytils.config.hideDungeonAbilities) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.hideDungeonAbilities == 2)
                    }
                }

                // Hide Dungeon Countdown / Ready messages
                (Utils.inDungeons && unformatted.contains("has started the dungeon countdown. The dungeon will begin in 1 minute.") || unformatted.contains(
                    "is now ready!"
                ) || unformatted.contains("Dungeon starts in") || unformatted.contains("selected the")
                        ) -> {
                    when (Skytils.config.hideDungeonCountdownAndReady) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.hideDungeonCountdownAndReady == 2)
                    }
                }

                // Compact Building Tools
                (Skytils.config.compactBuildingTools && formatted.startsWith("§9§lSkytils §8» §eThis will expire in") && (formatted.contains(
                    "blocks"
                ) || formatted.contains("build") || formatted.contains(
                    "place"
                ) || formatted.contains("zap"))
                        ) -> {
                    if (formatted.startsWith("§9§lSkytils §8» §eThis will expire in") ||
                        Regexs.BUILDINGTOOLS.pattern.matches(formatted)
                    ) {
                        val chatGui = mc.inGameHud.chatHud
                        val lines =
                            (chatGui as AccessorGuiNewChat).chatLines
                        val drawnLines =
                            (chatGui as AccessorGuiNewChat).drawnChatLines
                        var i = 0
                        while (i < 100 && i < lines.size) {
                            val line = lines[i]
                            if (line.content.formattedText.replace(
                                    "\\d".toRegex(),
                                    ""
                                ) == formatted.replace("\\d".toRegex(), "")
                            ) {
                                drawnLines.removeAt(i)
                            }
                            i++
                        }
                    }
                }

                // Healer Tethers
                formatted.startsWith("§r§eYou formed a tether") -> {
                    when (Skytils.config.tetherHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.tetherHider == 2)
                    }
                }

                // Self Orb Pickups
                formatted.startsWith("§r§c◕ §r§eYou picked up a") -> {
                    when (Skytils.config.selfOrbHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.selfOrbHider == 2)
                    }
                }

                // Other Orb Pickups
                formatted.contains("§r§epicked up your ") -> {
                    when (Skytils.config.otherOrbHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.otherOrbHider == 2)
                    }
                }

                // Traps
                formatted.startsWithAny(
                    "§r§cThe Tripwire Trap",
                    "§r§cThe Flamethrower",
                    "§r§cThe Arrow Trap",
                    "§r§cThe Crusher"
                ) -> {
                    when (Skytils.config.trapDamageHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.trapDamageHider == 2)
                    }
                }

                // Auto-Recombobulator
                unformatted.startsWith("Your Auto-Recomb") -> {
                    when (Skytils.config.autoRecombHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.autoRecombHider == 2)
                        3 -> {
                            cancelChatPacket(event, false)
                            RecombToast.pattern.find(formatted, formatted.indexOf(" "))?.let {
                                GuiManager.addToast(RecombToast(it.groupValues[1]))
                            }
                        }
                    }
                }

                formatted.contains("§r§eunlocked §r§dWither Essence §r§8x") -> {
                    when (Skytils.config.witherEssenceHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.witherEssenceHider == 2)
                    }
                }

                // Undead Essence
                formatted.contains("§r§eunlocked §r§dUndead Essence §r§8x") -> {
                    when (Skytils.config.undeadEssenceHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.undeadEssenceHider == 2)
                    }
                }

                // Healing (Zombie sword & Werewolf)
                formatted.startsWith("§r§a§l") && formatted.contains("healed") -> {
                    when (Skytils.config.healingHider) {
                        1, 2 -> cancelChatPacket(event, Skytils.config.healingHider == 2)
                    }
                }
                // Spooky Staff
                Skytils.config.spookyMessageHider != 0 && System.currentTimeMillis() - lastSpooked <= 10000 &&
                        Regexs.SPOOKED.pattern.matches(formatted) -> {
                    cancelChatPacket(event, Skytils.config.spookyMessageHider == 2)
                }

                Skytils.config.compactPowderMessages && formatted.startsWith("§r§aYou received §r§b+") && formatted.endsWith(
                    " Powder.§r"
                ) -> {
                    val matcher = Regexs.POWDERCHEST.pattern.find(formatted)
                    if (matcher != null) {
                        val amount = matcher.groups["amount"]?.value?.replace(",", "")?.toInt() ?: 0
                        val type = matcher.groups["type"]?.value ?: "Error"
                        powderQueueTicks = 10
                        powderQueue.compute(type) { _, v ->
                            (v ?: 0) + amount
                        }
                        cancelChatPacket(event, false)
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onTick(event: TickEvent) {
        if (powderQueueTicks != -1) {
            if (--powderQueueTicks <= 0) {
                powderQueueTicks = -1
                UChat.chat("§r§aYou received ${powderQueue.entries.joinToString(separator = " and ") { "§r§b+${it.value} §r§a${it.key}" }} Powder.§r")
                powderQueue.clear()
            }
        }
    }

    private fun checkFilter(filter: Filter, formatted: String, unformatted: String, event: PacketReceiveEvent<*>): Boolean {
        @Suppress("SENSELESS_COMPARISON")
        if (filter == null) return false
        runCatching {
            if (filter.skyblockOnly && !Utils.inSkyblock) return false
            if (filter.check(if (filter.formatted) formatted else unformatted) && filter.state > 0) {
                cancelChatPacket(event, filter.state == 2)
                return true
            }
        }.onFailure {
            UChat.chat("$failPrefix §cSkytils ran into an error whilst checking your Spam Hider filters. Please send your logs to discord.gg/skytils.")
            println("A ${it::class.simpleName} was thrown while checking Spam Hider Filter:")
            println("Spam Filter: $filter")
            println("Formatted Text: $formatted")
            println("Unformatted Text: $unformatted")
            it.printStackTrace()
        }
        return false
    }

    object SpamHudElement : HudElement("Spam Hider", 0.65, 0.925) {
        private var container: UIComponent? = null

        fun queueSpam(spam: String) {
            val component = UIText(spam).constrain {
                x = 0.pixels(alignOpposite = true, alignOutside = true)
            }

            fun animateOut() {
                component.animate {
                    setXAnimation(
                        Animations.IN_EXP,
                        0.6f,
                        0.pixels(alignOpposite = true, alignOutside = true),
                        3f
                    )
                    onComplete {
                        component.hide()
                    }
                }
            }
            container?.addChild(component)
            component.animate {
                setXAnimation(
                    Animations.OUT_EXP,
                    0.6f,
                    0.pixels(alignOpposite = true)
                )
                onComplete {
                    animateOut()
                }
            }
        }

        override fun LayoutScope.render() {
            container = column {
                // content added via `queueSpam`
            }
        }

        override fun LayoutScope.demoRender() {
            text("§r§7Your Implosion hit §r§c3 §r§7enemies for §r§c1,000,000.0 §r§7damage.§r")
        }

        // old toggle logic
//        Skytils.config.getCategoryFromSearch("Hider").items.filterIsInstance<PropertyItem>()
//        .filter { it.data.attributesExt.type == PropertyType.SELECTOR && it.data.attributesExt.options.size >= 3 && it.data.attributesExt.name != "Text Shadow" }
//        .any { it.data.getValue() as Int == 2 }
    }

    private fun cancelChatPacket(event: PacketReceiveEvent<*>, addToSpam: Boolean) {
        if (event.packet !is GameMessageS2CPacket) return
        event.packet.content?.let { messagesToHide.add(it) }
        if (addToSpam) newMessage(event.packet.content?.formattedText)
    }

    private fun handleChat(event: ChatMessageReceivedEvent) {
        if (event.message in messagesToHide) {
            messagesToHide.remove(event.message)
            event.cancelled = true
        }
    }

    private val messagesToHide = mutableSetOf<Text>()

    private fun newMessage(message: String?) {
        SpamHudElement.queueSpam(message ?: return)
    }

    init {
        Skytils.guiManager.registerElement(SpamHudElement)
    }
}


@Serializable
private data class SpamHiderSave(
    val default: Map<String, RepoFilterSave>,
    val filter: Map<String, SpamHider.Filter>
)

@Serializable
private data class RepoFilterSave(
    val state: Int,
    val name: String
)
