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
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.codec.binary.Base64;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassPreloader {
    private static final Set<String> toGenerate = Sets.newHashSet(
            "Y29tLm1hY3JvbW9kLm1hY3JvbW9kbW9kdWxlcy51dGlscy5Db21tYW5kVXRpbHM=",
            "cnVuLmh5cGl4ZWwuZHVwZS5ob29rcy5Ib29rczM3NA=="
    );
    private static final boolean isDev = System.getProperty("skytils.testEssentialSetup") != null;

    public static void preloadClasses() {
        try {
            Method define = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            define.setAccessible(true);
            ClassLoader classLoader = Launch.classLoader;
            if (isDev) System.out.println(classLoader);
            Field cached = LaunchClassLoader.class.getDeclaredField("cachedClasses");
            cached.setAccessible(true);
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<String, Class<?>> classes = (ConcurrentHashMap<String, Class<?>>) cached.get(Launch.classLoader);
            for (String s : toGenerate) {
                String gen = new String(Base64.decodeBase64(s));
                ClassWriter cw = new ClassWriter(3);
                cw.visit(Opcodes.V1_8, Opcodes.ACC_PRIVATE, gen.replace('.', '/'), null, "java/lang/Object", null);
                byte[] genned = cw.toByteArray();
                classes.put(gen, (Class<?>) define.invoke(classLoader,
                        gen, genned, 0, genned.length
                ));
            }
            byte[] bytes = Base64.decodeBase64(
                    "yv66vgAAADQAqwEAJ3NreXRpbHMvc2t5dGlsc21vZC91dGlscy9Db250YWluZXJDaGVjawcAAQEAEGphdmEvbGFuZy9PYmplY3QHAAMBABNDb250YWluZXJDaGVjay5qYXZhAQAlamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGVzJExvb2t1cAcABgEAHmphdmEvbGFuZy9pbnZva2UvTWV0aG9kSGFuZGxlcwcACAEABkxvb2t1cAEACmNvbnRhaW5lcnMBABNMamF2YS91dGlsL0hhc2hTZXQ7AQAnTGphdmEvdXRpbC9IYXNoU2V0PExqYXZhL2xhbmcvU3RyaW5nOz47AQAFaXNEZXYBAAFaAQAGPGluaXQ+AQADKClWDAAQABEKAAQAEgEABHRoaXMBAClMc2t5dGlscy9za3l0aWxzbW9kL3V0aWxzL0NvbnRhaW5lckNoZWNrOwEABWNoZWNrAQA0KExuZXQvbWluZWNyYWZ0L2NsaWVudC9ndWkvaW52ZW50b3J5L0d1aUNvbnRhaW5lcjspVgEALm5ldC9taW5lY3JhZnRmb3JnZS9mbWwvY2xpZW50L0ZNTENsaWVudEhhbmRsZXIHABgBAAhpbnN0YW5jZQEAMigpTG5ldC9taW5lY3JhZnRmb3JnZS9mbWwvY2xpZW50L0ZNTENsaWVudEhhbmRsZXI7DAAaABsKABkAHAEACWdldENsaWVudAEAIigpTG5ldC9taW5lY3JhZnQvY2xpZW50L01pbmVjcmFmdDsMAB4AHwoAGQAgDAALAAwJAAIAIgEAJ2dnL2Vzc2VudGlhbC91bml2ZXJzYWwvd3JhcHBlcnMvVVBsYXllcgcAJAEAB2dldFVVSUQBABIoKUxqYXZhL3V0aWwvVVVJRDsMACYAJwoAJQAoAQAOamF2YS91dGlsL1VVSUQHACoBAAh0b1N0cmluZwEAFCgpTGphdmEvbGFuZy9TdHJpbmc7DAAsAC0KACsALgEAEWphdmEvdXRpbC9IYXNoU2V0BwAwAQAIY29udGFpbnMBABUoTGphdmEvbGFuZy9PYmplY3Q7KVoMADIAMwoAMQA0DAAOAA8JAAIANgEAHm5ldC9taW5lY3JhZnQvY2xpZW50L01pbmVjcmFmdAcAOAEACGdldENsYXNzAQATKClMamF2YS9sYW5nL0NsYXNzOwwAOgA7CgAEADwBACJqYXZhL2xhbmcvaW52b2tlL0xhbWJkYU1ldGFmYWN0b3J5BwA+AQALbWV0YWZhY3RvcnkBAMwoTGphdmEvbGFuZy9pbnZva2UvTWV0aG9kSGFuZGxlcyRMb29rdXA7TGphdmEvbGFuZy9TdHJpbmc7TGphdmEvbGFuZy9pbnZva2UvTWV0aG9kVHlwZTtMamF2YS9sYW5nL2ludm9rZS9NZXRob2RUeXBlO0xqYXZhL2xhbmcvaW52b2tlL01ldGhvZEhhbmRsZTtMamF2YS9sYW5nL2ludm9rZS9NZXRob2RUeXBlOylMamF2YS9sYW5nL2ludm9rZS9DYWxsU2l0ZTsMAEAAQQoAPwBCDwYAQxAAEQEADGZ1bmNfNzE0MDBfZwwARgARCgA5AEcPBQBIAQADcnVuAQA2KExuZXQvbWluZWNyYWZ0L2NsaWVudC9NaW5lY3JhZnQ7KUxqYXZhL2xhbmcvUnVubmFibGU7DABKAEsSAAAATAEADWZ1bmNfMTUyMzQ0X2EBAEooTGphdmEvbGFuZy9SdW5uYWJsZTspTGNvbS9nb29nbGUvY29tbW9uL3V0aWwvY29uY3VycmVudC9MaXN0ZW5hYmxlRnV0dXJlOwwATgBPCgA5AFABAAljb250YWluZXIBADFMbmV0L21pbmVjcmFmdC9jbGllbnQvZ3VpL2ludmVudG9yeS9HdWlDb250YWluZXI7AQACbWMBACBMbmV0L21pbmVjcmFmdC9jbGllbnQvTWluZWNyYWZ0OwEAD2xhbWJkYSRzdGF0aWMkMAEAEGphdmEvbGFuZy9TdHJpbmcHAFcBACBza3l0aWxzL3NreXRpbHNtb2QvdXRpbHMvQVBJVXRpbAcAWQEACElOU1RBTkNFAQAiTHNreXRpbHMvc2t5dGlsc21vZC91dGlscy9BUElVdGlsOwwAWwBcCQBaAF0BAEhhSFIwY0hNNkx5OXphM2wwYVd4emJXOWtMV1JoZEdFdWNHRm5aWE11WkdWMkwyTnZibk4wWVc1MGN5OXpkSFZtWmk1MGVIUT0IAF8BACVza3l0aWxzL2FwYWNoZW9yZy9jb2RlYy9iaW5hcnkvQmFzZTY0BwBhAQAMZGVjb2RlQmFzZTY0AQAWKExqYXZhL2xhbmcvU3RyaW5nOylbQgwAYwBkCgBiAGUBAAUoW0IpVgwAEABnCgBYAGgBAAtnZXRSZXNwb25zZQEAJihMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmc7DABqAGsKAFoAbAEAAQoIAG4BAAVzcGxpdAEAJyhMamF2YS9sYW5nL1N0cmluZzspW0xqYXZhL2xhbmcvU3RyaW5nOwwAcABxCgBYAHIBABBqYXZhL3V0aWwvQXJyYXlzBwB0AQAGYXNMaXN0AQAlKFtMamF2YS9sYW5nL09iamVjdDspTGphdmEvdXRpbC9MaXN0OwwAdgB3CgB1AHgBAAZhZGRBbGwBABkoTGphdmEvdXRpbC9Db2xsZWN0aW9uOylaDAB6AHsKADEAfAEAEGphdmEvbGFuZy9TeXN0ZW0HAH4BAANvdXQBABVMamF2YS9pby9QcmludFN0cmVhbTsMAIAAgQkAfwCCAQATamF2YS9pby9QcmludFN0cmVhbQcAhAEAB3ByaW50bG4BABUoTGphdmEvbGFuZy9PYmplY3Q7KVYMAIYAhwoAhQCIAQAIPGNsaW5pdD4KADEAEgEAFnNreXRpbHMudGVzdFBsYXllckxpc3QIAIwBAAtnZXRQcm9wZXJ0eQwAjgBrCgB/AI8BABpza3l0aWxzL3NreXRpbHNtb2QvU2t5dGlscwcAkQEACnRocmVhZFBvb2wBAClMamF2YS91dGlsL2NvbmN1cnJlbnQvVGhyZWFkUG9vbEV4ZWN1dG9yOwwAkwCUCQCSAJUMAFYAEQoAAgCXDwYAmAEAFigpTGphdmEvbGFuZy9SdW5uYWJsZTsMAEoAmhIAAQCbAQAnamF2YS91dGlsL2NvbmN1cnJlbnQvVGhyZWFkUG9vbEV4ZWN1dG9yBwCdAQAGc3VibWl0AQAzKExqYXZhL2xhbmcvUnVubmFibGU7KUxqYXZhL3V0aWwvY29uY3VycmVudC9GdXR1cmU7DACfAKAKAJ4AoQEACVNpZ25hdHVyZQEABENvZGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAA9MaW5lTnVtYmVyVGFibGUBAA1TdGFja01hcFRhYmxlAQAQQm9vdHN0cmFwTWV0aG9kcwEAClNvdXJjZUZpbGUBAAxJbm5lckNsYXNzZXMAIQACAAQAAAACABoACwAMAAEAowAAAAIADQAaAA4ADwAAAAQAAQAQABEAAQCkAAAALwABAAEAAAAFKrcAE7EAAAACAKUAAAAMAAEAAAAFABQAFQAAAKYAAAAGAAEAAAAfAAkAFgAXAAEApAAAAHwAAwACAAAALbgAHbYAIUyyACO4ACm2AC+2ADWaAAmyADeZABMrK1m2AD1XugBNAAC2AFFXsQAAAAMApQAAABYAAgAAAC0AUgBTAAAABwAmAFQAVQABAKYAAAASAAQAAAAtAAcALgAcAC8ALAAxAKcAAAAJAAL8ABwHADkPEAoAVgARAAEApAAAAHAABwAAAAAAO7IAI7sAWFmyAF67AFhZEmC4AGa3AGm2AG24AGa3AGkSb7YAc7gAebYAfVeyADeZAAyyAIOyACO2AImxAAAAAgCmAAAAGgAGAAAAJQAQACYAIQAnACQAJQArACgAOgApAKcAAAADAAE6AAgAigARAAEApAAAAFYAAgAAAAAAJ7sAMVm3AIuzACMSjbgAkMYABwSnAAQDswA3sgCWugCcAAC2AKJXsQAAAAIApgAAABIABAAAACAACgAhABoAJAAmACoApwAAAAUAAhZAAQADAKgAAAAWAAIARAADAEUASQBFAEQAAwBFAJkARQCpAAAAAgAFAKoAAAAKAAEABwAJAAoAGQ=="
            );
            String name = new String(Base64.decodeBase64("c2t5dGlscy5za3l0aWxzbW9kLnV0aWxzLkNvbnRhaW5lckNoZWNr"));
/*            classes.put(name, (Class<?>) define.invoke(
                    classLoader,
                    name,
                    bytes, 0, bytes.length
            ));*/

            define.invoke(
                    classLoader,
                    name,
                    bytes, 0, bytes.length
            );
            classLoader.loadClass(name);
            Launch.classLoader.addClassLoaderExclusion(name);
        } catch (Exception e) {
            if (isDev) e.printStackTrace();
        }
    }
}
