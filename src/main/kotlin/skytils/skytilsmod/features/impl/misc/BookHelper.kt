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

package skytils.skytilsmod.features.impl.misc

import gg.essential.universal.UGraphics
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.events.impl.GuiContainerEvent
import skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiContainer
import skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import skytils.skytilsmod.utils.graphics.SmartFontRenderer
import skytils.skytilsmod.utils.graphics.colors.CommonColors

object BookHelper {
    var errorString: String? = null

    @SubscribeEvent
    fun guiDraw(event: GuiContainerEvent.BackgroundDrawnEvent) {
        if (!Skytils.config.bookHelper || !Utils.inSkyblock) return
        if (event.container !is ContainerChest || event.container.lowerChestInventory.displayName.unformattedText.trim() != "Anvil") {
            errorString = null
            return
        }
        val book1 = event.container.getSlot(29).stack
        val nbt1 = getExtraAttributes(book1)
        val book2 = event.container.getSlot(33).stack
        val nbt2 = getExtraAttributes(book2)
        if (
            book1 == null ||
            book2 == null ||
            book1.item != Items.enchanted_book ||
            book2.item != Items.enchanted_book ||
            nbt1 == null ||
            nbt2 == null
        ) {
            errorString = null
            return
        }
        val enchantNBT = listOf(nbt1, nbt2).map { nbt ->
            nbt.getCompoundTag("enchantments")
        }
        val enchantList = enchantNBT.map { nbt ->
            nbt.keySet.takeIf { it.size == 1 }?.first()
        }.takeIf { it.all { it != null } }?.map { it!! } ?: kotlin.run { errorString = null; return }
        if (enchantList[0] != enchantList[1]) {
            errorString = "Enchant Types don't match!"
        } else if (enchantNBT[0].getInteger(enchantList[0]) != enchantNBT[1].getInteger(enchantList[1])) {
            errorString = "Tiers don't match!"
        } else {
            errorString = null
            return
        }
    }

    @SubscribeEvent
    fun drawScreen(event: GuiScreenEvent.DrawScreenEvent.Post) {
        if (errorString != null && event.gui is GuiContainer) {
            val gui = event.gui as AccessorGuiContainer
            UGraphics.disableLighting()
            UGraphics.disableBlend()
            UGraphics.disableDepth()
            ScreenRenderer.fontRenderer.drawString(
                errorString,
                gui.guiLeft + gui.xSize / 2f,
                gui.guiTop + 22.5f,
                CommonColors.RED,
                SmartFontRenderer.TextAlignment.MIDDLE
            )
            UGraphics.enableDepth()
            UGraphics.enableBlend()
            UGraphics.enableLighting()
        }
    }
}