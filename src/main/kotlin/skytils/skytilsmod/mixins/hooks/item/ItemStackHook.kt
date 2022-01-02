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
package skytils.skytilsmod.mixins.hooks.item

import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.features.impl.handlers.GlintCustomizer
import skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.countMatches

const val starPattern = "§6✪"
const val masterStarPattern = "§c✪"

fun showEnchantmentGlint(stack: Any, cir: CallbackInfoReturnable<Boolean>) {
    (stack as ItemStack).apply {
        if (!Utils.inSkyblock) return
        val extraAttr = getExtraAttributes(this)
        if (extraAttr != null) {
            val itemId = getSkyBlockItemID(extraAttr)
            if (GlintCustomizer.overrides.containsKey(itemId)) {
                cir.returnValue = GlintCustomizer.overrides[itemId]
                return
            }
            if (Skytils.config.enchantGlintFix) {
                if (extraAttr.hasKey("enchantments") && extraAttr.getCompoundTag("enchantments").keySet.isNotEmpty()) {
                    cir.returnValue = true
                    return
                }
            }
        }
        if (tagCompound != null && tagCompound.hasKey("SkytilsForceGlint")) {
            cir.returnValue = tagCompound.getBoolean("SkytilsForceGlint")
        }
    }
}

fun modifyDisplayName(s: String): String {
    var displayName = s
    if (!Utils.inSkyblock) return displayName
    try {
        if (Skytils.config.compactStars && displayName.contains("✪")) {
            if (displayName.contains("§c✪")) {
                displayName = "${
                    displayName.replace(starPattern, "")
                        .replace(masterStarPattern, "")
                }§c${displayName.countMatches(masterStarPattern) + 5}✪"
            } else {
                displayName = "${displayName.replace(starPattern, "")}§6${displayName.countMatches(starPattern)}✪"
            }
        }
    } catch (ignored: Exception) {
    }
    return displayName
}