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

import net.minecraftforge.fml.relauncher.FMLSecurityManager;
import sun.security.util.SecurityConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;

import static gg.skytils.skytilsmod.tweaker.TweakerUtil.trySetLookAndFeel;

public class SkytilsSecurityManager extends SecurityManager {
    private final boolean isForge;

    public SkytilsSecurityManager(boolean isForge) {
        this.isForge = isForge;
    }

    @Override
    public void checkPermission(Permission perm) {
        String permName = perm.getName() != null ? perm.getName() : "missing";
        if (permName.startsWith("exitVM")) {
            Class<?>[] classContexts = getClassContext();
            String callingClass = classContexts.length > 3 ? classContexts[4].getName() : "none";
            String callingParent = classContexts.length > 4 ? classContexts[5].getName() : "none";
            // Skytils: allow Skytils tweaker classes to close the game
            if (callingClass.startsWith("gg.skytils.skytilsmod.tweaker.")) return;
            if (callingClass.startsWith("gg.skytils.skytilsmod.loader.")) return;
            // Skytils: allow the LaunchWrapper to close the game
            if (callingClass.equals("net.minecraft.launchwrapper.Launch") && callingParent.equals("net.minecraft.launchwrapper.Launch")) {
                showMessage();
                return;
            }
            // FML is allowed to call system exit and the Minecraft applet (from the quit button)
            if (!isForge && !(callingClass.startsWith("net.minecraftforge.fml.")
                    || "net.minecraft.server.dedicated.ServerHangWatchdog$1".equals(callingClass)
                    || "net.minecraft.server.dedicated.ServerHangWatchdog".equals(callingClass)
                    || ("net.minecraft.client.Minecraft".equals(callingClass) && "net.minecraft.client.Minecraft".equals(callingParent))
                    || ("net.minecraft.server.dedicated.DedicatedServer".equals(callingClass) && "net.minecraft.server.MinecraftServer".equals(callingParent)))
            ) {
                throw new FMLSecurityManager.ExitTrappedException();
            }
        } else if ("setSecurityManager".equals(permName)) {
            throw new SecurityException("Cannot replace the FML (Skytils) security manager");
        }
    }

    private void showMessage() {
        trySetLookAndFeel();

        // This makes the JOptionPane show on taskbar and stay on top
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JButton openLogs = new JButton("Open Logs Folder");
        openLogs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                try {
                    Desktop.getDesktop().open(new File("./logs"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        JButton openLatestLog = new JButton("Open Latest Log");
        openLatestLog.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                try {
                    Desktop.getDesktop().open(new File("./logs/latest.log"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Object[] options = new Object[]{openLatestLog, openLogs};
        JOptionPane.showOptionDialog(
                frame,
                "The game crashed whilst launching.",
                "Minecraft Crash",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[0]
        );
    }

    static void overrideSecurityManager(Boolean isForge) {
        try {
            SecurityManager s = new SkytilsSecurityManager(isForge);

            if (s.getClass().getClassLoader() != null) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    s.getClass().getProtectionDomain().implies
                            (SecurityConstants.ALL_PERMISSION);
                    return null;
                });
            }

            Field field = TweakerUtil.findField(System.class, "security");
            field.setAccessible(true);
            field.set(null, s);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}