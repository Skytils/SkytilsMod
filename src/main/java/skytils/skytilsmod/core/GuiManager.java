package skytils.skytilsmod.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.gui.LocationEditGui;
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

    private static Map<Integer, GuiElement> elements = new HashMap<>();
    private int counter = 0;
    private static Map<String, GuiElement> names = new HashMap<>();
    public static GuiToast toastGui = new GuiToast(Minecraft.getMinecraft());

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
        return this.elements;
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
    }
}
