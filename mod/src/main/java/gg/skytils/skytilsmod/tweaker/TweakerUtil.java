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
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

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
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        conn.setDoOutput(true);
        conn.addRequestProperty("User-Agent", "Skytils/" + Reference.VERSION);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        return IOUtils.toString(conn.getInputStream());
    }

    public static void trySetLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addToClasspath(URL url) throws Throwable {
        Launch.classLoader.addURL(url);
        ClassLoader parent = Launch.classLoader.getClass().getClassLoader();
        if (parent != null) {
            try {
                Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                addURL.invoke(parent, url);
            } catch (Throwable t) {
                t.printStackTrace();

                Field ucpField = parent.getClass().getDeclaredField("ucp");
                ucpField.setAccessible(true);

                Object ucp = ucpField.get(parent);
                Method ucpAddURL = ucp.getClass().getDeclaredMethod("addURL", URL.class);
                ucpAddURL.invoke(ucp, url);
            }
        }
    }
}