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

package skytils.skytilsmod.tweaker;

import kotlin.KotlinVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import skytils.skytilsmod.utils.StringUtilsKt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class SkytilsLoadingPlugin implements IFMLLoadingPlugin {

    private static final String kotlinErrorMessage =
        "<html><p>" +
        "Skytils has detected a mod with an older version of Kotlin.<br>" +
        "The most common culprit is the ChatTriggers mod.<br>" +
        "In order to resolve this conflict you must make Skytils be<br>" +
        "above this mod alphabetically in your mods folder.<br>" +
        "This tricks Forge into loading Skytils first.<br>" +
        "You can do this by renaming your Skytils jar to !Skytils.jar,<br>" +
        "or by renaming the other mod's jar to start with a Z.<br>" +
        "If you have already done this and are still getting this error,<br>" +
        "ask for support in the Discord." +
        "</p></html>";

    private static final String badMixinVersionMessage =
        "<html><p>" +
        "Skytils has detected an older version of Mixin.<br>" +
        "Many of my features require Mixin 0.7 or later!<br>" +
        "In order to resolve this conflict you must remove<br>" +
        "any mods with a Mixin version below 0.7<br>" +
        "If you have already done this and are still getting this error,<br>" +
        "ask for support in the Discord." +
        "</p></html>";

    private static final String liteloaderUserMessage =
        "<html><p>" +
        "Skytils has detected that you are using LiteLoader.<br>" +
        "LiteLoader bundles an older, incompatible version of Mixin.<br>" +
        "In order to resolve this conflict you must launch<br>" +
        "Minecraft without LiteLoader.<br>" +
        "If you have already done this and are still getting this error,<br>" +
        "ask for support in the Discord." +
        "</p></html>";

    public SkytilsLoadingPlugin() {
        if (!KotlinVersion.CURRENT.isAtLeast(1, 5, 0)) {
            showMessage(kotlinErrorMessage);
            exit();
        }
        if (!StringUtilsKt.startsWithAny(MixinEnvironment.getCurrentEnvironment().getVersion(), "0.7", "0.8")) {
            try {
                Class.forName("com.mumfrey.liteloader.launch.LiteLoaderTweaker");
                showMessage(liteloaderUserMessage);
                exit();
            } catch (ClassNotFoundException ignored) {
                showMessage(badMixinVersionMessage);
                exit();
            }
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    private void showMessage(String errorMessage) {
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

        Object[] options = new Object[]{discordLink, close};
        JOptionPane.showOptionDialog(
            frame,
            errorMessage,
            "Skytils Error",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE,
            icon,
            options,
            options[0]
        );
        exit();
    }

    /**
     * Bypasses forges security manager to exit the jvm
     */
    private void exit() {
        try {
            Class<?> clazz = Class.forName("java.lang.Shutdown");
            Method m_exit = clazz.getDeclaredMethod("exit", int.class);
            m_exit.setAccessible(true);
            m_exit.invoke(null, 0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
