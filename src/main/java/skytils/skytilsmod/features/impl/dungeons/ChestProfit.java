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

package skytils.skytilsmod.features.impl.dungeons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.features.impl.handlers.AuctionData;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.NumberUtil;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;
import skytils.skytilsmod.utils.graphics.colors.CustomColor;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Based off of chest profit from code by Quantizr
 * Licensed under GNU GPL v3, with permission given from author
 * @author Quantizr
 */
public class ChestProfit {

    private static final DungeonChestProfitElement element = new DungeonChestProfitElement();

    @SubscribeEvent
    public void onGUIDrawnEvent(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!Utils.inDungeons || DungeonTimer.scoreShownAt == -1) return;
        if (!Skytils.config.dungeonChestProfit) return;
        if (event.gui instanceof GuiChest) {
            ContainerChest chest = (ContainerChest) ((GuiChest) event.gui).inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            if (inv.getDisplayName().getUnformattedText().endsWith(" Chest")) {
                DungeonChest chestType = DungeonChest.getFromName(inv.getDisplayName().getUnformattedText());
                if (chestType != null) {
                    ItemStack openChest = inv.getStackInSlot(31);
                    if (openChest != null && openChest.getDisplayName().equals("§aOpen Reward Chest")) {

                        for (String unclean : ItemUtil.getItemLore(openChest)) {
                            String line = StringUtils.stripControlCodes(unclean);
                            if (line.contains("FREE")) {
                                chestType.price = 0;
                                break;
                            } else if (line.contains(" Coins")) {
                                chestType.price = Double.parseDouble(line.substring(0, line.indexOf(" ")).replaceAll(",", ""));
                                break;
                            }
                        }

                        chestType.value = 0;
                        chestType.items.clear();
                        for (int i = 11; i < 16; i++) {
                            ItemStack lootSlot = inv.getStackInSlot(i);
                            String identifier = AuctionData.getIdentifier(lootSlot);
                            if (identifier != null) {
                                Double value = AuctionData.lowestBINs.get(identifier);
                                if (value == null) value = 0D;
                                chestType.value += value;
                                chestType.items.add(new DungeonChestLootItem(lootSlot, identifier, value));
                            }
                        }
                    }
                    if (chestType.items.size() > 0) {
                        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

                        boolean leftAlign = element.getActualX() < sr.getScaledWidth() / 2f;
                        SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;

                        GlStateManager.color(1, 1, 1, 1);
                        GlStateManager.disableLighting();

                        int drawnLines = 1;
                        double profit = chestType.value - chestType.price;
                        ScreenRenderer.fontRenderer.drawString(chestType.displayText + "§f: §" + (profit > 0 ? "a" : "c") + NumberUtil.nf.format(profit), leftAlign ? element.getActualX() : element.getActualX() + element.getWidth(), element.getActualY(), chestType.displayColor, alignment, SmartFontRenderer.TextShadow.NORMAL);

                        for (DungeonChestLootItem item : chestType.items) {
                            String line = item.item.getDisplayName() + "§f: §a" + NumberUtil.nf.format(item.value);
                            ScreenRenderer.fontRenderer.drawString(line, leftAlign ? element.getActualX() : element.getActualX() + element.getWidth(), element.getActualY() + drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
                            drawnLines++;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        for (DungeonChest chest : DungeonChest.values()) {
            chest.reset();
        }
    }

    private enum DungeonChest {
        WOOD("Wood Chest", CommonColors.BROWN),
        GOLD("Gold Chest", CommonColors.YELLOW),
        DIAMOND("Diamond Chest", CommonColors.LIGHT_BLUE),
        EMERALD("Emerald Chest", CommonColors.LIGHT_GREEN),
        OBSIDIAN("Obsidian Chest", CommonColors.BLACK),
        BEDROCK("Bedrock Chest", CommonColors.LIGHT_GRAY);

        public String displayText;
        public CustomColor displayColor;
        public double price;
        public double value;
        public ArrayList<DungeonChestLootItem> items = new ArrayList<>();

        DungeonChest(String displayText, CustomColor color) {
            this.displayText = displayText;
            this.displayColor = color;
        }

        public void reset() {
            this.price = 0;
            this.value = 0;
            this.items.clear();
        }

        public static DungeonChest getFromName(String name) {
            for (DungeonChest chest : values()) {
                if (Objects.equals(chest.displayText, name)) {
                    return chest;
                }
            }
            return null;
        }

    }

    private static class DungeonChestLootItem {

        public ItemStack item;
        public String itemId;
        public double value;

        public DungeonChestLootItem(ItemStack item, String itemId, double value) {
            this.item = item;
            this.itemId = itemId;
            this.value = value;
        }
    }

    public static class DungeonChestProfitElement extends GuiElement {

        public DungeonChestProfitElement() {
            super("Dungeon Chest Profit", new FloatPair(200, 120));
            Skytils.GUIMANAGER.registerElement(this);
        }

        @Override
        public void render() {
            if (this.getToggled() && Utils.inDungeons) {

                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

                boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.disableLighting();

                int drawnLines = 0;
                for (DungeonChest chest : DungeonChest.values()) {
                    if (chest.items.size() == 0) continue;
                    double profit = chest.value - chest.price;
                    String line = chest.displayText + "§f: §" + (profit > 0 ? "a" : "c") + NumberUtil.format((long) profit);
                    SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                    ScreenRenderer.fontRenderer.drawString(line, leftAlign ? 0 : getWidth(), drawnLines * ScreenRenderer.fontRenderer.FONT_HEIGHT, chest.displayColor, alignment, SmartFontRenderer.TextShadow.NORMAL);
                    drawnLines++;
                }
            }
        }

        @Override
        public void demoRender() {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

            for (int i = 0; i < DungeonChest.values().length; i++) {
                DungeonChest chest = DungeonChest.values()[i];
                String line = chest.displayText + ": §a+300M";
                SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                ScreenRenderer.fontRenderer.drawString(line, leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY() + i * ScreenRenderer.fontRenderer.FONT_HEIGHT, chest.displayColor, alignment, SmartFontRenderer.TextShadow.NORMAL);
            }
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.dungeonChestProfit;
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT * DungeonChest.values().length;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("Obsidian Chest: 300M");
        }
    }

}
