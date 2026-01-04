/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.mixins.transformers.renderer.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import gg.skytils.skytilsmod.mixins.hooks.renderer.BlockRendererDispatcherHookKt;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.LevelSlice")
public class MixinLevelSlice {
    @Dynamic
    @ModifyReturnValue(method = "getBlockState(III)Lnet/minecraft/block/BlockState;", at = @At("RETURN"))
    private BlockState modifyBlockState(BlockState original, @Local(argsOnly = true) int blockX, @Local(argsOnly = true) int blockY, @Local(argsOnly = true) int blockZ){
        return BlockRendererDispatcherHookKt.modifyBlockState(new BlockPos(blockX, blockY, blockZ), original);
    }
}
