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

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.item.ItemStack
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.client
import skytils.skytilsmod.Skytils.Companion.json
import skytils.skytilsmod.core.Config
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.PetInfo
import skytils.skytilsmod.utils.toStringIfTrue
import kotlin.concurrent.fixedRateTimer
import kotlin.reflect.jvm.javaField

class AuctionData {

    companion object {
        val dataURL
            get() = "https://${Skytils.domain}/api/auctions/lowestbins"
        val lowestBINs = HashMap<String, Double>()
        fun getIdentifier(item: ItemStack?): String? {
            val extraAttr = ItemUtil.getExtraAttributes(item) ?: return null
            var id = ItemUtil.getSkyBlockItemID(extraAttr) ?: return null
            when (id) {
                "PET" -> if (extraAttr.hasKey("petInfo")) {
                    val petInfo = json.decodeFromString<PetInfo>(extraAttr.getString("petInfo"))
                    id = "PET-${petInfo.type}-${petInfo.tier}"
                }
                "ENCHANTED_BOOK" -> if (extraAttr.hasKey("enchantments")) {
                    val enchants = extraAttr.getCompoundTag("enchantments")
                    val enchant = enchants.keySet.firstOrNull()
                    if (enchant != null) {
                        id = "ENCHANTED_BOOK-${enchant.uppercase()}-${enchants.getInteger(enchant)}"
                    }
                }
                "POTION" -> if (extraAttr.hasKey("potion") && extraAttr.hasKey("potion_level")) {
                    id = "POTION-${
                        extraAttr.getString("potion")
                            .uppercase()
                    }-${extraAttr.getInteger("potion_level")}${"-ENHANCED".toStringIfTrue(extraAttr.hasKey("enhanced"))}${
                        "-EXTENDED".toStringIfTrue(
                            extraAttr.hasKey(
                                "extended"
                            )
                        )
                    }${"-SPLASH".toStringIfTrue(extraAttr.hasKey("splash"))}"
                }
                "RUNE" -> if (extraAttr.hasKey("runes")) {
                    val runes = extraAttr.getCompoundTag("runes")
                    val rune = runes.keySet.firstOrNull()
                    if (rune != null) {
                        id = "RUNE-${rune.uppercase()}-${runes.getInteger(rune)}"
                    }
                }
            }
            return id
        }
    }

    init {
        Skytils.config.registerListener(Config::fetchLowestBINPrices.javaField!!) { value: Boolean ->
            if (!value) lowestBINs.clear()
        }
        fixedRateTimer(name = "Skytils-FetchAuctionData", period = 60 * 1000L) {
            if (Skytils.config.fetchLowestBINPrices) {
                Skytils.IO.launch {
                    client.get(dataURL).body<JsonObject>().forEach { itemId, price ->
                        lowestBINs[itemId] = price.jsonPrimitive.double
                    }
                }
            }
        }
    }

}