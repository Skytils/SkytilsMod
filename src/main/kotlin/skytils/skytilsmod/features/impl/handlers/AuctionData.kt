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
package skytils.skytilsmod.features.impl.handlers

import com.google.gson.JsonObject
import net.minecraft.item.ItemStack
import skytils.hylin.skyblock.bazaar.BazaarProduct
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.gson
import skytils.skytilsmod.core.Config
import skytils.skytilsmod.utils.APIUtil
import skytils.skytilsmod.utils.ItemUtil
import kotlin.concurrent.fixedRateTimer
import kotlin.reflect.jvm.javaField

class AuctionData {

    companion object {
        val dataURL
            get() = "https://${Skytils.domain}/api/auctions/lowestbins"
        val lowestBINs = HashMap<String, Double>()
        val bazaarPrices = HashMap<String, BazaarProduct>()
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
        Skytils.config.registerListener(Config::fetchLowestBINPrices.javaField!!) { value: Boolean ->
            if (!value) {
                lowestBINs.clear()
                bazaarPrices.clear()
            }
        }
        fixedRateTimer(name = "Skytils-FetchAuctionData", period = 60 * 1000L) {
            if (Skytils.config.fetchLowestBINPrices) {
                val data = APIUtil.getJSONResponse(dataURL)
                for ((key, value) in data.entrySet()) {
                    lowestBINs[key] = value.asDouble
                }
            }
        }
        fixedRateTimer(name = "Skytils-FetchBazaarData", period = 10 * 60 * 1000L) {
            if(Skytils.config.fetchLowestBINPrices && Skytils.config.containerSellValue) {
                Skytils.hylinAPI.getBazaarData().whenComplete {
                    bazaarPrices.putAll(it.products)
                }
            }
        }
    }

}