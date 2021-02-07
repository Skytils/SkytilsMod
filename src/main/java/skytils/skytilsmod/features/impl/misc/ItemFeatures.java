package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.Utils;

public class ItemFeatures {

    private final static Minecraft mc = Minecraft.getMinecraft();

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
                    boolean flag = mc.theWorld.playerEntities.stream().anyMatch(p -> {
                        if (pos.distanceSq(p.getPosition()) <= 11 * 11) {
                            ItemStack item = p.getHeldItem();
                            if (item != null) {
                                if (item.getItem() == Items.iron_sword) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
                    if (flag) event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onTooltip(ItemTooltipEvent event) {
        ItemStack item = event.itemStack;

        if (Skytils.config.soulEaterLore) {
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
    public void onMouseInputPre(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!Utils.inSkyblock) return;
        if (Mouse.getEventButton() != 0 && Mouse.getEventButton() != 1 && Mouse.getEventButton() != 2) return;
        if (!Mouse.getEventButtonState()) return;

        if (event.gui instanceof GuiChest) {
            Container containerChest = ((GuiChest) event.gui).inventorySlots;
            if (containerChest instanceof ContainerChest) {
                GuiChest chest = (GuiChest) event.gui;

                IInventory inventory = ((ContainerChest) containerChest).getLowerChestInventory();
                Slot mouseSlot = Utils.getSlotUnderMouse(chest);
                if (mouseSlot == null) return;
                ItemStack item = mouseSlot.getStack();
                String inventoryName = inventory.getDisplayName().getUnformattedText();

                if (Skytils.config.onlyCollectEnchantedItems && inventoryName.contains("Minion") && item != null) {
                    if (!item.isItemEnchanted()) {
                        if (inventoryName.equals("Minion Chest")) {
                            boolean chestHasEnchantedItem = false;
                            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                                ItemStack stack = inventory.getStackInSlot(i);
                                if (stack == null) continue;
                                if (stack.isItemEnchanted()) {
                                    chestHasEnchantedItem = true;
                                    break;
                                }
                            }
                            if (chestHasEnchantedItem) {
                                event.setCanceled(true);
                                return;
                            }
                        } else {
                            ItemStack minionType = inventory.getStackInSlot(4);
                            if (minionType != null) {
                                if (StringUtils.stripControlCodes(minionType.getDisplayName()).contains("Minion")) {
                                    int index = mouseSlot.getSlotIndex();
                                    if (index >= 21 && index <= 43 && index % 9 >= 3 && index % 9 <= 7) {
                                        ItemStack firstUpgrade = inventory.getStackInSlot(37);
                                        ItemStack secondUpgrade = inventory.getStackInSlot(46);
                                        if (firstUpgrade != null) {
                                            if (StringUtils.stripControlCodes(firstUpgrade.getDisplayName()).contains("Super Compactor 3000")) {
                                                event.setCanceled(true);
                                                return;
                                            }
                                        }
                                        if (secondUpgrade != null) {
                                            if (StringUtils.stripControlCodes(secondUpgrade.getDisplayName()).contains("Super Compactor 3000")) {
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
            }
        }
    }
}
