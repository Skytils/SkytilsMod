/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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

package sharttils.sharttilsmod.utils;

import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

public class SuperSecretSettings {
    public static final LinkedHashSet<String> settings = new LinkedHashSet<>();
    public static final File saveLoc = new File(Launch.minecraftHome, "config/sharttils/supersecretsettings.txt");
    public static boolean dirty = false;

    // Secrets
    public static boolean azooPuzzoo = false;
    public static boolean breefingDog = false;
    public static boolean noSychic = false;
    public static boolean smolPeople = false;

    static {
        if (!saveLoc.exists()) {
            try {
                saveLoc.getParentFile().mkdirs();
                saveLoc.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(SuperSecretSettings::save, "Sharttils-SuperSecretSave"));
    }

    public static void add(String setting) {
        settings.add(setting);
        dirty = true;
        setSecrets();
    }

    public static void remove(String setting) {
        settings.remove(setting);
        dirty = true;
        setSecrets();
    }

    public static void clear() {
        settings.clear();
        dirty = true;
        setSecrets();
    }

    public static void load() {
        settings.clear();
        try {
            List<String> lines = IOUtils.readLines(new FileInputStream(saveLoc), Charsets.UTF_8);
            for (String line : lines) {
                if (!line.isEmpty()) {
                    settings.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSecrets();
    }

    public static void save() {
        if (!dirty) return;
        dirty = false;
        try {
            IOUtils.writeLines(settings, "\n", new FileOutputStream(saveLoc), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setSecrets() {
        azooPuzzoo = settings.contains("azoopuzzoo");
        breefingDog = settings.contains("breefingdog");
        noSychic = settings.contains("nosychic");
        smolPeople = settings.contains("smolpeople");
    }
}
