/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.mixins.transformers.patcher;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.skytils.skytilsmod.utils.PatcherCompatability;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.EntityRenderer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "club.sk1er.patcher.screen.render.caching.HUDCaching", remap = false)
public abstract class MixinHUDCaching {
    @Dynamic
    @WrapOperation(method = "renderCachedHud", at = @At(value = "FIELD", target = "Lclub/sk1er/patcher/config/PatcherConfig;hudCaching:Z", opcode = Opcodes.GETSTATIC))
    private static boolean renderCachedHud(Operation<Boolean> original, EntityRenderer renderer, GuiIngame guiIngame) {
        return !PatcherCompatability.INSTANCE.getDisableHUDCaching() && original.call();
    }
}
