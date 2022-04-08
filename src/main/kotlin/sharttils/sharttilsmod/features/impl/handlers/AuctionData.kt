/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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
package sharttils.sharttilsmod.features.impl.handlers

import com.google.gson.JsonObject
import net.minecraft.item.ItemStack
import sharttils.hylin.skyblock.bazaar.BazaarProduct
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.Sharttils.Companion.gson
import sharttils.sharttilsmod.core.Config
import sharttils.sharttilsmod.utils.APIUtil
import sharttils.sharttilsmod.utils.ItemUtil
import kotlin.concurrent.fixedRateTimer
import kotlin.reflect.jvm.javaField

class AuctionData {

    companion object {
        val dataURL
            get() = "https://${Sharttils.domain}/api/auctions/lowestbins"
        val lowestBINs = HashMap<String, Double>()
        fun getIdentifier(item: ItemStack?): String? {
            val extraAttr = ItemUtil.getExtraAttributes(item) ?: return null
            var id = ItemUtil.getSkyBlockItemID(extraAttr) ?: return null
            when (id) {
                "PET" -> if (extraAttr.hasKey("petInfo")) {
                    val petInfo = gson.fromJson(extraAttr.getString("petInfo"), JsonObject::class.java)
                    if (petInfo.has("type") && petInfo.has("tier")) {
                        id = "PET-" + petInfo["type"].asString + "-" + petInfo["tier"].asString
                    }
                }
                "ENCHANTED_BOOK" -> if (extraAttr.hasKey("enchantments")) {
                    val enchants = extraAttr.getCompoundTag("enchantments")
                    val enchant = enchants.keySet.firstOrNull()
                    if (enchant != null) {
                        id = "ENCHANTED_BOOK-" + enchant.uppercase() + "-" + enchants.getInteger(enchant)
                    }
                }
                "POTION" -> if (extraAttr.hasKey("potion") && extraAttr.hasKey("potion_level")) {
                    id = "POTION-" + extraAttr.getString("potion")
                        .uppercase() + "-" + extraAttr.getInteger("potion_level") + (if (extraAttr.hasKey("enhanced")) "-ENHANCED" else "") + (if (extraAttr.hasKey(
                            "extended"
                        )
                    ) "-EXTENDED" else "") + if (extraAttr.hasKey("splash")) "-SPLASH" else ""
                }
                "RUNE" -> if (extraAttr.hasKey("runes")) {
                    val runes = extraAttr.getCompoundTag("runes")
                    val rune = runes.keySet.firstOrNull()
                    if (rune != null) {
                        id = "RUNE-" + rune.uppercase() + "-" + runes.getInteger(rune)
                    }
                }
            }
            return id
        }
    }

    init {
        Sharttils.config.registerListener(Config::fetchLowestBINPrices.javaField!!) { value: Boolean ->
            if (!value) lowestBINs.clear()
        }
        fixedRateTimer(name = "Sharttils-FetchAuctionData", period = 60 * 1000L) {
            if (Sharttils.config.fetchLowestBINPrices) {
                val data = APIUtil.getJSONResponse(dataURL)
                for ((key, value) in data.entrySet()) {
                    lowestBINs[key] = value.asDouble
                }
            }
        }
    }

}