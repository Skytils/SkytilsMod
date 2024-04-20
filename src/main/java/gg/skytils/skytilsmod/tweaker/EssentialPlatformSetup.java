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

import gg.essential.universal.UDesktop;
import gg.skytils.skytilsmod.Reference;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;

import static gg.skytils.skytilsmod.tweaker.TweakerUtil.*;

public class EssentialPlatformSetup {
    private static final String[] dataURLCandidates = {System.getProperty("skytils.dataURL"), Reference.dataUrl, "https://skytilsmod-data.pages.dev/", "https://cdn.jsdelivr.net/gh/Skytils/SkytilsMod-Data@main/", "https://mirror.ghproxy.com/https://raw.githubusercontent.com/Skytils/SkytilsMod-Data/main/"};

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
    public static void setup() throws Throwable {
        boolean isDev = Launch.classLoader.findResource("net/minecraft/world/World.class") != null;

        try {
            DependencyLoader.loadDependencies();
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
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
                            UDesktop.browse(URI.create("https://skytils.gg"));
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
                            UDesktop.open(new File("./mods"));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

                showMessage("<html><p>" +
                        "Your version of Skytils (" + Reference.VERSION  + ") requires a<br>" +
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