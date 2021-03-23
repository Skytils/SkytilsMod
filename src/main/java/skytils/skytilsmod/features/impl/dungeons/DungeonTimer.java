package skytils.skytilsmod.features.impl.dungeons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.utils.MathUtil;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.util.Objects;

public class DungeonTimer {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static long dungeonStartTime = -1;
    private static long bloodOpenTime = -1;
    private static long bloodClearTime = -1;
    private static long bossEntryTime = -1;
    private static long bossClearTime = -1;

    private static long phase1ClearTime = -1;
    private static long phase2ClearTime = -1;
    private static long phase3ClearTime = -1;

    private static int witherDoors = 0;

    private static long scoreShownAt = -1;

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || event.type == 2) return;
        String message = event.message.getFormattedText();
        if (message.contains("§r§fTeam Score: §r") && scoreShownAt == -1) {
            scoreShownAt = System.currentTimeMillis();
            return;
        }

        if ((message.equals("§r§aDungeon starts in 1 second.§r") || message.equals("§r§aDungeon starts in 1 second. Get ready!§r")) && dungeonStartTime == -1) {
            dungeonStartTime = System.currentTimeMillis() + 1000;
            return;
        }

        if (message.endsWith("§r§ehas obtained §r§8Wither Key§r§e!§r") || message.equals("§r§eA §r§8Wither Key§r§e was picked up!§r")) {
            witherDoors++;
            return;
        }

        if (message.equals("§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r") || (message.startsWith("§r§c[BOSS] The Watcher§r§f") && bloodOpenTime == -1)) {
            bloodOpenTime = System.currentTimeMillis();
            return;
        }

        if (message.equals("§r§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass.§r")) {
            bloodClearTime = System.currentTimeMillis();
            return;
        }

        if ((message.startsWith("§r§c[BOSS] ") || message.startsWith("§r§4[BOSS] ")) && bloodClearTime != -1 && bossEntryTime == -1) {
            bossEntryTime = System.currentTimeMillis();
            return;
        }

        if (message.contains("§r§c☠ §r§eDefeated §r") && bossEntryTime != -1 && bossClearTime == -1) {
            bossClearTime = System.currentTimeMillis();
            return;
        }

        if (message.startsWith("§r§4[BOSS] Necron") && Objects.equals(DungeonsFeatures.dungeonFloor, "F7")) {
            if (message.endsWith("§r§cFINE! LET'S MOVE TO SOMEWHERE ELSE!!§r") && phase1ClearTime == -1) {
                phase1ClearTime = System.currentTimeMillis();
                return;
            }
            if (message.endsWith("§r§cCRAP!! IT BROKE THE FLOOR!§r") && phase2ClearTime == -1) {
                phase2ClearTime = System.currentTimeMillis();
                return;
            }
            if (message.endsWith("§r§cTHAT'S IT YOU HAVE DONE IT!§r") && phase3ClearTime == -1) {
                phase3ClearTime = System.currentTimeMillis();
                return;
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        dungeonStartTime = -1;
        bloodOpenTime = -1;
        bloodClearTime = -1;
        bossEntryTime = -1;
        bossClearTime = -1;

        phase1ClearTime = -1;
        phase2ClearTime = -1;
        phase3ClearTime = -1;

        witherDoors = 0;

        scoreShownAt = -1;
    }

    private static String timeFormat(double seconds) {
        if (seconds >= 60) {
            return MathUtil.fastFloor(seconds / 60) + "m " + Math.round(seconds % 60) + "s";
        } else {
            return Math.round(seconds) + "s";
        }
    }

    static {
        new DungeonTimerElement();
        new NecronPhaseTimerElement();
    }

    public static class DungeonTimerElement extends GuiElement {

        public DungeonTimerElement() {
            super("Dungeon Timer", new FloatPair(200, 80));
            Skytils.GUIMANAGER.registerElement(this);
        }

        @Override
        public void render() {
            EntityPlayerSP player = mc.thePlayer;
            World world = mc.theWorld;
            if (this.getToggled() && Utils.inDungeons && player != null && world != null) {

                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

                boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

                GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                String displayText = "\u00a7aReal Time: " + (dungeonStartTime == -1 ? "0s" : (double) ((bossClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : bossClearTime) - dungeonStartTime) / 1000f + "s") +
                        "\n\u00a7aTime Elapsed: " + (dungeonStartTime == -1 ? "0s" : timeFormat((double) ((bossClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : bossClearTime) - dungeonStartTime) / 1000f)) +
                        "\n\u00a77Wither Doors: " + witherDoors +
                        "\n\u00a74Blood Open: " + (bloodOpenTime == -1 ? "0s" : timeFormat((double) (bloodOpenTime - dungeonStartTime) / 1000f)) +
                        "\n\u00a7cWatcher Clear: " + (bloodClearTime == -1 ? "0s" : timeFormat((double) (bloodClearTime - bloodOpenTime) / 1000f)) +
                        "\n\u00a79Boss Entry: " + (bossEntryTime == -1 ? "0s" : timeFormat((double) (bossEntryTime - dungeonStartTime) / 1000f)) +
                        "\n\u00a7bBoss Clear: " + (bossClearTime == -1 ? "0s" : timeFormat((double) (bossClearTime - bossEntryTime) / 1000f));
                String[] lines = displayText.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                    ScreenRenderer.fontRenderer.drawString(lines[i], leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY() + i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
                }
                GlStateManager.scale(1/this.getScale(), 1/this.getScale(), 1.0F);
            }
        }

        @Override
        public void demoRender() {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

            String displayText = "\u00a7aReal Time: 0s" +
                    "\n\u00a7aTime Elapsed: 0s" +
                    "\n\u00a77Wither Doors: 0" +
                    "\n\u00a74Blood Open: 0s" +
                    "\n\u00a7cWatcher Clear: 0s" +
                    "\n\u00a79Boss Entry: 0s" +
                    "\n\u00a7bBoss Clear: 0s";
            String[] lines = displayText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                ScreenRenderer.fontRenderer.drawString(lines[i], leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY() + i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
            }
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT * 7;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("\u00a7cWatcher Clear: 0s");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.dungeonTimer;
        }
    }

    public static class NecronPhaseTimerElement extends GuiElement {

        public NecronPhaseTimerElement() {
            super("Necron Phase Timer", new FloatPair(200, 120));
            Skytils.GUIMANAGER.registerElement(this);
        }

        @Override
        public void render() {
            EntityPlayerSP player = mc.thePlayer;
            World world = mc.theWorld;
            if (this.getToggled() && Utils.inDungeons && player != null && world != null && bossEntryTime != -1 && Objects.equals(DungeonsFeatures.dungeonFloor, "F7")) {

                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

                boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

                GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                String displayText = "\u00a7bPhase 1: " + timeFormat((double) ((phase1ClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : phase1ClearTime) - bossEntryTime) / 1000f) +
                        "\n\u00a7cPhase 2: " + (phase1ClearTime == -1 ? "0s" : timeFormat((double) ((phase2ClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : phase2ClearTime) - phase1ClearTime) / 1000f)) +
                        "\n\u00a76Phase 3: " + (phase2ClearTime == -1 ? "0s" : timeFormat((double) ((phase3ClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : phase3ClearTime) - phase2ClearTime) / 1000f)) +
                        "\n\u00a74Phase 4: " + (phase3ClearTime == -1 ? "0s" : timeFormat((double) ((bossClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : bossClearTime) - phase3ClearTime) / 1000f));

                String[] lines = displayText.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                    ScreenRenderer.fontRenderer.drawString(lines[i], leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY() + i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
                }
                GlStateManager.scale(1/this.getScale(), 1/this.getScale(), 1.0F);
            }
        }

        @Override
        public void demoRender() {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

            String displayText = "\u00a7bPhase 1: 0s" +
                    "\n\u00a7cPhase 2: 0s" +
                    "\n\u00a76Phase 3: 0s" +
                    "\n\u00a74Phase 4: 0s";

            String[] lines = displayText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                ScreenRenderer.fontRenderer.drawString(lines[i], leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY() + i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
            }
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT * 4;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("\u00a7cPhase 1: 0s");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.necronPhaseTimer;
        }
    }

}
