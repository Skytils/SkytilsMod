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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    private final static Pattern starPattern = Pattern.compile("(§6✪)");

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

    @Shadow
    private NBTTagCompound stackTagCompound;

    @Inject(method="getDisplayName",at=@At("HEAD"), cancellable=true)
    public void getDisplayName(CallbackInfoReturnable<String> returnable) {
        try {
            if (Skytils.config.compactStars) {
                if (stackTagCompound.hasKey("display", 10)) {
                    NBTTagCompound nbtTagCompound = stackTagCompound.getCompoundTag("display");
                    if (nbtTagCompound.hasKey("Name", 8)) {
                        String name = nbtTagCompound.getString("Name");
                        Matcher starMatcher = starPattern.matcher(name);
                        if (starMatcher.find()) {
                            int count = 0;
                            int i = 0;
                            while (starMatcher.find(i)) {
                                count++;
                                i = starMatcher.start() + 1;
                            }
                            name = name.replaceAll(starPattern.toString(), "") + "§6" + count + "✪";
                        }
                        returnable.setReturnValue(name);

                    }
                }

            }
        } catch(Exception e) { }
    }
}
