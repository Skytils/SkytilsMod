/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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

package skytils.skytilsmod.mixins.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.features.impl.misc.MiscFeatures;
import skytils.skytilsmod.utils.Utils;

@Mixin(GuiIngameForge.class)
public abstract class MixinGuiIngameForge extends GuiIngame {
    public MixinGuiIngameForge(Minecraft mcIn) {
        super(mcIn);
    }

    @ModifyArgs(method = "renderToolHightlight", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawStringWithShadow(Ljava/lang/String;FFI)I"), remap = false)
    private void modifyItemHighlightPosition(Args args) {
        if (Skytils.config.moveableItemNameHighlight && Utils.inSkyblock) {
            FontRenderer fr = highlightingItemStack.getItem().getFontRenderer(highlightingItemStack);
            if (fr == null) fr = mc.fontRendererObj;

            String itemName = args.get(0);

            GuiElement element = MiscFeatures.ItemNameHighlightDummy.INSTANCE;

            float x = ((element.getActualX() - element.getActualWidth() / 2)  - fr.getStringWidth(itemName)) / 2;

            args.set(1, x);
            args.set(2, element.getActualY());
        }
    }
}
