package skytils.skytilsmod.features.impl.misc;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import skytils.skytilsmod.Skytils;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class ArmorColor {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static File colorFile;

    public static final HashMap<String, Color> armorColors = new HashMap<>();


    public ArmorColor() {
        colorFile = new File(Skytils.modDir, "armorcolors.json");
        reloadColors();
    }

    public static void reloadColors() {
        armorColors.clear();
        JsonObject dataObject;
        try (FileReader in = new FileReader(colorFile)) {
            dataObject = gson.fromJson(in, JsonObject.class);
            for (Map.Entry<String, JsonElement> colors : dataObject.entrySet()) {
                armorColors.put(colors.getKey(), Color.decode(colors.getValue().getAsString()));
            }
        } catch (Exception e) {
            dataObject = new JsonObject();
            try (FileWriter writer = new FileWriter(colorFile)) {
                gson.toJson(dataObject, writer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void saveColors() {
        try (FileWriter writer = new FileWriter(colorFile)) {
            JsonObject obj = new JsonObject();
            for (Map.Entry<String, Color> colors : armorColors.entrySet()) {
                obj.addProperty(colors.getKey(), String.format("#%06X", (0xFFFFFF & colors.getValue().getRGB())));
            }
            gson.toJson(obj, writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
