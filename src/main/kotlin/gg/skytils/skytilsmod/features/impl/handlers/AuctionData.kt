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
package gg.skytils.skytilsmod.features.impl.handlers

import gg.essential.universal.UChat
import gg.skytils.hypixel.types.skyblock.Pet
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.client
import gg.skytils.skytilsmod.Skytils.Companion.json
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.toStringIfTrue
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.item.ItemStack
import kotlin.concurrent.fixedRateTimer
import kotlin.reflect.jvm.javaField

object AuctionData {

    private val dataURL
        get() = "https://${Skytils.domain}/api/auctions/lowestbins"
    val lowestBINs = HashMap<String, Double>()

    fun getIdentifier(item: ItemStack?): String? {
        val extraAttr = ItemUtil.getExtraAttributes(item) ?: return null
        var id = ItemUtil.getSkyBlockItemID(extraAttr) ?: return null
        when (id) {
            "PET" -> if (extraAttr.getString("petInfo").startsWith("{")) {
                val petInfo = json.decodeFromString<Pet>(extraAttr.getString("petInfo"))
                id = "PET-${petInfo.type}-${petInfo.tier}"
            }

            "ATTRIBUTE_SHARD" -> if (extraAttr.hasKey("attributes")) {
                val attributes = extraAttr.getCompoundTag("attributes")
                val attribute = attributes.keySet.firstOrNull()
                if (attribute != null) {
                    id = "ATTRIBUTE_SHARD-${attribute.uppercase()}-${attributes.getInteger(attribute)}"
                }
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

            "RUNE", "UNIQUE_RUNE" -> if (extraAttr.hasKey("runes")) {
                val runes = extraAttr.getCompoundTag("runes")
                val rune = runes.keySet.firstOrNull()
                if (rune != null) {
                    id = "RUNE-${rune.uppercase()}-${runes.getInteger(rune)}"
                }
            }
        }
        return id
    }

    init {
        Skytils.config.registerListener(Config::fetchLowestBINPrices.javaField!!) { value: Boolean ->
            if (!value) lowestBINs.clear()
        }
        fixedRateTimer(name = "Skytils-FetchAuctionData", period = 60 * 1000L) {
            if (Skytils.config.fetchLowestBINPrices) {
                Skytils.IO.launch {
                    client.get(dataURL).let {
                        if (it.headers["cf-cache-status"] == "STALE") {
                            UChat.chat("${Skytils.failPrefix} Uh oh! Auction data is stale, prices may be inaccurate.")
                        }
                        it.body<JsonObject>().forEach { itemId, price ->
                            lowestBINs[itemId] = price.jsonPrimitive.double
                        }
                    }
                }
            }
        }
    }

}