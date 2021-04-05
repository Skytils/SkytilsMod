package skytils.skytilsmod.features.impl.dungeons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
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
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.events.BossBarEvent;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.events.SendChatMessageEvent;
import skytils.skytilsmod.utils.*;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonsFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Pattern playerPattern = Pattern.compile("(?:\\[.+?] )?(\\w+)");
    private static final Pattern deathOrPuzzleFail = Pattern.compile("(?:^ ☠ .+ and became a ghost\\.$)|(?:^PUZZLE FAIL! .+$)|(?:^\\[STATUE\\] Oruo the Omniscient: .+ chose the wrong answer!)");
    private static final String[] WATCHER_MOBS = {"Revoker", "Psycho", "Reaper", "Cannibal", "Mute", "Ooze", "Putrid", "Freak", "Leech", "Tear", "Parasite", "Flamer", "Skull", "Mr. Dead", "Vader", "Frost", "Walker", "Wandering Soul", "Bonzo", "Scarf", "Livid"};
    public static String dungeonFloor = null;
    public static boolean hasBossSpawned = false;
    private static boolean isInTerracottaPhase = false;
    private static double terracottaEndTime = -1;
    private static int rerollClicks = 0;
    private static boolean foundLivid = false;
    private static Entity livid = null;

    static {
        new LividGuiElement();
    }

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
            if (terracottaEndTime > 0 && Skytils.config.showSadanInterest) {
                double timeLeft = terracottaEndTime - (((double) System.currentTimeMillis()) / 1000f);
                if (timeLeft >= 0) {
                    BossStatus.healthScale = ((float) timeLeft) / 105;
                    BossStatus.statusBarTime = 100;
                    BossStatus.bossName = "§r§c§lSadan's Interest: §r§6" + ((int) timeLeft) + "s";
                    BossStatus.hasColorModifier = false;
                } else {
                    terracottaEndTime = -2;
                }
            }
            if (Skytils.config.findCorrectLivid && !foundLivid) {
                if (Utils.equalsOneOf(dungeonFloor, "F5", "M5")) {
                    List<Entity> loadedLivids = new ArrayList<>();
                    List<Entity> entities = mc.theWorld.getLoadedEntityList();
                    for (Entity entity : entities) {
                        String name = entity.getName();
                        if (name.contains("Livid") && name.length() > 5 && name.charAt(1) == name.charAt(5) && !loadedLivids.contains(entity)) {
                            loadedLivids.add(entity);
                        }
                    }
                    if (loadedLivids.size() > 8) {
                        livid = loadedLivids.get(0);
                        foundLivid = true;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onBossBarSet(BossBarEvent.Set event) {
        if (!Utils.inDungeons) return;
        IBossDisplayData displayData = event.displayData;
        String unformatted = StringUtils.stripControlCodes(event.displayData.getDisplayName().getUnformattedText());
        if (Objects.equals(dungeonFloor, "F7")) {
            if (unformatted.contains("Necron")) {
                switch (Skytils.config.necronHealth) {
                    case 2:
                        BossStatus.healthScale = displayData.getHealth() / displayData.getMaxHealth();
                        BossStatus.statusBarTime = 100;
                        BossStatus.bossName = displayData.getDisplayName().getFormattedText() + "§r§8 - §r§d" + String.format("%.1f", BossStatus.healthScale * 100) + "%";
                        BossStatus.hasColorModifier = event.hasColorModifier;
                        event.setCanceled(true);
                        break;
                    case 1:
                        BossStatus.healthScale = displayData.getHealth() / displayData.getMaxHealth();
                        BossStatus.statusBarTime = 100;
                        BossStatus.bossName = displayData.getDisplayName().getFormattedText() + "§r§8 - §r§a" + NumberUtil.format((long) (BossStatus.healthScale * 1_000_000_000)) + "§r§8/§r§a1B§r§c❤";
                        BossStatus.hasColorModifier = event.hasColorModifier;
                        event.setCanceled(true);
                        break;
                    case 0:
                }
            }
            return;
        }
        if (Objects.equals(dungeonFloor, "F6")) {
            if (terracottaEndTime == -1) {
                if (unformatted.contains("Sadan's Interest Level")) {
                    terracottaEndTime = (((double) System.currentTimeMillis()) / 1000f) + 105;
                }
            } else if (terracottaEndTime > 0 && Skytils.config.showSadanInterest) {
                event.setCanceled(true);
            }
            return;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());

        if (Utils.inDungeons) {
            if (Skytils.config.autoCopyFailToClipboard) {
                Matcher deathFailMatcher = deathOrPuzzleFail.matcher(unformatted);
                if (deathFailMatcher.find()) {
                    if (!unformatted.contains("disconnect")) {
                        GuiScreen.setClipboardString(unformatted);
                        mc.thePlayer.addChatMessage(new ChatComponentText("§aCopied death/fail to clipboard."));
                    }
                    event.message.getChatStyle()
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§aClick to copy to clipboard.")))
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skytilscopyfail " + unformatted));
                }
            }

            if (Skytils.config.hideF4Spam && unformatted.startsWith("[CROWD]"))
                event.setCanceled(true);

            if (unformatted.startsWith("[BOSS]") && unformatted.contains(":")) {
                if (!unformatted.startsWith("[BOSS] The Watcher")) {
                    hasBossSpawned = true;
                }
                if (unformatted.contains("Sadan")) {
                    if (unformatted.contains("So you made it all the way here"))
                        isInTerracottaPhase = true;
                    if (unformatted.contains("ENOUGH!") || unformatted.contains("It was inevitable."))
                        isInTerracottaPhase = false;
                }
            }
        }
    }

    @SubscribeEvent
    public void onSendChatMessage(SendChatMessageEvent event) {
        if (event.message.startsWith("/skytilscopyfail") && !event.addToChat) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§aCopied selected death/fail to clipboard."));
            GuiScreen.setClipboardString(event.message.substring("/skytilscopyfail ".length()));
            event.setCanceled(true);
        }
    }

    // Show hidden fels
    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre event) {
        if (Utils.inDungeons) {
            if (event.entity.isInvisible()) {
                if (Skytils.config.showHiddenFels && event.entity instanceof EntityEnderman) {
                    event.entity.setInvisible(false);
                }

                if (Skytils.config.showHiddenShadowAssassins && event.entity instanceof EntityPlayer && event.entity.getName().contains("Shadow Assassin")) {
                    event.entity.setInvisible(false);
                }

                if (Skytils.config.showStealthyBloodMobs && event.entity instanceof EntityPlayer) {
                    for (String name : WATCHER_MOBS) {
                        if (event.entity.getName().trim().equals(name)) {
                            event.entity.setInvisible(false);
                            break;
                        }
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
                if (Skytils.config.hideTerracotaNametags) {
                    String name = StringUtils.stripControlCodes(event.entity.getCustomNameTag());
                    if (name.contains("Terracotta "))
                        mc.theWorld.removeEntity(event.entity);
                }
                if (Skytils.config.hideNonStarredNametags) {
                    String name = StringUtils.stripControlCodes(event.entity.getCustomNameTag());
                    if (!name.startsWith("✯ ") && name.contains("❤"))
                        if (name.contains("Lurker") || name.contains("Dreadlord") || name.contains("Souleater") || name.contains("Zombie") || name.contains("Skeleton") || name.contains("Skeletor") || name.contains("Sniper") || name.contains("Super Archer") || name.contains("Spider") || name.contains("Fels") || name.contains("Withermancer"))
                            mc.theWorld.removeEntity(event.entity);
                }
            }
            if (event.entity instanceof EntityBat && Skytils.config.showBatHitboxes && !hasBossSpawned && event.entity.getMaxHealth() == 100 && !mc.getRenderManager().isDebugBoundingBox() && !event.entity.isInvisible()) {
                RenderUtil.drawOutlinedBoundingBox(event.entity.getEntityBoundingBox(), new Color(0, 255, 255, 255), 3, 1f);
            }
            if (event.entity instanceof EntitySkeleton && Skytils.config.boxSkeletonMasters && !mc.getRenderManager().isDebugBoundingBox() && !event.entity.isInvisible() && Objects.equals(ItemUtil.getSkyBlockItemID(event.entity.getCurrentArmor(0)), "SKELETON_MASTER_BOOTS")) {
                RenderUtil.drawOutlinedBoundingBox(event.entity.getEntityBoundingBox(), new Color(255, 107, 11, 255), 3, 1f);
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

                                Matcher matcher = playerPattern.matcher(StringUtils.stripControlCodes(item.getDisplayName()));
                                if (!matcher.find()) continue;
                                String name = matcher.group(1);
                                if (name.equals("Unknown")) continue;
                                String dungeonClass = "";
                                for (String l : ScoreboardUtil.getSidebarLines()) {
                                    String line = ScoreboardUtil.cleanSB(l);
                                    if (line.contains(name)) {
                                        dungeonClass = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                                        break;
                                    }
                                }
                                String text = fr.trimStringToWidth(item.getDisplayName().substring(0, 2) + name, 32);
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

                                double scale = 0.9f;
                                double scaleReset = 1 / scale;
                                GlStateManager.disableLighting();
                                GlStateManager.disableDepth();
                                GlStateManager.disableBlend();
                                GlStateManager.translate(0, 0, 1);
                                if (shouldDrawBkg)
                                    Gui.drawRect(x - 2, y - 2, x + fr.getStringWidth(text) + 2, y + fr.FONT_HEIGHT + 2, new Color(47, 40, 40).getRGB());
                                fr.drawStringWithShadow(text, x, y, new Color(255, 255, 255).getRGB());
                                GlStateManager.scale(scale, scale, scale);
                                fr.drawString(dungeonClass, (float) (scaleReset * (x + 7)), (float) (scaleReset * (guiTop + slot.yDisplayPosition + 18)), new Color(255, 255, 0).getRGB(), true);
                                GlStateManager.scale(scaleReset, scaleReset, scaleReset);
                                GlStateManager.translate(0, 0, -1);
                                GlStateManager.enableLighting();
                                GlStateManager.enableDepth();

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
                if (Skytils.config.hideTerminalCompletionTitles && Utils.inDungeons && !unformatted.contains(mc.thePlayer.getName()) && (unformatted.contains("activated a terminal!") || unformatted.contains("completed a device!") || unformatted.contains("activated a lever!"))) {
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

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        rerollClicks = 0;
    }

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (!Utils.inDungeons) return;
        if (event.container instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) event.container;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText();
            if (chestName.endsWith(" Chest")) {
                if (Skytils.config.kismetRerollConfirm > 0 && event.slotId == 50) {
                    rerollClicks++;
                    int neededClicks = Skytils.config.kismetRerollConfirm - rerollClicks;
                    if (neededClicks > 0) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent event) {
        if (event.itemStack != null) {
            if (Utils.inDungeons && Skytils.config.kismetRerollConfirm > 0 && ItemUtil.getDisplayName(event.itemStack).contains("Reroll") && SBInfo.getInstance().lastOpenContainerName.endsWith(" Chest")) {
                for (int i = 0; i < event.toolTip.size(); i++) {
                    if (event.toolTip.get(i).contains("Click to reroll")) {
                        int neededClicks = Skytils.config.kismetRerollConfirm - rerollClicks;
                        event.toolTip.set(i, "§eClick §a" + neededClicks + "§e times to reroll this chest!");
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        dungeonFloor = null;
        hasBossSpawned = false;
        isInTerracottaPhase = false;
        terracottaEndTime = -1;
        livid = null;
        foundLivid = false;
    }

    static class LividGuiElement extends GuiElement {

        public LividGuiElement() {
            super("Livid HP", new FloatPair(0.05f, 0.4f));
            Skytils.GUIMANAGER.registerElement(this);
        }

        public void render() {
            EntityPlayerSP player = mc.thePlayer;
            World world = mc.theWorld;
            if (this.getToggled() && Utils.inDungeons && player != null && world != null) {
                if (livid==null) return;

                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

                boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

                GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                ScreenRenderer.fontRenderer.drawString(livid.getName().replace("§l", ""), leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY(), CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);

                GlStateManager.scale(1/this.getScale(), 1/this.getScale(), 1.0F);
            }
        }

        public void demoRender() {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

            String text = "§r§f﴾ Livid §e6.9M§c❤ §f﴿";
            SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
            ScreenRenderer.fontRenderer.drawString(text, leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY(), CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("§r§f﴾ Livid §e6.9M§c❤ §f﴿");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.findCorrectLivid;
        }
    }
}