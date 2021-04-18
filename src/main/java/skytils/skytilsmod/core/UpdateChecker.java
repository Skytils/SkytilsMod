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

package skytils.skytilsmod.core;

import com.google.gson.JsonObject;
import kotlin.Unit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.gui.UpdateGui;
import skytils.skytilsmod.utils.APIUtil;

import java.io.*;

public class UpdateChecker {

    private final static Minecraft mc = Minecraft.getMinecraft();
    private final static UpdateGetter updateGetter = new UpdateGetter();

    static {
        Thread thread = new Thread(updateGetter);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if (!(e.gui instanceof GuiMainMenu)) return;
        if (updateGetter.updateObj == null) return;
        try {
/*            Notifications notifs = Notifications.INSTANCE;
            notifs.pushNotification("New Skytils Version Available", "Click here to download", () -> {
                Skytils.displayScreen = new UpdateGui();
                return Unit.INSTANCE;
            });*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getUpdateDownloadURL() {
        return updateGetter.updateObj.get("assets").getAsJsonArray().get(0).getAsJsonObject().get("browser_download_url").getAsString();
    }

    public static String getJarNameFromUrl(String url) {
        String[] sUrl = url.split("/");
        return sUrl[sUrl.length - 1];
    }

    public static void scheduleCopyUpdateAtShutdown(String jarName) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Attempting to apply Skytils update.");
                File oldJar = Skytils.jarFile;

                if (oldJar == null || !oldJar.exists() || oldJar.isDirectory()) {
                    System.out.println("Old jar file not found.");
                    return;
                }

                File newJar = new File(new File(Skytils.modDir, "updates"), jarName);
                copyFile(newJar, oldJar);
                newJar.delete();
                System.out.println("Successfully applied Skytils update.");
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }));
    }

    /**
     * Taken from Wynntils under GNU Affero General Public License v3.0
     * Modified to perform faster
     * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
     * @author Wynntils
     * Copy a file from a location to another
     *
     * @param sourceFile The source file
     * @param destFile Where it will be
     */
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (destFile == null || !destFile.exists()) {
            destFile = new File(new File(sourceFile.getParentFile(), "mods"), "Skytils.jar");
            sourceFile.renameTo(destFile);
            return;
        }

        InputStream source = null;
        OutputStream dest = null;
        try {
            source = new FileInputStream(sourceFile);
            dest = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = source.read(buffer)) > 0) {
                dest.write(buffer, 0, length);
            }
        } finally {
            source.close();
            dest.close();
        }
    }

    private static class UpdateGetter implements Runnable {
        private volatile JsonObject updateObj = null;

        /**
         * Modified version from Danker's Skyblock Mod, taken under GPL 3.0 license.
         * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
         * @author bowser0000
         */
        @Override
        public void run() {
            System.out.println("Checking for updates...");

            JsonObject latestRelease = Skytils.config.updateChannel == 1 ? APIUtil.getArrayResponse("https://api.github.com/repos/Skytils/SkytilsMod/releases").get(0).getAsJsonObject() : APIUtil.getJSONResponse("https://api.github.com/repos/Skytils/SkytilsMod/releases/latest");
            String latestTag = latestRelease.get("tag_name").getAsString();
            DefaultArtifactVersion currentVersion = new DefaultArtifactVersion(Skytils.VERSION);
            DefaultArtifactVersion latestVersion = new DefaultArtifactVersion(latestTag.substring(1));

            if (latestTag.contains("pre") || Skytils.VERSION.contains("pre") && currentVersion.compareTo(latestVersion) >= 0) {
                double currentPre = 0;
                double latestPre = 0;
                if (Skytils.VERSION.contains("pre")) {
                    currentPre = Double.parseDouble(Skytils.VERSION.substring(Skytils.VERSION.indexOf("pre") + 3));
                }

                if (latestTag.contains("pre")) {
                    latestPre = Double.parseDouble(latestTag.substring(latestTag.indexOf("pre") + 3));
                }

                if (latestPre > currentPre || (latestPre == 0 && currentVersion.compareTo(latestVersion) == 0)) {
                    updateObj = latestRelease;
                }
            } else if (currentVersion.compareTo(latestVersion) < 0) {
                updateObj = latestRelease;
            }
        }

    }
}
