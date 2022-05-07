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

package gg.skytils.skytilsmod.tweaker;

import gg.essential.loader.stage0.EssentialSetupTweaker;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class SkytilsTweaker extends EssentialSetupTweaker {
    private final ITweaker delegateTweaker;

    public SkytilsTweaker() {
        try {
            ClassLoader cl = getClass().getClassLoader();
            LaunchClassLoader lcl = Launch.classLoader;
            URL loaderLoc = cl.getResource("assets/skytils/loader/loader-dev.jar");
            boolean isDev;
            if (loaderLoc != null) {
                System.out.println("Skytils is running in a development environment.");
                isDev = true;
            } else {
                // shadow explodes jar files
                Path systemLoc = Paths.get("./assets/skytils/loader/loader.jar");
                Files.createDirectories(systemLoc.getParent());
                try (InputStream is = cl.getResourceAsStream("assets/skytils/loader/loader.notjar")) {
                    Objects.requireNonNull(is, "Resource stream is null");
                    Files.copy(is, systemLoc, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                loaderLoc = systemLoc.toUri().toURL();
                isDev = false;
            }
            Objects.requireNonNull(loaderLoc, "Loader was not found");

            addURL(lcl.getClass().getClassLoader(), loaderLoc);
            lcl.addClassLoaderExclusion("gg.skytils.skytilsmod.loader.");
            lcl.addTransformerExclusion("gg.skytils.skytilsmod.loader.");
            lcl.addURL(loaderLoc);
            delegateTweaker = (ITweaker)
                    Class.forName("gg.skytils.skytilsmod.loader.SkytilsLoader", true, lcl)
                            .getDeclaredConstructor(boolean.class)
                            .newInstance(isDev);
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to initialize loader", e);
        }
    }

    private void addURL(ClassLoader cl, URL url) throws ReflectiveOperationException {
        Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        m.setAccessible(true);
        m.invoke(cl, url);
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        delegateTweaker.acceptOptions(args, gameDir, assetsDir, profile);
        super.acceptOptions(args, gameDir, assetsDir, profile);
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        delegateTweaker.injectIntoClassLoader(classLoader);
        super.injectIntoClassLoader(classLoader);
    }

    @Override
    public String getLaunchTarget() {
        return super.getLaunchTarget();
    }

    @Override
    public String[] getLaunchArguments() {
        return super.getLaunchArguments();
    }
}
