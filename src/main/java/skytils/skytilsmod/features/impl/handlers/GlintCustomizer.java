package skytils.skytilsmod.features.impl.handlers;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import skytils.skytilsmod.Skytils;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class GlintCustomizer {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static File saveFile;

    public static final HashMap<String, Boolean> overrides = new HashMap<>();
    public static final HashMap<String, Color> glintColors = new HashMap<>();


    public GlintCustomizer() {
        saveFile = new File(Skytils.modDir, "customizedglints.json");
        reloadSave();
    }

    public static void reloadSave() {
        overrides.clear();
        glintColors.clear();
        JsonObject data;
        try (FileReader in = new FileReader(saveFile)) {
            data = gson.fromJson(in, JsonObject.class);
            for (Map.Entry<String, JsonElement> dataEntry : data.entrySet()) {
                JsonObject entry = dataEntry.getValue().getAsJsonObject();
                if (entry.has("override")) {
                    overrides.put(dataEntry.getKey(), entry.get("override").getAsBoolean());
                }
                if (entry.has("color")) {
                    glintColors.put(dataEntry.getKey(), Color.decode(entry.get("color").getAsString()));
                }
            }
        } catch (Exception e) {
            data = new JsonObject();
            try (FileWriter writer = new FileWriter(saveFile)) {
                gson.toJson(data, writer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void writeSave() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            JsonObject obj = new JsonObject();
            for (Map.Entry<String, Boolean> override : overrides.entrySet()) {
                JsonObject child = new JsonObject();
                child.add("override", new JsonPrimitive(override.getValue()));
                obj.add(override.getKey(), child);
            }
            for (Map.Entry<String, Color> color : glintColors.entrySet()) {
                if (obj.has(color.getKey())) {
                    obj.get(color.getKey()).getAsJsonObject().addProperty("color", String.format("#%06X", (0xFFFFFF & color.getValue().getRGB())));
                    continue;
                }
                JsonObject child = new JsonObject();
                child.add("color", new JsonPrimitive(String.format("#%06X", (0xFFFFFF & color.getValue().getRGB()))));
                obj.add(color.getKey(), child);
            }
            gson.toJson(obj, writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
