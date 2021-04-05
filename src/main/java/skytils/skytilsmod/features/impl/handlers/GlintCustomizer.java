package skytils.skytilsmod.features.impl.handlers;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;
import skytils.skytilsmod.utils.graphics.colors.CustomColor;

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
    public static final HashMap<String, CustomColor> glintColors = new HashMap<>();


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
                    CustomColor color = Utils.customColorFromString(entry.get("color").getAsString());
                    glintColors.put(dataEntry.getKey(), color == null ? CommonColors.WHITE : color);
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
            for (Map.Entry<String, CustomColor> color : glintColors.entrySet()) {
                String stringValue = color.getValue().toString();
                if (obj.has(color.getKey())) {
                    obj.get(color.getKey()).getAsJsonObject().addProperty("color", stringValue);
                    continue;
                }
                JsonObject child = new JsonObject();
                child.add("color", new JsonPrimitive(stringValue));
                obj.add(color.getKey(), child);
            }
            gson.toJson(obj, writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
