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

import gg.essential.loader.stage0.EssentialSetupTweaker;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SkytilsTweaker extends EssentialSetupTweaker {
    @SuppressWarnings("unchecked")
    public SkytilsTweaker() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ArrayList<String> tweakClassNames = (ArrayList<String>) Launch.blackboard.get("TweakClasses");
        ArrayList<ITweaker> tweakers = (ArrayList<ITweaker>) Launch.blackboard.get("Tweaks");
        if (!tweakClassNames.contains("gg.essential.loader.stage0.EssentialSetupTweaker")) {
            LogWrapper.log(Level.INFO, "Skytils injecting Essential tweaker");
            Launch.classLoader.addClassLoaderExclusion("gg.essential.loader.stage0");
            ITweaker tweaker = (ITweaker) Class.forName("gg.essential.loader.stage0.EssentialSetupTweaker", true, Launch.classLoader)
                    .newInstance();
            tweakers.add(tweaker);
        } else {
            LogWrapper.log(Level.INFO, "Skytils found another mod uses Essential tweaker -- will not inject");
        }
        if (Launch.blackboard.get("mixin.initialised") == null) {
            LogWrapper.log(Level.INFO, "Mixin was not loaded yet, Skytils will bootstrap it");
            MixinBootstrap.init();
        } else {
            LogWrapper.log(Level.INFO, "Mixin was already bootstrapped, Skytils is skipping");
        }
    }

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {

    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {

    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return new String[0];
    }
}