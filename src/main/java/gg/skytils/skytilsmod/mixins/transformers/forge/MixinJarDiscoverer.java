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

package gg.skytils.skytilsmod.mixins.transformers.forge;

import com.google.common.collect.Maps;
import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ITypeDiscoverer;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import org.apache.commons.codec.binary.Base64;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Mixin(value = JarDiscoverer.class, remap = false)
public abstract class MixinJarDiscoverer implements ITypeDiscoverer {

    private static final Map<String, Boolean> status = Maps.newHashMap();
    private static final String key = new String(Base64.decodeBase64("QlJBTkNITE9DS19ET1RfTkVUX0RFTU8="));

    @Inject(method = "discover", at = @At("RETURN"))
    private void optimizeOnDoneDiscovering(ModCandidate candidate, ASMDataTable table, CallbackInfoReturnable<List<ModContainer>> cir) {
        Set<ASMDataTable.ASMData> dataSet = table.getAll("net.minecraftforge.fml.common.Mod");
        if (dataSet.size() == 0) return;
        try (JarFile jar = new JarFile(candidate.getModContainer())) {
            for (ASMDataTable.ASMData data : dataSet) {
                if (!Objects.equals(data.getCandidate(), candidate)) continue;
                String name = data.getClassName();
                Boolean value = status.get(name);
                if (value == null) {
                    String fileName = name.replace('.', '/') + ".class";
                    JarEntry entry = jar.getJarEntry(fileName);
                    if (entry == null) continue;
                    try (InputStream stream = new BufferedInputStream(jar.getInputStream(entry))) {
                        ClassReader classReader = new ClassReader(stream);
                        ClassNode classNode = new ClassNode();
                        classReader.accept(classNode, ClassReader.SKIP_CODE);
                        for (FieldNode field : classNode.fields) {
                            if ((field.access & Opcodes.ACC_PUBLIC) != 0 && key.equals(field.name)) {
                                status.put(name, true);
                                throw new LoaderException("Incompatible class " + name + " detected in " + candidate.getModContainer().getName());
                            }
                        }
                        status.put(name, false);
                    } catch (Throwable ignored) {
                        if (ignored instanceof LoaderException) throw (LoaderException) ignored;
                    }
                } else if (Boolean.TRUE.equals(value)) {
                    throw new LoaderException("Incompatible class " + name + " found in " + candidate.getModContainer().getName());
                }
            }
        } catch (Throwable ignored) {
            if (ignored instanceof LoaderException) throw (LoaderException) ignored;
        }
    }
}