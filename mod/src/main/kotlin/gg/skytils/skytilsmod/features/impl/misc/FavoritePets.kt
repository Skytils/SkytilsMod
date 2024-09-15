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

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.basicYConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UMatrixStack
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.play.ChatMessageReceivedEvent
import gg.skytils.event.impl.screen.GuiContainerPreDrawSlotEvent
import gg.skytils.event.impl.screen.GuiContainerSlotClickEvent
import gg.skytils.event.impl.screen.ScreenDrawEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.utils.ItemRarity
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.RenderUtil.highlight
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.toStringIfTrue
import kotlinx.serialization.encodeToString
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import java.io.File
import java.io.Reader
import java.io.Writer

object FavoritePets : PersistentSave(File(Skytils.modDir, "favoritepets.json")), EventSubscriber {

    private val favorited = hashSetOf<String>()
    private var highlighting = false
    private val petNameRegex =
        Regex("^§7\\[Lvl (?<lvl>\\d{1,3})] (?<rarityColor>§[0-9a-f])(?<name>.+)\\b(?<skinned> ✦)?$")
    private val petLevelUpRegex =
        Regex("§r§aYour §r(?<rarityColor>§[0-9a-f])(?<name>.+)\\b(?<skinned> §r§6✦)? §r§alevelled up to level §r§9(?<lvl>\\d{1,3})§r§a!§r")

    private val window = Window(ElementaVersion.V5)
    private val button = SimpleButton("${if (highlighting) "§6" else "§f"}Favorite").constrain {
        x = 50.pixels
        y = basicYConstraint {
            (((mc.currentScreen as? GuiChest)?.height?.div(2) ?: 0) + 100f)
        }
        width = 100.pixels
        height = 20.pixels
    }.onMouseClick {
        highlighting = !highlighting
        (this as SimpleButton).text.setText("${if (highlighting) "§6" else "§f"}Favorite")
    } childOf window

    override fun setup() {
        register(::onChat, priority = gg.skytils.event.EventPriority.Highest)
        register(::onSlotClick, priority = gg.skytils.event.EventPriority.Highest)
        register(::onScreenDraw)
        register(::onSlotDraw)
    }

    fun onChat(event: ChatMessageReceivedEvent) {
        if (!Utils.inSkyblock) return

        val formatted = event.message.formattedText
        if (formatted.contains(" §r§alevelled up to level §r§9")) {
            petLevelUpRegex.find(formatted)?.let {
                for (favorite in favorited) {
                    if (favorite == getPetIdFromMatcher(it.groups, true)) {
                        favorited.add(getPetIdFromMatcher(it.groups, false))
                        favorited.remove(favorite)
                        markDirty<FavoritePets>()
                        break
                    }
                }
            }
        }

    }

    fun onScreenDraw(event: ScreenDrawEvent) {
        if (!Utils.inSkyblock || !Skytils.config.highlightFavoritePets) return
        val chest = event.screen as? GuiChest ?: return
        val container = chest.inventorySlots as ContainerChest
        if (container.lowerChestInventory.name.startsWith("Pets")) {
            window.draw(UMatrixStack.Compat.get())
        }
    }

    fun onSlotClick(event: GuiContainerSlotClickEvent) {
        if (!Utils.inSkyblock || !highlighting) return
        val chest = event.container as? ContainerChest ?: return
        if (!chest.lowerChestInventory.name.startsWith("Pets")) return
        if (event.slot?.hasStack != true || event.slotId < 10 || event.slotId > 43 || Utils.equalsOneOf(
                event.slot!!.slotNumber % 9,
                0,
                8
            )
        ) return
        val item = event.slot!!.stack!!
        val petId = getPetIdFromItem(item) ?: return
        event.cancelled = true
        if (favorited.contains(petId)) favorited.remove(petId) else favorited.add(petId)
        markDirty<FavoritePets>()
    }

    fun onSlotDraw(event: GuiContainerPreDrawSlotEvent) {
        if (!Utils.inSkyblock || !Skytils.config.highlightFavoritePets) return
        val chest = event.container as? ContainerChest ?: return
        if (!chest.lowerChestInventory.name.startsWith("Pets")) return
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

    private fun getPetIdFromItem(item: ItemStack): String? {
        return petNameRegex.find(ItemUtil.getDisplayName(item))?.let { getPetIdFromMatcher(it.groups) }
    }


    private fun getPetIdFromMatcher(groups: MatchGroupCollection, sub1: Boolean = false): String {
        return "${
            groups["name"]!!.value.uppercase().replace(" ", "_")
        }-${ItemRarity.byBaseColor(groups["rarityColor"]!!.value)?.rarityName}-${
            groups["lvl"]!!.value.toInt().minus(if (sub1) 1 else 0)
        }${"-SKINNED".toStringIfTrue(groups["skinned"] != null)}"
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
