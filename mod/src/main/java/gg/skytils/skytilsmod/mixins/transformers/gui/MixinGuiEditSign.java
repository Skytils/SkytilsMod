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

import gg.essential.universal.UKeyboard;
import gg.essential.universal.UScreen;
import gg.skytils.skytilsmod.Skytils;
import gg.skytils.skytilsmod.mixins.hooks.gui.GuiEditSignHookKt;
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiEditSign;
import gg.skytils.skytilsmod.utils.Utils;
import net.minecraft.client.gui.inventory.GuiEditSign;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiEditSign.class)
public class MixinGuiEditSign {
    @Inject(method = "keyTyped", at = @At("TAIL"))
    public void skytils$enterToConfirmSign(char typedChar, int keyCode, CallbackInfo ci) {
        if (!Skytils.Companion.getConfig().getPressEnterToConfirmSignQuestion()) return;
        if (!Utils.INSTANCE.getInSkyblock()) return;
        if (keyCode != UKeyboard.KEY_ENTER) return;
        if (!GuiEditSignHookKt.isConfirmableSign((AccessorGuiEditSign) this)) return;
        UScreen.displayScreen(null);
    }
}
