/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.mixins.transformers.sba;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gg.skytils.skytilsmod.core.Config;
import gg.skytils.skytilsmod.utils.RenderUtil;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "codes.biscuit.skyblockaddons.features.backpacks.ContainerPreviewManager", remap = false)
public class MixinContainerPreviewManager {
    @Dynamic
    @WrapWithCondition(method = "drawContainerPreviews", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;func_180450_b(Lnet/minecraft/item/ItemStack;II)V"))
    private static boolean drawRarityBackground(RenderItem instance, ItemStack itemStack, int x, int y) {
        if (Config.INSTANCE.getShowItemRarity()) {
            RenderUtil.renderRarity(itemStack, x, y);
        }
        return true;
    }
}
