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

package gg.skytils.skytilsmod.tweaker;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;

public class DuplicateSkytilsChecker {
    public static void checkForDuplicates() throws IOException {

        Enumeration<URL> ggSkytilsUrls = DuplicateSkytilsChecker.class.getClassLoader().getResources("gg/skytils/skytilsmod/Skytils.class");
        Enumeration<URL> skytilsmodUrls = DuplicateSkytilsChecker.class.getClassLoader().getResources("skytils/skytilsmod/Skytils.class");

        if (ggSkytilsUrls.hasMoreElements() && skytilsmodUrls.hasMoreElements()) {
            HashSet<URL> urls = Sets.newHashSet(Collections.list(ggSkytilsUrls));
            urls.addAll(Collections.list(skytilsmodUrls));

            String message = "Duplicate Skytils classes found! Remove the duplicate jar files and try again.\n" + urls;
            throw new RuntimeException(message);
        }
    }
}
