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

package gg.skytils.skytilsmod.mixins.transformers.gui;

import gg.essential.universal.UScreen;
import gg.skytils.skytilsmod.Skytils;
import gg.skytils.skytilsmod.gui.OptionsGui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public class MixinGuiIngameMenu extends GuiScreen {

    @Inject(method = "initGui", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (Skytils.getConfig().getConfigButtonOnPause()) {
            int x = this.width - 105;
            int y = this.height - 22;
            for (GuiButton button : this.buttonList) {
                int otherX = button.xPosition;
                int otherY = button.yPosition;
                if (otherX >= x && otherY < y + 20) {
                    y = otherY - 20 - 2;
                }
            }
            this.buttonList.add(new GuiButton(6969420, x, Math.max(0, y), 100, 20, "Skytils"));
        }
    }

    @Inject(method = "actionPerformed", at = @At("HEAD"), cancellable = true)
    private void clicked(GuiButton button, CallbackInfo ci) {
        if (button.id == 6969420 && Skytils.getConfig().getConfigButtonOnPause()) {
            UScreen.displayScreen(new OptionsGui());
            ci.cancel();
        }
    }
}
