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

package skytils.skytilsmod.features.impl.misc;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.DamageBlockEvent;
import skytils.skytilsmod.events.PacketEvent;
import skytils.skytilsmod.utils.StringUtils;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public void onReceivePacket(PacketEvent.ReceiveEvent event) {
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

    static double trapperStart = -1;
    static boolean animalFound = false;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock || !Skytils.config.trapperPing) return;
        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (message.contains("[NPC] Trevor The Trapper: You can find your")) {
            trapperStart = System.currentTimeMillis();
            animalFound = false;
        } else if (message.contains("Return to the Trapper soon to get a new animal to hunt!")) {
            if ((System.currentTimeMillis() - trapperStart) > 60000) { //1 minute cooldown
                Utils.playLoudSound("note.pling", 1);
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE + "Trapper cooldown has already expired!"));
                trapperStart = -1;
            }
            animalFound = true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!Utils.inSkyblock || !Skytils.config.trapperPing) return;
        if (trapperStart > 0) {
            if ((System.currentTimeMillis() - trapperStart) > 60000 && animalFound) { //1 minute cooldown
                trapperStart = -1;
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE + "Trapper cooldown has now expired!"));
                new Thread(() -> {
                    try {
                        for (int i = 0; i < 5; i++) {
                            Utils.playLoudSound("note.pling", 1);
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        trapperStart = -1;
    }


    /**
     * Modified version from Danker's Skyblock Mod, taken under GPL 3.0 license.
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */

    static String acceptTrapperCommand = "";
    static boolean commandSent = false;

    @SubscribeEvent
    public void checkChat(ClientChatReceivedEvent event) {
        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());

        if (!Utils.inSkyblock) return;

        if (message.contains("Click an option:")) {
            List<IChatComponent> listOfSiblings = event.message.getSiblings();
            for (IChatComponent sibling : listOfSiblings) {
                if (sibling.getUnformattedText().contains("[YES]")) {
                    acceptTrapperCommand = sibling.getChatStyle().getChatClickEvent().getValue();
                    commandSent = false;
                }
            }
            if (Skytils.config.acceptTrapperTask) mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.LIGHT_PURPLE + "Open chat then click anywhere on screen to accept task"));
        }
    }

    @SubscribeEvent
    public void onMouseInputPost(GuiScreenEvent.MouseInputEvent.Post event) {
        if (!Utils.inSkyblock) return;
        if (Mouse.getEventButton() == 0 && event.gui instanceof GuiChat) {
            if (Skytils.config.acceptTrapperTask && !commandSent) {
                Minecraft.getMinecraft().thePlayer.sendChatMessage(acceptTrapperCommand);
                commandSent = true;
            }
        }
    }
}
