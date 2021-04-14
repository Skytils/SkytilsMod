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

package skytils.skytilsmod.features.impl.overlays;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.features.impl.handlers.AuctionData;
import skytils.skytilsmod.gui.commandaliases.elements.CleanButton;
import skytils.skytilsmod.mixins.AccessorGuiEditSign;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.NumberUtil;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class AuctionPriceOverlay {

    private static ItemStack lastAuctionedStack;
    private static String lastEnteredInput = "";
    private static boolean undercut = false;
    private static final GuiButton tooltipLocationButton = new GuiButton(999, 2, 2, 20, 20, "bruh");

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (!Utils.inSkyblock || !Skytils.config.betterAuctionPriceInput) return;

        if (event.gui instanceof GuiEditSign && Utils.equalsOneOf(SBInfo.getInstance().lastOpenContainerName, "Create Auction", "Create BIN Auction")) {
            TileEntitySign sign = ((AccessorGuiEditSign) event.gui).getTileSign();
            if (sign != null && sign.getPos().getY() == 0 && sign.signText[1].getUnformattedText().equals("^^^^^^^^^^^^^^^") && sign.signText[2].getUnformattedText().equals("Your auction") && sign.signText[3].getUnformattedText().equals("starting bid")) {
                event.gui = new AuctionPriceScreen((GuiEditSign) event.gui);
            }
        }
    }

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (!Utils.inSkyblock || !Skytils.config.betterAuctionPriceInput) return;

        if (event.gui instanceof GuiChest) {
            if (Utils.equalsOneOf(SBInfo.getInstance().lastOpenContainerName, "Create Auction", "Create BIN Auction") && event.slotId == 31) {
                ItemStack auctionItem = event.container.getSlot(13).getStack();
                if (auctionItem != null) {
                    if (auctionItem.getDisplayName().equals("§a§l§nAUCTION FOR ITEM:")) {
                        lastAuctionedStack = auctionItem;
                    }
                }
            }
            if (Utils.equalsOneOf(SBInfo.getInstance().lastOpenContainerName, "Confirm Auction", "Confirm BIN Auction")) {
                if (event.slotId == 11) {
                    lastAuctionedStack = null;
                }
            }
        }
    }

    private static boolean isUndercut() {
        if (!undercut || lastAuctionedStack == null) return false;
        String id = AuctionData.getIdentifier(lastAuctionedStack);
        return id != null && AuctionData.lowestBINs.containsKey(id);
    }

    /**
     * This code was modified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/44630965
     * @author Sachin Rao
     */
    private static Double getActualValueFromCompactNumber(String value) {
        String lastAlphabet = value.replaceAll("[^a-zA-Z]*$", "")
                .replaceAll(".(?!$)", "");
        long multiplier = 1L;

        switch (lastAlphabet.toLowerCase()) {
            case "k":
                multiplier = 1_000L;
                break;
            case "m":
                multiplier = 1_000_000L;
                break;
            case "b":
                multiplier = 1_000_000_000L;
                break;
            case "t":
                multiplier = 1_000_000_000_000L;
                break;
            default:
                break;
        }

        String[] values = value.split(lastAlphabet);

        if (multiplier == 1) {
            return null;
        } else {
            double valueMultiplier = Double.parseDouble(values[0]);
            double valueAdder;
            try {
                valueAdder = Double.parseDouble(values[1]);
            } catch (ArrayIndexOutOfBoundsException ex) {
                valueAdder = 0.0d;
            }
            double total = (valueMultiplier * multiplier) + valueAdder;
            return total;
        }
    }

    /**
     * This code was modified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/44630965
     * @author Sachin Rao
     */
    private static boolean isProperCompactNumber(String value) {
        value = value.replaceAll("\\s+", "");
        String count = value.replaceAll("[.0-9]+", "");
        return count.length() < 2;
    }

    private static double getValueOfEnchantments(ItemStack item) {
        NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(item);
        if (extraAttr == null || !extraAttr.hasKey("enchantments")) return 0;

        NBTTagCompound enchantments = extraAttr.getCompoundTag("enchantments");

        double total = 0;

        for (String enchantName : enchantments.getKeySet()) {
            String id = "ENCHANTED_BOOK-" + enchantName.toUpperCase(Locale.US) + "-" + enchantments.getInteger(enchantName);
            Double price = AuctionData.lowestBINs.get(id);
            if (price == null) continue;

            double npcPrice = Double.MAX_VALUE;
            switch (id) {
                case "ENCHANTED_BOOK-TELEKINESIS-1":
                    npcPrice = 100;
                    break;
                case "ENCHANTED_BOOK-TRUE_PROTECTION-1":
                    npcPrice = 900_000;
                    break;
            }

            total += Math.min(npcPrice, price);
        }

        return total;
    }

    private static double getHotPotatoBookValue(ItemStack item) {
        NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(item);
        if (extraAttr == null || !extraAttr.hasKey("hot_potato_count")) return 0;

        int potatoCount = extraAttr.getInteger("hot_potato_count");

        int hpbs = Math.min(potatoCount, 10);
        int fpbs = potatoCount - hpbs;

        Double hpbPrice = AuctionData.lowestBINs.get("HOT_POTATO_BOOK");
        Double fpbPrice = AuctionData.lowestBINs.get("FUMING_POTATO_BOOK");

        double total = 0;

        if (hpbPrice != null) total += hpbs * hpbPrice;
        if (fpbPrice != null) total += fpbs * fpbPrice;

        return total;
    }

    public static class AuctionPriceScreen extends GuiScreen {

        public CleanButton undercutButton;
        public GuiTextField priceField;
        public TileEntitySign sign;
        public SmartFontRenderer fr = ScreenRenderer.fontRenderer;

        public boolean dragging = false;

        private float xOffset = 0;
        private float yOffset = 0;

        public AuctionPriceScreen(GuiEditSign oldScreen) {
            this.sign = ((AccessorGuiEditSign) oldScreen).getTileSign();
        }

        @Override
        public void initGui() {
            this.buttonList.clear();
            Keyboard.enableRepeatEvents(true);
            sign.setEditable(false);
            priceField = new GuiTextField(0, fontRendererObj, width/2-135, height/2, 270, 20);
            priceField.setMaxStringLength(15);
            priceField.setValidator((text) -> text.toLowerCase().replaceAll("[^0-9.kmb]", "").length() == text.length());
            priceField.setFocused(true);
            priceField.setText(lastEnteredInput);
            priceField.setCursorPositionEnd();
            priceField.setSelectionPos(0);
            buttonList.add(undercutButton = new CleanButton(0, width/2 - 100, height/2 + 25, 200, 20, !isUndercut() ? "Mode: Normal" : "Mode: Undercut"));
            buttonList.add(tooltipLocationButton);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            onMouseMove(mouseX, mouseY);
            drawGradientRect(0, 0, this.width, this.height, new Color(117, 115, 115, 25).getRGB(), new Color(0,0, 0,200).getRGB());
            priceField.drawTextBox();
            if (lastAuctionedStack != null) {
                String auctionIdentifier = AuctionData.getIdentifier(lastAuctionedStack);
                if (auctionIdentifier != null) {
                    // this might actually have multiple items as the price
                    Double valuePer = AuctionData.lowestBINs.get(auctionIdentifier);
                    if (valuePer != null) fr.drawString("Clean Lowest BIN Price: §b" + NumberUtil.nf.format(valuePer * lastAuctionedStack.stackSize) + (lastAuctionedStack.stackSize > 1 ? " §7(" + NumberUtil.nf.format(valuePer) + " each§7)" : ""), this.width/2f, this.height/2f-50, CommonColors.ORANGE, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
                }
                double enchantValue = getValueOfEnchantments(lastAuctionedStack);
                if (enchantValue > 0)
                    fr.drawString("Estimated Enchantment Value: §b" + NumberUtil.nf.format(enchantValue), this.width/2f+200, this.height/2f-50, CommonColors.ORANGE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
                double hpbValue = getHotPotatoBookValue(lastAuctionedStack);
                if (hpbValue > 0)
                    fr.drawString("HPB Value: §b" + NumberUtil.nf.format(hpbValue), this.width/2f+200, this.height/2f-25, CommonColors.ORANGE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
                if (isUndercut()) {
                    String input = getInput();
                    fr.drawString("Listing For: " + (input == null ? "§cInvalid Value" : input), this.width/2f, this.height/2f-25, CommonColors.ORANGE, SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
                }
                if (tooltipLocationButton.enabled) {
                    ArrayList<String> lore = Lists.newArrayList(ItemUtil.getItemLore(lastAuctionedStack));
                    if (lore.size() > 3) {
                        lore.remove(0);
                        lore.remove(lore.size() - 1);
                        lore.remove(lore.size() - 1);
                    }
                    if (lore.size() > 0) {
                        int largestLen = 0;
                        for (String line : lore) {
                            int len = fr.getStringWidth(line);
                            if (len > largestLen) largestLen = len;
                        }
                        int x = tooltipLocationButton.xPosition;
                        int y = tooltipLocationButton.yPosition - 20;
                        tooltipLocationButton.width = largestLen;
                        tooltipLocationButton.height = lore.size() * fr.FONT_HEIGHT + 20;
                        fr.drawString("You're selling: " + lastAuctionedStack.stackSize + "x", x, y, CommonColors.ORANGE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NONE);
                        drawHoveringText(lore, x-10, y+30, fr);
                        GlStateManager.disableLighting();
                    }
                }
            }

            undercutButton.drawButton(mc, mouseX, mouseY);
        }

        @Override
        public void updateScreen() {
            undercutButton.displayString = !isUndercut() ? "Mode: Normal" : "Mode: Undercut";
            priceField.updateCursorCounter();
            super.updateScreen();
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_ESCAPE) {
                sign.markDirty();
                mc.displayGuiScreen(null);
                return;
            }
            priceField.textboxKeyTyped(typedChar, keyCode);
            String input = getInput();
            if (input == null) {
                sign.signText[0] = new ChatComponentText("Invalid Value");
            } else {
                sign.signText[0] = new ChatComponentText(input);
                lastEnteredInput = priceField.getText();
            }
        }

        @Override
        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            priceField.mouseClicked(mouseX, mouseY, mouseButton);
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }

        @Override
        protected void mouseReleased(int mouseX, int mouseY, int state) {
            super.mouseReleased(mouseX, mouseY, state);
            dragging = false;
        }

        protected void onMouseMove(int mouseX, int mouseY) {
            ScaledResolution sr = new ScaledResolution(mc);
            float minecraftScale = sr.getScaleFactor();
            float floatMouseX = Mouse.getX() / minecraftScale;
            float floatMouseY = (Display.getHeight() - Mouse.getY()) / minecraftScale;
            if (dragging) {
                tooltipLocationButton.xPosition = (int) (floatMouseX - xOffset);
                tooltipLocationButton.yPosition = (int) (floatMouseY - yOffset);
            }
        }

        @Override
        protected void actionPerformed(GuiButton button) throws IOException {
            if (button.id == 0) {
                undercut = !undercut;
            } else if (button.id == 999) {
                dragging = true;
                ScaledResolution sr = new ScaledResolution(mc);
                float minecraftScale = sr.getScaleFactor();
                float floatMouseX = Mouse.getX() / minecraftScale;
                float floatMouseY = (mc.displayHeight - Mouse.getY()) / minecraftScale;

                xOffset = floatMouseX - tooltipLocationButton.xPosition;
                yOffset = floatMouseY - tooltipLocationButton.yPosition;
            }
        }

        @Override
        public void onGuiClosed() {
            Keyboard.enableRepeatEvents(false);
            NetHandlerPlayClient nethandlerplayclient = mc.getNetHandler();

            if (nethandlerplayclient != null) {
                nethandlerplayclient.addToSendQueue(new C12PacketUpdateSign(sign.getPos(), sign.signText));
            }

            sign.setEditable(true);
        }

        public String getInput() {
            String input = priceField.getText();
            if (isUndercut()) {
                double lbin = AuctionData.lowestBINs.get(AuctionData.getIdentifier(lastAuctionedStack));
                try {
                    double num = Double.parseDouble(input);
                    double actualValue = (lbin - num) * lastAuctionedStack.stackSize;
                    if (actualValue < 0) return null;
                    String stringified = Long.toString((long)actualValue);
                    return stringified.length() > 15 ? NumberUtil.format((long) actualValue) : stringified;
                } catch(NumberFormatException ignored) {
                }
                if (isProperCompactNumber(input)) {
                    Double num = getActualValueFromCompactNumber(input);
                    if (num != null) {
                        double actualValue = (lbin - num) * lastAuctionedStack.stackSize;
                        if (actualValue < 0) return null;
                        String stringified = Long.toString((long)actualValue);
                        return stringified.length() > 15 ? NumberUtil.format((long) actualValue) : stringified;
                    }
                }
                return null;
            }
            return input;
        }
    }

}
