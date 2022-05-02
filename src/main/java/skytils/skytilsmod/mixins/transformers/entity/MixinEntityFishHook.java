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

package skytils.skytilsmod.mixins.transformers.entity;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

@Mixin(EntityFishHook.class)
public abstract class MixinEntityFishHook extends Entity {

    public MixinEntityFishHook(World worldIn) {
        super(worldIn);
    }

    @ModifyArg(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isAABBInMaterial(Lnet/minecraft/util/AxisAlignedBB;Lnet/minecraft/block/material/Material;)Z"))
    private Material modifyLiquid(AxisAlignedBB aabb, Material materialIn) {
        if (!Utils.INSTANCE.getInSkyblock() || !Skytils.Companion.getConfig().getLavaBobber()) return materialIn;
        return this.worldObj.isAABBInMaterial(aabb, Material.lava) ? Material.lava : materialIn;
    }
}
