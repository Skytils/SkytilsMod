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

package skytils.skytilsmod.mixins.transformers.dsm;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import skytils.skytilsmod.utils.StringUtilsKt;

@Pseudo
@Mixin(targets = "me.Danker.features.ColouredNames", remap = false)
public class MixinColoredNames {
    @Dynamic
    @Redirect(method = "onRenderLiving", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;hasCustomName()Z"))
    private boolean replaceColoredNameCheck(Entity entity) {
        String customName = entity.getCustomNameTag();
        return customName.length() > 0 && !customName.endsWith("§c❤") && !StringUtilsKt.stripControlCodes(customName).endsWith(" Hits");
    }
}
