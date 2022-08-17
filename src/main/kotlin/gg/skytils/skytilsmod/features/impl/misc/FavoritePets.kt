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

package gg.skytils.skytilsmod.features.impl.misc

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent
import gg.skytils.skytilsmod.utils.ItemRarity
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.Utils
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.io.Reader
import java.io.Writer
import java.util.regex.Matcher
import java.util.regex.Pattern

object FavoritePets : PersistentSave(File(Skytils.modDir, "favoritepets.json")) {

    private val favorited = hashSetOf<String>()
    private var highlighting = false
    private val petNameRegex =
        Pattern.compile("^§7\\[Lvl (?<lvl>\\d{1,3})] (?<rarityColor>§[0-9a-f])(?<name>.+)\\b(?<skinned> ✦)?$")
    private val petLevelUpRegex =
        Pattern.compile("§r§aYour §r(?<rarityColor>§[0-9a-f])(?<name>.+)\\b(?<skinned> §r§6✦)? §r§alevelled up to level §r§9(?<lvl>\\d{1,3})§r§a!§r")

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte()) return

        val formatted = event.message.formattedText
        if (formatted.contains(" §r§alevelled up to level §r§9")) {
            val matcher = petLevelUpRegex.matcher(formatted)
            if (matcher.find())
                for (favorite in favorited) {
                    if (favorite == "${
                            matcher.group("name").uppercase().replace(" ", "_")
                        }-${ItemRarity.byBaseColor(matcher.group("rarityColor"))?.rarityName}-${
                            matcher.group("lvl").toInt() - 1
                        }${
                            if (matcher.group("skinned") != null) "-SKINNED" else ""
                        }"
                    ) {
                        favorited.add(getPetIdFromMatcher(matcher))
                        favorited.remove(favorite)
                        markDirty<FavoritePets>()
                        break
                    }
                }
        }

    }

    @SubscribeEvent
    fun onGuiScreenEvent(event: GuiScreenEvent) {
        if (!Utils.inSkyblock || !Skytils.config.highlightFavoritePets) return
        if (event.gui is GuiChest) {
            val chest = event.gui as GuiChest
            val container = chest.inventorySlots as ContainerChest
            if (container.lowerChestInventory.name.endsWith(") Pets") || container.lowerChestInventory.name == "Pets") {
                when {
                    event is GuiScreenEvent.InitGuiEvent -> event.buttonList.add(
                        GuiButton(
                            69420,
                            50,
                            chest.height / 2 + 100,
                            100,
                            20,
                            "${if (highlighting) "§6" else "§f"}Favorite"
                        )
                    )
                    event is GuiScreenEvent.ActionPerformedEvent.Pre && event.button.id == 69420 -> {
                        highlighting = !highlighting
                        event.button.displayString = "${if (highlighting) "§6" else "§f"}Favorite"
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onSlotClick(event: GuiContainerEvent.SlotClickEvent) {
        if (!Utils.inSkyblock || !highlighting || event.container !is ContainerChest) return
        val chest = event.container
        if (!chest.lowerChestInventory.name.endsWith(") Pets") && chest.lowerChestInventory.name != "Pets") return
        if (event.slot == null || !event.slot.hasStack || event.slotId < 10 || event.slotId > 43 || Utils.equalsOneOf(
                event.slot.slotNumber % 9,
                0,
                8
            )
        ) return
        val item = event.slot.stack!!
        val petId = getPetIdFromItem(item)
        event.isCanceled = true
        if (favorited.contains(petId)) favorited.remove(petId) else favorited.add(petId)
        markDirty<FavoritePets>()
    }

    @SubscribeEvent
    fun onSlotDraw(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inSkyblock || !Skytils.config.highlightFavoritePets || event.container !is ContainerChest) return
        val chest = event.container
        if (!chest.lowerChestInventory.name.endsWith(") Pets") && chest.lowerChestInventory.name != "Pets") return
        if (!event.slot.hasStack || event.slot.slotNumber < 10 || event.slot.slotNumber > 43 || Utils.equalsOneOf(
                event.slot.slotNumber % 9,
                0,
                8
            )
        ) return
        val item = event.slot.stack
        val petId = getPetIdFromItem(item)
        if (favorited.contains(petId)) {
            GlStateManager.translate(0f, 0f, 2f)
            event.slot highlight Skytils.config.favoritePetColor
            GlStateManager.translate(0f, 0f, -2f)
        }
    }

    private fun getPetIdFromItem(item: ItemStack): String {
        val matcher = petNameRegex.matcher(ItemUtil.getDisplayName(item))
        if (!matcher.matches()) return ""
        return getPetIdFromMatcher(matcher)
    }

    private fun getPetIdFromMatcher(matcher: Matcher): String {
        return "${
            matcher.group("name").uppercase().replace(" ", "_")
        }-${ItemRarity.byBaseColor(matcher.group("rarityColor"))?.rarityName}-${matcher.group("lvl")}${
            if (matcher.group("skinned") != null) "-SKINNED" else ""
        }"
    }

    override fun read(reader: Reader) {
        favorited.clear()
        favorited.addAll(json.decodeFromString<Set<String>>(reader.readText()))
    }

    override fun write(writer: Writer) {
        writer.write(json.encodeToString(favorited))
    }

    override fun setDefault(writer: Writer) {
        writer.write("[]")
    }

}
