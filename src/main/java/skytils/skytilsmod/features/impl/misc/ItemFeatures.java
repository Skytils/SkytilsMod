package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.ReceivePacketEvent;

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
}
