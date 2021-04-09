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

package skytils.skytilsmod.features.impl.dungeons.solvers;

import net.minecraft.block.BlockButtonStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.DataFetcher;
import skytils.skytilsmod.utils.Utils;

import java.util.HashMap;

/**
 * Original code was taken from Danker's Skyblock Mod under GPL 3.0 license and modified by the Skytils team
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
public class TriviaSolver {
    public static HashMap<String, String[]> triviaSolutions = new HashMap<>();
    public static String[] triviaAnswers = null;
    public static String triviaAnswer = null;

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (Skytils.config.triviaSolver && Utils.inDungeons) {
        if (unformatted.startsWith("[STATUE] Oruo the Omniscient: ") && unformatted.contains("answered Question #") && unformatted.endsWith("correctly!")) triviaAnswer = null;
            if (unformatted.equals("[STATUE] Oruo the Omniscient: I am Oruo the Omniscient. I have lived many lives. I have learned all there is to know.") && triviaSolutions.size() == 0) {
                mc.thePlayer.addChatMessage(new ChatComponentText("§cSkytils failed to load solutions for Trivia."));
                DataFetcher.reloadData();
            }
            if (unformatted.contains("What SkyBlock year is it?")) {
                double currentTime = System.currentTimeMillis() / 1000d;

                double diff = Math.floor(currentTime - 1560276000);

                int year = (int) (diff / 446400 + 1);
                triviaAnswers = new String[]{"Year " + year};
            } else {
                for (String question : triviaSolutions.keySet()) {
                    if (unformatted.contains(question)) {
                        triviaAnswers = triviaSolutions.get(question);
                        break;
                    }
                }
            }
            // Set wrong answers to red and remove click events
            if (triviaAnswers != null && (unformatted.contains("ⓐ") || unformatted.contains("ⓑ") || unformatted.contains("ⓒ"))) {
                String answer = null;
                boolean isSolution = false;
                for (String solution : triviaAnswers) {
                    if (unformatted.contains(solution)) {
                        isSolution = true;
                        answer = solution;
                        break;
                    }
                }
                if (!isSolution) {
                    char letter = unformatted.charAt(5);
                    String option = unformatted.substring(6);
                    event.message = new ChatComponentText("     " + EnumChatFormatting.GOLD + letter + EnumChatFormatting.RED + option);
                    return;
                } else {
                    triviaAnswer = answer;
                }
            }
        }
    }

    //@SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!Utils.inDungeons || !Skytils.config.triviaSolver || (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) return;
        IBlockState block = event.world.getBlockState(event.pos);

        if (block.getBlock() == Blocks.stone_button) {
            if (triviaAnswer != null) {

                EntityArmorStand answerLabel = null;
                for (Entity e : mc.theWorld.loadedEntityList) {
                    if (!(e instanceof EntityArmorStand)) continue;
                    if (!e.hasCustomName()) continue;
                    EntityArmorStand entity = (EntityArmorStand) e;
                    String name = entity.getCustomNameTag();
                    if (name.contains(triviaAnswer) && (name.contains("ⓐ") || name.contains("ⓑ") || name.contains("ⓒ"))) {
                        answerLabel = entity;
                        break;
                    }
                }

                if (answerLabel != null) {
                    System.out.println("Found Answer Marker " + answerLabel.getCustomNameTag() + " at " + answerLabel.posX + ", " + answerLabel.posY + ", " + answerLabel.posZ);
                    BlockPos buttonBlock = new BlockPos(answerLabel.posX, 70, answerLabel.posZ);
                    BlockPos blockBehind = new BlockPos(event.pos.offset(block.getValue(BlockButtonStone.FACING).getOpposite()));
                    if (mc.theWorld.getBlockState(buttonBlock).getBlock() == Blocks.double_stone_slab && mc.theWorld.getBlockState(blockBehind).getBlock() == Blocks.double_stone_slab && !buttonBlock.equals(blockBehind)) {
                        boolean isRight = false;
                        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
                            if (buttonBlock.offset(dir).equals(event.pos)) {
                                isRight = true;
                                break;
                            }
                        }
                        if (!isRight) {
                            System.out.println("Wrong button clicked, position: " + event.pos.getX() + ", " + event.pos.getY() + ", " + event.pos.getZ());
                            if (!(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
                                event.setCanceled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderArmorStandPre(RenderLivingEvent.Pre<EntityArmorStand> event) {
        if (Skytils.config.triviaSolver && triviaAnswer != null) {
            if (event.entity instanceof EntityArmorStand && event.entity.hasCustomName()) {
                String name = event.entity.getCustomNameTag();
                if (name.contains("ⓐ") || name.contains("ⓑ") || name.contains("ⓒ")) {
                    if (!name.contains(triviaAnswer)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        triviaAnswer = null;
    }

}
