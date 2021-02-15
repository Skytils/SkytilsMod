package skytils.skytilsmod.mixins;

import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.features.impl.misc.ArmorColor;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

@Mixin(ItemArmor.class)
public class MixinItemArmor {

    @Inject(method = "getColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getTagCompound()Lnet/minecraft/nbt/NBTTagCompound;"), cancellable = true)
    private void replaceArmorColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!Utils.inSkyblock) return;
        NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(stack);
        if (extraAttributes != null && extraAttributes.hasKey("uuid")) {
            String uuid = extraAttributes.getString("uuid");
            if (ArmorColor.armorColors.containsKey(uuid)) {
                cir.setReturnValue(ArmorColor.armorColors.get(uuid).getRGB());
            }
        }
    }

    @Inject(method = "getColorFromItemStack", at = @At("HEAD"), cancellable = true)
    private void replaceStackArmorColor(ItemStack stack, int renderPass, CallbackInfoReturnable<Integer> cir) {
        if (!Utils.inSkyblock) return;
        NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(stack);
        if (extraAttributes != null && extraAttributes.hasKey("uuid")) {
            String uuid = extraAttributes.getString("uuid");
            if (ArmorColor.armorColors.containsKey(uuid)) {
                cir.setReturnValue(ArmorColor.armorColors.get(uuid).getRGB());
            }
        }
    }

    @Inject(method = "hasColor", at = @At("HEAD"), cancellable = true)
    private void hasCustomArmorColor(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!Utils.inSkyblock) return;
        NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(stack);
        if (extraAttributes != null && extraAttributes.hasKey("uuid")) {
            String uuid = extraAttributes.getString("uuid");
            if (ArmorColor.armorColors.containsKey(uuid)) {
                cir.setReturnValue(true);
            }
        }
    }
}
