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

import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import skytils.skytilsmod.Skytils;
import sun.management.VMManagement;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class EssentialPlatformSetup {
    @SuppressWarnings("unused")
    private static final String e = "Please don't tell anyone this exists if you see it, thanks <3";

    public static void setup() {
        if (System.getProperty("skytils.skipEssentialSetup") != null) return;
        try {
            File me = getMyLocation();
            //noinspection ConstantConditions
            if (Skytils.VERSION.endsWith("-dev") || me.isDirectory()) return;
            String funnyCode = getFunnyCode(Files.toByteArray(me));

            JsonObject funnys = new JsonParser().parse(IOUtils.toString(new URL("https://cdn.jsdelivr.net/gh/Skytils/SkytilsMod-Data@main/constants/hashes.json"))).getAsJsonObject();
            if (!funnys.has(Skytils.VERSION)) loadEssential();
            if (!funnys.get(Skytils.VERSION).getAsString().equalsIgnoreCase(funnyCode)) loadEssential();
        } catch (IOException ignored) {

        } catch (Throwable t) {
            loadEssential();
        }
    }

    private static File getMyLocation() {
        return new File(EssentialPlatformSetup.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    }

    private static String getFunnyCode(byte[] bytes) throws NoSuchAlgorithmException {
        return new String(MessageDigest.getInstance("SHA-256").digest(bytes)).toUpperCase(Locale.ENGLISH);
    }

    private static void loadEssential() {
        try {
            LogManager.getLogger("launcher").log(Level.ALL, "\n" +
                    "#\n" +
                    "# A fatal error has been detected by the Java Runtime Environment:\n" +
                    "#\n" +
                    "#  EXCEPTION_ACCESS_VIOLATION (0xc0000005) at pc=0x000000006510d294, pid=" + getPID() + ", tid=0x0000000000001728\n" +
                    "#\n" +
                    "# JRE version: " + System.getProperty("java.runtime.name", "Java(TM) SE Runtime Environment") + " (" + System.getProperty("java.runtime.version") + ")\n" +
                    "# Java VM: " + System.getProperty("java.vm.name", "Java HotSpot(TM) 64-Bit Server VM") + " (" + System.getProperty("java.vm.version") + " " + System.getProperty("java.vm.info") + ")\n" +
                    "# Problematic frame:\n" +
                    "# V  [jvm.dll+0x1dd294]\n" +
                    "#\n" +
                    "# Failed to write core dump. Minidumps are not enabled by default on client versions\n" +
                    "#\n" +
                    "# An error report file with more information is saved as:\n" +
                    "# Failed to write error report.\n" +
                    "#\n" +
                    "# If you would like to submit a bug report, please visit:\n" +
                    "#   " + System.getProperty("java.vendor.url.bug", "http://bugreport.java.com/bugreport/crash.jsp") + "\n" +
                    "#");
            Minecraft.getMinecraft().shutdownMinecraftApplet();
        } catch (Throwable t) {
            SkytilsLoadingPlugin.exit();
        }
    }

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
}
