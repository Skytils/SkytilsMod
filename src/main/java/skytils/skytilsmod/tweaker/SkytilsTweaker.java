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
import sun.security.util.SecurityConstants;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

@SuppressWarnings("unused")
public class SkytilsTweaker extends EssentialSetupTweaker {
    public SkytilsTweaker() {
        if (System.getProperty("skytils.noSecurityManager") == null && (System.getSecurityManager().getClass() == SecurityManager.class || System.getSecurityManager().getClass() == FMLSecurityManager.class)) {
            System.out.println("Skytils is setting the security manager... Set the flag skytils.noSecurityManager to prevent this behavior.");
            overrideSecurityManager(System.getSecurityManager().getClass() == FMLSecurityManager.class);
            System.out.println("Current security manager: " + System.getSecurityManager());
        }
        EssentialPlatformSetup.setup();
    }

    // Bypass the FML security manager in order to set our own
    private void overrideSecurityManager(boolean isForge) {
        try {
            SecurityManager s = new SkytilsSecurityManager(isForge);

            if (s.getClass().getClassLoader() != null) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    s.getClass().getProtectionDomain().implies
                            (SecurityConstants.ALL_PERMISSION);
                    return null;
                });
            }

            Method m = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            m.setAccessible(true);
            Field[] fields = (Field[]) m.invoke(System.class, false);
            Method m2 = Class.class.getDeclaredMethod("searchFields", Field[].class, String.class);
            m2.setAccessible(true);

            Field field = (Field) m2.invoke(System.class, fields, "security");
            field.setAccessible(true);
            field.set(null, s);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
