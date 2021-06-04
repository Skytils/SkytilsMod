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

package skytils.skytilsmod.mixins.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.features.impl.dungeons.solvers.terminals.SelectAllColorSolver;
import skytils.skytilsmod.features.impl.dungeons.solvers.terminals.StartsWithSequenceSolver;
import skytils.skytilsmod.utils.Utils;

@Mixin(Slot.class)
public abstract class MixinSlot {

    @Shadow public abstract ItemStack getStack();

    @Shadow @Final public IInventory inventory;
    @Shadow @Final private int slotIndex;
    @Shadow public int slotNumber;

    @Inject(method = "getStack", at = @At("HEAD"), cancellable = true)
    private void markTerminalItems(CallbackInfoReturnable<ItemStack> cir) {
        if (!Utils.inSkyblock) return;
        ItemStack item = this.inventory.getStackInSlot(this.slotIndex);
        if (item == null) return;
        item = item.copy();
        if (!item.isItemEnchanted() && (SelectAllColorSolver.shouldClick.contains(this.slotNumber) || StartsWithSequenceSolver.shouldClick.contains(this.slotNumber))) {
            if (item.getTagCompound() == null) {
                item.setTagCompound(new NBTTagCompound());
            }
            item.getTagCompound().setBoolean("SkytilsForceGlint", true);
            cir.setReturnValue(item);
        }
    }

}
