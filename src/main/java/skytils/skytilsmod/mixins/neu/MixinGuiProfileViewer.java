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

package skytils.skytilsmod.mixins.neu;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.RenderUtil;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.profileviewer.GuiProfileViewer", remap = false)
public class MixinGuiProfileViewer {
    @ModifyArgs(method = "drawInvsPage", at = @At(value = "INVOKE", target = "Lio/github/moulberry/notenoughupdates/util/Utils;drawItemStack(Lnet/minecraft/item/ItemStack;II)V"))
    private void renderRarity(Args args) {
        if (Skytils.config.showItemRarity) {
            ItemStack stack = args.get(0);
            int x = args.get(1);
            int y = args.get(2);
            RenderUtil.renderRarity(stack, x, y);
        }
    }
}
