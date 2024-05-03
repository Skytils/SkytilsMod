/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

public class Reference {
    public static String dataUrl = "https://data.skytils.gg/";
    public static final String MOD_ID = "skytils";
    public static final String MOD_NAME = "Skytils";
    @NotNull
    public static final String VERSION = getVersion();
    public static final String UNKNOWN_VERSION = "unknown";

    private static String getVersion() {
        try {
            Enumeration<URL> urls = Skytils.class.getClassLoader().getResources("mcmod.info");
            Gson gson = new Gson();
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (InputStream is = url.openStream()) {
                    JsonArray jsonArray = gson.fromJson(new InputStreamReader(is), JsonArray.class);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JsonObject json = jsonArray.get(i).getAsJsonObject();
                        if (json.get("modid").getAsString().equals("skytils")) {
                            String version = json.get("version").getAsString();
                            return version.isEmpty() ? UNKNOWN_VERSION : version;
                        }
                    }
                }
            }
            return UNKNOWN_VERSION;
        } catch (IOException ioe) {
            LogManager.getLogger(Reference.class).fatal("Failed while getting Skytils version", ioe);
            return UNKNOWN_VERSION;
        }
    }

    public static final int apiVersion = 5;
}
