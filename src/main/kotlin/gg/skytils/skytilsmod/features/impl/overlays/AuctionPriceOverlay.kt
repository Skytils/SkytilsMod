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
package gg.skytils.skytilsmod.features.impl.overlays

import gg.essential.universal.UKeyboard
import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.events.impl.GuiContainerEvent.SlotClickEvent
import gg.skytils.skytilsmod.features.impl.handlers.AuctionData
import gg.skytils.skytilsmod.features.impl.misc.ContainerSellValue
import gg.skytils.skytilsmod.gui.elements.CleanButton
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiEditSign
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.NumberUtil
import gg.skytils.skytilsmod.utils.NumberUtil.roundToPrecision
import gg.skytils.skytilsmod.utils.SBInfo
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.graphics.ScreenRenderer
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C12PacketUpdateSign
import net.minecraft.tileentity.TileEntitySign
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display
import java.awt.Color

object AuctionPriceOverlay {
    private var lastAuctionedStack: ItemStack? = null
    private var lastEnteredInput = ""
    private var undercut = false
    private val tooltipLocationButton = GuiButton(999, 2, 2, 20, 20, "bruh")

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (!Utils.inSkyblock || !Skytils.config.betterAuctionPriceInput) return
        if (event.gui is GuiEditSign && Utils.equalsOneOf(
                SBInfo.lastOpenContainerName,
                "Create Auction",
                "Create BIN Auction"
            )
        ) {
            val sign =
                (event.gui as AccessorGuiEditSign).tileSign
            if (sign != null && sign.pos.y == 0 && sign.signText[1].unformattedText == "^^^^^^^^^^^^^^^" && sign.signText[2].unformattedText == "Your auction" && sign.signText[3].unformattedText == "starting bid") {
                event.gui = AuctionPriceScreen(event.gui as GuiEditSign)
            }
        }
    }

    @SubscribeEvent
    fun onGuiKey(event: GuiScreenEvent.KeyboardInputEvent) {
        if (!Utils.inSkyblock || !Skytils.config.betterAuctionPriceInput) return
        if (event.gui is GuiChest && Keyboard.getEventKeyState() && Keyboard.getEventKey() == UKeyboard.KEY_ENTER) {
            if (Utils.equalsOneOf(
                    SBInfo.lastOpenContainerName,
                    "Create Auction",
                    "Create BIN Auction"
                )
            ) {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 29, 2, 3, mc.thePlayer)
                event.isCanceled = true
            } else if (Utils.equalsOneOf(
                    SBInfo.lastOpenContainerName,
                    "Confirm Auction",
                    "Confirm BIN Auction"
                )
            ) {
                mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, 11, 2, 3, mc.thePlayer)
                event.isCanceled = true
            }
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickEvent) {
        if (!Utils.inSkyblock || !Skytils.config.betterAuctionPriceInput) return
        if (event.gui is GuiChest) {
            if (Utils.equalsOneOf(
                    SBInfo.lastOpenContainerName,
                    "Create Auction",
                    "Create BIN Auction"
                ) && event.slotId == 31
            ) {
                val auctionItem = event.container.getSlot(13).stack
                if (auctionItem != null) {
                    if (auctionItem.displayName == "§a§l§nAUCTION FOR ITEM:") {
                        lastAuctionedStack = auctionItem
                    }
                }
            }
            if (Utils.equalsOneOf(
                    SBInfo.lastOpenContainerName,
                    "Confirm Auction",
                    "Confirm BIN Auction"
                )
            ) {
                if (event.slotId == 11) {
                    lastAuctionedStack = null
                }
            }
        }
    }

    open class AuctionPriceScreen(oldScreen: GuiEditSign) : GuiScreen() {
        private lateinit var undercutButton: CleanButton
        private lateinit var priceField: GuiTextField
        private var sign: TileEntitySign =
            (oldScreen as AccessorGuiEditSign).tileSign
        var fr: SmartFontRenderer = ScreenRenderer.fontRenderer
        private var dragging = false
        private var xOffset = 0f
        private var yOffset = 0f
        override fun initGui() {
            buttonList.clear()
            Keyboard.enableRepeatEvents(true)
            sign.setEditable(false)
            priceField = GuiTextField(0, fr, width / 2 - 135, height / 2, 270, 20)
            priceField.maxStringLength = 15
            priceField.setValidator { text: String? ->
                text!!.lowercase().replace("[^0-9.kmb]".toRegex(), "").length == text.length
            }
            priceField.isFocused = true
            priceField.text = lastEnteredInput
            priceField.setCursorPositionEnd()
            priceField.setSelectionPos(0)
            buttonList.add(
                CleanButton(
                    0,
                    width / 2 - 100,
                    height / 2 + 25,
                    200,
                    20,
                    if (!isUndercut()) "Mode: Normal" else "Mode: Undercut"
                ).also { undercutButton = it })
            buttonList.add(tooltipLocationButton)
            sign.signText[0] = ChatComponentText(input)
        }

        override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
            onMouseMove()
            drawGradientRect(0, 0, width, height, Color(117, 115, 115, 25).rgb, Color(0, 0, 0, 200).rgb)
            priceField.drawTextBox()
            if (lastAuctionedStack != null) {
                val auctionIdentifier = AuctionData.getIdentifier(lastAuctionedStack)
                if (auctionIdentifier != null) {
                    // this might actually have multiple items as the price
                    val valuePer = AuctionData.lowestBINs[auctionIdentifier]
                    if (valuePer != null) fr.drawString(
                        "Clean Lowest BIN Price: §b" + NumberUtil.nf.format((valuePer * lastAuctionedStack!!.stackSize).toInt()) + if (lastAuctionedStack!!.stackSize > 1) " §7(" + NumberUtil.nf.format(
                            valuePer.roundToPrecision(2)
                        ) + " each§7)" else "",
                        width / 2f,
                        height / 2f - 50,
                        CommonColors.ORANGE,
                        SmartFontRenderer.TextAlignment.MIDDLE,
                        SmartFontRenderer.TextShadow.NONE
                    )
                }
                val estimatedValue = ContainerSellValue.getItemValue(lastAuctionedStack!!)
                if (estimatedValue > 0) fr.drawString(
                    "Estimated Value: §b" + NumberUtil.nf.format(
                        estimatedValue
                    ),
                    width / 2f + 200,
                    height / 2f - 50,
                    CommonColors.ORANGE,
                    SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                    SmartFontRenderer.TextShadow.NONE
                )
                if (isUndercut()) {
                    val input = input
                    fr.drawString(
                        "Listing For: ${
                            input ?: "§cInvalid Value"
                        }",
                        width / 2f,
                        height / 2f - 25,
                        CommonColors.ORANGE,
                        SmartFontRenderer.TextAlignment.MIDDLE,
                        SmartFontRenderer.TextShadow.NONE
                    )
                }
                if (tooltipLocationButton.enabled) {
                    val lore = ArrayList(ItemUtil.getItemLore(lastAuctionedStack!!))
                    if (lore.size > 3) {
                        lore.removeAt(0)
                        lore.removeAt(lore.size - 1)
                        lore.removeAt(lore.size - 1)
                    }
                    if (lore.size > 0) {
                        var largestLen = 0
                        for (line in lore) {
                            val len = fr.getStringWidth(line)
                            if (len > largestLen) largestLen = len
                        }
                        val x = tooltipLocationButton.xPosition
                        val y = tooltipLocationButton.yPosition - 20
                        tooltipLocationButton.width = largestLen
                        tooltipLocationButton.height = lore.size * fr.FONT_HEIGHT + 20
                        fr.drawString(
                            "You're selling: ${lastAuctionedStack!!.stackSize}x",
                            x.toFloat(),
                            y.toFloat(),
                            CommonColors.ORANGE,
                            SmartFontRenderer.TextAlignment.LEFT_RIGHT,
                            SmartFontRenderer.TextShadow.NONE
                        )
                        drawHoveringText(lore, x - 10, y + 30, fr)
                        GlStateManager.disableLighting()
                    }
                }
            }
            undercutButton.drawButton(mc, mouseX, mouseY)
        }

        override fun updateScreen() {
            undercutButton.displayString = if (!isUndercut()) "Mode: Normal" else "Mode: Undercut"
            priceField.updateCursorCounter()
            super.updateScreen()
        }


        override fun keyTyped(typedChar: Char, keyCode: Int) {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
                sign.markDirty()
                mc.displayGuiScreen(null)
                return
            }
            priceField.textboxKeyTyped(typedChar, keyCode)
            val input = input
            if (input == null) {
                sign.signText[0] = ChatComponentText("Invalid Value")
            } else {
                sign.signText[0] = ChatComponentText(input)
                lastEnteredInput = priceField.text
            }
        }


        override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
            priceField.mouseClicked(mouseX, mouseY, mouseButton)
            super.mouseClicked(mouseX, mouseY, mouseButton)
        }

        override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
            super.mouseReleased(mouseX, mouseY, state)
            dragging = false
        }

        private fun onMouseMove() {
            val sr = UResolution
            val minecraftScale = sr.scaleFactor.toFloat()
            val floatMouseX = Mouse.getX() / minecraftScale
            val floatMouseY = (Display.getHeight() - Mouse.getY()) / minecraftScale
            if (dragging) {
                tooltipLocationButton.xPosition = (floatMouseX - xOffset).toInt()
                tooltipLocationButton.yPosition = (floatMouseY - yOffset).toInt()
            }
        }


        override fun actionPerformed(button: GuiButton) {
            if (button.id == 0) {
                undercut = !undercut
                sign.signText[0] = ChatComponentText(input ?: "Invalid Value")
            } else if (button.id == 999) {
                dragging = true
                val sr = UResolution
                val minecraftScale = sr.scaleFactor.toFloat()
                val floatMouseX = Mouse.getX() / minecraftScale
                val floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale
                xOffset = floatMouseX - tooltipLocationButton.xPosition
                yOffset = floatMouseY - tooltipLocationButton.yPosition
            }
        }

        override fun onGuiClosed() {
            Keyboard.enableRepeatEvents(false)
            val nethandlerplayclient = mc.netHandler
            nethandlerplayclient?.addToSendQueue(C12PacketUpdateSign(sign.pos, sign.signText))
            sign.setEditable(true)
        }

        val input: String?
            get() {
                val input = priceField.text
                if (isUndercut()) {
                    val lbin = AuctionData.lowestBINs[AuctionData.getIdentifier(lastAuctionedStack)]!!
                    try {
                        val num = input.toDouble()
                        val actualValue = (lbin - num) * lastAuctionedStack!!.stackSize
                        if (actualValue < 0) return null
                        val stringified = actualValue.toLong().toString()
                        return if (stringified.length > 15) NumberUtil.format(actualValue.toLong()) else stringified
                    } catch (ignored: NumberFormatException) {
                    }
                    if (isProperCompactNumber(input)) {
                        val num = getActualValueFromCompactNumber(input)
                        if (num != null) {
                            val actualValue = (lbin - num) * lastAuctionedStack!!.stackSize
                            if (actualValue < 0) return null
                            val stringified = actualValue.toLong().toString()
                            return if (stringified.length > 15) NumberUtil.format(actualValue.toLong()) else stringified
                        }
                    }
                    return null
                }
                return input.lowercase()
            }

    }

    private fun isUndercut(): Boolean {
        if (!undercut || lastAuctionedStack == null) return false
        val id = AuctionData.getIdentifier(lastAuctionedStack)
        return id != null && AuctionData.lowestBINs.containsKey(id)
    }

    /**
     * This code was modified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/44630965
     * @author Sachin Rao
     */
    private fun getActualValueFromCompactNumber(value: String): Double? {
        val lastAlphabet = value.replace("[^a-zA-Z]*$".toRegex(), "")
            .replace(".(?!$)".toRegex(), "")
        var multiplier = 1L
        when (lastAlphabet.lowercase()) {
            "k" -> multiplier = 1000L
            "m" -> multiplier = 1000000L
            "b" -> multiplier = 1000000000L
            "t" -> multiplier = 1000000000000L
            else -> {
            }
        }
        val values = value.split(lastAlphabet.toRegex()).toTypedArray()
        return if (multiplier == 1L) {
            null
        } else {
            val valueMultiplier: Double = try {
                values[0].toDouble()
            } catch (ex: ArrayIndexOutOfBoundsException) {
                0.0
            } catch (ex: NumberFormatException) {
                0.0
            }
            val valueAdder: Double = try {
                values[1].toDouble()
            } catch (ex: ArrayIndexOutOfBoundsException) {
                0.0
            } catch (ex: NumberFormatException) {
                0.0
            }
            valueMultiplier * multiplier + valueAdder
        }
    }

    /**
     * This code was modified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/44630965
     * @author Sachin Rao
     */
    private fun isProperCompactNumber(value: String): Boolean {
        val count = value.replace("\\s+".toRegex(), "").replace("[.0-9]+".toRegex(), "")
        return count.length < 2
    }
}