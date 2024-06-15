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

import com.aayushatharva.brotli4j.Brotli4jLoader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.Security;

import static gg.skytils.skytilsmod.tweaker.TweakerUtil.addToClasspath;

public class DependencyLoader {

    private static final String MAVEN_CENTRAL_ROOT = "https://repo1.maven.org/maven2/";
    public static boolean hasNativeBrotli = false;

    public static void loadDependencies() {
        loadBrotli();
        if (Security.getProvider("BC") == null) loadBCProv();
    }

    public static File loadDependency(String path) throws Throwable {
        File downloadLocation = new File("./libraries/" + path);
        Path downloadPath = downloadLocation.toPath();

        downloadLocation.getParentFile().mkdirs();
        if (!downloadLocation.exists() || Files.size(downloadPath) == 0) {
            try (InputStream in = new URL(MAVEN_CENTRAL_ROOT + path).openStream()) {
                Files.copy(in, downloadPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        System.out.printf("Dependency size for %s: %s%n", path.substring(path.lastIndexOf('/') + 1), Files.size(downloadPath));

        addToClasspath(downloadLocation.toURI().toURL());

        return downloadLocation;
    }

    public static void loadBCProv() {
        try {
            loadDependency("org/bouncycastle/bcprov-jdk18on/1.78.1/bcprov-jdk18on-1.78.1.jar");
            System.out.println("Bouncy Castle provider loaded");
        } catch (Throwable t) {
            System.out.println("Failed to load Bouncy Castle providers");
            t.printStackTrace();
        }
    }

    public static void loadBrotli() {
        if (System.getProperty("skytils.noNativeBrotli") != null) {
            System.out.println("Native Brotli disabled by system property");
            hasNativeBrotli = false;
            return;
        }

        try {
            String brotli4jPlatform = getBrotli4jPlatform();
            loadDependency(String.format("com/aayushatharva/brotli4j/native-%s/1.16.0/native-%s-1.16.0.jar", brotli4jPlatform, brotli4jPlatform));
            Brotli4jLoader.ensureAvailability();
            hasNativeBrotli = true;
            System.out.println("Native Brotli loaded");
        } catch (Throwable t) {
            System.out.println("Failed to load native Brotli");
            t.printStackTrace();
            hasNativeBrotli = false;
        }
    }

    public static String getBrotli4jPlatform() {
        String osName = System.getProperty("os.name");
        String archName = System.getProperty("os.arch");

        if ("Linux".equalsIgnoreCase(osName)) {
            if ("amd64".equalsIgnoreCase(archName)) {
                return "linux-x86_64";
            } else if ("aarch64".equalsIgnoreCase(archName)) {
                return "linux-aarch64";
            } else if ("arm".equalsIgnoreCase(archName)) {
                return "linux-armv7";
            } else if ("s390x".equalsIgnoreCase(archName)) {
                return "linux-s390x";
            } else if ("ppc64le".equalsIgnoreCase(archName)) {
                return "linux-ppc64le";
            } else if ("riscv64".equalsIgnoreCase(archName)) {
                return "linux-riscv64";
            }
        } else if (osName.startsWith("Windows")) {
            if ("amd64".equalsIgnoreCase(archName)) {
                return "windows-x86_64";
            } else if ("aarch64".equalsIgnoreCase(archName)) {
                return "windows-aarch64";
            }
        } else if (osName.startsWith("Mac")) {
            if ("x86_64".equalsIgnoreCase(archName)) {
                return "osx-x86_64";
            } else if ("aarch64".equalsIgnoreCase(archName)) {
                return "osx-aarch64";
            }
        }
        throw new UnsupportedOperationException("Unsupported OS and Architecture: " + osName + ", " + archName);
    }
}
