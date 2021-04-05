package skytils.skytilsmod.features.impl.handlers;

import com.google.gson.*;
import net.minecraft.client.Minecraft;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.colors.ColorFactory;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;
import skytils.skytilsmod.utils.graphics.colors.CustomColor;

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

    public static final HashMap<String, CustomColor> armorColors = new HashMap<>();


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
                CustomColor color = Utils.customColorFromString(colors.getValue().getAsString());
                armorColors.put(colors.getKey(), color == null ? CommonColors.BLACK : color);
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
            for (Map.Entry<String, CustomColor> colors : armorColors.entrySet()) {
                obj.addProperty(colors.getKey(), colors.getValue().toString());
            }
            gson.toJson(obj, writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
