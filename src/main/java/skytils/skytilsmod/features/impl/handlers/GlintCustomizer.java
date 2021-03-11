package skytils.skytilsmod.features.impl.handlers;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import skytils.skytilsmod.Skytils;

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


    public GlintCustomizer() {
        saveFile = new File(Skytils.modDir, "customizedglints.json");
        reloadSave();
    }

    public static void reloadSave() {
        overrides.clear();
        JsonObject data;
        try (FileReader in = new FileReader(saveFile)) {
            data = gson.fromJson(in, JsonObject.class);
            for (Map.Entry<String, JsonElement> dataEntry : data.entrySet()) {
                JsonObject entry = dataEntry.getValue().getAsJsonObject();
                if (entry.has("override")) {
                    overrides.put(dataEntry.getKey(), entry.get("override").getAsBoolean());
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
            gson.toJson(obj, writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
