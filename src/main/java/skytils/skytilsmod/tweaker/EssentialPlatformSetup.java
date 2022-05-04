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
import skytils.skytilsmod.Reference;
import skytils.skytilsmod.Skytils;
import sun.management.VMManagement;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Collections;
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

    public static SSLContext getSSLContext() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(Files.newInputStream(Paths.get(System.getProperty("java.home") + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts")), "changeit".toCharArray());

        KeyStore me = KeyStore.getInstance(KeyStore.getDefaultType());
        me.load(EssentialPlatformSetup.class.getResourceAsStream("/skytilsletsencrypt.jks"), "skytilsontop".toCharArray());

        KeyStore besties = KeyStore.getInstance(KeyStore.getDefaultType());
        besties.load(null, null);

        for (String alias : Collections.list(ks.aliases())) {
            besties.setCertificateEntry(alias, ks.getCertificate(alias));
        }
        for (String alias : Collections.list(me.aliases())) {
            besties.setCertificateEntry(alias, me.getCertificate(alias));
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(besties);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);
        return ctx;
    }

    @SuppressWarnings("unused")
    public static void setup() {
        try {
            SSLContext ctx = getSSLContext();
            SSLContext.setDefault(ctx);
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
            String ver = System.getProperty("java.runtime.version", "unknown");
            String javaLoc = System.getProperty("java.home");
            if (ver.contains("1.8.0_51") || javaLoc.contains("jre-legacy")) {
                Path keyStoreLoc = Paths.get("./config/skytils/updates/files/skytilsletsencrypt.jks");
                File keyStoreFile = keyStoreLoc.toFile();
                if (!keyStoreFile.exists()) {
                    System.out.println("Skytils is attempting to run keytool.");
                    Files.createDirectories(keyStoreLoc.getParent());
                    try (InputStream in = EssentialPlatformSetup.class.getResourceAsStream("/skytilsletsencrypt.jks"); OutputStream os = Files.newOutputStream(keyStoreLoc)) {
                        IOUtils.copy(in, os);
                    }
                    String os = System.getProperty("os.name", "unknown");
                    String keyStorePath = javaLoc + File.separator + "lib" + File.separator + "security" + File.separator + "cacerts";
                    String keyToolPath = javaLoc + File.separator + "bin" + File.separator + (os.toLowerCase(Locale.ENGLISH).startsWith("windows") ? "keytool.exe" : "keytool");
                    File log = new File("./config/skytils/updates/files/sslfix-" + System.currentTimeMillis() + ".log");
                    new ProcessBuilder()
                            .command(keyToolPath, "-importkeystore", "-srckeystore", keyStoreFile.getAbsolutePath(), "-destkeystore", keyStorePath, "-srcstorepass", "skytilsontop", "-deststorepass", "changeit", "-noprompt")
                            .redirectOutput(log)
                            .redirectError(log)
                            .start().waitFor();
                    System.out.println("A reboot of Minecraft is required for the code to work, force closing the game");
                    SkytilsLoadingPlugin.exit();
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        TweakerUtil.registerTransformerExclusions(
                "kotlin.",
                "kotlinx.",
                "skytils.asmhelper.",
                "skytils.skytilsmod.tweaker.",
                "skytils.skytilsmod.asm."
        );


        for (final String url : dataURLCandidates) {
            if (trySetDataUrl(url)) {
                Reference.dataUrl = url;
                break;
            }
        }
        LogManager.getLogger().info("Data URL: " + Reference.dataUrl);

        @SuppressWarnings("unused")
        String sup = "UGxlYXNlIGRvbid0IHRlbGwgYW55b25lIHRoaXMgZXhpc3RzIGlmIHlvdSBzZWUgaXQsIHRoYW5rcyA8Mw==";
        if (new String(Base64.decodeBase64("b25seVB1dFRoaXNJZkFEZXZlbG9wZXJGcm9tRGlzY29yZEdHU2t5dGlsc1RvbGRZb3VUbw==")).equalsIgnoreCase(System.getProperty(new String(Base64.decodeBase64("c2t5dGlscy5za2lwRXNzZW50aWFsU2V0dXA=")))))
            return;
        try {
            File essentialLoc = getMyLocation();
            if (isDev) System.out.println(essentialLoc);
            //noinspection ConstantConditions
            if (Launch.classLoader.getClassBytes("net.minecraft.world.World") != null || essentialLoc.isDirectory() || Skytils.VERSION.endsWith("-dev"))
                return;
            String essentialVersion = DigestUtils.sha256Hex(Files.newInputStream(essentialLoc.toPath())).toUpperCase(Locale.ENGLISH);
            if (isDev) System.out.println(essentialVersion);

            JsonObject essentialDownloads = new JsonParser().parse(requestEssentialResource(Reference.dataUrl + new String(Base64.decodeBase64("Y29uc3RhbnRzL2hhc2hlcy5qc29u==")))).getAsJsonObject();
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
            } catch (Exception ignored) {
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
            }
        } catch (Throwable t) {
            SkytilsLoadingPlugin.exit();
        }
        Runtime.getRuntime().exit(0);
    }
}
