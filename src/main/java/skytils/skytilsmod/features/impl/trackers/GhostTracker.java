package skytils.skytilsmod.features.impl.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

public class GhostTracker {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static int sorrow = 0;
    public static int bagofcash = 0;
    public static int volta = 0;
    public static int plasma = 0;
    public static int ghostlyboots = 0;

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (Skytils.config.GhostTracker && Utils.inMist && message.startsWith("RARE DROP! ") && !message.contains(":")) {
            if (message.contains("Sorrow")) {
                sorrow++;
            }
            if (message.contains("Volta")) {
                volta++;
            }
            if (message.contains("Plasma")) {
                plasma++;
            }
            if (message.contains("Bag of Cash")) {
                bagofcash++;
            }
            if (message.contains("Ghostly Boots")) {
                ghostlyboots++;
            }
        }
    }

    static {
        new GhostTrackerElement();
    }

    public static class GhostTrackerElement extends GuiElement {

        public GhostTrackerElement() {
            super("Ghost Tracker", new FloatPair(200, 80));
            Skytils.GUIMANAGER.registerElement(this);
        }

        @Override
        public void render() {
            EntityPlayerSP player = mc.thePlayer;
            World world = mc.theWorld;

            if (this.getToggled() && player != null && world != null && Utils.inMist) {
                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

                boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

                GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                String displayText = "\u00a79\u00a7lGhost Tracker" +
                        "\n\u00a7dSorrows: " + sorrow +
                        "\n\u00a7dBags of Cash: " + bagofcash +
                        "\n\u00a75Ghostly Boots: " + ghostlyboots +
                        "\n\u00a73Voltas: " + volta +
                        "\n\u00a73Plasmas: " + plasma;
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

            String displayText = "\u00a79\u00a7lGhost Tracker" +
                    "\n\u00a7dSorrows: 0" +
                    "\n\u00a7dBags of Cash: 0" +
                    "\n\u00a75Ghostly Boots: 0" +
                    "\n\u00a73Voltas: 0" +
                    "\n\u00a73Plasmas: 0";
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
            return ScreenRenderer.fontRenderer.getStringWidth("\u00a7dBags of Cash: 0");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.GhostTracker;
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        if (Skytils.config.resetTrackers) {
            sorrow = 0;
            volta = 0;
            plasma = 0;
            bagofcash = 0;
            ghostlyboots = 0;
        }
    }
}
