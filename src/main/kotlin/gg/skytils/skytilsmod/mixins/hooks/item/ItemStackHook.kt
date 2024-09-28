/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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
package gg.skytils.skytilsmod.mixins.hooks.item

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.features.impl.handlers.GlintCustomizer
import gg.skytils.skytilsmod.utils.ItemUtil.getExtraAttributes
import gg.skytils.skytilsmod.utils.ItemUtil.getSkyBlockItemID
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.countMatches
import gg.skytils.skytilsmod.utils.ifNull
import net.minecraft.item.ItemStack
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

const val starPattern = "§6✪✪✪✪✪"
const val starPatternSingle = "✪"
val masterStars = ('➊'..'➎').toList()
val masterStarPattern = Regex("§c[${masterStars.joinToString("")}]")

fun showEnchantmentGlint(stack: Any, cir: CallbackInfoReturnable<Boolean>) {
    (stack as ItemStack).apply {
        if (!Utils.inSkyblock) return
        val extraAttr = getExtraAttributes(this)
        if (extraAttr != null) {
            val itemId = getSkyBlockItemID(extraAttr)
            GlintCustomizer.glintItems[itemId]?.override?.let {
                cir.returnValue = it
                return
            }
            if (Skytils.config.enchantGlintFix) {
                if (extraAttr.hasKey("enchantments") && extraAttr.getCompoundTag("enchantments").keySet.isNotEmpty()) {
                    cir.returnValue = true
                    return
                }
            }
        }
        if (tagCompound?.hasKey("SkytilsForceGlint") == true) {
            cir.returnValue = tagCompound.getBoolean("SkytilsForceGlint")
        }
    }
}

fun modifyDisplayName(s: String): String {
    var displayName = s
    if (!Utils.inSkyblock) return displayName
    try {
        if (Skytils.config.starDisplayType != 0 && displayName.contains("✪")) {
            if (Skytils.config.starDisplayType == 2) {
                masterStarPattern.find(displayName)?.let {
                    val star = it.value.last()
                    val count = masterStars.indexOf(star) + 1 + 5
                    displayName = "${
                        displayName.replace(starPattern, "")
                            .replace(masterStarPattern, "")
                    }§c${count}✪"
                }.ifNull {
                    displayName = "${displayName.replace(starPattern, "")}§6${displayName.countMatches(starPatternSingle)}✪"
                }
            } else if (Skytils.config.starDisplayType == 1) {
                masterStarPattern.find(displayName)?.let {
                    val star = it.value.last()
                    val count = masterStars.indexOf(star) + 1
                    displayName = displayName.replace(masterStarPattern, "")
                        .replaceFirst("§6${starPatternSingle.repeat(count)}", "§c✪§6".repeat(count))
                }
            }
        }
    } catch (ignored: Exception) {
    }
    return displayName
}
