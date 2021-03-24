package skytils.skytilsmod.features.impl.dungeons;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.events.AddChatMessageEvent;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.events.SendChatMessageEvent;
import skytils.skytilsmod.utils.*;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreCalculation {
    public static final Pattern partyAssistSecretsPattern = Pattern.compile("^Party > .+: \\$SKYTILS-DUNGEON-SCORE-ROOM\\$: \\[(?<name>.+)\\] \\((?<secrets>\\d+)\\)$");

    public static HashMap<String, Integer> rooms = new HashMap<>();

    public static boolean mimicKilled = false;

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static int ticks = 0;

    @SubscribeEvent
    public void onAddChatMessage(AddChatMessageEvent event) {
        if (!Utils.inDungeons) return;
        try {
            String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
            if (unformatted.equals("null") || unformatted.startsWith("Dungeon Rooms: Use this command in dungeons")) {
                event.setCanceled(true);
            }
            if (unformatted.startsWith("{") && unformatted.endsWith("}")) {
                JsonObject obj = new Gson().fromJson(unformatted, JsonObject.class);
                if (obj.has("name") && obj.has("category") && obj.has("secrets")) {
                    String name = obj.get("name").getAsString();
                    int secrets = obj.get("secrets").getAsInt();
                    if (!ScoreCalculation.rooms.containsKey(name)) {
                        ScoreCalculation.rooms.put(name, secrets);
                        if (Skytils.config.scoreCalculationAssist) {
                            Skytils.sendMessageQueue.add("/pc $SKYTILS-DUNGEON-SCORE-ROOM$: [" + name + "] (" + secrets + ")");
                        }
                    }
                    event.setCanceled(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (Utils.inDungeons && ticks % 30 == 0 && mc.thePlayer != null && mc.theWorld != null) {
            if (!DungeonsFeatures.hasBossSpawned && Skytils.usingDungeonRooms && (Skytils.config.showScoreCalculation || Skytils.config.scoreCalculationAssist)) {
                ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/room json");
            }
            ticks = 0;
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || mc.thePlayer == null) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        try {
            if (Skytils.config.scoreCalculationReceiveAssist) {
                if (unformatted.startsWith("Party > ")) {
                    if (unformatted.contains("$SKYTILS-DUNGEON-SCORE-MIMIC$")) {
                        mimicKilled = true;
                        event.setCanceled(true);
                        return;
                    }

                    if (unformatted.contains("$SKYTILS-DUNGEON-SCORE-ROOM$")) {
                        Matcher matcher = partyAssistSecretsPattern.matcher(unformatted);
                        if (matcher.find()) {
                            String name = matcher.group("name");
                            int secrets = Integer.parseInt(matcher.group("secrets"));
                            if (!rooms.containsKey(name)) {
                                rooms.put(name, secrets);
                            }
                            event.setCanceled(true);
                            return;
                        }
                    }
                }
            }
        } catch (NumberFormatException ignored) {

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Skytils.config.removePartyChatNotifFromScoreCalc) {
            if (unformatted.startsWith("Party > ") && mc.thePlayer != null && !unformatted.contains(mc.thePlayer.getName())) {
                mc.thePlayer.playSound("random.orb", 1, 1);
            }
        }
    }

    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event) {
        if (!Utils.inDungeons) return;
        if (event.entity instanceof EntityZombie) {
            EntityZombie entity = (EntityZombie) event.entity;
            if (entity.isChild() && entity.getCurrentArmor(0) == null && entity.getCurrentArmor(1) == null && entity.getCurrentArmor(2) == null && entity.getCurrentArmor(3) == null) {
                if (!mimicKilled) {
                    mimicKilled = true;
                    if (Skytils.config.scoreCalculationAssist) {
                        Skytils.sendMessageQueue.add("/pc $SKYTILS-DUNGEON-SCORE-MIMIC$");
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!Utils.inDungeons) return;
        if (event.packet instanceof S29PacketSoundEffect) {
            S29PacketSoundEffect packet = (S29PacketSoundEffect) event.packet;
            String sound = packet.getSoundName();
            float pitch = packet.getPitch();
            float volume = packet.getVolume();

            if (Skytils.config.removePartyChatNotifFromScoreCalc && sound.equals("random.orb") && pitch == 1f && volume == 1f) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onSendChat(SendChatMessageEvent event) {
        if (event.message.equals("/debugscorecalcrooms")) {
            mc.thePlayer.addChatMessage(new ChatComponentText(String.valueOf(rooms)));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        mimicKilled = false;
        rooms.clear();
    }

    static {
        new ScoreCalculationElement();
    }

    public static class ScoreCalculationElement extends GuiElement {

        private static final Pattern deathsTabPattern = Pattern.compile("§r§a§lDeaths: §r§f\\((?<deaths>\\d+)\\)§r");
        private static final Pattern missingPuzzlePattern = Pattern.compile("§r (?<puzzle>.+): §r§7\\[§r§6§l✦§r§7\\]§r");
        private static final Pattern failedPuzzlePattern = Pattern.compile("§r (?<puzzle>.+): §r§7\\[§r§c§l✖§r§7\\] §r§f\\((?:§r(?<player>.+))?§r§f\\)§r");
        private static final Pattern secretsFoundPattern = Pattern.compile("§r Secrets Found: §r§b(?<secrets>\\d+)§r");
        private static final Pattern cryptsPattern = Pattern.compile("§r Crypts: §r§6(?<crypts>\\d+)§r");

        private static final Pattern dungeonClearedPattern = Pattern.compile("Dungeon Cleared: (?<percentage>\\d+)%");
        private static final Pattern timeElapsedPattern = Pattern.compile("Time Elapsed: (?:(?<hrs>\\d+)h )?(?:(?<min>\\d+)m )?(?:(?<sec>\\d+)s)?");

        public ScoreCalculationElement() {
            super("Dungeon Score Estimate", new FloatPair(200, 100));
            Skytils.GUIMANAGER.registerElement(this);
        }

        @Override
        public void render() {
            EntityPlayerSP player = mc.thePlayer;
            World world = mc.theWorld;
            if (this.getToggled() && Utils.inDungeons && player != null && world != null) {
                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                float x = this.getActualX();
                float y = this.getActualY();
                boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

                ArrayList<String> text = new ArrayList<>();

                int deaths = 0;
                int missingPuzzles = 0;
                int failedPuzzles = 0;
                int foundSecrets = 0;
                int totalSecrets = rooms.values().stream().mapToInt(Integer::intValue).sum();
                int clearedPercentage = 0;
                double secondsElapsed = 0;
                int crypts = 0;
                boolean isPaul = Objects.equals(MayorInfo.currentMayor, "Paul") && MayorInfo.mayorPerks.contains("EZPZ");

                for (NetworkPlayerInfo pi : TabListUtils.getTabEntries()) {
                    try {
                        String name = mc.ingameGUI.getTabList().getPlayerName(pi);
                        if (name.contains("Deaths:")) {
                            Matcher matcher = deathsTabPattern.matcher(name);
                            if (matcher.find()) {
                                deaths = Integer.parseInt(matcher.group("deaths"));
                                continue;
                            }
                        }
                        if (name.contains("✦")) {
                            Matcher matcher = missingPuzzlePattern.matcher(name);
                            if (matcher.find()) {
                                missingPuzzles++;
                                continue;
                            }
                        }
                        if (name.contains("✖")) {
                            Matcher matcher = failedPuzzlePattern.matcher(name);
                            if (matcher.find()) {
                                failedPuzzles++;
                                continue;
                            }
                            continue;
                        }
                        if (name.contains("Secrets Found:")) {
                            Matcher matcher = secretsFoundPattern.matcher(name);
                            if (matcher.find()) {
                                foundSecrets = Integer.parseInt(matcher.group("secrets"));
                                continue;
                            }
                        }
                        if (name.contains("Crypts:")) {
                            Matcher matcher = cryptsPattern.matcher(name);
                            if (matcher.find()) {
                                crypts = Integer.parseInt(matcher.group("crypts"));
                                continue;
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }

                for (String l : ScoreboardUtil.getSidebarLines()) {
                    String line = ScoreboardUtil.cleanSB(l);
                    if (line.startsWith("Dungeon Cleared:")) {
                        Matcher matcher = dungeonClearedPattern.matcher(line);
                        if (matcher.find()) {
                            clearedPercentage = Integer.parseInt(matcher.group("percentage"));
                            continue;
                        }
                    }
                    if (line.startsWith("Time Elapsed:")) {
                        Matcher matcher = timeElapsedPattern.matcher(line);
                        if (matcher.find()) {
                            int hours;
                            int minutes;
                            int seconds;
                            try {
                                hours = Integer.parseInt(matcher.group("hrs"));
                            } catch (IllegalStateException | NumberFormatException e) {
                                hours = 0;
                            }
                            try {
                                minutes = Integer.parseInt(matcher.group("min"));
                            } catch (IllegalStateException | NumberFormatException e) {
                                minutes = 0;
                            }
                            try {
                                seconds = Integer.parseInt(matcher.group("sec"));
                            } catch (IllegalStateException | NumberFormatException e) {
                                seconds = 0;
                            }

                            secondsElapsed = (hours * 3600) + (minutes * 60) + seconds;
                            continue;
                        }
                    }
                }

                int skillScore = (100 - (2 * deaths) - (14 * (missingPuzzles + failedPuzzles)));
                double discoveryScore = (MathUtil.clamp(Math.floor(60 * (clearedPercentage/100f)), 0,60) + (totalSecrets <= 0 ? 0 : MathUtil.clamp(Math.floor((40f*foundSecrets)/totalSecrets), 0, 40)));
                double speedScore;
                int bonusScore = ((mimicKilled ? 2 : 0) + Math.min(crypts, 5) + (isPaul ? 10 : 0));

                double countedSeconds = Objects.equals(DungeonsFeatures.dungeonFloor, "F2") ? Math.max(0, secondsElapsed - 120) : secondsElapsed;
                if (countedSeconds <= 1320) {
                    speedScore = 100;
                } else if (1320 < countedSeconds && countedSeconds <= 1420) {
                    speedScore = 232 - (0.1 * countedSeconds);
                } else if (1420 < countedSeconds && countedSeconds <= 1820) {
                    speedScore = 161 - (0.05 * countedSeconds);
                } else if (1820 < countedSeconds && countedSeconds <= 3920) {
                    speedScore = (392/3f) - ((1/30f) * countedSeconds);
                } else speedScore = 0;

                text.add("§6Deaths:§a " + deaths);
                text.add("§6Missing Puzzles:§a " + missingPuzzles);
                text.add("§6Failed Puzzles:§a " + failedPuzzles);
                text.add("§6Secrets Found:§a " + foundSecrets);
                if (totalSecrets != 0) text.add("§6Estimated Secret Count:§a " + totalSecrets);
                text.add("§6Crypts:§a " + crypts);
                if (DungeonsFeatures.dungeonFloor.equals("F6") || DungeonsFeatures.dungeonFloor.equals("F7")) {
                    text.add("§6Mimic Killed:" + (ScoreCalculation.mimicKilled ? "§a ✓" : " §c X"));
                }
                if (isPaul) {
                    text.add("§6EZPZ: §a+10");
                }

                text.add("§6Skill Score:§a " + skillScore);
                if (totalSecrets != 0) text.add("§6Estimated Discovery Score:§a " + (int)discoveryScore);
                if (speedScore != 100) text.add("§6Speed Score:§a " + (int)speedScore);
                text.add("§6Estimated Bonus Score:§a " + bonusScore);
                if (totalSecrets != 0) text.add("§6Estimated Total Score:§a " + (int)(skillScore + discoveryScore + speedScore + bonusScore));
                if (!Skytils.usingDungeonRooms) text.add("§cDownload the Dungeon Rooms Mod for discovery estimate.");

                GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                for (int i = 0; i < text.size(); i++) {
                    SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                    ScreenRenderer.fontRenderer.drawString(text.get(i), leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY() + i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
                }
                GlStateManager.scale(1/this.getScale(), 1/this.getScale(), 1.0F);
            }
        }

        @Override
        public void demoRender() {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

            ArrayList<String> text = new ArrayList<>();
            text.add("§6Secrets Found: 99");
            text.add("§6Estimated Secret Count: 99");
            text.add("§6Crypts: 99");
            text.add("§6Mimic Killed:§a ✓");

            for (int i = 0; i < text.size(); i++) {
                SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                ScreenRenderer.fontRenderer.drawString(text.get(i), leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY() + i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
            }
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT * 4;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("§6Estimated Secret Count: 99");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.showScoreCalculation;
        }
    }

}
