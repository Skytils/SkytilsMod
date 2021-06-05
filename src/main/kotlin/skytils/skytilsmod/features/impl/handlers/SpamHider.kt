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
package skytils.skytilsmod.features.impl.handlers

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.GuiManager
import skytils.skytilsmod.core.structure.FloatPair
import skytils.skytilsmod.core.structure.GuiElement
import skytils.skytilsmod.events.PacketEvent.ReceiveEvent
import skytils.skytilsmod.events.SetActionBarEvent
import skytils.skytilsmod.mixins.accessors.AccessorGuiNewChat
import skytils.skytilsmod.utils.stripControlCodes
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextAlignment
import skytils.skytilsmod.utils.graphics.SmartFontRenderer.TextShadow
import skytils.skytilsmod.utils.graphics.colors.CommonColors
import skytils.skytilsmod.utils.startsWithAny
import skytils.skytilsmod.utils.toasts.*
import skytils.skytilsmod.utils.toasts.BlessingToast.BlessingBuff
import java.util.*
import java.util.regex.Pattern
import kotlin.math.sin

class SpamHider {
    companion object {
        private val mc = Minecraft.getMinecraft()
        var spamMessages = ArrayList<SpamMessage>()
        private fun cancelChatPacket(ReceivePacketEvent: ReceiveEvent, addToSpam: Boolean) {
            if (ReceivePacketEvent.packet !is S02PacketChat) return
            Utils.cancelChatPacket(ReceivePacketEvent)
            if (addToSpam) newMessage(ReceivePacketEvent.packet.chatComponent.formattedText)
        }

        private fun newMessage(message: String) {
            spamMessages.add(SpamMessage(message, 0, 0.0))
        }

        init {
            SpamGuiElement()
        }
    }

    private var lastBlessingType = ""
    private var abilityUses = 0
    private var lastAbilityUsed = ""

    private val SEA_CREATURES = setOf<String>(
        // Jerry Workshop
        "Frozen Steve fell into the pond long ago.",
        "It's a snowman! He looks harmless.",
        "The Grinch stole Jerry's Gifts...get them back!",
        // Spooky Fishing
        "Phew! It's only a scarecrow.",
        "You hear trotting from beneath the waves, you caught a Nightmare.",
        "It must be a full moon, it's a Werewolf!",
        // Fishing Festival
        "A tiny fin emerges from the water, you've caught a Nurse Shark.",
        "You spot a fin as blue as the water it came from, it's a Blue Shark.",
        "A striped beast bounds from the depths, the wild Tiger Shark!",
        // Regular
        "A Squid appeared.",
        "You caught a Sea Walker.",
        "Pitch darkness reveals you've caught a Night Squid.",
        "You stumbled upon a Sea Guardian.",
        "It looks like you've disrupted the Sea Witch's brewing session. Watch out, she's furious!",
        "You reeled in a Sea Archer.",
        "The Monster of The Deep emerges from the dark depths...",
        "Huh? A Catfish!",
        "Is this even a Fish? It's the Carrot King!",
        "Gross! A Sea Leech!",
        "You've discovered a Guardian Defender of the sea.",
        "You have awoken the Deep Sea Protector, prepare for a battle!",
    )

    private val LEGENDARY_SEA_CREATURES = setOf<String>(
        "What is this creature!?",
        "The spirit of a long lost Phantom Fisher has come to haunt you.",
        "This can't be! The manifestation of death himself!",
        "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!",
        "The Water Hydra has come to test your Strength.",
        "The Sea Emperor arises from the depths..."
    )

    @SubscribeEvent
    fun onActionBarDisplay(event: SetActionBarEvent) {
        if (!Utils.inSkyblock) return
        val manaUsageMatcher = Regexs.MANAUSED.pattern.matcher(event.message)
        if (Skytils.config.manaUseHider != 0 && manaUsageMatcher.find()) {
            val manaUsage = manaUsageMatcher.group(1) ?: return
            var spaced = ""

            for (i in manaUsage) spaced += " "

            event.message = event.message.replace(manaUsage, spaced)
            if (Skytils.config.manaUseHider == 2) {
                if (lastAbilityUsed != manaUsage || abilityUses % 3 == 0) {
                    lastAbilityUsed = manaUsage
                    abilityUses = 1
                    newMessage(manaUsage)
                } else abilityUses++
            }
        }
    }

    private enum class Regexs(var pattern: Pattern) {
        BLESSINGBUFF(Pattern.compile("(?<buff1>\\d[\\d,.%]+?) (?<symbol1>\\S{1,2})")),
        BLESSINGGRANT(Pattern.compile("Grant.{1,2} you .* and .*\\.")),
        BLESSINGNAME(Pattern.compile("Blessing of (?<blessing>\\w+)")),
        BUILDINGTOOLS(Pattern.compile("(§eZapped §a\\d+ §eblocks! §a§lUNDO§r)|(§r§eUnzapped §r§c\\d+ §r§eblocks away!§r)|(§r§cYou may not Grand Architect that many blocks! \\(\\d+/\\d+\\)§r)|(§r§cYou have \\(\\d+/\\d+\\) of what you're attempting to place!§r)|(§eYou built §a\\d+ §eblocks! §a§lUNDO§r)|(§r§eUndid latest Grand Architect use of §r§c\\d+ §r§eblocks!§r)")),
        MANAUSED(Pattern.compile("(§b-\\d+ Mana \\(§6.+§b\\))"));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    fun onChatPacket(event: ReceiveEvent) {
        if (event.packet !is S02PacketChat) return
        val packet = event.packet
        if (packet.type.toInt() == 2) return
        val unformatted = packet.chatComponent.unformattedText.stripControlCodes()
        val formatted = packet.chatComponent.formattedText

        // Profile
        if (formatted.startsWith("§aYou are playing on profile:")) {
            when (Skytils.config.profileHider) {
                1, 2 -> cancelChatPacket(event, Skytils.config.profileHider == 2)
                else -> {
                }
            }
        }
        if (!Utils.inSkyblock) return
        try {
            // Hide Mort Messages
            if (Utils.inDungeons && unformatted.startsWith("[NPC] Mort")) {
                when (Skytils.config.hideMortMessages) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.hideMortMessages == 2)
                    else -> {
                    }
                }
            }

            // Hide Boss Messages
            if (Utils.inDungeons && unformatted.startsWith("[BOSS]") && !unformatted.startsWith("[BOSS] The Watcher")) {
                when (Skytils.config.hideBossMessages) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.hideBossMessages == 2)
                    else -> {
                    }
                }
            }
            if (Skytils.config.hideOruoMessages && Utils.inDungeons && unformatted.startsWith("[STATUE] Oruo the Omniscient: ") && !unformatted.contains(
                    "You should have listened"
                ) && !unformatted.contains("Yikes") && !unformatted.contains("chose the wrong answer") && !unformatted.contains(
                    "thinks the answer is"
                ) && !(unformatted.contains("answered Question #") && unformatted.endsWith("correctly!"))
            ) {
                cancelChatPacket(event, false)
            }
            if (unformatted.contains(":")) return

            //Autopet hider
            if (unformatted.startsWith("Autopet equipped your")) {
                when (Skytils.config.hideAutopetMessages) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.hideAutopetMessages == 2)
                    else -> {
                    }
                }
            }

            // CantUseAbilityHider
            if (unformatted.startsWith("You cannot use abilities in this room!")) {
                when (Skytils.config.hideCantUseAbility) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.hideCantUseAbility == 2)
                    else -> {
                    }
                }
            }

            //No enemies nearby
            if (formatted.startsWith("§r§cThere are no enemies nearby!")) {
                when (Skytils.config.hideNoEnemiesNearby) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.hideNoEnemiesNearby == 2)
                    else -> {
                    }
                }
            }

            // Implosion
            if (formatted.contains("§r§7Your Implosion hit ")) {
                when (Skytils.config.implosionHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.implosionHider == 2)
                    else -> {
                    }
                }
            }

            // Midas Staff
            if (formatted.contains("§r§7Your Molten Wave hit ")) {
                when (Skytils.config.midasStaffHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.midasStaffHider == 2)
                    else -> {
                    }
                }
            }

            // Spirit Sceptre
            if (formatted.contains("§r§7Your Spirit Sceptre hit ")) {
                when (Skytils.config.spiritSceptreHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.spiritSceptreHider == 2)
                    else -> {
                    }
                }
            }

            // Giant Sword
            if (formatted.contains("§r§7Your Giant's Sword hit ")) {
                when (Skytils.config.giantSwordHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.giantSwordHider == 2)
                    else -> {
                    }
                }
            }

            // Livid Dagger
            if (formatted.contains("§r§7Your Livid Dagger hit")) {
                when (Skytils.config.lividHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.lividHider == 2)
                    else -> {
                    }
                }
            }

            // Ray of Hope
            if (formatted.startsWith("§r§7Your Ray of Hope hit")) {
                when (Skytils.config.hopeHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.hopeHider == 2)
                    else -> {
                    }
                }
            }

            // Blessings
            if (formatted.contains("§r§6§lDUNGEON BUFF!")) {
                when (Skytils.config.blessingHider) {
                    1 -> cancelChatPacket(event, false)
                    2 -> {
                        val blessingTypeMatcher = Regexs.BLESSINGNAME.pattern.matcher(unformatted)
                        if (blessingTypeMatcher.find()) {
                            lastBlessingType = blessingTypeMatcher.group("blessing").lowercase()
                            cancelChatPacket(event, false)
                        }
                    }
                    else -> {
                    }
                }
            } else if (unformatted.contains("Grant")) {
                if (Regexs.BLESSINGGRANT.pattern.matcher(unformatted).find()) {
                    when (Skytils.config.blessingHider) {
                        1 -> cancelChatPacket(event, false)
                        2 -> {
                            val blessingBuffMatcher = Regexs.BLESSINGBUFF.pattern.matcher(unformatted)
                            val buffs: MutableList<BlessingBuff> = ArrayList()
                            while (blessingBuffMatcher.find()) {
                                val symbol =
                                    if (blessingBuffMatcher.group("symbol1") == "he") "\u2764" else blessingBuffMatcher.group(
                                        "symbol1"
                                    )
                                buffs.add(BlessingBuff(blessingBuffMatcher.group("buff1"), symbol))
                            }
                            if (lastBlessingType != "") GuiManager.toastGui.add(BlessingToast(lastBlessingType, buffs))
                            cancelChatPacket(event, false)
                        }
                        else -> {
                        }
                    }
                }
            } else if (unformatted.contains("Blessing of ")) {
                when (Skytils.config.blessingHider) {
                    1, 2 -> cancelChatPacket(event, false)
                    else -> {
                    }
                }
            }

            // Keys
            // Wither
            if (formatted.contains("§r§8Wither Key") && Utils.inDungeons) {
                when (Skytils.config.witherKeyHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.witherKeyHider == 2)
                    3 -> {
                        cancelChatPacket(event, false)
                        if (unformatted.contains("was picked up")) {
                            GuiManager.toastGui.add(KeyToast("wither", ""))
                        } else {
                            val player = formatted.substringBefore("§r§f §r§ehas")
                            GuiManager.toastGui.add(KeyToast("wither", player))
                        }
                    }
                    else -> {
                    }
                }
            } else if (formatted.contains("§r§e§lRIGHT CLICK §r§7on §r§7a §r§8WITHER §r§7door§r§7 to open it.")) {
                when (Skytils.config.witherKeyHider) {
                    1, 2, 3 -> cancelChatPacket(event, false)
                    else -> {
                    }
                }
            }
            // Blood
            if (unformatted.contains("Blood Key") && Utils.inDungeons) {
                when (Skytils.config.bloodKeyHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.bloodKeyHider == 2)
                    3 -> {
                        cancelChatPacket(event, false)
                        if (unformatted.contains("was picked up")) {
                            GuiManager.toastGui.add(KeyToast("blood", ""))
                        } else {
                            val player = formatted.substring(0, formatted.indexOf("§r§f §r§ehas"))
                            GuiManager.toastGui.add(KeyToast("blood", player))
                        }
                    }
                    else -> {
                    }
                }
            } else if (unformatted.contains("RIGHT CLICK on the BLOOD DOOR to open it.")) {
                when (Skytils.config.bloodKeyHider) {
                    1, 2, 3 -> cancelChatPacket(event, false)
                    else -> {
                    }
                }
            }

            // Superboom tnt
            if (formatted.contains("§r§9Superboom TNT") && Utils.inDungeons) {
                when (Skytils.config.superboomHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.superboomHider == 2)
                    3 -> {
                        cancelChatPacket(event, false)
                        val username = Minecraft.getMinecraft().thePlayer.name
                        if (!formatted.contains(username)) return
                        GuiManager.toastGui.add(SuperboomToast())
                    }
                    else -> {
                    }
                }
            }

            // Revive Stone
            if (formatted.contains("§r§6Revive Stone") && Utils.inDungeons) {
                when (Skytils.config.reviveStoneHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.reviveStoneHider == 2)
                    3 -> {
                        cancelChatPacket(event, false)
                        val username = Minecraft.getMinecraft().thePlayer.name
                        if (!formatted.contains(username)) return
                        GuiManager.toastGui.add(ReviveStoneToast())
                    }
                    else -> {
                    }
                }
            }

            // Combo
            if (unformatted.contains("Combo")) {
                when (Skytils.config.comboHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.comboHider == 2)
                    3 -> {
                        if (unformatted.startsWith("Your Kill Combo has expired!")) {
                            GuiManager.toastGui.add(ComboEndToast())
                        } else {
                            GuiManager.toastGui.add(ComboToast(formatted))
                        }
                        cancelChatPacket(event, false)
                    }
                    else -> {
                    }
                }
            }

            // Blessing enchant
            if (formatted.startsWith("§r§aYour Blessing enchant")) {
                when (Skytils.config.blessingEnchantHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.blessingEnchantHider == 2)
                    else -> {
                    }
                }
            }

            // Blessing bair
            if (formatted.startsWith("§r§aYour bait got you double")) {
                when (Skytils.config.blessedBaitHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.blessedBaitHider == 2)
                    else -> {
                    }
                }
            }

            // Sea creature catch
            if (SEA_CREATURES.contains(unformatted)) {
                when (Skytils.config.scCatchHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.scCatchHider == 2)
                    else -> {
                    }
                }
            }

            // Legendary sea creature catch
            if (LEGENDARY_SEA_CREATURES.contains(unformatted)) {
                when (Skytils.config.legendaryScCatchHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.legendaryScCatchHider == 2)
                    else -> {
                    }
                }
            }

            // Good catch
            if (unformatted.startsWith("GOOD CATCH! You found")) {
                when (Skytils.config.goodTreasureHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.goodTreasureHider == 2)
                    else -> {
                    }
                }
            }

            // Great catch
            if (unformatted.startsWith("GREAT CATCH! You found")) {
                when (Skytils.config.greatTreasureHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.greatTreasureHider == 2)
                    else -> {
                    }
                }
            }

            // Compact
            if (unformatted.startsWith("COMPACT! You found")) {
                when (Skytils.config.compactHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.compactHider == 2)
                    else -> {
                    }
                }
            }

            // Blocks in the way
            if (unformatted.contains("There are blocks in the way")) {
                when (Skytils.config.inTheWayHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.inTheWayHider == 2)
                    else -> {
                    }
                }
            }

            // Cooldown
            if (unformatted.contains("cooldown")) {
                when (Skytils.config.cooldownHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.cooldownHider == 2)
                    else -> {
                    }
                }
            }

            // Out of mana
            if (unformatted.contains("You do not have enough mana to do this!") || unformatted.startsWith("Not enough mana!")) {
                when (Skytils.config.manaMessages) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.manaMessages == 2)
                    else -> {
                    }
                }
            }

            //Hide Abilities
            if (Utils.inDungeons && unformatted.contains("is now available!") && !unformatted.contains("Mining Speed Boost") && !unformatted.contains(
                    "Pickobulus"
                ) || unformatted.contains("is ready to use!") || unformatted.startsWith("Used") || unformatted.contains(
                    "Your Guided Sheep hit"
                ) || unformatted.contains("Your Thunderstorm hit") || unformatted.contains("Your Wish healed") || unformatted.contains(
                    "Your Throwing Axe hit"
                ) || unformatted.contains("Your Explosive Shot hit") || unformatted.contains("Your Seismic Wave hit")
            ) {
                when (Skytils.config.hideDungeonAbilities) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.hideDungeonAbilities == 2)
                    else -> {
                    }
                }
            }

            // Hide Dungeon Countdown / Ready messages
            if (Utils.inDungeons && unformatted.contains("has started the dungeon countdown. The dungeon will begin in 1 minute.") || unformatted.contains(
                    "is now ready!"
                ) || unformatted.contains("Dungeon starts in") || unformatted.contains("selected the")
            ) {
                when (Skytils.config.hideDungeonCountdownAndReady) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.hideDungeonCountdownAndReady == 2)
                    else -> {
                    }
                }
            }

            // Compact Building Tools
            if (Skytils.config.compactBuildingTools && (formatted.contains("blocks") || formatted.contains("build") || formatted.contains(
                    "place"
                ) || formatted.contains("zap"))
            ) {
                if (Regexs.BUILDINGTOOLS.pattern.matcher(formatted).matches()) {
                    val chatGui = mc.ingameGUI.chatGUI
                    val lines = (chatGui as AccessorGuiNewChat).chatLines
                    val drawnLines = (chatGui as AccessorGuiNewChat).drawnChatLines
                    var i = 0
                    while (i < 100 && i < lines.size) {
                        val line = lines[i]
                        if (line.chatComponent.formattedText.replace(
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
            if (formatted.startsWith("§r§eYou formed a tether")) {
                when (Skytils.config.tetherHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.tetherHider == 2)
                    else -> {
                    }
                }
            }

            // Self Orb Pickups
            if (formatted.startsWith("§r§c◕ §r§eYou picked up a")) {
                when (Skytils.config.selfOrbHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.selfOrbHider == 2)
                    else -> {
                    }
                }
            }

            // Other Orb Pickups
            if (formatted.contains("§r§epicked up your ")) {
                when (Skytils.config.otherOrbHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.otherOrbHider == 2)
                    else -> {
                    }
                }
            }

            // Traps
            if (formatted.startsWithAny(
                    "§r§cThe Tripwire Trap",
                    "§r§cThe Flamethrower",
                    "§r§cThe Arrow Trap",
                    "§r§cThe Crusher"
                )
            ) {
                when (Skytils.config.trapDamageHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.trapDamageHider == 2)
                    else -> {
                    }
                }
            }

            // Auto-Recombobulator
            if (unformatted.startsWith("Your Auto-Recomb")) {
                when (Skytils.config.autoRecombHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.autoRecombHider == 2)
                    3 -> {
                        val matcher = RecombToast.pattern.matcher(formatted)
                        if (matcher.find(formatted.indexOf(" "))) {
                            GuiManager.toastGui.add(RecombToast(matcher.group(1)))
                        }
                        cancelChatPacket(event, false)
                    }
                    else -> {
                    }
                }
            }

            // Wither Essence
            if (formatted.contains("§r§eunlocked §r§dWither Essence §r§8x")) {
                when (Skytils.config.witherEssenceHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.witherEssenceHider == 2)
                    else -> {
                    }
                }
            }

            // Undead Essence
            if (formatted.contains("§r§eunlocked §r§dUndead Essence §r§8x")) {
                when (Skytils.config.undeadEssenceHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.undeadEssenceHider == 2)
                    else -> {
                    }
                }
            }

            // Healing (Zombie sword & Werewolf)
            if (formatted.startsWith("§r§a§l") && formatted.contains("healed")) {
                when (Skytils.config.healingHider) {
                    1, 2 -> cancelChatPacket(event, Skytils.config.healingHider == 2)
                    else -> {
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class SpamMessage(var message: String, var time: Long, var height: Double)
    class SpamGuiElement : GuiElement("Spam Gui", 1.0f, FloatPair(0.65f, 0.925f)) {
        /**
         * Based off of Soopyboo32's SoopyApis module
         * https://github.com/Soopyboo32
         *
         * @author Soopyboo32
         */
        override fun render() {
            val now = System.currentTimeMillis()
            val timePassed = now - lastTimeRender
            val sr = ScaledResolution(Minecraft.getMinecraft())
            val animDiv = timePassed.toDouble() / 1000.0
            lastTimeRender = now
            spamMessages.reverse()
            var i = 0
            while (i in spamMessages.indices) {
                val message = spamMessages[i]
                val messageWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(
                    message.message.stripControlCodes()
                )
                if (actualY > sr.scaledHeight / 2f) {
                    message.height = message.height + (i * 10 - message.height) * (animDiv * 5)
                } else if (actualY < sr.scaledHeight / 2f) {
                    message.height = message.height + (i * -10 - message.height) * (animDiv * 5)
                }
                var animOnOff = 0.0
                if (message.time < 500) {
                    animOnOff = 1 - message.time / 500.0
                }
                if (message.time > 3500) {
                    animOnOff = (message.time - 3500) / 500.0
                }
                animOnOff *= 90.0
                animOnOff += 90.0
                animOnOff = animOnOff * Math.PI / 180
                animOnOff = sin(animOnOff)
                animOnOff *= -1.0
                animOnOff += 1.0
                val x = animOnOff * (messageWidth + 30) * if (actualX < sr.scaledWidth / 2f) -1 else 1
                val y = -1 * message.height
                val shadow: TextShadow = when (Skytils.config.spamShadow) {
                    1 -> TextShadow.NONE
                    2 -> TextShadow.OUTLINE
                    else -> TextShadow.NORMAL
                }
                val alignment =
                    if (actualX < sr.scaledWidth / 2f) TextAlignment.LEFT_RIGHT else TextAlignment.RIGHT_LEFT
                ScreenRenderer.fontRenderer.drawString(
                    message.message,
                    (if (actualX < sr.scaledWidth / 2f) x else x + width).toFloat(),
                    y.toFloat(),
                    CommonColors.WHITE,
                    alignment,
                    shadow
                )
                if (message.time > 4000) {
                    spamMessages.remove(message)
                    i--
                }
                message.time += timePassed
                i++
            }
            spamMessages.reverse()
        }

        override fun demoRender() {
            val messageWidth =
                Minecraft.getMinecraft().fontRendererObj.getStringWidth("§r§7Your Implosion hit §r§c3 §r§7enemies for §r§c1,000,000.0 §r§7damage.§r".stripControlCodes())
            val shadow: TextShadow = when (Skytils.config.spamShadow) {
                1 -> TextShadow.NONE
                2 -> TextShadow.OUTLINE
                else -> TextShadow.NORMAL
            }
            val x = (sin(90 * Math.PI / 180) * -1 + 1) * (messageWidth + 30)
            val y = 0.0
            ScreenRenderer.fontRenderer.drawString(
                "§r§7Your Implosion hit §r§c3 §r§7enemies for §r§c1,000,000.0 §r§7damage.§r",
                x.toFloat(),
                y.toFloat(),
                CommonColors.WHITE,
                TextAlignment.LEFT_RIGHT,
                shadow
            )
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§r§7Your Implosion hit §r§c3 §r§7enemies for §r§c1,000,000.0 §r§7damage.§r")
        override val toggled: Boolean
            get() = Skytils.config.profileHider == 2 ||
                    Skytils.config.implosionHider == 2 ||
                    Skytils.config.midasStaffHider == 2 ||
                    Skytils.config.spiritSceptreHider == 2 ||
                    Skytils.config.giantSwordHider == 2 ||
                    Skytils.config.lividHider == 2 ||
                    Skytils.config.hopeHider == 2 ||
                    Skytils.config.manaUseHider == 2 ||
                    Skytils.config.bloodKeyHider == 2 ||
                    Skytils.config.hideBossMessages == 2 ||
                    Skytils.config.hideDungeonCountdownAndReady == 2 ||
                    Skytils.config.hideDungeonAbilities == 2 ||
                    Skytils.config.hideMortMessages == 2 ||
                    Skytils.config.superboomHider == 2 ||
                    Skytils.config.reviveStoneHider == 2 ||
                    Skytils.config.witherKeyHider == 2 ||
                    Skytils.config.inTheWayHider == 2 ||
                    Skytils.config.hideCantUseAbility == 2 ||
                    Skytils.config.comboHider == 2 ||
                    Skytils.config.cooldownHider == 2 ||
                    Skytils.config.hideNoEnemiesNearby == 2 ||
                    Skytils.config.manaMessages == 2 ||
                    Skytils.config.blessingEnchantHider == 2 ||
                    Skytils.config.blessedBaitHider == 2 ||
                    Skytils.config.tetherHider == 2 ||
                    Skytils.config.selfOrbHider == 2 ||
                    Skytils.config.otherOrbHider == 2 ||
                    Skytils.config.trapDamageHider == 2 ||
                    Skytils.config.autoRecombHider == 2 ||
                    Skytils.config.witherEssenceHider == 2 ||
                    Skytils.config.undeadEssenceHider == 2 ||
                    Skytils.config.healingHider == 2

        companion object {
            var lastTimeRender = Date().time
        }

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
}
