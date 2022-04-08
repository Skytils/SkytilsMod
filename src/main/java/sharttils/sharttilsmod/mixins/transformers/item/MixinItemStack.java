/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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

package sharttils.sharttilsmod.mixins.transformers.item;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sharttils.sharttilsmod.mixins.hooks.item.ItemStackHookKt;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
    private void showEnchantmentGlint(CallbackInfoReturnable<Boolean> cir) {
        ItemStackHookKt.showEnchantmentGlint(this, cir);
    }

    @ModifyVariable(method = "getDisplayName", at = @At(value = "STORE"))
    private String modifyDisplayName(String s) {
        return ItemStackHookKt.modifyDisplayName(s);
    }
}
