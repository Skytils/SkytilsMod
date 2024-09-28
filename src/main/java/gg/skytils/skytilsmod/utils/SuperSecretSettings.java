/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.utils;

import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;

public class SuperSecretSettings {
    public static final LinkedHashSet<String> settings = new LinkedHashSet<>();
    public static final File saveLoc = new File(Launch.minecraftHome, "config/skytils/supersecretsettings.txt");
    public static boolean dirty = false;

    // Secrets
    public static boolean azooPuzzoo = false;
    public static boolean bennettArthur = false;
    public static boolean breefingDog = false;
    public static boolean catGaming = false;
    public static boolean chamberOfSecrets = false;
    public static boolean cattiva = false;
    public static boolean jamCat = false;
    public static boolean noSychic = false;
    public static boolean palworld = false;
    public static boolean sheepifyRebellion = false;
    public static boolean smolSelf = false;
    public static boolean smolPeople = false;
    public static boolean tryItAndSee = false;
    public static boolean twilightGiant = false;

    static {
        if (!saveLoc.exists()) {
            try {
                saveLoc.getParentFile().mkdirs();
                saveLoc.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(SuperSecretSettings::save, "Skytils-SuperSecretSave"));
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
            List<String> lines = IOUtils.readLines(Files.newInputStream(saveLoc.toPath()), Charsets.UTF_8);
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
            IOUtils.writeLines(settings, "\n", Files.newOutputStream(saveLoc.toPath()), Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setSecrets() {
        azooPuzzoo = settings.contains("azoopuzzoo");
        bennettArthur = settings.contains("bennettarthur");
        breefingDog = settings.contains("breefingdog");
        catGaming = settings.contains("catgaming");
        chamberOfSecrets = settings.contains("chamberofsecrets");
        cattiva = settings.contains("cattiva");
        jamCat = settings.contains("jamcat");
        noSychic = settings.contains("nosychic");
        palworld = settings.contains("palworld");
        sheepifyRebellion = settings.contains("sheepifyRebellion");
        smolSelf = settings.contains("smolself");
        smolPeople = settings.contains("smolpeople");
        tryItAndSee = settings.contains("tryItAndSee");
        twilightGiant = settings.contains("twilightGiant");
    }
}
