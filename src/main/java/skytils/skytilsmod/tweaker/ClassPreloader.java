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

import com.google.common.collect.Sets;
import org.apache.commons.codec.binary.Base64;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Method;
import java.util.Set;

public class ClassPreloader {
    private static final Set<String> toGenerate = Sets.newHashSet(
            "Y29tLm1hY3JvbW9kLm1hY3JvbW9kbW9kdWxlcy51dGlscy5Db21tYW5kVXRpbHM=",
            "cnVuLmh5cGl4ZWwuZHVwZS5ob29rcy5Ib29rczM3NA=="
    );
    private static boolean isDev = System.getProperty("skytils.testEssentialSetup") != null;

    public static void preloadClasses() {
        try {
            Method define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            define.setAccessible(true);
            for (String s : toGenerate) {
                String gen = new String(Base64.decodeBase64(s));
                ClassNode cn = new ClassNode();
                cn.visit(Opcodes.V1_8, Opcodes.ACC_PRIVATE, gen.replace('.', '/'), null, "java/lang/Object", null);
                ClassWriter cw = new ClassWriter(3);
                cn.accept(cw);
                byte[] genned = cw.toByteArray();
                define.invoke(ClassPreloader.class.getClassLoader(),
                        gen, genned, 0, genned.length
                );
            }
        } catch (Exception e) {
            if (isDev) e.printStackTrace();
        }
    }
}
