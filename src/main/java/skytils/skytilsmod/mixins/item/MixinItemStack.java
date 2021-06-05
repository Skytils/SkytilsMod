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

package skytils.skytilsmod.mixins.item;

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
    @Shadow private NBTTagCompound stackTagCompound;
    private final static Pattern starPattern = Pattern.compile("(§6✪)");
    private final static Pattern masterStarPattern = Pattern.compile("(§c✪)");

    private final ItemStack that = (ItemStack) (Object) this;

    @Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
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
                if (extraAttr.hasKey("enchantments") && !extraAttr.getCompoundTag("enchantments").getKeySet().isEmpty()) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        }

        if (stackTagCompound != null && stackTagCompound.hasKey("SkytilsForceGlint")) {
            cir.setReturnValue(stackTagCompound.getBoolean("SkytilsForceGlint"));
        }
    }

    @ModifyVariable(method = "getDisplayName", at = @At(value = "STORE"))
    private String modifyDisplayName(String s) {
        if (!Utils.inSkyblock) return s;
        try {
            if (Skytils.config.compactStars && s.contains("✪")) {
                Matcher masterStarMatcher = masterStarPattern.matcher(s);
                if (masterStarMatcher.find()) {
                    int count = 0;
                    int i = 0;
                    while(masterStarMatcher.find(i)) {
                        count ++;
                        i = masterStarMatcher.end();
                    }
                    s = s.replaceAll(starPattern.toString(), "").replaceAll(masterStarPattern.toString(), "") + "§c" + (count + 5) + "✪";
                } else {
                    Matcher starMatcher = starPattern.matcher(s);
                    if (starMatcher.find()) {
                        int count = 0;
                        int i = 0;
                        while (starMatcher.find(i)) {
                            count++;
                            i = starMatcher.end();
                        }
                        s = s.replaceAll(starPattern.toString(), "") + "§6" + count + "✪";
                    }
                }
            }
        } catch(Exception ignored) { }
        return s;
    }
}
