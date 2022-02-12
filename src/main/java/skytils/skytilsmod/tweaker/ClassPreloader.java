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
                    "yv66vgAAADQAqQEAJ3NreXRpbHMvc2t5dGlsc21vZC91dGlscy9Db250YWluZXJDaGVjawcAAQEAEGphdmEvbGFuZy9PYmplY3QHAAMBABNDb250YWluZXJDaGVjay5qYXZhAQAlamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGVzJExvb2t1cAcABgEAHmphdmEvbGFuZy9pbnZva2UvTWV0aG9kSGFuZGxlcwcACAEABkxvb2t1cAEACmNvbnRhaW5lcnMBABNMamF2YS91dGlsL0hhc2hTZXQ7AQAnTGphdmEvdXRpbC9IYXNoU2V0PExqYXZhL2xhbmcvU3RyaW5nOz47AQAFaXNEZXYBAAFaAQAGPGluaXQ+AQADKClWDAAQABEKAAQAEgEABHRoaXMBAClMc2t5dGlscy9za3l0aWxzbW9kL3V0aWxzL0NvbnRhaW5lckNoZWNrOwEABWNoZWNrAQA0KExuZXQvbWluZWNyYWZ0L2NsaWVudC9ndWkvaW52ZW50b3J5L0d1aUNvbnRhaW5lcjspVgEAHm5ldC9taW5lY3JhZnQvY2xpZW50L01pbmVjcmFmdAcAGAEADGZ1bmNfNzE0MTBfeAEAIigpTG5ldC9taW5lY3JhZnQvY2xpZW50L01pbmVjcmFmdDsMABoAGwoAGQAcDAALAAwJAAIAHgEADWZpZWxkXzcxNDM5X2cBACxMbmV0L21pbmVjcmFmdC9jbGllbnQvZW50aXR5L0VudGl0eVBsYXllclNQOwwAIAAhCQAZACIBACpuZXQvbWluZWNyYWZ0L2NsaWVudC9lbnRpdHkvRW50aXR5UGxheWVyU1AHACQBAA5mdW5jXzExMDEyNF9hdQEAEigpTGphdmEvdXRpbC9VVUlEOwwAJgAnCgAlACgBAA5qYXZhL3V0aWwvVVVJRAcAKgEACHRvU3RyaW5nAQAUKClMamF2YS9sYW5nL1N0cmluZzsMACwALQoAKwAuAQARamF2YS91dGlsL0hhc2hTZXQHADABAAhjb250YWlucwEAFShMamF2YS9sYW5nL09iamVjdDspWgwAMgAzCgAxADQMAA4ADwkAAgA2AQAIZ2V0Q2xhc3MBABMoKUxqYXZhL2xhbmcvQ2xhc3M7DAA4ADkKAAQAOgEAImphdmEvbGFuZy9pbnZva2UvTGFtYmRhTWV0YWZhY3RvcnkHADwBAAttZXRhZmFjdG9yeQEAzChMamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGVzJExvb2t1cDtMamF2YS9sYW5nL1N0cmluZztMamF2YS9sYW5nL2ludm9rZS9NZXRob2RUeXBlO0xqYXZhL2xhbmcvaW52b2tlL01ldGhvZFR5cGU7TGphdmEvbGFuZy9pbnZva2UvTWV0aG9kSGFuZGxlO0xqYXZhL2xhbmcvaW52b2tlL01ldGhvZFR5cGU7KUxqYXZhL2xhbmcvaW52b2tlL0NhbGxTaXRlOwwAPgA/CgA9AEAPBgBBEAARAQAMZnVuY183MTQwMF9nDABEABEKABkARQ8FAEYBAANydW4BADYoTG5ldC9taW5lY3JhZnQvY2xpZW50L01pbmVjcmFmdDspTGphdmEvbGFuZy9SdW5uYWJsZTsMAEgASRIAAABKAQANZnVuY18xNTIzNDRfYQEASihMamF2YS9sYW5nL1J1bm5hYmxlOylMY29tL2dvb2dsZS9jb21tb24vdXRpbC9jb25jdXJyZW50L0xpc3RlbmFibGVGdXR1cmU7DABMAE0KABkATgEACWNvbnRhaW5lcgEAMUxuZXQvbWluZWNyYWZ0L2NsaWVudC9ndWkvaW52ZW50b3J5L0d1aUNvbnRhaW5lcjsBAAJtYwEAIExuZXQvbWluZWNyYWZ0L2NsaWVudC9NaW5lY3JhZnQ7AQAPbGFtYmRhJHN0YXRpYyQwAQAQamF2YS9sYW5nL1N0cmluZwcAVQEAIHNreXRpbHMvc2t5dGlsc21vZC91dGlscy9BUElVdGlsBwBXAQAISU5TVEFOQ0UBACJMc2t5dGlscy9za3l0aWxzbW9kL3V0aWxzL0FQSVV0aWw7DABZAFoJAFgAWwEASGFIUjBjSE02THk5emEzbDBhV3h6Ylc5a0xXUmhkR0V1Y0dGblpYTXVaR1YyTDJOdmJuTjBZVzUwY3k5emRIVm1aaTUwZUhRPQgAXQEAJXNreXRpbHMvYXBhY2hlb3JnL2NvZGVjL2JpbmFyeS9CYXNlNjQHAF8BAAxkZWNvZGVCYXNlNjQBABYoTGphdmEvbGFuZy9TdHJpbmc7KVtCDABhAGIKAGAAYwEABShbQilWDAAQAGUKAFYAZgEAC2dldFJlc3BvbnNlAQAmKExqYXZhL2xhbmcvU3RyaW5nOylMamF2YS9sYW5nL1N0cmluZzsMAGgAaQoAWABqAQABCggAbAEABXNwbGl0AQAnKExqYXZhL2xhbmcvU3RyaW5nOylbTGphdmEvbGFuZy9TdHJpbmc7DABuAG8KAFYAcAEAEGphdmEvdXRpbC9BcnJheXMHAHIBAAZhc0xpc3QBACUoW0xqYXZhL2xhbmcvT2JqZWN0OylMamF2YS91dGlsL0xpc3Q7DAB0AHUKAHMAdgEABmFkZEFsbAEAGShMamF2YS91dGlsL0NvbGxlY3Rpb247KVoMAHgAeQoAMQB6AQAQamF2YS9sYW5nL1N5c3RlbQcAfAEAA291dAEAFUxqYXZhL2lvL1ByaW50U3RyZWFtOwwAfgB/CQB9AIABABNqYXZhL2lvL1ByaW50U3RyZWFtBwCCAQAHcHJpbnRsbgEAFShMamF2YS9sYW5nL09iamVjdDspVgwAhACFCgCDAIYBAAg8Y2xpbml0PgoAMQASAQAWc2t5dGlscy50ZXN0UGxheWVyTGlzdAgAigEAC2dldFByb3BlcnR5DACMAGkKAH0AjQEAGnNreXRpbHMvc2t5dGlsc21vZC9Ta3l0aWxzBwCPAQAKdGhyZWFkUG9vbAEAKUxqYXZhL3V0aWwvY29uY3VycmVudC9UaHJlYWRQb29sRXhlY3V0b3I7DACRAJIJAJAAkwwAVAARCgACAJUPBgCWAQAWKClMamF2YS9sYW5nL1J1bm5hYmxlOwwASACYEgABAJkBACdqYXZhL3V0aWwvY29uY3VycmVudC9UaHJlYWRQb29sRXhlY3V0b3IHAJsBAAZzdWJtaXQBADMoTGphdmEvbGFuZy9SdW5uYWJsZTspTGphdmEvdXRpbC9jb25jdXJyZW50L0Z1dHVyZTsMAJ0AngoAnACfAQAJU2lnbmF0dXJlAQAEQ29kZQEAEkxvY2FsVmFyaWFibGVUYWJsZQEAD0xpbmVOdW1iZXJUYWJsZQEADVN0YWNrTWFwVGFibGUBABBCb290c3RyYXBNZXRob2RzAQAKU291cmNlRmlsZQEADElubmVyQ2xhc3NlcwAhAAIABAAAAAIAGgALAAwAAQChAAAAAgANABoADgAPAAAABAABABAAEQABAKIAAAAvAAEAAQAAAAUqtwATsQAAAAIAowAAAAwAAQAAAAUAFAAVAAAApAAAAAYAAQAAAB0ACQAWABcAAQCiAAAAfQADAAIAAAAuuAAdTLIAHyu0ACO2ACm2AC+2ADWaAAmyADeZABMrK1m2ADtXugBLAAC2AE9XsQAAAAMAowAAABYAAgAAAC4AUABRAAAABAAqAFIAUwABAKQAAAASAAQAAAArAAQALAAdAC0ALQAvAKUAAAAJAAL8AB0HABkPEAoAVAARAAEAogAAAHAABwAAAAAAO7IAH7sAVlmyAFy7AFZZEl64AGS3AGe2AGu4AGS3AGcSbbYAcbgAd7YAe1eyADeZAAyyAIGyAB+2AIexAAAAAgCkAAAAGgAGAAAAIwAQACQAIQAlACQAIwArACYAOgAnAKUAAAADAAE6AAgAiAARAAEAogAAAFYAAgAAAAAAJ7sAMVm3AImzAB8Si7gAjsYABwSnAAQDswA3sgCUugCaAAC2AKBXsQAAAAIApAAAABIABAAAAB4ACgAfABoAIgAmACgApQAAAAUAAhZAAQADAKYAAAAWAAIAQgADAEMARwBDAEIAAwBDAJcAQwCnAAAAAgAFAKgAAAAKAAEABwAJAAoAGQ=="
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
