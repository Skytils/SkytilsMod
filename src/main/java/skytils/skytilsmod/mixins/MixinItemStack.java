package skytils.skytilsmod.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow public abstract ItemStack copy();

    @Inject(method = "isItemEnchanted", at = @At("HEAD"), cancellable = true)
    private void showEnchantmentGlint(CallbackInfoReturnable<Boolean> cir) {
        if (!Utils.inSkyblock) return;
        if (Skytils.config.enchantGlintFix) {
            NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(copy());
            if (extraAttr != null && extraAttr.hasKey("enchantments") && !extraAttr.getCompoundTag("enchantments").getKeySet().isEmpty()) {
                cir.setReturnValue(true);
            }
        }
    }
}
