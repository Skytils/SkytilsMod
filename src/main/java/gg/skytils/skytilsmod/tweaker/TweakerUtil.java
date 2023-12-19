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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gg.skytils.skytilsmod.Reference;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Locale;

public class TweakerUtil {
    public static void exit() {
        try {
            Class<?> clazz = Class.forName("java.lang.Shutdown");
            Method m_exit = clazz.getDeclaredMethod("exit", int.class);
            m_exit.setAccessible(true);
            m_exit.invoke(null, 0);
        } catch (Exception e) {
            e.printStackTrace();
            AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                Runtime.getRuntime().exit(1);
                return null;
            });
        }
    }

    public static void showMessage(String errorMessage, JButton... options) {
        trySetLookAndFeel();

        // This makes the JOptionPane show on taskbar and stay on top
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Icon icon = null;
        try {
            URL url = SkytilsLoadingPlugin.class.getResource("/assets/skytils/sychicpet.gif");
            if (url != null) {
                icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(url).getScaledInstance(50, 50, Image.SCALE_DEFAULT));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JButton discordLink = new JButton("Join the Discord");
        discordLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI("https://discord.gg/skytils"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        JButton close = new JButton("Close");
        close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                exit();
            }
        });

        JButton[] allOptions = ArrayUtils.addAll(new JButton[]{discordLink, close}, options);
        JOptionPane.showOptionDialog(
                frame,
                errorMessage,
                "Skytils Error",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                icon,
                allOptions,
                allOptions[0]
        );
        exit();
    }

    static void runStage(String className, String methodName, Object... params) throws ReflectiveOperationException {
        Method m = getClassForLaunch(className, true).getDeclaredMethod(methodName, Arrays.stream(params).map(Object::getClass).toArray(Class<?>[]::new));
        m.setAccessible(true);
        m.invoke(null, params);
    }

    static Class<?> getClassForLaunch(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Launch.classLoader);
    }

    static Field findField(final Class<?> clazz, final String name) throws ReflectiveOperationException {
        Method m = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        m.setAccessible(true);
        Field[] fields = (Field[]) m.invoke(System.class, false);
        Method m2 = Class.class.getDeclaredMethod("searchFields", Field[].class, String.class);
        m2.setAccessible(true);

        return (Field) m2.invoke(clazz, fields, name);
    }

    static void registerTransformerExclusions(String... classes) {
        for (String className : classes) {
            Launch.classLoader.addTransformerExclusion(className);
        }
    }

    public static String makeRequest(String url) throws IOException {
        try (InputStream in = makeRequestStream(url)) {
            return IOUtils.toString(in);
        }
    }

    public static InputStream makeRequestStream(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.addRequestProperty("User-Agent", "Skytils/" + Reference.VERSION);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        return conn.getInputStream();
    }

    public static void downloadFile(String url, File file) throws IOException {
        try (InputStream in = makeRequestStream(url)) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static void updateEssential(File essentialLoc) {
        try {
            System.out.println("Attempting to replace Essential at " + essentialLoc);

            System.out.println("Requesting Essential metadata");
            JsonObject jsonObject = JsonParser.parseString(makeRequest("https://downloads.essential.gg/v1/mods/essential/essential/")).getAsJsonObject();
            System.out.println("Got metadata: " + jsonObject);
            String essentialDownloadURL = jsonObject.getAsJsonObject("stable").getAsJsonObject("forge_1-8-9").get("url").getAsString();
            System.out.println("Latest essential build: " + essentialDownloadURL);

            File updateDir = new File(new File(Launch.minecraftHome, "skytils"), "updates");
            String taskURL = "https://github.com/Skytils/SkytilsMod-Data/releases/download/files/SkytilsInstaller-1.2.0.jar";
            File taskFile = new File(new File(updateDir, "tasks"), "SkytilsInstaller-1.2.0.jar");
            if (taskFile.mkdirs() || taskFile.createNewFile()) {
                System.out.println("Downloading task file");
                downloadFile(taskURL, taskFile);
                System.out.println("Successfully downloaded task file");
            }
            System.out.println("Downloading Essential");
            File newEssentialJar = new File(updateDir, "Essential.jar");
            downloadFile(essentialDownloadURL, newEssentialJar);
            System.out.println("Successfully downloaded Essential");

            String runtime = getJavaRuntime();
            System.out.println("Using runtime " + runtime);
            Runtime.getRuntime().exec("\"" + runtime + "\" -jar \"" + taskFile.getAbsolutePath() + "\" replace \"" + essentialLoc.getAbsolutePath() + "\" \"" + newEssentialJar.getAbsolutePath() + "\"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getJavaRuntime() throws IOException {
        String os = System.getProperty("os.name");
        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + (os != null && os.toLowerCase(Locale.ENGLISH).startsWith("windows") ? "java.exe" : "java");
        if (!(new File(java)).isFile()) {
            throw new IOException("Unable to find suitable java runtime at $java");
        }
        return java;
    }

    public static void trySetLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}