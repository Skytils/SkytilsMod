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

import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.ModMetadata;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class Reference {
    public static String dataUrl = "https://data.skytils.gg/";
    public static final String MOD_ID = "skytils";
    public static final String MOD_NAME = "Skytils";
    @NotNull
    public static final String VERSION = getVersion();
    public static final String UNKNOWN_VERSION = "unknown";

    private static String getVersion() {
        URL url = Skytils.class.getResource("mcmod.info");
        if (url == null) return UNKNOWN_VERSION;
        try (InputStream input = url.openStream()) {
            MetadataCollection metadataCollection = MetadataCollection.from(input, "skytils");
            ModMetadata metadata = metadataCollection.getMetadataForId("skytils",
                    new HashMap<String, Object>() {{
                    put("name", "Skytils");
                    put("version", UNKNOWN_VERSION);
                }});
            return metadata.version;
        } catch (IOException e) {
            return UNKNOWN_VERSION;
        }
    }

    public static final int apiVersion = 5;
}
