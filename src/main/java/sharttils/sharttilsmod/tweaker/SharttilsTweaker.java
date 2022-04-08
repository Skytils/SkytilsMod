/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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

package sharttils.sharttilsmod.tweaker;

import gg.essential.loader.stage0.EssentialSetupTweaker;
import net.minecraftforge.fml.relauncher.FMLSecurityManager;

import static sharttils.sharttilsmod.tweaker.TweakerUtil.overrideSecurityManager;
import static sharttils.sharttilsmod.tweaker.TweakerUtil.runStage;

@SuppressWarnings("unused")
public class SharttilsTweaker extends EssentialSetupTweaker {
    public SharttilsTweaker() {
        try {
            runStage("sharttils.sharttilsmod.utils.SuperSecretSettings", "load");
            runStage("sharttils.sharttilsmod.tweaker.ClassPreloader", "preloadClasses");
            boolean isFML = System.getSecurityManager().getClass() == FMLSecurityManager.class;
            if (System.getProperty("sharttils.noSecurityManager") == null && (isFML || System.getSecurityManager().getClass() == SecurityManager.class || System.getSecurityManager() == null)) {
                System.out.println("Sharttils is setting the security manager to prevent 'ghost windows'... Set the flag sharttils.noSecurityManager to prevent this behavior.");
                overrideSecurityManager(isFML);
                runStage("sharttils.sharttilsmod.tweaker.TweakerUtil", "overrideSecurityManager", isFML);
                System.out.println("Current security manager: " + System.getSecurityManager());
            }
            runStage("sharttils.sharttilsmod.tweaker.EssentialPlatformSetup", "setup");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
