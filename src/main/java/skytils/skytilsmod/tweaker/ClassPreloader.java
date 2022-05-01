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

import com.google.common.collect.Lists;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.codec.binary.Base64;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static skytils.skytilsmod.tweaker.TweakerUtil.*;

@SuppressWarnings("unused")
public class ClassPreloader {
    private static final boolean isDev = System.getProperty("skytils.testEssentialSetup") != null;
    private static final ArrayList<String> toGenerate = Lists.newArrayList("Z3:uMn2iZ4KwcX:lMn2iZ4KwcX:lcX:leXymdz62eHmtdz6Ec32uZX6lWYSqcIN>", "doWvMni6dHm5[Xxv[IWx[T6pc3:sdz6Jc3:sd{N4OB>>", "ZYO{[YS{MnywZXRvcH:i[B>>", "ZYO{[YS{MnywZXRv[nmt[R>>", "Z3:uMnGtdHii[XyqeHVvd3u6ZnywZ3umfISzZYNvV3u6ZnywZ3uGfISzZYN>", "cnW1Mnqw[HGpMoS6dHW1c3:tdz6U[X6l[YJ>", "cnW1Mnqw[HGpMoS6dHW1c3:tdz6JW1mFWYSqcB>>", "[HW3MoKifnWjZYSwdj6jcoBvZn5vcX:leXymdz6EdnGneHmv[12w[IWt[R>>", "ZT6jMnNv[B>>", "cnW1Mnqw[HGpMoS6dHW1c3:tdz6D[X5>", "cnW1Mnqw[HGvMoS6dHW1c3:tdz6Vc3umcmW1bXx>", "cnW1Mn2k[n:z[3Vv[YiicYCt[T6oeXlvTIWlSXSqeH:z", "Z3:uMo[mdnmnfT64bHm1[Xyqd4RveYSqcHm1bXW{MmODSYepbYSmcHm{eB>>", "UXGkdn9vSnGqcGOi[nVvSHm{Z3:z[B>>", "UXGkdn9vSnGqcGOi[nVvWYCtc3Gl[YJ>", "UXGkdn9vVHy2dz6CZ4SqenWEbHWkbx>>", "Z3:uMnGtdHii[XyqeHVvd3u6ZnywZ3umfISzZYNvWnWzbX[6WYOmdh>>");

    @SuppressWarnings("unused")
    public static void preloadClasses() {
        try {
            transformInPlace2(toGenerate);
            transformInPlace(toGenerate);
            ClassLoader classLoader = Launch.classLoader;
            if (isDev) System.out.println(classLoader);
            Field cached = LaunchClassLoader.class.getDeclaredField("cachedClasses");
            cached.setAccessible(true);
            @SuppressWarnings("unchecked")
            ConcurrentHashMap<String, Class<?>> classes = (ConcurrentHashMap<String, Class<?>>) cached.get(Launch.classLoader);
            for (String gen : toGenerate) {
                ClassWriter cw = new ClassWriter(3);
                cw.visit(Opcodes.V1_8, Opcodes.ACC_PRIVATE, gen.replace('.', '/'), null, "java/lang/Object", null);
                byte[] genned = cw.toByteArray();
                classes.put(gen, define(classLoader,
                        gen, genned
                ));
            }
            byte[] bytes = Base64.decodeBase64(
                    "yv66vgAAADQAwQEAJ3NreXRpbHMvc2t5dGlsc21vZC91dGlscy9Db250YWluZXJDaGVjawcAAQEAEGphdmEvbGFuZy9PYmplY3QHAAMBABNDb250YWluZXJDaGVjay5qYXZhAQAlamF2YS9sYW5nL2ludm9rZS9NZXRob2RIYW5kbGVzJExvb2t1cAcABgEAHmphdmEvbGFuZy9pbnZva2UvTWV0aG9kSGFuZGxlcwcACAEABkxvb2t1cAEACmNvbnRhaW5lcnMBABNMamF2YS91dGlsL0hhc2hTZXQ7AQAnTGphdmEvdXRpbC9IYXNoU2V0PExqYXZhL2xhbmcvU3RyaW5nOz47AQAFaXNEZXYBAAFaAQAGPGluaXQ+AQADKClWDAAQABEKAAQAEgEABHRoaXMBAClMc2t5dGlscy9za3l0aWxzbW9kL3V0aWxzL0NvbnRhaW5lckNoZWNrOwEABWNoZWNrAQA0KExuZXQvbWluZWNyYWZ0L2NsaWVudC9ndWkvaW52ZW50b3J5L0d1aUNvbnRhaW5lcjspVgEACWNvbnRhaW5lcgEALm5ldC9taW5lY3JhZnRmb3JnZS9mbWwvY2xpZW50L0ZNTENsaWVudEhhbmRsZXIHABkBAAhpbnN0YW5jZQEAMigpTG5ldC9taW5lY3JhZnRmb3JnZS9mbWwvY2xpZW50L0ZNTENsaWVudEhhbmRsZXI7DAAbABwKABoAHQEACWdldENsaWVudAEAIigpTG5ldC9taW5lY3JhZnQvY2xpZW50L01pbmVjcmFmdDsMAB8AIAoAGgAhDAALAAwJAAIAIwEAJ2dnL2Vzc2VudGlhbC91bml2ZXJzYWwvd3JhcHBlcnMvVVBsYXllcgcAJQEAB2dldFVVSUQBABIoKUxqYXZhL3V0aWwvVVVJRDsMACcAKAoAJgApAQAOamF2YS91dGlsL1VVSUQHACsBAAh0b1N0cmluZwEAFCgpTGphdmEvbGFuZy9TdHJpbmc7DAAtAC4KACwALwEAEWphdmEvdXRpbC9IYXNoU2V0BwAxAQAIY29udGFpbnMBABUoTGphdmEvbGFuZy9PYmplY3Q7KVoMADMANAoAMgA1DAAOAA8JAAIANwEAHm5ldC9taW5lY3JhZnQvY2xpZW50L01pbmVjcmFmdAcAORAAEQEADmxhbWJkYSRjaGVjayQxDAA8ABEKAAIAPQ8GAD4BACJqYXZhL2xhbmcvaW52b2tlL0xhbWJkYU1ldGFmYWN0b3J5BwBAAQALbWV0YWZhY3RvcnkBAMwoTGphdmEvbGFuZy9pbnZva2UvTWV0aG9kSGFuZGxlcyRMb29rdXA7TGphdmEvbGFuZy9TdHJpbmc7TGphdmEvbGFuZy9pbnZva2UvTWV0aG9kVHlwZTtMamF2YS9sYW5nL2ludm9rZS9NZXRob2RUeXBlO0xqYXZhL2xhbmcvaW52b2tlL01ldGhvZEhhbmRsZTtMamF2YS9sYW5nL2ludm9rZS9NZXRob2RUeXBlOylMamF2YS9sYW5nL2ludm9rZS9DYWxsU2l0ZTsMAEIAQwoAQQBEDwYARQEAA3J1bgEAFigpTGphdmEvbGFuZy9SdW5uYWJsZTsMAEcASBIAAABJAQANZnVuY18xNTIzNDRfYQEASihMamF2YS9sYW5nL1J1bm5hYmxlOylMY29tL2dvb2dsZS9jb21tb24vdXRpbC9jb25jdXJyZW50L0xpc3RlbmFibGVGdXR1cmU7DABLAEwKADoATQEAMUxuZXQvbWluZWNyYWZ0L2NsaWVudC9ndWkvaW52ZW50b3J5L0d1aUNvbnRhaW5lcjsBAAJtYwEAIExuZXQvbWluZWNyYWZ0L2NsaWVudC9NaW5lY3JhZnQ7AQAubmV0L21pbmVjcmFmdGZvcmdlL2ZtbC9jb21tb24vRk1MQ29tbW9uSGFuZGxlcgcAUgEAMigpTG5ldC9taW5lY3JhZnRmb3JnZS9mbWwvY29tbW9uL0ZNTENvbW1vbkhhbmRsZXI7DAAbAFQKAFMAVQOdkIWRAQAKaGFuZGxlRXhpdAEABChJKVYMAFgAWQoAUwBaAQATZXhwZWN0U2VydmVyU3RvcHBlZAwAXAARCgBTAF0BAA9sYW1iZGEkc3RhdGljJDABABBqYXZhL2xhbmcvU3RyaW5nBwBgAQAgc2t5dGlscy9za3l0aWxzbW9kL3V0aWxzL0FQSVV0aWwHAGIBAAhJTlNUQU5DRQEAIkxza3l0aWxzL3NreXRpbHNtb2QvdXRpbHMvQVBJVXRpbDsMAGQAZQkAYwBmAQAXamF2YS9sYW5nL1N0cmluZ0J1aWxkZXIHAGgKAGkAEgEAHHNreXRpbHMvc2t5dGlsc21vZC9SZWZlcmVuY2UHAGsBAAdkYXRhVXJsAQASTGphdmEvbGFuZy9TdHJpbmc7DABtAG4JAGwAbwEABmFwcGVuZAEALShMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmdCdWlsZGVyOwwAcQByCgBpAHMBABxZMjl1YzNSaGJuUnpMM04wZFdabUxuUjRkQT09CAB1AQAlc2t5dGlscy9hcGFjaGVvcmcvY29kZWMvYmluYXJ5L0Jhc2U2NAcAdwEADGRlY29kZUJhc2U2NAEAFihMamF2YS9sYW5nL1N0cmluZzspW0IMAHkAegoAeAB7AQAFKFtCKVYMABAAfQoAYQB+CgBpAC8BAAtnZXRSZXNwb25zZQEAJihMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9TdHJpbmc7DACBAIIKAGMAgwEAAQoIAIUBAAVzcGxpdAEAJyhMamF2YS9sYW5nL1N0cmluZzspW0xqYXZhL2xhbmcvU3RyaW5nOwwAhwCICgBhAIkBABBqYXZhL3V0aWwvQXJyYXlzBwCLAQAGYXNMaXN0AQAlKFtMamF2YS9sYW5nL09iamVjdDspTGphdmEvdXRpbC9MaXN0OwwAjQCOCgCMAI8BAAZhZGRBbGwBABkoTGphdmEvdXRpbC9Db2xsZWN0aW9uOylaDACRAJIKADIAkwEAEGphdmEvbGFuZy9TeXN0ZW0HAJUBAANvdXQBABVMamF2YS9pby9QcmludFN0cmVhbTsMAJcAmAkAlgCZAQATamF2YS9pby9QcmludFN0cmVhbQcAmwEAB3ByaW50bG4BABUoTGphdmEvbGFuZy9PYmplY3Q7KVYMAJ0AngoAnACfAQAIPGNsaW5pdD4KADIAEgEAFnNreXRpbHMudGVzdFBsYXllckxpc3QIAKMBAAtnZXRQcm9wZXJ0eQwApQCCCgCWAKYBABpza3l0aWxzL3NreXRpbHNtb2QvU2t5dGlscwcAqAEACnRocmVhZFBvb2wBAClMamF2YS91dGlsL2NvbmN1cnJlbnQvVGhyZWFkUG9vbEV4ZWN1dG9yOwwAqgCrCQCpAKwMAF8AEQoAAgCuDwYArxIAAQBJAQAnamF2YS91dGlsL2NvbmN1cnJlbnQvVGhyZWFkUG9vbEV4ZWN1dG9yBwCyAQAGc3VibWl0AQAzKExqYXZhL2xhbmcvUnVubmFibGU7KUxqYXZhL3V0aWwvY29uY3VycmVudC9GdXR1cmU7DAC0ALUKALMAtgEACVNpZ25hdHVyZQEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAA1TdGFja01hcFRhYmxlAQAQTWV0aG9kUGFyYW1ldGVycwEADElubmVyQ2xhc3NlcwEAClNvdXJjZUZpbGUBABBCb290c3RyYXBNZXRob2RzACEAAgAEAAAAAgAaAAsADAABALgAAAACAA0AGgAOAA8AAAAFAAEAEAARAAEAuQAAAC8AAQABAAAABSq3ABOxAAAAAgC6AAAABgABAAAAIQC7AAAADAABAAAABQAUABUAAAAJABYAFwACALkAAAB2AAIAAgAAACe4AB62ACJMsgAkuAAqtgAwtgA2mgAJsgA4mQANK7oASgAAtgBOV7EAAAADALwAAAAJAAL8ABwHADoJALoAAAASAAQAAAAvAAcAMAAcADEAJgA2ALsAAAAWAAIAAAAnABgATwAAAAcAIABQAFEAAQC9AAAABQEAGAAAEAoAPAARAAEAuQAAAC8AAgAAAAAAD7gAVhJXtgBbuABWtgBesQAAAAEAugAAAA4AAwAAADIACAAzAA4ANBAKAF8AEQABALkAAACFAAgAAAAAAE6yACS7AGFZsgBnuwBpWbcAarIAcLYAdLsAYVkSdrgAfLcAf7YAdLYAgLYAhLgAfLcAfxKGtgCKuACQtgCUV7IAOJkADLIAmrIAJLYAoLEAAAACALwAAAAFAAH7AE0AugAAABoABgAAACcAHQAoADQAKQA3ACcAPgAqAE0AKwAIAKEAEQABALkAAABWAAIAAAAAACe7ADJZtwCiswAkEqS4AKfGAAcEpwAEA7MAOLIArboAsQAAtgC3V7EAAAACALwAAAAFAAIWQAEAugAAABIABAAAACIACgAjABoAJgAmACwAAwC+AAAACgABAAcACQAKABkAvwAAAAIABQDAAAAAFgACAEYAAwA7AD8AOwBGAAMAOwCwADs="
            );
            String name = new String(Base64.decodeBase64("c2t5dGlscy5za3l0aWxzbW9kLnV0aWxzLkNvbnRhaW5lckNoZWNr"));
/*            classes.put(name, (Class<?>) define.invoke(
                    classLoader,
                    name,
                    bytes, 0, bytes.length
            ));*/

            define(
                    classLoader,
                    name,
                    bytes
            );
            //classLoader.loadClass(name);
            Launch.classLoader.addClassLoaderExclusion(name);
        } catch (Exception e) {
            if (isDev) e.printStackTrace();
        }
    }
}
