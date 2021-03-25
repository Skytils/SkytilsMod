package skytils.skytilsmod.mixins;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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
            return;
        }
    }

}
