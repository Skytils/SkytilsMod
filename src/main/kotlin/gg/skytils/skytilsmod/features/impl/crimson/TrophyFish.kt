/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.crimson

import gg.essential.universal.ChatColor
import gg.essential.universal.UGraphics
import gg.essential.universal.wrappers.UPlayer
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.API
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.core.structure.GuiElement
import gg.skytils.skytilsmod.utils.*
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.UUID

object TrophyFish {
    private val trophyFish = mutableMapOf<String, Fish>()
    private val trophyFishRegex = Regex("♔ TROPHY FISH! You caught an? ([\\w\\- ]+) (BRONZE|SILVER|GOLD|DIAMOND)!")


    init {
        Skytils.guiManager.registerElement(TrophyFishDisplay())
    }

    suspend fun loadFromApi() {
        trophyFish.clear()
        trophyFish.putAll(getTrophyFishData(UPlayer.getUUID()) ?: return)
    }

    suspend fun getTrophyFishData(uuid: UUID): Map<String, Fish>? {
        val trophyFishData = API.getSelectedSkyblockProfile(uuid)?.members?.get(uuid.nonDashedString())?.trophy_fish
        return trophyFishData?.fish_count?.entries?.associate { (fish, data) ->
            fish to Fish(data.bronze, data.silver, data.gold, data.diamond)
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || SBInfo.mode != SkyblockIsland.CrimsonIsle.mode || !Config.trophyFishTracker) return
        printDevMessage(event.message.formattedText, "trophyspam")
        trophyFishRegex.matchEntire(event.message.formattedText.stripControlCodes())?.destructured?.let { (type, tier) ->
            printDevMessage("Found trophy fish of $type of tier $tier", "trophy")
            val fish = TrophyFish.entries.find { it.actualName.lowercase() == type.lowercase() } ?: return@let
            printDevMessage("Trophy fish type: ${fish.name}", "trophy")
            val field = when (tier.lowercase()) {
                "diamond" -> Fish::diamond
                "gold" -> Fish::gold
                "silver" -> Fish::silver
                "bronze" -> Fish::bronze
                else -> return@let
            }
            trophyFish[fish.name]?.let { data ->
                printDevMessage("Updating ${fish.actualName} $tier to ${field.get(data) + 1}", "trophy")
                field.set(data, field.get(data) + 1)
            }
        }
    }

    fun generateLocalTrophyFishList(total: Boolean = false) =
        generateTrophyFishList(trophyFish, total)

    fun generateTrophyFishList(data: Map<String, Fish>, total: Boolean = false) =
        data.entries
            .mapNotNull { (fish, data) -> (TrophyFish.entries.find { it.name == fish } ?: return@mapNotNull null) to data }
            .sortedBy { (type, _) -> TrophyFish.entries.indexOf(type) }
            .map { (type, data) ->
                type.formattedName +
                        if (total) {
                            " ${ChatColor.DARK_AQUA}[${ChatColor.LIGHT_PURPLE}${data.total}${ChatColor.DARK_AQUA}] "
                        } else {
                            " ${ChatColor.DARK_AQUA}» "
                        } +
                        "${ChatColor.DARK_GRAY}${data.bronze}${ChatColor.DARK_AQUA}-" +
                        "${ChatColor.GRAY}${data.silver}${ChatColor.DARK_AQUA}-" +
                        "${ChatColor.GOLD}${data.gold}${ChatColor.DARK_AQUA}-" +
                        "${ChatColor.AQUA}${data.diamond}"
        }

    fun generateLocalTotalTrophyFish() =
        generateTotalTrophyFish(trophyFish)

    fun generateTotalTrophyFish(data: Map<String, Fish>) =
        "${ChatColor.LIGHT_PURPLE}Total ${ChatColor.DARK_AQUA}» ${ChatColor.LIGHT_PURPLE}" + data.values.fold(0) { acc, fish ->
            acc + fish.total
        }

    class Fish(var bronze: Int = 0, var silver: Int = 0, var gold: Int = 0, var diamond: Int = 0) {
        val total: Int
            get() = bronze + silver + gold + diamond
    }

    enum class TrophyFish(val actualName: String, val color: ChatColor) {
        sulphur_skitter("Sulphur Skitter", ChatColor.WHITE),
        obfuscated_fish_1("Obfuscated 1", ChatColor.WHITE),
        steaming_hot_flounder("Steaming-Hot Flounder", ChatColor.WHITE),
        gusher("Gusher", ChatColor.WHITE),
        blobfish("Blobfish", ChatColor.WHITE),
        obfuscated_fish_2("Obfuscated 2", ChatColor.GREEN),
        slugfish("Slugfish", ChatColor.GREEN),
        flyfish("Flyfish", ChatColor.GREEN),
        obfuscated_fish_3("Obfuscated 3", ChatColor.BLUE),
        lava_horse("Lavahorse", ChatColor.BLUE),
        mana_ray("Mana Ray", ChatColor.BLUE),
        volcanic_stonefish("Volcanic Stonefish", ChatColor.BLUE),
        vanille("Vanille", ChatColor.BLUE),
        skeleton_fish("Skeleton Fish", ChatColor.DARK_PURPLE),
        moldfin("Moldfin", ChatColor.DARK_PURPLE),
        soul_fish("Soul Fish", ChatColor.DARK_PURPLE),
        karate_fish("Karate Fish", ChatColor.DARK_PURPLE),
        golden_fish("Golden Fish", ChatColor.GOLD);

        val formattedName: String
            get() = "$color$actualName"
    }

    class TrophyFishDisplay : GuiElement("Trophy Fish Display", 1f, 0, 0) {
        val alignment = if (scaleX > sr.scaledWidth / 2f) SmartFontRenderer.TextAlignment.RIGHT_LEFT else SmartFontRenderer.TextAlignment.LEFT_RIGHT
        override fun render() {
            if (!toggled || !Utils.inSkyblock || SBInfo.mode != SkyblockIsland.CrimsonIsle.mode) return
            generateLocalTrophyFishList(Config.showTrophyFishTotals).forEachIndexed { idx, str ->
                fr.drawString(
                    str,
                    0f,
                    (idx * fr.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE,
                    alignment,
                    textShadow
                )
            }
            if (Config.showTotalTrophyFish) {
                fr.drawString(
                    generateLocalTotalTrophyFish(),
                    0f,
                    (trophyFish.size * fr.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE,
                    alignment,
                    textShadow
                )
            }
        }

        override fun demoRender() {
            TrophyFish.entries.forEachIndexed { idx, fish ->
                fr.drawString(
                    "${fish.formattedName} ${ChatColor.DARK_AQUA}» "+
                            "${ChatColor.DARK_GRAY}999${ChatColor.DARK_AQUA}-" +
                            "${ChatColor.GRAY}99${ChatColor.DARK_AQUA}-" +
                            "${ChatColor.GOLD}9${ChatColor.DARK_AQUA}-" +
                            "${ChatColor.AQUA}0",
                    0f,
                    (idx * fr.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE,
                    SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                    SmartFontRenderer.TextShadow.NORMAL
                )
            }
            if (Config.showTotalTrophyFish) {
                fr.drawString(
                    "${ChatColor.LIGHT_PURPLE}Total ${ChatColor.DARK_AQUA}» 9999",
                    0f,
                    (trophyFish.size * fr.FONT_HEIGHT).toFloat(),
                    CommonColors.WHITE,
                    alignment,
                    textShadow
                )
            }
        }

        override val toggled: Boolean
            get() = Config.trophyFishTracker

        override val height: Int // This converts the boolean to an int (1 for true, 0 for false)
            get() = (TrophyFish.entries.size + Config.showTotalTrophyFish.compareTo(false)) * UGraphics.getFontHeight()
        override val width: Int
            get() = UGraphics.getStringWidth("Steaming-Hot Flounder » 999-99-99-9")

    }
}