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

package gg.skytils.skytilsmod.mixins.transformers.neu;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import gg.skytils.skytilsmod.mixins.hooks.neu.GuiProfileViewerHookKt;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;


/**
 * These methods don't exist after NotEnoughUpdates/NotEnoughUpdates#81, and rarity drawing is handled by NEU.
 * This mixin is only for versions before that.
 */
@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer", remap = false)
public abstract class MixinGuiProfileViewer extends GuiScreen {
    @Dynamic
    @WrapWithCondition(method = "drawInvsPage", at = @At(value = "INVOKE", target = "Lio/github/moulberry/notenoughupdates/util/Utils;drawItemStack(Lnet/minecraft/item/ItemStack;II)V"))
    private boolean renderRarityOnInvPage(ItemStack stack, int x, int y) {
        GuiProfileViewerHookKt.renderRarityOnPage(stack, x, y);
        return true;
    }

    @Dynamic
    @WrapWithCondition(method = "drawPetsPage", at = @At(value = "INVOKE", target = "Lio/github/moulberry/notenoughupdates/util/Utils;drawItemStack(Lnet/minecraft/item/ItemStack;II)V", ordinal = 0))
    private boolean renderRarityOnPetsPage(ItemStack stack, int x, int y) {
        GuiProfileViewerHookKt.renderRarityOnPage(stack, x, y);
        return true;
    }
}