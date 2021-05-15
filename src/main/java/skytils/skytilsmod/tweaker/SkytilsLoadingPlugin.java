package skytils.skytilsmod.tweaker;

import kotlin.KotlinVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Map;

public class SkytilsLoadingPlugin implements IFMLLoadingPlugin {

    private static final String errorMessage =
        "<html><p>" +
        "Skytils has detected a mod with an older version of Kotlin.<br>" +
        "In order to resolve this conflict you must make Skytils be<br>" +
        "above this mod alphabetically in your mods folder.<br>" +
        "You can do this by renaming your Skytils jar to !Skytils.jar.<br>" +
        "If you have already done this and are still getting this error,<br>" +
        "ask for support in the Discord." +
        "</p></html>";

    public SkytilsLoadingPlugin() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.skytils.json");
        if (!KotlinVersion.CURRENT.isAtLeast(1, 5, 0)) {
            showMessage(errorMessage);
            exit();
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
            null,
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
