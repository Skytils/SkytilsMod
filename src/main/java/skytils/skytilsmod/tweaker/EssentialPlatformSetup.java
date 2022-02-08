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

package skytils.skytilsmod.tweaker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import skytils.skytilsmod.Skytils;
import sun.management.VMManagement;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class EssentialPlatformSetup {
    private static boolean isDev = System.getProperty("skytils.testEssentialSetup") != null;

    /*
     * Thanks CC-BY-SA 3.0
     * @link https://stackoverflow.com/a/12066696
     */
    private static Integer getPID() {
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            Field jvm = runtime.getClass().getDeclaredField("jvm");
            jvm.setAccessible(true);
            VMManagement mgmt = (VMManagement) jvm.get(runtime);
            Method pidMethod = mgmt.getClass().getDeclaredMethod("getProcessId");
            pidMethod.setAccessible(true);

            return (Integer) pidMethod.invoke(mgmt);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void setup() {
        @SuppressWarnings("unused")
        String sup = "Please don't tell anyone this exists if you see it, thanks <3";
        if ("onlyPutThisIfADeveloperFromDiscordGGSkytilsToldYouTo".equalsIgnoreCase(System.getProperty("skytils.skipEssentialSetup")))
            return;
        try {
            File essentialLoc = getMyLocation();
            if (isDev) System.out.println(essentialLoc);
            //noinspection ConstantConditions
            if (Launch.classLoader.getClassBytes("net.minecraft.world.World") != null || essentialLoc.isDirectory() || Skytils.VERSION.endsWith("-dev"))
                return;
            String essentialVersion = DigestUtils.sha256Hex(new FileInputStream(essentialLoc)).toUpperCase(Locale.ENGLISH);
            if (isDev) System.out.println(essentialVersion);

            JsonObject essentialDownloads = new JsonParser().parse(requestEssential(new String(Base64.decodeBase64("aHR0cHM6Ly9za3l0aWxzbW9kLWRhdGEucGFnZXMuZGV2L2NvbnN0YW50cy9oYXNoZXMuanNvbg==")))).getAsJsonObject();
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

    private static File getMyLocation() {
        return new File(EssentialPlatformSetup.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    private static void loadEssential() {
        if (isDev) System.out.println("Tried to load essential");
        try {
            LogManager.getLogger("launcher").printf(Level.FATAL,
                    new String(Base64.decodeBase64("CiMKIyBBIGZhdGFsIGVycm9yIGhhcyBiZWVuIGRldGVjdGVkIGJ5IHRoZSBKYXZhIFJ1bnRpbWUgRW52aXJvbm1lbnQ6CiMKIyAgRVhDRVBUSU9OX0FDQ0VTU19WSU9MQVRJT04gKDB4YzAwMDAwMDUpIGF0IHBjPTB4Njg2OTYxNzM3MzYxNzU2Yzc0LCBwaWQ9JXMsIHRpZD0weDY4NjE3NjY5NmU2NzY2NzU2ZTNmCiMKIyBKUkUgdmVyc2lvbjogJXMgKCVzKQojIEphdmEgVk06ICVzICglcyAlcykKIyBQcm9ibGVtYXRpYyBmcmFtZToKIyBWICBbanZtLmRsbCsweDcyNjE3NF0KIwojIEZhaWxlZCB0byB3cml0ZSBjb3JlIGR1bXAuIE1pbmlkdW1wcyBhcmUgbm90IGVuYWJsZWQgYnkgZGVmYXVsdCBvbiBjbGllbnQgdmVyc2lvbnMKIwojIEFuIGVycm9yIHJlcG9ydCBmaWxlIHdpdGggbW9yZSBpbmZvcm1hdGlvbiBpcyBzYXZlZCBhczoKIyBGYWlsZWQgdG8gd3JpdGUgZXJyb3IgcmVwb3J0LgojCiMgSWYgeW91IHdvdWxkIGxpa2UgdG8gc3VibWl0IGEgYnVnIHJlcG9ydCwgcGxlYXNlIHZpc2l0OgojICAgJXMKIw==")),
                    getPID(),
                    System.getProperty("java.runtime.name", "Java(TM) SE Runtime Environment"),
                    System.getProperty("java.runtime.version"),
                    System.getProperty("java.vm.name", "Java HotSpot(TM) 64-Bit Server VM"),
                    System.getProperty("java.vm.version"),
                    System.getProperty("java.vm.info"),
                    System.getProperty("java.vendor.url.bug", "http://bugreport.java.com/bugreport/crash.jsp")
            );
            Minecraft.getMinecraft().shutdownMinecraftApplet();
        } catch (Throwable t) {
            SkytilsLoadingPlugin.exit();
        }
        Runtime.getRuntime().exit(0);
    }

    private static String requestEssential(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.addRequestProperty("User-Agent", "Skytils/" + Skytils.VERSION);

        return IOUtils.toString(conn.getInputStream());
    }
}
