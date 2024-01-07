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

package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.utils.ItemUtil.getItemLore
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.SkyblockIsland
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
    TrophyFishingProgress.kt

    A Kotlin class nested within an object by Erymanthus | RayDeeUx.

    Intended to display missing/undiscovered Trophy Fishes. Until Hylin
    rises from its grave, this is the closest thing I could concoct to a HUD
    element that displays such information. Unfortunately requires prior
    access to the Trophy Fishing menu (because, again, Hylin is somehow in
    public archive as of writing this comment block).

    Inspired by FluxCapacitor2's ContainerSellValue,
    TrophyFishingProgress displays a player's current progress
    in Trophy Fishing. This is in a separate file instead of being in MiscFeatures.kt
    as this HUD display relies on multiple Forge events
    (and so would be very out-of-place if inside MiscFeatures.kt).

*   @author Erymanthus | RayDeeUx
*/

object TrophyFishingProgress {

    private val trophyFishingProgressDisplay = TrophyFishingProgressDisplay() //do not delete this otherwise the HUD WONT SHOW
    private val TROPHY_FISH_MESSAGE_REGEX = Regex("(?:[^:\\n]*)(?:§.)+TROPHY FISH! (?:§.)+You caught an? (?:§.)+(?<trophyFishType>[\\w ]+)(?:§.)+ (?:§.)+(?<trophyFishTier>[A-Z]+)(?:§.)+\\.(?:[^:\\n]*)").toPattern()
    private var trophyFishMissing = mutableListOf(
        "§c§lTrophy Fishes Missing:",
        "§c§lVisit Odger!",
    )
    //TODO: maybe move the below list to the skytils mod data repo
    private val noProgressTrophyFishes = listOf(
        "§fSulphur Skitter",
        "§fObfuscated 1",
        "§fSteaming-Hot Flounder",
        "§fGusher",
        "§fBlobfish",
        "§aObfuscated 2",
        "§aSlugfish",
        "§aFlyfish",
        "§9Obfuscated 3",
        "§9Lavahorse",
        "§9Mana Ray",
        "§9Volcanic Stonefish",
        "§9Vanille",
        "§5Skeleton Fish",
        "§5Moldfin",
        "§5Soul Fish",
        "§5Karate Fish",
        "§6Golden Fish",
    )
    private val possibleTrophyTiers = listOf(
        "BRONZE",
        "SILVER",
        "GOLD",
        "DIAMOND",
    )
    private val possibleAbbreviations = listOf(
        "§8B",
        "§7S",
        "§6G",
        "§bD",
    )

    private fun String.removeJunk(theAbbreviation: String) =
        replace(theAbbreviation, "") //initial removal of the tier that was discovered
        .replace("/ §r /", "/") //remove space (for discoveries of gold and silver trophies)
        .replace("§r: §r / §", "§r: §") //remove space (for discoveries of diamond trophies)
        .removeSuffix("§r / ") //remove space (for discoveries of bronze trophies)
        .removeSuffix("§r / §r") //remove space (funky edge cases, i guess)
        .trim() //remove space (at the end)

    class TrophyFishingProgressDisplay : GuiElement("Trophy Fishing Progress Display", x = 150, y = 20) {
        internal val rightAlign: Boolean
            get() = scaleX > UResolution.scaledWidth / 2f
        internal val textPosX: Float
            get() = if (rightAlign) scaleWidth else 0f
        internal val alignment: SmartFontRenderer.TextAlignment
            get() = if (rightAlign) SmartFontRenderer.TextAlignment.RIGHT_LEFT
            else SmartFontRenderer.TextAlignment.LEFT_RIGHT
        override fun render() {
            val player = Skytils.mc.thePlayer
            if (toggled && Utils.inSkyblock && player != null && SBInfo.mode == SkyblockIsland.CrimsonIsle.mode) {
                var i = 0
                for (trophyFish in trophyFishMissing) {
                    if (!Skytils.config.noProgressTrophies && (trophyFish.endsWith("§r: §bD§r / §6G§r / §7S§r / §8B§r"))) {
                        continue
                    }
                    fr.drawString(
                        trophyFish, textPosX, (i * fr.FONT_HEIGHT).toFloat(),
                        CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL
                    )
                    i++
                }
            }
        }

        override fun demoRender() {
            listOf(
                "§c§lTrophy Fishes Missing:",
                "§fSteaming-Hot Flounder§r: §bD§r / §6G§r / §7S§r / §8B",
                "§9Vanille§r: §bD§r / §6G§r / §8B",
                "§6Golden Fish§r: §bD§r / §7S§r / §8B",
                "§5Karate Fish§r: §6G§r / §8B",
            ).forEachIndexed { i, str ->
                fr.drawString(
                    str, textPosX, (i * fr.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL
                )
            }
        }

        override val height: Int
            get() = ScreenRenderer.fontRenderer.FONT_HEIGHT
        override val width: Int
            get() = ScreenRenderer.fontRenderer.getStringWidth("§fSteaming-Hot Flounder§r: §bD§r / §6G§r / §7S§r / §8B")

        override val toggled: Boolean
            get() = Skytils.config.trophyFishingProgress

        init {
            Skytils.guiManager.registerElement(this)
        }
    }
    @SubscribeEvent
    fun onPostBackgroundDrawn(event: GuiScreenEvent.BackgroundDrawnEvent) {
        if (!Utils.inSkyblock) return
        val container = (Skytils.mc.currentScreen as? GuiChest)?.inventorySlots as? ContainerChest ?: return
        val chestName = container.lowerChestInventory.name
        if (!chestName.endsWith("Trophy Fishing")) return
        if (!Skytils.config.trophyFishingProgress) return

        var listBuilding = mutableListOf("§c§lTrophy Fishes Missing:")

        val slots = container.inventorySlots.filter {
            it.hasStack && it.inventory != Skytils.mc.thePlayer.inventory
            && ((it.stack.item == Items.skull) || (it.stack.item == Items.dye))
            && (!it.stack.displayName.endsWith("Fillet Trophy Fish"))
        }

        val clone = noProgressTrophyFishes.toMutableList()

        for (s in slots) {
            val stack = s.stack
            if (clone.isEmpty()) break
            if (stack.displayName.isEmpty()) continue
            if ((stack.displayName.startsWith("§c§k"))) {
                val trophyFishName = clone.first()
                listBuilding.add("$trophyFishName§r: §bD§r / §6G§r / §7S§r / §8B§r")
                clone.remove(trophyFishName)
            } else if ((stack.displayName.startsWith("§"))) {
                clone.remove(stack.displayName)
                var stringBuilding = "${stack.displayName}§r: "
                val missingTiers = getItemLore(stack).filter {
                    it.contains("§c✖")
                }
                missingTiers.forEach {
                    stringBuilding = "$stringBuilding${it.take(3)}§r / "
                }
                listBuilding.add(stringBuilding.removeSuffix("§r / "))
            }
        }
        trophyFishMissing = listBuilding
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST) //not sure why i needed to specify eventpriority to get this part of the feature working but here we are
    fun onChat(event: ClientChatReceivedEvent) {
        if (trophyFishMissing.any { it == "§c§lVisit Odger!" }) return
        if (!(Utils.inSkyblock && SBInfo.mode == SkyblockIsland.CrimsonIsle.mode)) return
        if (event.type.toInt() == 2) return

        val formatted = event.message.formattedText
        val unformatted = event.message.unformattedText.stripControlCodes()

        //§6§lTROPHY FISH! §r§bYou caught a §r§9Vanille§r§r§r §r§l§r§8§lBRONZE§r§b.
        //TROPHY FISH! You caught a Vanille BRONZE.
        //Vanille BRONZE
        //§6§lTROPHY FISH! §r§bYou caught a §r§aSlugfish§r§r§r §r§l§r§8§lBRONZE§r§b.
        //§6§lTROPHY FISH! §r§bYou caught a §r§fBlobfish§r§r§r §r§l§r§8§lBRONZE§r§b.

        //why it requires .contains i have no clue
        val trophyFishMatcherOnFormatted = TROPHY_FISH_MESSAGE_REGEX.matcher(formatted)
        if (!trophyFishMatcherOnFormatted.matches()) return
        if (!possibleTrophyTiers.any { unformatted.contains(it) }) return
        val trophyFish = trophyFishMatcherOnFormatted.group("trophyFishType")
        val trophyTier = trophyFishMatcherOnFormatted.group("trophyFishTier")

        val theAbbreviation = when (trophyTier.take(1)) {
            //"§bDiamond §c✖", "§6Gold §c✖", "§7Silver §a✔", "§8Bronze §c✖"
            "B" -> "§8B"
            "S" -> "§7S"
            "G" -> "§6G"
            "D" -> "§bD"
            else -> "§5${trophyTier.take(1)}"
        }

        val clone = trophyFishMissing
        val listBuildingAgain = mutableListOf<String>()

        for (entry in clone) {
            if (entry.contains(trophyFish) && entry.contains(theAbbreviation)) {
                //§fGusher§r: §bD§r / §6G§r
                val newEntry = entry
                    .replace("§6Golden Fish", "§6golden fish")
                    .removeJunk(theAbbreviation)
                    .replace("§6golden fish", "§6Golden Fish")
                //this edge case can commit bathtub toaster
                if (possibleAbbreviations.any{ newEntry.contains(it) } && newEntry.split("§r:").last().isNotEmpty()) {
                    listBuildingAgain.add(newEntry)
                }
            }
            else {
                listBuildingAgain.add(entry)
            }
        }

        trophyFishMissing = listBuildingAgain
    }
}