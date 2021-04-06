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

package skytils.skytilsmod.features.impl.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class KeyShortcuts {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static File saveFile;
    public static final HashMap<String, Integer> shortcuts = new HashMap<>();

    public KeyShortcuts() {
        saveFile = new File(Skytils.modDir, "keyshortcuts.json");
        reloadShortcuts();
    }

    public static void reloadShortcuts() {
        shortcuts.clear();
        try (FileReader in = new FileReader(saveFile)) {
            JsonObject data = gson.fromJson(in, JsonObject.class);
            for (Map.Entry<String, JsonElement> shortcut : data.entrySet()) {
                shortcuts.put(shortcut.getKey(), shortcut.getValue().getAsInt());
            }
        } catch (Exception e) {
            try (FileWriter writer = new FileWriter(saveFile)) {
                gson.toJson(new JsonObject(), writer);
            } catch (Exception ignored) {

            }
        }
    }

    public static void saveShortcuts() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            JsonObject obj = new JsonObject();
            for (Map.Entry<String, Integer> shortcut : shortcuts.entrySet()) {
                obj.addProperty(shortcut.getKey(), shortcut.getValue());
            }
            gson.toJson(obj, writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onInput(InputEvent event) {
        if (!Utils.inSkyblock) return;
        for (Map.Entry<String, Integer> shortcut : shortcuts.entrySet()) {
            String message = shortcut.getKey();
            int code = shortcut.getValue();
            if (code == 0) continue;
            boolean isDown = code > 0 ? event instanceof InputEvent.KeyInputEvent && Keyboard.getEventKeyState() && Keyboard.getEventKey() == code : event instanceof InputEvent.MouseInputEvent && Mouse.getEventButtonState() && Mouse.getEventButton() == code + 100;
            if (isDown) {
                if (message.startsWith("/") && ClientCommandHandler.instance.executeCommand(mc.thePlayer, message) != 0) break;

                Skytils.sendMessageQueue.add(message);
            }
        }
    }

}
