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

import gg.skytils.skytilsmod.Skytils;
import gg.skytils.skytilsmod.gui.OptionsGui;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public class MixinGuiIngameMenu extends Screen {
    protected MixinGuiIngameMenu(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        if (Skytils.getConfig().getConfigButtonOnPause()) {
            int x = this.width - 105;
            int y = this.height - 22;
            for (Element element : this.children()) {
                if (element instanceof ButtonWidget button) {
                    int otherX = button.getX();
                    int otherY = button.getY();
                    if (otherX >= x && otherY < y + 20) {
                        y = otherY - 20 - 2;
                    }
                }
            }
            this.addDrawableChild(
                    ButtonWidget.builder(Text.literal("Skytils"), button -> {
                        Skytils.displayScreen = new OptionsGui();
                    })
                            .position(x, Math.max(0, y))
                            .size(100, 20)
                            .build()
            );
        }
    }
}
