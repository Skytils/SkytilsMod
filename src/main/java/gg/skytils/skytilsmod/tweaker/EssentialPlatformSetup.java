/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gg.skytils.skytilsmod.Reference;
import gg.skytils.skytilsmod.Skytils;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Locale;

public class EssentialPlatformSetup {
    private static final String[] dataURLCandidates = {System.getProperty("skytils.dataURL"), Reference.dataUrl, "https://skytilsmod-data.pages.dev/", "https://cdn.jsdelivr.net/gh/Skytils/SkytilsMod-Data@main/"};
    private static final boolean isDev = System.getProperty("skytils.testEssentialSetup") != null;

    private static boolean trySetDataUrl(String url) {
        if (url == null) return false;
        try {
            return requestEssentialResource(url + "CANYOUSEEME").contains("YOUCANSEEME");
        } catch (Exception e) {
            LogManager.getLogger().error("Failed to contact url " + url);
            return false;
        }
    }

    private static String requestEssentialResource(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.addRequestProperty("User-Agent", "Skytils/" + Skytils.VERSION);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        return IOUtils.toString(conn.getInputStream());
    }

    private static File getMyLocation() {
        return new File(EssentialPlatformSetup.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }


    @SuppressWarnings("unused")
    public static void setup() {
        TweakerUtil.registerTransformerExclusions(
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

        @SuppressWarnings("unused")
        String sup = "Please tell everyone this exists if you see it, thanks <3"; // No more secrets, lily
        if ("onlyPutThisIfADeveloperFromDiscordGGSkytilsToldYouTo".equalsIgnoreCase(System.getProperty("skytils.skipEssentialSetup"))
                || "alsoSkipIfYouJustFeelLikeItBecauseFuckMakingRules".equalsIgnoreCase(System.getProperty("skytils.skipEssentialSetup")))
            return;
        try {
            File essentialLoc = getMyLocation();
            if (isDev) System.out.println(essentialLoc);
            //noinspection ConstantConditions
            if (Launch.classLoader.getClassBytes("net.minecraft.world.World") != null || essentialLoc.isDirectory() || Skytils.VERSION.endsWith("-dev"))
                return;
            String essentialVersion = DigestUtils.sha256Hex(Files.newInputStream(essentialLoc.toPath())).toUpperCase(Locale.ENGLISH);
            if (isDev) System.out.println(essentialVersion);

            JsonObject essentialDownloads = new JsonParser().parse(requestEssentialResource(Reference.dataUrl + "constants/hashes.json")).getAsJsonObject();
            if (isDev) System.out.println(essentialDownloads);
            if (!essentialDownloads.has(Skytils.VERSION)) loadEssential();
            if (!essentialDownloads.get(Skytils.VERSION).getAsString().equalsIgnoreCase(essentialVersion))
                loadEssential();
        } catch (IOException e) {
            if (isDev) e.printStackTrace();
        } catch (Throwable t) {
            loadEssential();
            Runtime.getRuntime().exit(0);
        }
    }

    private static void loadEssential() {
        if (isDev) System.out.println("Tried to load essential");
        try {
            try {
                TweakerUtil.runStage("gg.essential.loader.stage0.LoaderStage0", "doMixinUpgrade");
            } catch (Exception e) {
                e.printStackTrace();
                Minecraft.getMinecraft().shutdownMinecraftApplet();
            }
        } catch (Throwable t) {
            SkytilsLoadingPlugin.exit();
        }
        Runtime.getRuntime().exit(0);
    }
}
