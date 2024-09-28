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

import gg.essential.loader.stage0.EssentialSetupTweaker;
import net.hypixel.modapi.tweaker.HypixelModAPITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.FMLSecurityManager;

import java.io.File;
import java.util.List;

import static gg.skytils.skytilsmod.tweaker.SkytilsSecurityManager.overrideSecurityManager;
import static gg.skytils.skytilsmod.tweaker.TweakerUtil.registerTransformerExclusions;
import static gg.skytils.skytilsmod.tweaker.TweakerUtil.runStage;

@SuppressWarnings("unused")
public class SkytilsTweaker extends EssentialSetupTweaker {

    public SkytilsTweaker() throws Throwable {
        DuplicateSkytilsChecker.checkForDuplicates();
        runStage("gg.skytils.skytilsmod.utils.SuperSecretSettings", "load");
        boolean isFML = System.getSecurityManager() != null && System.getSecurityManager().getClass().equals(FMLSecurityManager.class);
        if (System.getProperty("skytils.noSecurityManager") == null && (System.getSecurityManager() == null || isFML || System.getSecurityManager().getClass() == SecurityManager.class)) {
            System.out.println("Skytils is setting the security manager to prevent 'ghost windows'... Set the flag skytils.noSecurityManager to prevent this behavior.");
            overrideSecurityManager(isFML);
            runStage("gg.skytils.skytilsmod.tweaker.SkytilsSecurityManager", "overrideSecurityManager", isFML);
            System.out.println("Current security manager: " + System.getSecurityManager());
        }
        registerTransformerExclusions(
                "kotlin.",
                "kotlinx.",
                "gg.skytils.asmhelper.",
                "gg.skytils.skytilsmod.tweaker.",
                "gg.skytils.skytilsmod.asm."
        );
        DependencyLoader.loadDependencies();
        runStage("gg.skytils.skytilsmod.utils.EssentialPlatformSetup", "setup");
    }


    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        super.acceptOptions(args, gameDir, assetsDir, profile);
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        super.injectIntoClassLoader(classLoader);

        @SuppressWarnings("unchecked")
        List<String> tweakClassNames = (List<String>) Launch.blackboard.get("TweakClasses");
        tweakClassNames.add(HypixelModAPITweaker.class.getName());
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
