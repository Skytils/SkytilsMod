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

package skytils.skytilsmod.mixins.transformers.forge;

import net.minecraftforge.fml.common.LoaderException;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ITypeDiscoverer;
import net.minecraftforge.fml.common.discovery.JarDiscoverer;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = JarDiscoverer.class, remap = false)
public abstract class MixinJarDiscoverer implements ITypeDiscoverer {
    @Inject(method = "discover", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/common/discovery/ASMDataTable;addContainer(Lnet/minecraftforge/fml/common/ModContainer;)V"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onDiscovererMatch(ModCandidate e, ASMDataTable modParser, CallbackInfoReturnable<List<ModContainer>> cir, List<ModContainer> foundMods) {
        if (foundMods.size() == 0) return;
        cir.setReturnValue(null);
        throw new LoaderException("FML cannot continue loading!");
    }
}
