package skytils.skytilsmod.features.impl.misc;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiRenderItemEvent;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class ItemFeatures {

    private final static Minecraft mc = Minecraft.getMinecraft();

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
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.entityPlayer != mc.thePlayer) return;
        ItemStack item = event.entityPlayer.getHeldItem();
        if (item == null) return;

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            if (Skytils.config.blockUselessZombieSword && item.getDisplayName().contains("Zombie Sword") && mc.thePlayer.getHealth() >= mc.thePlayer.getMaxHealth()) {
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
                    boolean flag = mc.theWorld.playerEntities.stream().anyMatch(p -> {
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

        NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(item);

        if (extraAttributes != null) {
            String stackTip = "";
            if (Skytils.config.showPotionTier && extraAttributes.hasKey("potion_level")) {
                stackTip = String.valueOf(extraAttributes.getInteger("potion_level"));
            } else if (Skytils.config.showEnchantedBookTier && item.getItem() == Items.enchanted_book && extraAttributes.hasKey("enchantments")) {
                NBTTagCompound enchantments = extraAttributes.getCompoundTag("enchantments");
                Set<String> enchantmentNames = enchantments.getKeySet();
                if (enchantments.getKeySet().size() == 1) {
                    stackTip = String.valueOf(enchantments.getInteger(enchantmentNames.iterator().next()));
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
    }

}
