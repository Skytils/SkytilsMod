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

package skytils.skytilsmod.features.impl.dungeons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
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

    public static long dungeonStartTime = -1;
    public static long bloodOpenTime = -1;
    public static long bloodClearTime = -1;
    public static long bossEntryTime = -1;
    public static long bossClearTime = -1;

    public static long phase1ClearTime = -1;
    public static long phase2ClearTime = -1;
    public static long phase3ClearTime = -1;

    public static int witherDoors = 0;

    public static long scoreShownAt = -1;

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
            if (Skytils.config.dungeonTimer) mc.thePlayer.addChatMessage(new ChatComponentText("§bBlood took " + Math.round((bloodOpenTime - dungeonStartTime) / 1000f) + " seconds to open."));
            return;
        }

        if (message.equals("§r§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass.§r")) {
            bloodClearTime = System.currentTimeMillis();
            if (Skytils.config.dungeonTimer) mc.thePlayer.addChatMessage(new ChatComponentText("§bWatcher took " + Math.round((bloodClearTime - bloodOpenTime) / 1000f) + " seconds to clear."));
            return;
        }

        if ((message.startsWith("§r§c[BOSS] ") || message.startsWith("§r§4[BOSS] ")) && bloodClearTime != -1 && bossEntryTime == -1) {
            bossEntryTime = System.currentTimeMillis();
            return;
        }

        if (message.contains("§r§c☠ §r§eDefeated §r") && bossEntryTime != -1 && bossClearTime == -1) {
            bossClearTime = System.currentTimeMillis();
            if (Skytils.config.dungeonTimer) mc.thePlayer.addChatMessage(new ChatComponentText("§7Wither Doors: " + witherDoors + "\n" +
                    "§cBlood took " + Math.round((bloodOpenTime - dungeonStartTime) / 1000f) + " seconds to open." + "\n" +
                    "§bWatcher took " + Math.round((bloodClearTime - bloodOpenTime) / 1000f) + " seconds to clear." + "\n" +
                    "§9Boss entry was " + timeFormat((double) (bossEntryTime - dungeonStartTime) / 1000f) + "."));
            return;
        }

        if (message.startsWith("§r§4[BOSS] Necron") && Objects.equals(DungeonsFeatures.dungeonFloor, "F7")) {
            if (message.endsWith("§r§cFINE! LET'S MOVE TO SOMEWHERE ELSE!!§r") && phase1ClearTime == -1) {
                phase1ClearTime = System.currentTimeMillis();
                if (Skytils.config.necronPhaseTimer) mc.thePlayer.addChatMessage(new ChatComponentText("§bPhase 1 took " + Math.round((phase1ClearTime - bossEntryTime) / 1000f) + " seconds."));
                return;
            }
            if (message.endsWith("§r§cCRAP!! IT BROKE THE FLOOR!§r") && phase2ClearTime == -1) {
                phase2ClearTime = System.currentTimeMillis();
                if (Skytils.config.necronPhaseTimer) mc.thePlayer.addChatMessage(new ChatComponentText("§bPhase 2 took " + Math.round((phase2ClearTime - phase1ClearTime) / 1000f) + " seconds."));
                return;
            }
            if (message.endsWith("§r§cTHAT'S IT YOU HAVE DONE IT!§r") && phase3ClearTime == -1) {
                phase3ClearTime = System.currentTimeMillis();
                if (Skytils.config.necronPhaseTimer) mc.thePlayer.addChatMessage(new ChatComponentText("§bPhase 3 took " + Math.round((phase3ClearTime - phase2ClearTime) / 1000f) + " seconds."));
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

                String displayText = "§aReal Time: " + (dungeonStartTime == -1 ? "0s" : (double) ((bossClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : bossClearTime) - dungeonStartTime) / 1000f + "s") +
                        "\n§aTime Elapsed: " + (dungeonStartTime == -1 ? "0s" : timeFormat((double) ((bossClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : bossClearTime) - dungeonStartTime) / 1000f)) +
                        "\n§7Wither Doors: " + witherDoors +
                        "\n§4Blood Open: " + (bloodOpenTime == -1 ? "0s" : timeFormat((double) (bloodOpenTime - dungeonStartTime) / 1000f)) +
                        "\n§cWatcher Clear: " + (bloodClearTime == -1 ? "0s" : timeFormat((double) (bloodClearTime - bloodOpenTime) / 1000f)) +
                        "\n§9Boss Entry: " + (bossEntryTime == -1 ? "0s" : timeFormat((double) (bossEntryTime - dungeonStartTime) / 1000f)) +
                        "\n§bBoss Clear: " + (bossClearTime == -1 ? "0s" : timeFormat((double) (bossClearTime - bossEntryTime) / 1000f));
                String[] lines = displayText.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                    ScreenRenderer.fontRenderer.drawString(lines[i], leftAlign ? 0 : getWidth(), i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
                }
            }
        }

        @Override
        public void demoRender() {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

            String displayText = "§aReal Time: 0s" +
                    "\n§aTime Elapsed: 0s" +
                    "\n§7Wither Doors: 0" +
                    "\n§4Blood Open: 0s" +
                    "\n§cWatcher Clear: 0s" +
                    "\n§9Boss Entry: 0s" +
                    "\n§bBoss Clear: 0s";
            String[] lines = displayText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                ScreenRenderer.fontRenderer.drawString(lines[i], leftAlign ? 0 : getActualWidth(), i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
            }
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT * 7;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("§cWatcher Clear: 0s");
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

                String displayText = "§bPhase 1: " + timeFormat((double) ((phase1ClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : phase1ClearTime) - bossEntryTime) / 1000f) +
                        "\n§cPhase 2: " + (phase1ClearTime == -1 ? "0s" : timeFormat((double) ((phase2ClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : phase2ClearTime) - phase1ClearTime) / 1000f)) +
                        "\n§6Phase 3: " + (phase2ClearTime == -1 ? "0s" : timeFormat((double) ((phase3ClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : phase3ClearTime) - phase2ClearTime) / 1000f)) +
                        "\n§4Phase 4: " + (phase3ClearTime == -1 ? "0s" : timeFormat((double) ((bossClearTime == -1 ? (scoreShownAt == -1 ? System.currentTimeMillis() : scoreShownAt) : bossClearTime) - phase3ClearTime) / 1000f));

                String[] lines = displayText.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                    ScreenRenderer.fontRenderer.drawString(lines[i], leftAlign ? 0 : getWidth(), i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
                }
            }
        }

        @Override
        public void demoRender() {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

            String displayText = "§bPhase 1: 0s" +
                    "\n§cPhase 2: 0s" +
                    "\n§6Phase 3: 0s" +
                    "\n§4Phase 4: 0s";

            String[] lines = displayText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                ScreenRenderer.fontRenderer.drawString(lines[i], leftAlign ? 0 : getWidth(), i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
            }
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT * 4;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("§cPhase 1: 0s");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.necronPhaseTimer;
        }
    }

}
