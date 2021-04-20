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

package skytils.skytilsmod.features.impl.overlays

import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.gson
import skytils.skytilsmod.events.GuiContainerEvent
import skytils.skytilsmod.utils.ItemRarity
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.Utils
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.regex.Matcher
import java.util.regex.Pattern

class FavoritePetOverlay {

    private var highlighting = false
    private val petNameRegex =
        Pattern.compile("^§7\\[Lvl (?<lvl>\\d{1,3})] (?<rarityColor>§[0-9a-f])(?<name>.+)\\b(?<skinned> ✦)?$")
    private val petLevelUpRegex =
        Pattern.compile("§r§aYour §r(?<rarityColor>§[0-9a-f])(?<name>.+)\\b(?<skinned> §r§6✦)? §r§alevelled up to level §r§9(?<lvl>\\d{1,3})§r§a!§r")

    companion object {
        private val favorited = HashSet<String>()
        private var saveFile = File(Skytils.modDir, "favoritepets.json")

        fun reloadSave() {
            favorited.clear()
            var dataObject: JsonArray
            try {
                FileReader(saveFile).use { `in` ->
                    dataObject = gson.fromJson(`in`, JsonArray::class.java)
                    for (value in dataObject) {
                        favorited.add(value.asString)
                    }
                }
            } catch (e: Exception) {
                dataObject = JsonArray()
                try {
                    FileWriter(saveFile).use { writer -> gson.toJson(dataObject, writer) }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }

        fun saveFavorites() {
            try {
                FileWriter(saveFile).use { writer ->
                    val arr = JsonArray()
                    for (value in favorited) {
                        arr.add(JsonPrimitive(value))
                    }
                    gson.toJson(arr, writer)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(event: ClientChatReceivedEvent) {
        if (!Utils.inSkyblock || event.type == 2.toByte()) return

        val formatted = event.message.formattedText
        if (formatted.contains(" §r§alevelled up to level §r§9")) {
            val matcher = petLevelUpRegex.matcher(formatted)
            if (matcher.find())
                for (favorite in favorited) {
                    if (favorite == "${
                            matcher.group("name").toUpperCase().replace(" ", "_")
                        }-${ItemRarity.byBaseColor(matcher.group("rarityColor"))?.rarityName}-${
                            matcher.group("lvl").toInt() - 1
                        }${
                            if (matcher.group("skinned") != null) "-SKINNED" else ""
                        }"
                    ) {
                        favorited.add(getPetIdFromMatcher(matcher))
                        favorited.remove(favorite)
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
            if (container.lowerChestInventory.name.endsWith(") Pets")) {
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
        val chest = event.container as ContainerChest
        if (!chest.lowerChestInventory.name.endsWith(") Pets")) return
        if (event.slot == null || event.slotId < 10 || event.slotId > 43 || !event.slot!!.hasStack) return
        val item = event.slot!!.stack!!
        val petId = getPetIdFromItem(item)
        event.isCanceled = true
        if (favorited.contains(petId)) favorited.remove(petId) else favorited.add(petId)
    }

    @SubscribeEvent
    fun onSlotDraw(event: GuiContainerEvent.DrawSlotEvent.Pre) {
        if (!Utils.inSkyblock || !Skytils.config.highlightFavoritePets || event.container !is ContainerChest) return
        val chest = event.container as ContainerChest
        if (!chest.lowerChestInventory.name.endsWith(") Pets")) return
        if (event.slot.slotNumber < 10 || event.slot.slotNumber > 43 || !event.slot.hasStack) return
        val item = event.slot.stack!!
        val petId = getPetIdFromItem(item)
        if (favorited.contains(petId)) {
            GlStateManager.translate(0f, 0f, 2f)
            Gui.drawRect(
                event.slot.xDisplayPosition,
                event.slot.yDisplayPosition,
                event.slot.xDisplayPosition + 16,
                event.slot.yDisplayPosition + 16,
                Skytils.config.favoritePetColor.rgb
            )
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
            matcher.group("name").toUpperCase().replace(" ", "_")
        }-${ItemRarity.byBaseColor(matcher.group("rarityColor"))?.rarityName}-${matcher.group("lvl")}${
            if (matcher.group("skinned") != null) "-SKINNED" else ""
        }"
    }

    init {
        reloadSave()
    }

}
