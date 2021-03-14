package skytils.skytilsmod.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.gui.LocationEditGui;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.toasts.GuiToast;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiManager {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static File positionFile;
    public static Map<String, FloatPair> GUIPOSITIONS;

    private static final Map<Integer, GuiElement> elements = new HashMap<>();
    private int counter = 0;
    private static final Map<String, GuiElement> names = new HashMap<>();
    public static GuiToast toastGui = new GuiToast(Minecraft.getMinecraft());

    public static String title = null;
    public static String subtitle = null;
    public static int titleDisplayTicks = 0;
    public static int subtitleDisplayTicks = 0;

    public GuiManager() {
        positionFile  = new File(Skytils.modDir, "guipositions.json");
        GUIPOSITIONS = new HashMap<>();
        readConfig();
    }

    public boolean registerElement(GuiElement e) {
        try {
            counter++;
            elements.put(counter, e);
            names.put(e.getName(), e);
            return true;
        } catch(Exception err) {
            err.printStackTrace();
            return false;
        }
    }

    public GuiElement getByID(Integer ID) {
        return elements.get(ID);
    }

    public GuiElement getByName(String name) {
        return names.get(name);
    }

    public List<GuiElement> searchElements(String query) {
        List<GuiElement> results = new ArrayList<>();
        for(Map.Entry<String, GuiElement> e : names.entrySet()) {
            if(e.getKey().equals(query)) results.add(e.getValue());
        }
        return results;
    }

    public Map<Integer,GuiElement> getElements() {
        return elements;
    }

    public static void readConfig() {
        JsonObject file;
        try (FileReader in = new FileReader(positionFile)) {
            file = gson.fromJson(in, JsonObject.class);
            for (Map.Entry<String, JsonElement> e : file.entrySet()) {
                try {
                    GUIPOSITIONS.put(e.getKey(), new FloatPair(e.getValue().getAsJsonObject().get("x").getAsJsonObject().get("value").getAsFloat(), e.getValue().getAsJsonObject().get("y").getAsJsonObject().get("value").getAsFloat()));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } catch (Exception e) {
            GUIPOSITIONS = new HashMap<>();
            try (FileWriter writer = new FileWriter(positionFile)) {
                gson.toJson(GUIPOSITIONS, writer);
            } catch (Exception ignored) {

            }
        }
    }

    public static void saveConfig() {
        for (Map.Entry<String, GuiElement> e : names.entrySet()) {
            GUIPOSITIONS.put(e.getKey(), e.getValue().getPos());
        }
        try (FileWriter writer = new FileWriter(positionFile)) {
            gson.toJson(GUIPOSITIONS, writer);
        } catch (Exception ignored) {

        }
    }

    @SubscribeEvent
    public void renderPlayerInfo(final RenderGameOverlayEvent.Post event) {
        if (Skytils.usingLabymod && !(Minecraft.getMinecraft().ingameGUI instanceof GuiIngameForge)) return;
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE && event.type != RenderGameOverlayEvent.ElementType.JUMPBAR)
            return;
        if (Minecraft.getMinecraft().currentScreen instanceof LocationEditGui)
            return;
        for(Map.Entry<Integer, GuiElement> e : elements.entrySet()) {
            try {
                e.getValue().render();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        renderTitles(event.resolution);
    }

    // LabyMod Support
    @SubscribeEvent
    public void renderPlayerInfoLabyMod(final RenderGameOverlayEvent event) {
        if (!Skytils.usingLabymod) return;
        if (event.type != null) return;
        if (Minecraft.getMinecraft().currentScreen instanceof LocationEditGui)
            return;
        for(Map.Entry<Integer, GuiElement> e : elements.entrySet()) {
            try {
                e.getValue().render();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        renderTitles(event.resolution);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (titleDisplayTicks > 0) {
            titleDisplayTicks--;
        } else {
            titleDisplayTicks = 0;
            GuiManager.title = null;
        }

        if (subtitleDisplayTicks > 0) {
            subtitleDisplayTicks--;
        } else {
            subtitleDisplayTicks = 0;
            GuiManager.subtitle = null;
        }
    }

    public static void createTitle(String title, int ticks) {
        Utils.playLoudSound("random.orb", 0.5);
        GuiManager.title = title;
        GuiManager.titleDisplayTicks = ticks;
    }

    /**
     * Adapted from SkyblockAddons under MIT license
     * @link https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
     * @author BiscuitDevelopment
     */
    private void renderTitles(ScaledResolution scaledResolution) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || !Utils.inSkyblock) {
            return;
        }

        int scaledWidth = scaledResolution.getScaledWidth();
        int scaledHeight = scaledResolution.getScaledHeight();
        if (title != null) {
            int stringWidth = mc.fontRendererObj.getStringWidth(title);

            float scale = 4; // Scale is normally 4, but if its larger than the screen, scale it down...
            if (stringWidth * scale > (scaledWidth * 0.9F)) {
                scale = (scaledWidth * 0.9F) / (float) stringWidth;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale); // TODO Check if changing this scale breaks anything...

            mc.fontRendererObj.drawString(title, (float) (-mc.fontRendererObj.getStringWidth(title) / 2), -20.0F, 0xFF0000, true);

            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
        if (subtitle != null) {
            int stringWidth = mc.fontRendererObj.getStringWidth(subtitle);

            float scale = 2; // Scale is normally 2, but if its larger than the screen, scale it down...
            if (stringWidth * scale > (scaledWidth * 0.9F)) {
                scale = (scaledWidth * 0.9F) / (float) stringWidth;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);  // TODO Check if changing this scale breaks anything...

            mc.fontRendererObj.drawString(subtitle, -mc.fontRendererObj.getStringWidth(subtitle) / 2F, -23.0F,
                    0xFF0000, true);

            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

}
