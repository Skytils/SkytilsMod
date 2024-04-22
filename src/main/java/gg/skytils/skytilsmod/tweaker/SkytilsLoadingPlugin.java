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

import gg.skytils.skytilsmod.Skytils;
import kotlin.KotlinVersion;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.Properties;

import static gg.skytils.skytilsmod.tweaker.TweakerUtil.exit;
import static gg.skytils.skytilsmod.tweaker.TweakerUtil.showMessage;

@IFMLLoadingPlugin.Name("Skytils On Top")
@IFMLLoadingPlugin.SortingIndex(69)
public class SkytilsLoadingPlugin implements IFMLLoadingPlugin {

    private static final Logger LOGGER = LogManager.getLogger("Skytils Loading Plugin");

    public static final String missingDependency =
            "<html><p>" +
                    "Skytils has detected a possible missing dependency<br>" +
                    "The most likely reason is Essential failed to load.<br>" +
                    "Wait a bit, then restart your game.<br>" +
                    "Essential might also not work in your country.<br>" +
                    "Check the Skytils Discord for any announcements, and<br>" +
                    "if there are none, ask for support." +
                    "</p></html>";

    public static final String badMixinVersionMessage =
            "<html><p>" +
                    "Skytils has detected an older version of Mixin.<br>" +
                    "Many of my features require Mixin 0.8 or later!<br>" +
                    "In order to resolve this conflict you must remove<br>" +
                    "any mods with a Mixin version below 0.8.<br>" +
                    "You can also try to rename Skytils to be above other mods alphabetically<br>" +
                    "by changing Skytils.jar to !Skytils.jar<br>" +
                    "If you have already done this and are still getting this error,<br>" +
                    "ask for support in the Discord.";

    public static final String liteloaderUserMessage =
            "<html><p>" +
                    "Skytils has detected that you are using LiteLoader.<br>" +
                    "LiteLoader bundles an older, incompatible version of Mixin.<br>" +
                    "In order to resolve this conflict you must launch<br>" +
                    "Minecraft without LiteLoader.<br>" +
                    "If you have already done this and are still getting this error,<br>" +
                    "ask for support in the Discord." +
                    "</p></html>";

    private static final String voidChatMessage =
            "<html><p>" +
                    "Skytils has detected that you are using VoidChat.<br>" +
                    "VoidChat breaks many of my features!<br>" +
                    "In order to resolve this conflict you must remove<br>" +
                    "VoidChat from your Minecraft mods folder.<br>" +
                    "A good alternative is Patcher at https://sk1er.club/mods/Patcher.<br>" +
                    "If you have already done this and are still getting this error,<br>" +
                    "ask for support in the Discord." +
                    "</p></html>";

    private static final String betterFPSMessage =
            "<html><p>" +
                    "Skytils has detected that you are using BetterFPS.<br>" +
                    "BetterFPS breaks my core plugins, and also breaks the game!<br>" +
                    "In order to resolve this conflict you must remove<br>" +
                    "BetterFPS from your Minecraft mods folder.<br>" +
                    "You probably will not notice a change in your FPS.<br>" +
                    "Video showcasing breaking changes: https://streamable.com/q4ip5u.<br>" +
                    "If you have already done this and are still getting this error,<br>" +
                    "ask for support in the Discord." +
                    "</p></html>";

    private static final String essentialUpdateDeniedMessage =
            "<html><p>" +
                    "Skytils has detected that your Essential is out of date and you have denied the update. <br>" +
                    "In order for Skytils to function, we rely on many APIs provided by Essential. <br>" +
                    "Please restart your game and accept Essential's update pop-up. <br>" +
                    "Alternatively, click the \"Accept Essential Update\" button below and restart your game." +
                    "</p></html>";

    private static final String essentialUpdateAcceptedMessage =
            "<html><p>" +
                    "Skytils has detected that your Essential is out of date but you have accepted the update. <br>" +
                    "Please restart your game and allow Essential to update." +
                    "</p></html>";

    private final SkytilsLoadingPluginKt kotlinPlugin;

    public SkytilsLoadingPlugin() {
        if (System.getProperty("skytils.skipStartChecks") == null) {
            if (!checkForClass("kotlin.KotlinVersion") || !checkForClass("gg.essential.api.EssentialAPI")) {
                showMessage(missingDependency);
                exit();
            }
            if (!KotlinVersion.CURRENT.isAtLeast(1, 9, 0)) {
                EssentialPendingUpdateMode essentialUpdateMode = checkPendingEssentialUpdateStatus();
                if (essentialUpdateMode == EssentialPendingUpdateMode.Denied) {
                    // removes the pendingUpdateResolution key
                    updatePendingEssentialUpdateStatus(false);
                    JButton acceptEssentialUpdate = new JButton("Accept Essential Update");
                    acceptEssentialUpdate.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // updates the pendingUpdateResolution key to true
                            updatePendingEssentialUpdateStatus(true);
                            exit();
                        }
                    });
                    showMessage(essentialUpdateDeniedMessage, acceptEssentialUpdate);
                    exit();
                } else if (essentialUpdateMode == EssentialPendingUpdateMode.Accepted) {
                    showMessage(essentialUpdateAcceptedMessage);
                    exit();
                } else if (essentialUpdateMode == EssentialPendingUpdateMode.NoUpdate) {
                    // FIXME: This should never be reached
                    showMessage("You should not be here, please contact `sychic` on discord. <br>" +
                            "Join discord.gg/skytils and ping Sychic in #general.");
                    exit();
                }
            }
            if (checkForClass("com.sky.voidchat.EDFMLLoadingPlugin")) {
                showMessage(voidChatMessage);
                exit();
            }
            if (checkForClass("me.guichaguri.betterfps.BetterFpsHelper")) {
                showMessage(betterFPSMessage);
                exit();
            }
        }
        kotlinPlugin = new SkytilsLoadingPluginKt();
    }

    private boolean checkForClass(String className) {
        try {
            Class.forName(className, false, getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * Checks the status of essential's update prompt
     * @return update status
     */
    private EssentialPendingUpdateMode checkPendingEssentialUpdateStatus() {
        Path propertiesPath = Launch.minecraftHome.toPath().resolve(Paths.get("essential/essential-loader.properties"));
        if (Files.exists(propertiesPath)) {
            try (InputStream input = Files.newInputStream(propertiesPath.toFile().toPath())) {
                Properties properties = new Properties();
                properties.load(input);
                String value = properties.getProperty("pendingUpdateResolution");
                if (value.equals("true")) {
                    return EssentialPendingUpdateMode.Accepted;
                } else if (value.equals("false")) {
                    return EssentialPendingUpdateMode.Denied;
                }
                LOGGER.info("Failed to find `pendingUpdateResolution` in properties file.");
                return EssentialPendingUpdateMode.NoUpdate;
            } catch (IOException ioe) {
                LOGGER.fatal("Failed to read essential/essential-loader.properties file", ioe);
                return EssentialPendingUpdateMode.NoUpdate;
            }
        }
        LOGGER.fatal("Unable to find essential/essential-loader.properties file. How did this happen?");
        return EssentialPendingUpdateMode.NoUpdate;
    }

    private enum EssentialPendingUpdateMode {
        Denied,
        Accepted,
        NoUpdate
    }

    /**
     * Updates the `pendingUpdateResolution` key based on the `accepted` boolean:
     * false - remove the key to trigger Essential's own updater
     * true - update the key to true to auto update on next launch
     * @param accepted whether the update has been accepted or not
     */
    private void updatePendingEssentialUpdateStatus(boolean accepted) {
        Path propertiesPath = Launch.minecraftHome.toPath().resolve(Paths.get("essential/essential-loader.properties"));
        if (Files.exists(propertiesPath)) {
            try {
                Properties properties = new Properties();
                try (InputStream input = Files.newInputStream(propertiesPath)) {
                    properties.load(input);
                }
                // pendingUpdateResolution will always be false because we do not run this code when it is true
                if (accepted) {
                    // Setting "pendingUpdateResolution" to true will cause Essential to go through its
                    // own auto update process
                    // see: https://github.com/EssentialGG/EssentialLoader/blob/79358c93a5f26e4b0440e9c1c964b6f4e2d12615/docs/container-mods.md?plain=1#L97
                    properties.setProperty("pendingUpdateResolution", "true");
                } else {
                    // Removing the "pendingUpdateResolution" property will cause Essential to prompt the user using the loader's GUI
                    // see: https://github.com/EssentialGG/EssentialLoader/blob/79358c93a5f26e4b0440e9c1c964b6f4e2d12615/docs/container-mods.md?plain=1#L94-L95
                    properties.remove("pendingUpdateResolution");
                }
                Path temp = Files.createTempFile(propertiesPath.getParent(), "skytils-temp-essential-loader", ".properties");
                try (OutputStream output = Files.newOutputStream(temp)) {
                    properties.store(output, "Updated by Skytils version " + Skytils.VERSION);
                }
                try {
                    LOGGER.debug("Attempting atomic move of {} to {}", temp, propertiesPath);
                    Files.move(temp, propertiesPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (FileSystemException fse) {
                    LOGGER.fatal("Atomic move failed", fse);
                    Files.move(temp, propertiesPath, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    LOGGER.debug("Deleting temp file {}", temp);
                    Files.deleteIfExists(temp);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // This point should never be reached but log anyway
            LOGGER.fatal("Unable to find essential/essential-loader.properties file. How did we get here?");
        }
    }

    @Override
    public String[] getASMTransformerClass() {
        return kotlinPlugin.getASMTransformerClass();
    }

    @Override
    public String getModContainerClass() {
        return kotlinPlugin.getModContainerClass();
    }

    @Override
    public String getSetupClass() {
        return kotlinPlugin.getSetupClass();
    }

    @Override
    public void injectData(Map<String, Object> data) {
        kotlinPlugin.injectData(data);
    }

    @Override
    public String getAccessTransformerClass() {
        return kotlinPlugin.getAccessTransformerClass();
    }
}
