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

import net.minecraft.launchwrapper.Launch;
import sun.security.util.SecurityConstants;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

class TweakerUtil {
    static void runStage(String className, String methodName, Object... params) throws ReflectiveOperationException {
        getClassForLaunch(className, true).getDeclaredMethod(methodName).invoke(null, params);
    }

    static Class<?> getClassForLaunch(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, Launch.classLoader);
    }

    static Field findField(final Class<?> clazz, final String name) throws ReflectiveOperationException {
        Method m = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        m.setAccessible(true);
        Field[] fields = (Field[]) m.invoke(System.class, false);
        Method m2 = Class.class.getDeclaredMethod("searchFields", Field[].class, String.class);
        m2.setAccessible(true);

        return (Field) m2.invoke(clazz, fields, name);
    }

    static Class<?> define(ClassLoader classLoader, String name, byte[] bytes) throws ReflectiveOperationException {
        Method define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        define.setAccessible(true);
        return (Class<?>) define.invoke(classLoader, name, bytes, 0, bytes.length);
    }

    static void registerTransformerExclusions(String... classes) {
        for (String className : classes) {
            Launch.classLoader.addTransformerExclusion(className);
        }
    }

    static void overrideSecurityManager(boolean isForge) {
        try {
            SecurityManager s = new SkytilsSecurityManager(isForge);

            if (s.getClass().getClassLoader() != null) {
                AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                    s.getClass().getProtectionDomain().implies
                            (SecurityConstants.ALL_PERMISSION);
                    return null;
                });
            }

            Field field = TweakerUtil.findField(System.class, "security");
            field.setAccessible(true);
            field.set(null, s);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
