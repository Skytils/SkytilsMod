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
package skytils.skytilsmod.features.impl.handlers

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.apache.commons.lang3.time.StopWatch
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.core.Config
import skytils.skytilsmod.utils.APIUtil
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.Utils
import java.util.*
import kotlin.concurrent.fixedRateTimer

class AuctionData {

    companion object {
        const val dataURL = "https://sbe-stole-skytils.design/api/auctions/lowestbins"
        val lowestBINs = HashMap<String, Double>()
        private val gson = GsonBuilder().setPrettyPrinting().create()
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
                    if (!enchants.hasNoTags()) {
                        val enchant = enchants.keySet.iterator().next()
                        id = "ENCHANTED_BOOK-" + enchant.toUpperCase(Locale.US) + "-" + enchants.getInteger(enchant)
                    }
                }
                "POTION" -> if (extraAttr.hasKey("potion") && extraAttr.hasKey("potion_level")) {
                    id = "POTION-" + extraAttr.getString("potion")
                        .toUpperCase(Locale.US) + "-" + extraAttr.getInteger("potion_level") + (if (extraAttr.hasKey("enhanced")) "-ENHANCED" else "") + (if (extraAttr.hasKey(
                            "extended"
                        )
                    ) "-EXTENDED" else "") + if (extraAttr.hasKey("splash")) "-SPLASH" else ""
                }
            }
            return id
        }
    }

    init {
        Skytils.config.registerListener(Config::fetchLowestBINPrices) { value ->
            if (!value) lowestBINs.clear()
        }
        fixedRateTimer(name = "Skytils-FetchAuctionData", period = 60 * 1000) {
            if (Skytils.config.fetchLowestBINPrices) {
                val data = APIUtil.getJSONResponse(dataURL)
                for ((key, value) in data.entrySet()) {
                    lowestBINs[key] = value.asDouble
                }
            }
        }
    }

}