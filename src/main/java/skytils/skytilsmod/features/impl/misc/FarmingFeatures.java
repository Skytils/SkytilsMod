package skytils.skytilsmod.features.impl.misc;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.DamageBlockEvent;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class FarmingFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private long lastNotifyBreakTime = 0;

    @SubscribeEvent
    public void onAttemptBreak(DamageBlockEvent event) {
        if (!Utils.inSkyblock || mc.thePlayer == null || mc.theWorld == null) return;
        EntityPlayerSP p = mc.thePlayer;
        ItemStack heldItem = p.getHeldItem();
        Block block = mc.theWorld.getBlockState(event.pos).getBlock();
        if (Skytils.config.preventBreakingFarms && heldItem != null) {

            ArrayList<Block> farmBlocks = new ArrayList<>(Arrays.asList(
                    Blocks.dirt,
                    Blocks.farmland,
                    Blocks.carpet,
                    Blocks.glowstone,
                    Blocks.sea_lantern,
                    Blocks.soul_sand,
                    Blocks.waterlily,
                    Blocks.standing_sign,
                    Blocks.wall_sign,
                    Blocks.wooden_slab,
                    Blocks.double_wooden_slab,
                    Blocks.oak_fence,
                    Blocks.dark_oak_fence,
                    Blocks.birch_fence,
                    Blocks.spruce_fence,
                    Blocks.acacia_fence,
                    Blocks.jungle_fence,
                    Blocks.oak_fence_gate,
                    Blocks.acacia_fence_gate,
                    Blocks.birch_fence_gate,
                    Blocks.jungle_fence_gate,
                    Blocks.spruce_fence_gate,
                    Blocks.dark_oak_fence_gate,
                    Blocks.glass,
                    Blocks.glass_pane,
                    Blocks.stained_glass,
                    Blocks.stained_glass_pane
            ));

            if ((heldItem.getItem() instanceof ItemHoe || heldItem.getItem() instanceof ItemAxe) && farmBlocks.contains(block)) {
                event.setCanceled(true);
                if (System.currentTimeMillis() - lastNotifyBreakTime > 10000) {
                    lastNotifyBreakTime = System.currentTimeMillis();
                    p.playSound("note.bass", 1, 0.5f);
                    ChatComponentText notif = new ChatComponentText(EnumChatFormatting.RED + "Skytils has prevented you from breaking that block!");
                    p.addChatMessage(notif);
                }
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!Utils.inSkyblock) return;
        if (event.packet instanceof S45PacketTitle) {
            S45PacketTitle packet = (S45PacketTitle) event.packet;
            if (packet.getMessage() != null) {
                String unformatted = StringUtils.stripControlCodes(packet.getMessage().getUnformattedText());
                if (Skytils.config.hideFarmingRNGTitles && unformatted.contains("DROP!")) {
                    event.setCanceled(true);
                }
            }
        }
    }

}
