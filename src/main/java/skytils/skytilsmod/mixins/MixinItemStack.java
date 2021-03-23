package skytils.skytilsmod.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.features.impl.handlers.GlintCustomizer;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    private final static Pattern starPattern = Pattern.compile("(§6✪)");

    private final ItemStack that = (ItemStack) (Object) this;

    @Inject(method = "isItemEnchanted", at = @At("HEAD"), cancellable = true)
    private void showEnchantmentGlint(CallbackInfoReturnable<Boolean> cir) {
        if (!Utils.inSkyblock) return;
        NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(that);
        if (extraAttr != null) {
            String itemId = ItemUtil.getSkyBlockItemID(extraAttr);
            if (GlintCustomizer.overrides.containsKey(itemId)) {
                cir.setReturnValue(GlintCustomizer.overrides.get(itemId));
                return;
            }
            if (Skytils.config.enchantGlintFix) {
                if (extraAttr != null && extraAttr.hasKey("enchantments") && !extraAttr.getCompoundTag("enchantments").getKeySet().isEmpty()) {
                    cir.setReturnValue(true);
                    return;
                }
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
