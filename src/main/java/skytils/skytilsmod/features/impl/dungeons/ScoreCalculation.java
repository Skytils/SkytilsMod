package skytils.skytilsmod.features.impl.dungeons;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityZombie;
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
import skytils.skytilsmod.events.SendChatMessageEvent;
import skytils.skytilsmod.utils.TabListUtils;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreCalculation {
    public static final Pattern partyAssistSecretsPattern = Pattern.compile("^Party > .+: \\$SKYTILS-DUNGEON-SCORE-ROOM\\$: \\[(?<name>.+)\\] \\((?<secrets>\\d+)\\)$");
    private static final Pattern secretsFoundPattern = Pattern.compile("§r Secrets Found: §r§b(?<secrets>\\d+)§r");
    private static final Pattern cryptsDestroyedPattern = Pattern.compile("§r Crypts: §r§6(?<crypts>\\d+)§r");

    public static HashMap<String, Integer> rooms = new HashMap<>();

    public static boolean mimicKilled = false;

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static int ticks = 0;

    @SubscribeEvent
    public void onAddChatMessage(AddChatMessageEvent event) {
        if (!Utils.inDungeons) return;
        try {
            String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
            if (unformatted.equals("null")) {
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
            ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/room json");
            ticks = 0;
        }

    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || mc.thePlayer == null) return;
        try {
            String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
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

                for (NetworkPlayerInfo pi : TabListUtils.getTabEntries()) {
                    String name = mc.ingameGUI.getTabList().getPlayerName(pi);
                    if (name.contains("Secrets Found:")) {
                        Matcher matcher = secretsFoundPattern.matcher(name);
                        if (matcher.find()) {
                            text.add("\u00a76Secrets Found:\u00a7a " + matcher.group("secrets"));
                            continue;
                        }
                    }
                    if (name.contains("Crypts:")) {
                        Matcher matcher = cryptsDestroyedPattern.matcher(name);
                        if (matcher.find()) {
                            text.add("\u00a76Crypts:\u00a7a " + matcher.group("crypts"));
                            continue;
                        }
                    }
                }

                text.add(1, "\u00a76Estimated Secret Count:\u00a7a " + (Integer) rooms.values().stream().mapToInt(Integer::intValue).sum());

                if (DungeonsFeatures.dungeonFloor.equals("F6") || DungeonsFeatures.dungeonFloor.equals("F7")) {
                    text.add("\u00a76Mimic Killed:" + (ScoreCalculation.mimicKilled ? "\u00a7a ✓" : " \u00a7c X"));
                }

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
            text.add("\u00a76Secrets Found: 99");
            text.add("\u00a76Estimated Secret Count: 99");
            text.add("\u00a76Crypts: 99");
            text.add("\u00a76Mimic Killed:\u00a7a ✓");

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
            return ScreenRenderer.fontRenderer.getStringWidth("\u00a76Estimated Secret Count: 99");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.showScoreCalculation;
        }
    }

}
