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

package skytils.skytilsmod.mixins.world;

import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(World.class)
public abstract class MixinWorld implements IBlockAccess {
    @Redirect(method = "getSkyColorBody", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;lastLightningBolt:I"))
    private int lightningSkyColor(World world) {
        if (Skytils.config.hideLightning && Utils.inSkyblock) return 0;
        else return world.getLastLightningBolt();
    }
}
