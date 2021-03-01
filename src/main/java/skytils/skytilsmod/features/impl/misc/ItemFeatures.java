package skytils.skytilsmod.features.impl.misc;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.events.GuiRenderItemEvent;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemFeatures {

    private final static Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.BackgroundDrawnEvent event) {
        if (event.gui instanceof GuiChest) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            if (chestName.startsWith("Salvage")) {
                if (Skytils.config.highlightSalvageableItems) {
                    for (Slot slot : mc.thePlayer.inventoryContainer.inventorySlots) {
                        ItemStack stack = slot.getStack();
                        if (stack == null) continue;
                        NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(stack);
                        if (extraAttr == null || !extraAttr.hasKey("baseStatBoostPercentage") || extraAttr.hasKey("dungeon_item_level")) continue;
                        RenderUtil.drawOnSlot(mc.thePlayer.inventory.getSizeInventory(), slot.xDisplayPosition, slot.yDisplayPosition + 1, new Color(15, 233, 233, 225).getRGB());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (!Utils.inSkyblock) return;
        if (event.container instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) event.container;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            if (event.slot != null && event.slot.getHasStack()) {
                ItemStack item = event.slot.getStack();
                if (item == null) return;
                NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(item);
                if (Skytils.config.protectStarredItems) {
                    if (chestName.startsWith("Salvage") && extraAttr != null) {
                        boolean inSalvageGui = false;
                        if (item.getDisplayName().contains("Salvage") || item.getDisplayName().contains("Essence")) {
                            ItemStack salvageItem = inv.getStackInSlot(13);
                            if (salvageItem == null) return;
                            item = salvageItem;
                            inSalvageGui = true;
                        }
                        if ((item.getDisplayName().contains("✪") || extraAttr.hasKey("dungeon_item_level")) && (event.slot.inventory == mc.thePlayer.inventory || inSalvageGui)) {
                            mc.thePlayer.playSound("note.bass", 1, 0.5f);
                            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Skytils has stopped you from salvaging that item!"));
                            event.setCanceled(true);
                            return;
                        }
                    }
                    if (!chestName.equals("Large Chest") && !chestName.contains("Auction") && inv.getSizeInventory() == 54 && extraAttr != null) {
                        ItemStack sellItem = inv.getStackInSlot(49);
                        if (sellItem != null) {
                            if ((sellItem.getItem() == Item.getItemFromBlock(Blocks.hopper) && sellItem.getDisplayName().contains("Sell Item")) || ItemUtil.getItemLore(sellItem).stream().anyMatch(s -> s.contains("buyback"))) {
                                if ((item.getDisplayName().contains("✪") || extraAttr.hasKey("dungeon_item_level")) && (event.slot.inventory == mc.thePlayer.inventory && event.slotId != 49)) {
                                    mc.thePlayer.playSound("note.bass", 1, 0.5f);
                                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Skytils has stopped you from selling that item!"));
                                    event.setCanceled(true);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack item = event.itemStack;

        if (Skytils.config.showSoulEaterBonus) {
            if (item.hasTagCompound()) {
                NBTTagCompound tags = item.getSubCompound("ExtraAttributes", false);
                if (tags != null) {
                    if (tags.hasKey("ultimateSoulEaterData")) {

                        int bonus = tags.getInteger("ultimateSoulEaterData");

                        boolean foundStrength = false;

                        for (int i = 0; i < event.toolTip.size(); i++) {
                            String line = event.toolTip.get(i);
                            if (line.contains("§7Strength:")) {
                                event.toolTip.add(i + 1, EnumChatFormatting.DARK_RED + " Soul Eater Bonus: " + EnumChatFormatting.GREEN + bonus);
                                foundStrength = true;
                                break;
                            }
                        }

                        if (!foundStrength) {
                            int index = event.showAdvancedItemTooltips ? 4 : 2;
                            event.toolTip.add(event.toolTip.size() - index, "");
                            event.toolTip.add(event.toolTip.size() - index, EnumChatFormatting.DARK_RED + " Soul Eater Bonus: " + EnumChatFormatting.GREEN + bonus);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.entityPlayer != mc.thePlayer) return;
        ItemStack item = event.entityPlayer.getHeldItem();
        if (item == null) return;

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            if (Skytils.config.blockUselessZombieSword && item.getDisplayName().contains("Zombie Sword") && mc.thePlayer.getHealth() >= mc.thePlayer.getMaxHealth()) {
                event.setCanceled(true);
            }
            if (Skytils.config.blockGiantsSlam && item.getDisplayName().contains("Giant's Sword")) {
                event.setCanceled(true);
            }
        } else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            ArrayList<Block> interactables = new ArrayList<>(Arrays.asList(
                    Blocks.acacia_door,
                    Blocks.anvil,
                    Blocks.beacon,
                    Blocks.bed,
                    Blocks.birch_door,
                    Blocks.brewing_stand,
                    Blocks.command_block,
                    Blocks.crafting_table,
                    Blocks.chest,
                    Blocks.dark_oak_door,
                    Blocks.daylight_detector,
                    Blocks.daylight_detector_inverted,
                    Blocks.dispenser,
                    Blocks.dropper,
                    Blocks.enchanting_table,
                    Blocks.ender_chest,
                    Blocks.furnace,
                    Blocks.hopper,
                    Blocks.jungle_door,
                    Blocks.lever,
                    Blocks.noteblock,
                    Blocks.powered_comparator,
                    Blocks.unpowered_comparator,
                    Blocks.powered_repeater,
                    Blocks.unpowered_repeater,
                    Blocks.standing_sign,
                    Blocks.wall_sign,
                    Blocks.trapdoor,
                    Blocks.trapped_chest,
                    Blocks.wooden_button,
                    Blocks.stone_button,
                    Blocks.oak_door,
                    Blocks.skull
            ));

            Block block = event.world.getBlockState(event.pos).getBlock();
            if (Utils.inDungeons) {
                interactables.add(Blocks.coal_block);
                interactables.add(Blocks.stained_hardened_clay);
            }
            if (!interactables.contains(block)) {
                if (Skytils.config.blockUselessZombieSword && item.getDisplayName().contains("Zombie Sword") && mc.thePlayer.getHealth() >= mc.thePlayer.getMaxHealth()) {
                    event.setCanceled(true);
                }
                if (Skytils.config.blockGiantsSlam && item.getDisplayName().contains("Giant's Sword")) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof S2APacketParticles) {
            S2APacketParticles packet = (S2APacketParticles) event.packet;

            EnumParticleTypes type = packet.getParticleType();

            boolean longDistance = packet.isLongDistance();
            int count = packet.getParticleCount();
            float speed = packet.getParticleSpeed();
            float xOffset = packet.getXOffset();
            float yOffset = packet.getYOffset();
            float zOffset = packet.getZOffset();

            double x = packet.getXCoordinate();
            double y = packet.getYCoordinate();
            double z = packet.getZCoordinate();

            BlockPos pos = new BlockPos(x, y, z);

            if (type == EnumParticleTypes.EXPLOSION_LARGE && Skytils.config.hideImplosionParticles) {
                if (longDistance && count == 8 && speed == 8 && xOffset == 0 && yOffset == 0 && zOffset == 0) {
                    boolean flag = ImmutableList.copyOf(mc.theWorld.playerEntities).stream().anyMatch(p -> {
                        if (pos.distanceSq(p.getPosition()) <= 11 * 11) {
                            ItemStack item = p.getHeldItem();
                            if (item != null) {
                                return item.getItem() == Items.iron_sword;
                            }
                        }
                        return false;
                    });
                    if (flag) event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderItemOverlayPost(GuiRenderItemEvent.RenderOverlayEvent.Post event) {
        ItemStack item = event.stack;
        if (!Utils.inSkyblock || item == null || item.stackSize != 1) return;

        String stackTip = "";

        NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(item);

        if (extraAttributes != null) {
            if (Skytils.config.showPotionTier && extraAttributes.hasKey("potion_level")) {
                stackTip = String.valueOf(extraAttributes.getInteger("potion_level"));
            } else if (Skytils.config.showEnchantedBookTier && item.getItem() == Items.enchanted_book && extraAttributes.hasKey("enchantments")) {
                NBTTagCompound enchantments = extraAttributes.getCompoundTag("enchantments");
                Set<String> enchantmentNames = enchantments.getKeySet();
                if (enchantments.getKeySet().size() == 1) {
                    stackTip = String.valueOf(enchantments.getInteger(enchantmentNames.iterator().next()));
                }
            }
        }

        List<String> lore = ItemUtil.getItemLore(item);

        if(!lore.isEmpty()) {
            if (Skytils.config.showPetCandies && item.getItem() == Items.skull) {
                Pattern candyPattern = Pattern.compile("§a\\((\\d+)/10\\) Pet Candy Used");
                for(String line : lore) {
                    Matcher candyLineMatcher = candyPattern.matcher(line);
                    if(candyLineMatcher.matches()) {
                        stackTip = String.valueOf(candyLineMatcher.group(1));
                        break;
                    }
                }
            }
        }


        if (stackTip.length() > 0) {
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            event.fr.drawStringWithShadow(stackTip, (float)(event.x + 17 - event.fr.getStringWidth(stackTip)), (float)(event.y + 9), 16777215);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    static {
        new SoulStrengthGuiElement();
    }

    public static class SoulStrengthGuiElement extends GuiElement {

        public SoulStrengthGuiElement() {
            super("Soul Eater Strength", new FloatPair(200, 10));
            Skytils.GUIMANAGER.registerElement(this);
        }

        @Override
        public void render() {
            EntityPlayerSP player = mc.thePlayer;
            if (this.getToggled() && Utils.inSkyblock && player != null) {
                ItemStack item = mc.thePlayer.getHeldItem();

                if (item != null) {
                    NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(item);
                    if (extraAttr != null) {
                        if (extraAttr.hasKey("ultimateSoulEaterData")) {

                            int bonus = extraAttr.getInteger("ultimateSoulEaterData");

                            float x = this.getActualX();
                            float y = this.getActualY();

                            GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                            mc.fontRendererObj.drawString("\u00a7cSoul Strength: \u00a7a" + bonus, x, y, 0xFFFFFF, true);
                            GlStateManager.scale(1/this.getScale(), 1/this.getScale(), 1.0F);
                        }
                    }
                }
            }
        }

        @Override
        public void demoRender() {
            ScreenRenderer.fontRenderer.drawString("\u00a7cSoul Strength: \u00a7a1000", this.getActualX(), this.getActualY(), CommonColors.WHITE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NORMAL);
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("\u00a7cSoul Strength: \u00a7a1000");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.showSoulEaterBonus;
        }
    }

}
