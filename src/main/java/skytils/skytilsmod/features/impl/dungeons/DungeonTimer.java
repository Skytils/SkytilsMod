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
import skytils.skytilsmod.utils.NumberUtil;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

public class DungeonTimer {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static long dungeonStartTime = -1;
    private static long bloodOpenTime = -1;
    private static long bloodClearTime = -1;
    private static long bossEntryTime = -1;
    private static long bossClearTime = -1;

    private static int witherDoors = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || event.type == 2) return;
        String message = event.message.getFormattedText();
        System.out.println(message);
        if (message.equals("§r§aDungeon starts in 1 second.§r") && dungeonStartTime == -1) {
            dungeonStartTime = System.currentTimeMillis() + 1000;
            return;
        }

        if (message.endsWith("§r§ehas obtained §r§8Wither Key§r§e!§r") || message.equals("§r§eA §r§8Wither Key§r§e was picked up!§r")) {
            witherDoors++;
            return;
        }

        if (message.equals("§r§cThe §r§c§lBLOOD DOOR§r§c has been opened!§r")) {
            bloodOpenTime = System.currentTimeMillis();
            return;
        }

        if (message.equals("§r§c[BOSS] The Watcher§r§f: You have proven yourself. You may pass.§r")) {
            bloodClearTime = System.currentTimeMillis();
            return;
        }

        if (message.startsWith("§r§c[BOSS] ") && bloodClearTime != -1 && bossEntryTime == -1) {
            bossEntryTime = System.currentTimeMillis();
            return;
        }

        if (message.startsWith("§r                       §r§c☠ §r§eDefeated §r") && bossEntryTime != -1 && bossClearTime == -1) {
            bossClearTime = System.currentTimeMillis();
            return;
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        dungeonStartTime = -1;
        bloodOpenTime = -1;
        bloodClearTime = -1;
        bossEntryTime = -1;
        bossClearTime = -1;

        witherDoors = 0;
    }

    static {
        new DungeonTimerElement();
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
                String displayText = "\u00a7aTime: " + (dungeonStartTime == -1 ? "Not Started" : (double) ((bossClearTime == -1 ? System.currentTimeMillis() : bossClearTime) - dungeonStartTime) / 1000f) +
                        "\n\u00a77Wither Doors: " + witherDoors +
                        "\n\u00a74Blood Open: " + (bloodOpenTime == -1 ? "Not Opened" : NumberUtil.round((double) (bloodOpenTime - dungeonStartTime) / 1000f, 1)) + "s" +
                        "\n\u00a7cWatcher Clear: " + (bloodClearTime == -1 ? "Not Cleared" : NumberUtil.round((double) (bloodClearTime - bloodOpenTime) / 1000f, 1)) + "s" +
                        "\n\u00a79Boss Clear: " + (bossClearTime == -1 ? "Not Cleared" : NumberUtil.round((double) (bossClearTime - bossEntryTime) / 1000f, 1)) + "s";
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

            String displayText = "\u00a7aTime: Not Started" +
                    "\n\u00a77Wither Doors: 0" +
                    "\n\u00a74Blood Open: Not Opened" +
                    "\n\u00a7cWatcher Clear: Not Cleared" +
                    "\n\u00a79Boss Clear: Not Cleared";
            String[] lines = displayText.split("\n");
            for (int i = 0; i < lines.length; i++) {
                SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                ScreenRenderer.fontRenderer.drawString(lines[i], leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY() + i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
            }
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT * 5;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("\u00a7cWatcher Clear: Not Cleared");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.dungeonTimer;
        }
    }

}
