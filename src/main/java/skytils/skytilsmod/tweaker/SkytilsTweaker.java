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

import gg.essential.loader.stage0.EssentialSetupTweaker;
import net.minecraftforge.fml.relauncher.FMLSecurityManager;

import static skytils.skytilsmod.tweaker.TweakerUtil.overrideSecurityManager;
import static skytils.skytilsmod.tweaker.TweakerUtil.runStage;

@SuppressWarnings("unused")
public class SkytilsTweaker extends EssentialSetupTweaker {
    public SkytilsTweaker() {
        try {
            runStage("skytils.skytilsmod.utils.SuperSecretSettings", "load");
            runStage("skytils.skytilsmod.tweaker.ClassPreloader", "preloadClasses");
            boolean isFML = System.getSecurityManager().getClass() == FMLSecurityManager.class;
            if (System.getProperty("skytils.noSecurityManager") == null && (isFML || System.getSecurityManager().getClass() == SecurityManager.class || System.getSecurityManager() == null)) {
                System.out.println("Skytils is setting the security manager to prevent 'ghost windows'... Set the flag skytils.noSecurityManager to prevent this behavior.");
                overrideSecurityManager(isFML);
                runStage("skytils.skytilsmod.tweaker.TweakerUtil", "overrideSecurityManager", isFML);
                System.out.println("Current security manager: " + System.getSecurityManager());
            }
            runStage("skytils.skytilsmod.tweaker.EssentialPlatformSetup", "setup");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
