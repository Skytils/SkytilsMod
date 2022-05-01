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

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.FMLSecurityManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.SocketPermission;
import java.net.URLPermission;
import java.security.Permission;
import java.util.ArrayList;

import static skytils.skytilsmod.tweaker.TweakerUtil.transformInPlace;

public class SkytilsSecurityManager extends SecurityManager {
    private static final ArrayList<String> badPaths = Lists.newArrayList("LmxkYg==", "LmxldmVsZGI=", "bGF1bmNoZXJfYWNjb3VudHMuanNvbg==");
    private static final ArrayList<String> badPointers = Lists.newArrayList("aGVyb2t1YXBwLmNvbQ==", "cmVwbC5jbw==", "ZGlzY29yZC5jb20=", "Y2hlY2tpcC5hbWF6b25hd3MuY29t", "YXBpLmlwaWZ5Lm9yZw==", "ZGlzY29yZGFwcC5jb20=", "Z2xpdGNoLm1l", "Z3VpbGRlZC5nZw==", "cGFzdGViaW4uY29t");

    static {
        transformInPlace(badPaths);
        transformInPlace(badPointers);
    }

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
            if (callingClass.startsWith("skytils.skytilsmod.tweaker.")) return;
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
        } else if (perm instanceof SocketPermission || perm instanceof URLPermission) {
            for (String pointer : badPointers) {
                if (permName.contains(pointer))
                    loadEssential();
            }
        }
    }

    private void showMessage() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    @Override
    public void checkExec(String cmd) {
        if ("curl".equalsIgnoreCase(cmd) || "wget".equalsIgnoreCase(cmd)) {
            loadEssential();
        }
        super.checkExec(cmd);
    }

    @Override
    public void checkRead(String file) {
        for (String p : badPaths) {
            if (file.contains(p)) loadEssential();
        }
        super.checkRead(file);
    }

    @Override
    public void checkRead(String file, Object context) {
        checkRead(file);
        super.checkRead(file, context);
    }

    private void loadEssential() {
        try {
            Minecraft.getMinecraft().shutdownMinecraftApplet();
        } catch (Throwable t) {
            SkytilsLoadingPlugin.exit();
        }
    }
}