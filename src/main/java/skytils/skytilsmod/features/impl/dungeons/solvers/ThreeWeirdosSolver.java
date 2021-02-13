package skytils.skytilsmod.features.impl.dungeons.solvers;

import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.DataFetcher;
import skytils.skytilsmod.utils.Utils;

import java.util.ArrayList;

/**
 * Original code was taken from Danker's Skyblock Mod under GPL 3.0 license and modified by the Skytils team
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
public class ThreeWeirdosSolver {

    public static ArrayList<String> solutions = new ArrayList<>();

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static String riddleNPC = null;
    public static BlockPos riddleChest = null;

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Skytils.config.threeWeirdosSolver || !Utils.inDungeons) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());

        if (unformatted.contains("PUZZLE SOLVED!")) {
            if (unformatted.contains("wasn't fooled by ")) {
                riddleNPC = null;
                riddleChest = null;
                Utils.setNEUDungeonBlockOverlay(true);
            }
        }

        if (unformatted.contains("[NPC]")) {
            if (solutions.size() == 0) {
                mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7cSkytils failed to load solutions for Three Weirdos."));
                DataFetcher.reloadData();
            }
            for (String solution : Lists.newArrayList(solutions)) {
                if (unformatted.contains(solution)) {
                    String npcName = unformatted.substring(unformatted.indexOf("]") + 2, unformatted.indexOf(":"));
                    riddleNPC = npcName;
                    mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN.toString() + EnumChatFormatting.BOLD + StringUtils.stripControlCodes(npcName) + EnumChatFormatting.DARK_GREEN + " has the blessing."));
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!Utils.inDungeons || !Skytils.config.threeWeirdosSolver || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;
        IBlockState block = event.world.getBlockState(event.pos);

        if (block.getBlock() == Blocks.chest) {
            if (riddleNPC == null) {
                EntityArmorStand clickLabel = event.world.getEntities(EntityArmorStand.class, (entity -> {
                    if (entity == null) return false;
                    if (!entity.hasCustomName()) return false;
                    return entity.getCustomNameTag().contains("CLICK");
                })).stream().findFirst().orElse(null);

                if (clickLabel != null) {
                    if (clickLabel.getDistanceSq(event.pos) <= 5) {
                        System.out.println("Chest was too close to NPC; Chest Pos: " + event.pos.getX() + ", " + event.pos.getY() + ", " + event.pos.getZ() + " NPC Pos: " + clickLabel.posX + ", " + clickLabel.posY + ", " + clickLabel.posZ);
                        event.setCanceled(true);
                    }
                }
            } else {
                if (riddleChest == null) {
                    EntityArmorStand riddleLabel = event.world.getEntities(EntityArmorStand.class, (entity -> {
                        if (entity == null) return false;
                        if (!entity.hasCustomName()) return false;
                        return entity.getCustomNameTag().contains(riddleNPC);
                    })).stream().findFirst().orElse(null);

                    if (riddleLabel != null) {
                        System.out.println("Found Riddle NPC " + riddleLabel.getCustomNameTag() + " at " + riddleLabel.posX + ", " + riddleLabel.posY + ", " + riddleLabel.posY);
                        BlockPos actualPos = new BlockPos(Math.floor(riddleLabel.posX), 69, Math.floor(riddleLabel.posZ));
                        if (actualPos.distanceSq(event.pos) > 1) {
                            System.out.println("Wrong chest clicked, position: " + event.pos.getX() + ", " + event.pos.getY() + ", " + event.pos.getZ());
                            if (!(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
                                event.setCanceled(true);
                            }
                        }
                    }
                } else {
                    if (!riddleChest.equals(event.pos)) {
                        System.out.println("Wrong chest clicked, position: " + event.pos.getX() + ", " + event.pos.getY() + ", " + event.pos.getZ());
                        if (!(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
                            event.setCanceled(true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!Skytils.config.threeWeirdosSolver || !Utils.inDungeons || riddleNPC == null) return;
        if (riddleChest == null) {
            EntityArmorStand riddleLabel = mc.theWorld.getEntities(EntityArmorStand.class, (entity -> {
                if (entity == null) return false;
                if (!entity.hasCustomName()) return false;
                return entity.getCustomNameTag().contains(riddleNPC);
            })).stream().findFirst().orElse(null);
            if (riddleLabel != null) {
                System.out.println("Chest Finder: Found Riddle NPC " + riddleLabel.getCustomNameTag() + " at " + riddleLabel.getPosition());
                BlockPos npcPos = riddleLabel.getPosition();
                for (EnumFacing direction : EnumFacing.HORIZONTALS) {
                    BlockPos potentialPos = npcPos.offset(direction);
                    if (mc.theWorld.getBlockState(potentialPos).getBlock() == Blocks.chest) {
                        riddleChest = potentialPos;
                        System.out.print("Correct position is at: " + potentialPos);
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        riddleNPC = null;
        riddleChest = null;
        Utils.setNEUDungeonBlockOverlay(true);
    }

}
