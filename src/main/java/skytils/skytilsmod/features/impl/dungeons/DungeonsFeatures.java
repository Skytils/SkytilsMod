package skytils.skytilsmod.features.impl.dungeons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.player.*;
import org.lwjgl.opengl.GL11;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.ScoreboardUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonsFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static String dungeonFloor = null;

    private static boolean isInTerracottaPhase = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) return;
        if (Utils.inDungeons) {
            if (dungeonFloor == null) {
                for (String s : ScoreboardUtil.getSidebarLines()) {
                    String line = ScoreboardUtil.cleanSB(s);
                    if (line.contains("The Catacombs (")) {
                        dungeonFloor = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
                        break;
                    }
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());

        if (Utils.inDungeons) {
            if (Skytils.config.autoCopyFailToClipboard) {
                Matcher deathFailMatcher = Pattern.compile("(?:^ ☠ .+ and became a ghost\\.$)|(?:^PUZZLE FAIL! .+$)|(?:^\\[STATUE\\] Oruo the Omniscient: .+ chose the wrong answer!)").matcher(unformatted);
                if (deathFailMatcher.find()) {
                    GuiScreen.setClipboardString(unformatted);
                    mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7aCopied death/fail to clipboard."));
                }
            }

            if (Skytils.config.hideF4Spam && unformatted.startsWith("[CROWD]"))
                event.setCanceled(true);

            if (unformatted.startsWith("[BOSS] Sadan") && unformatted.contains(":")) {
                if (unformatted.contains("So you made it all the way here...and you wish to defy me? Sadan?!"))
                    isInTerracottaPhase = true;
                if (unformatted.contains("ENOUGH!") || unformatted.contains("It was inevitable."))
                    isInTerracottaPhase = false;
            }
        }
    }
    
    // Show hidden fels
    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre event) {
        if (Utils.inDungeons) {
        	if (Skytils.config.showHiddenFels && event.entity instanceof EntityEnderman) {
                event.entity.setInvisible(false);
            }

            if (Skytils.config.showHiddenShadowAssassins && event.entity instanceof EntityPlayer) {
                if (event.entity.getName().contains("Shadow Assassin")) {
                    event.entity.setInvisible(false);
                }
            }
        }

        if (event.entity instanceof EntityArmorStand && event.entity.hasCustomName()) {
            if (Skytils.config.hideWitherMinerNametags) {
                String name = StringUtils.stripControlCodes(event.entity.getCustomNameTag());
                if (name.contains("Wither Miner") || name.contains("Wither Guard") || name.contains("Apostle")) {
                    mc.theWorld.removeEntity(event.entity);
                }
            }

            if (Skytils.config.hideF4Nametags) {
                String name = StringUtils.stripControlCodes(event.entity.getCustomNameTag());
                if (name.contains("Spirit") && !name.contains("Spirit Bear")) {
                    mc.theWorld.removeEntity(event.entity);
                }
            }
        }
    }

    // Spirit leap names
    @SubscribeEvent
    public void onGuiDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!Utils.inSkyblock) return;
        if (event.gui instanceof GuiChest) {
            GuiChest inventory = (GuiChest) event.gui;
            Container containerChest = inventory.inventorySlots;
            if (containerChest instanceof ContainerChest) {
                ScaledResolution sr = new ScaledResolution(mc);
                FontRenderer fr = mc.fontRendererObj;
                int guiLeft = (sr.getScaledWidth() - 176) / 2;
                int guiTop = (sr.getScaledHeight() - 222) / 2;

                List<Slot> invSlots = inventory.inventorySlots.inventorySlots;
                String displayName = ((ContainerChest) containerChest).getLowerChestInventory().getDisplayName().getUnformattedText().trim();
                int chestSize = inventory.inventorySlots.inventorySlots.size();

                if (Utils.inDungeons && ((Skytils.config.spiritLeapNames && displayName.equals("Spirit Leap")) || (Skytils.config.reviveStoneNames && displayName.equals("Revive A Teammate")))) {
                    int people = 0;
                    for (Slot slot : invSlots) {
                        if (slot.inventory == mc.thePlayer.inventory) continue;
                        if (slot.getHasStack()) {
                            ItemStack item = slot.getStack();
                            if (item.getItem() == Items.skull) {
                                people++;
                                String name = item.getDisplayName();

                                //slot is 16x16
                                int x = guiLeft + slot.xDisplayPosition + 8;
                                int y = guiTop + slot.yDisplayPosition;
                                // Move down when chest isn't 6 rows
                                if (chestSize != 90) y += (6 - (chestSize - 36) / 9) * 9;

                                if (people % 2 != 0) {
                                    y -= 15;
                                } else {
                                    y += 20;
                                }

                                Pattern player_pattern = Pattern.compile("(?:\\[.+?] )?(\\w+)");
                                Matcher matcher = player_pattern.matcher(StringUtils.stripControlCodes(name));
                                if (!matcher.find()) continue;
                                String text = fr.trimStringToWidth(name.substring(0, 2) + matcher.group(1), 32);
                                x -= fr.getStringWidth(text) / 2;

                                boolean shouldDrawBkg = true;
                                if (Skytils.usingNEU && !displayName.equals("Revive A Teammate")) {
                                    try {
                                        Class<?> neuClass = Class.forName("io.github.moulberry.notenoughupdates.NotEnoughUpdates");
                                        Field neuInstance = neuClass.getDeclaredField("INSTANCE");
                                        Object neu = neuInstance.get(null);
                                        Field neuConfig = neuClass.getDeclaredField("config");
                                        Object config = neuConfig.get(neu);
                                        Field improvedSBMenu = config.getClass().getDeclaredField("improvedSBMenu");
                                        Object improvedSBMenuS = improvedSBMenu.get(config);
                                        Field enableSbMenus = improvedSBMenuS.getClass().getDeclaredField("enableSbMenus");
                                        boolean customGuiEnabled = enableSbMenus.getBoolean(improvedSBMenuS);
                                        if (customGuiEnabled) shouldDrawBkg = false;
                                    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
                                    }
                                }

                                GL11.glPushMatrix();
                                GL11.glTranslated(0, 0, 10);
                                if (shouldDrawBkg) Gui.drawRect(x - 2, y - 2, x + fr.getStringWidth(text) + 2, y + fr.FONT_HEIGHT + 2, new Color(47, 40, 40).getRGB());
                                fr.drawStringWithShadow(text, x, y, new Color(255, 255,255).getRGB());
                                GL11.glTranslated(0, 0, -10);
                                GL11.glPopMatrix();

                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onDrawSlot(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if (!Utils.inSkyblock) return;
        Slot slot = event.slot;
        if (event.container instanceof ContainerChest) {
            ContainerChest cc = (ContainerChest) event.container;
            String displayName = cc.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (slot.getHasStack()) {
                ItemStack item = slot.getStack();
                if (Skytils.config.spiritLeapNames && displayName.equals("Spirit Leap")) {
                    if (item.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!Utils.inSkyblock) return;
        if (event.packet instanceof S45PacketTitle) {
            S45PacketTitle packet = (S45PacketTitle) event.packet;
            if (packet.getMessage() != null && mc.thePlayer != null) {
                String unformatted = StringUtils.stripControlCodes(packet.getMessage().getUnformattedText());
                if (Skytils.config.hideTerminalCompletionTitles && Utils.inDungeons && !unformatted.contains(mc.thePlayer.getName()) &&(unformatted.contains("activated a terminal!") || unformatted.contains("completed a device!") || unformatted.contains("activated a lever!"))) {
                    event.setCanceled(true);
                }
            }
        }
        if (event.packet instanceof S29PacketSoundEffect) {
            S29PacketSoundEffect packet = (S29PacketSoundEffect) event.packet;
            if (Skytils.config.disableTerracottaSounds && isInTerracottaPhase) {
                String sound = packet.getSoundName();
                float pitch = packet.getPitch();
                float volume = packet.getVolume();

                if (sound.equals("game.player.hurt") && pitch == 0 && volume == 0)
                    event.setCanceled(true);
                if (sound.equals("random.eat") && pitch == 0.6984127f && volume == 1)
                    event.setCanceled(true);
            }
        }
    }
    
    // Cancel Abilities
    /**
     * Taken from Danker's Skyblock Mod under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
    */
    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (!Utils.inSkyblock || Minecraft.getMinecraft().thePlayer != event.entityPlayer) return;
        ItemStack item = event.entityPlayer.getHeldItem();
        if (item == null) return;

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            if (Skytils.config.disableAotd && item.getDisplayName().contains("Aspect of the Dragons")) {
                event.setCanceled(true);
            }
            if (Skytils.config.disableLivid && item.getDisplayName().contains("Livid Dagger")) {
                event.setCanceled(true);
            }
            if (Skytils.config.disableFury && item.getDisplayName().contains("Shadow Fury")) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        dungeonFloor = null;
        isInTerracottaPhase = false;
    }

}