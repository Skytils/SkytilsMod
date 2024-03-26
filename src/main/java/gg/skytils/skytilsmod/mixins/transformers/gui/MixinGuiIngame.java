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

package gg.skytils.skytilsmod.mixins.transformers.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import gg.skytils.skytilsmod.mixins.hooks.gui.GuiIngameHookKt;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.player.EntityPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame extends Gui {

    @Shadow
    protected String recordPlaying;

    @Shadow
    protected int recordPlayingUpFor;

    @Shadow
    protected boolean recordIsPlaying;

    @ModifyExpressionValue(method = "renderSelectedItem", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/GuiIngame;remainingHighlightTicks:I", opcode = Opcodes.GETFIELD))
    private int alwaysShowItemHighlight(int original) {
        return GuiIngameHookKt.alwaysShowItemHighlightOptifine(original);
    }

    @Inject(method = "setRecordPlaying(Ljava/lang/String;Z)V", at = @At("HEAD"), cancellable = true)
    private void onSetActionBar(String message, boolean isPlaying, CallbackInfo ci) {
        if (GuiIngameHookKt.onSetActionBar(message, isPlaying, ci)) {
            this.recordPlaying = GuiIngameHookKt.getRecordPlaying();
            this.recordPlayingUpFor = GuiIngameHookKt.getRecordPlayingUpFor();
            this.recordIsPlaying = GuiIngameHookKt.getRecordIsPlaying();
        }
    }

    @Inject(method = "renderHotbarItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemAndEffectIntoGUI(Lnet/minecraft/item/ItemStack;II)V"))
    private void renderRarityOnHotbar(int index, int xPos, int yPos, float partialTicks, EntityPlayer player, CallbackInfo ci) {
        GuiIngameHookKt.renderRarityOnHotbar(index, xPos, yPos, partialTicks, player, ci);
    }

    @ModifyVariable(method = "renderVignette", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private float disableWorldBorder(float f) {
        return GuiIngameHookKt.onWorldBorder(f);
    }

}
