package skytils.skytilsmod.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    private final static Pattern starPattern = Pattern.compile("(§6✪)");

    @Shadow public abstract ItemStack copy();
    @Shadow private NBTTagCompound stackTagCompound;

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

    @ModifyVariable(method = "getDisplayName", at = @At(value = "STORE"))
    private String modifyDisplayName(String s) {
        if (!Utils.inSkyblock) return s;
        try {
            if (Skytils.config.compactStars) {
                Matcher starMatcher = starPattern.matcher(s);
                if (starMatcher.find()) {
                    int count = 0;
                    int i = 0;
                    while (starMatcher.find(i)) {
                        count++;
                        i = starMatcher.start() + 1;
                    }
                    s = s.replaceAll(starPattern.toString(), "") + "§6" + count + "✪";
                }
            }
        } catch(Exception e) { }
        return s;
    }
}
