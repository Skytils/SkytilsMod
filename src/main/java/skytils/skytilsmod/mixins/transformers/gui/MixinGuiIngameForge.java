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

package skytils.skytilsmod.mixins.transformers.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.GuiIngameForge;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import skytils.skytilsmod.mixins.hooks.gui.GuiIngameForgeHookKt;

@Mixin(GuiIngameForge.class)
public abstract class MixinGuiIngameForge extends GuiIngame {
    public MixinGuiIngameForge(Minecraft mcIn) {
        super(mcIn);
    }

    @ModifyExpressionValue(method = "renderToolHightlight", at = @At(value = "FIELD", target = "Lnet/minecraftforge/client/GuiIngameForge;remainingHighlightTicks:I", opcode = Opcodes.GETFIELD, ordinal = 0))
    private int alwaysShowItemHighlight(int original) {
        return GuiIngameForgeHookKt.alwaysShowItemHighlight(original);
    }

    @ModifyArgs(method = "renderToolHightlight", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"))
    private void modifyItemHighlightPosition(Args args) {
        GuiIngameForgeHookKt.modifyItemHighlightPosition(args, highlightingItemStack);
    }

    @ModifyArgs(method = "renderRecordOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V"))
    private void modifyActionBarPosition(Args args) {
        GuiIngameForgeHookKt.modifyActionBarPosition(args);
    }

    @ModifyVariable(method = "renderHealth", at = @At(value = "STORE"), ordinal = 1, remap = false)
    private float removeAbsorption(float absorption) {
        return GuiIngameForgeHookKt.setAbsorptionAmount(absorption);
    }
}
