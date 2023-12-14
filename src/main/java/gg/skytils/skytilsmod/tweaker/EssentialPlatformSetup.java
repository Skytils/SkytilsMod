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

package gg.skytils.skytilsmod.tweaker;

import gg.skytils.skytilsmod.Reference;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;

import static gg.skytils.skytilsmod.tweaker.TweakerUtil.*;

public class EssentialPlatformSetup {
    private static final String[] dataURLCandidates = {System.getProperty("skytils.dataURL"), Reference.dataUrl, "https://skytilsmod-data.pages.dev/", "https://cdn.jsdelivr.net/gh/Skytils/SkytilsMod-Data@main/"};

    private static boolean trySetDataUrl(String url) {
        if (url == null) return false;
        try {
            return makeRequest(url + "CANYOUSEEME").contains("YOUCANSEEME");
        } catch (Exception e) {
            LogManager.getLogger().error("Failed to contact url " + url);
            return false;
        }
    }

    @SuppressWarnings("unused")
    public static void setup() {
        try {
            String ver = System.getProperty("java.runtime.version", "unknown");
            String javaLoc = System.getProperty("java.home");
            if (ver.contains("1.8.0_51") || javaLoc.contains("jre-legacy")) {
                System.out.println("Minecraft is running on legacy Java 8");
                Path keyStoreLoc = Paths.get("./config/skytils/updates/files/skytilscacerts.jks");
                File keyStoreFile = keyStoreLoc.toFile();
                //check if the file matches what's in the jar based on filesize
                if (!keyStoreFile.exists()) {
                    System.out.println("Skytils is attempting to run keytool.");
                    Files.createDirectories(keyStoreLoc.getParent());
                    try (InputStream in = EssentialPlatformSetup.class.getResourceAsStream("/skytilscacerts.jks"); OutputStream os = Files.newOutputStream(keyStoreLoc)) {
                        IOUtils.copy(Objects.requireNonNull(in), os);
                    }
                    String os = System.getProperty("os.name", "unknown");

                    Path keyStorePath = Paths.get(javaLoc, "lib", "security", "cacerts").toAbsolutePath();
                    Path keyToolPath = Paths.get(javaLoc, "bin", (os.toLowerCase(Locale.ENGLISH).startsWith("windows") ? "keytool.exe" : "keytool")).toAbsolutePath();
                    File log = new File("./config/skytils/updates/files/sslfix-" + System.currentTimeMillis() + ".log");
                    new ProcessBuilder()
                            .command(keyToolPath.toString(), "-importkeystore", "-srckeystore", keyStoreFile.getAbsolutePath(), "-destkeystore", keyStorePath.toString(), "-srcstorepass", "skytilsontop", "-deststorepass", "changeit", "-noprompt")
                            .redirectOutput(log)
                            .redirectError(log)
                            .start().waitFor();
                    System.out.println("A reboot of Minecraft is required for the code to work, force closing the game");
                    exit();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        registerTransformerExclusions(
                "kotlin.",
                "kotlinx.",
                "gg.skytils.asmhelper.",
                "gg.skytils.skytilsmod.tweaker.",
                "gg.skytils.skytilsmod.asm."
        );

        for (final String url : dataURLCandidates) {
            if (trySetDataUrl(url)) {
                Reference.dataUrl = url;
                break;
            }
        }
        LogManager.getLogger().info("Data URL: " + Reference.dataUrl);

        try {
            if (Integer.parseInt(makeRequest(Reference.dataUrl + "api/version").trim()) > Reference.apiVersion) {
                JButton btn = new JButton("Open Website");
                btn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            Desktop.getDesktop().browse(URI.create("https://skytils.gg"));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                JButton openModFolder = new JButton("Open Mod Folder");
                openModFolder.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        try {
                            Desktop.getDesktop().open(new File("./mods"));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                showMessage("<html><p>" +
                        "Your version of Skytils requires a<br>" +
                        "mandatory update before you can play!<br>" +
                        "Please download the latest version,<br>" +
                        "join the Discord for support.<br>" +
                        "</p></html>", btn, openModFolder);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}