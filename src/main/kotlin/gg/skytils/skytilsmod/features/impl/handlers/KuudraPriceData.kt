/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

import gg.essential.lib.caffeine.cache.Cache
import gg.essential.lib.caffeine.cache.Caffeine
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.utils.ItemUtil
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import net.minecraft.item.ItemStack
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

object KuudraPriceData {
    private val kuudraPriceCache: Cache<String, AttributePricedItem> = Caffeine.newBuilder()
        .weakKeys()
        .weakValues()
        .expireAfterWrite(5.minutes.toJavaDuration())
        .maximumSize(100)
        .build()

    suspend fun getAttributePricedItem(item: ItemStack): AttributePricedItem? {
        val extraAttr = ItemUtil.getExtraAttributes(item) ?: return null
        val attributes = extraAttr.getCompoundTag("attributes")
        if (attributes.hasNoTags()) return null
        val itemId = ItemUtil.getSkyBlockItemID(extraAttr)
        val attributeItem = "$itemId ${attributes.keySet.joinToString("\n") { "${it}_${ attributes.getInteger(it) }" }}"

        if (kuudraPriceCache.getIfPresent(attributeItem) != null) return kuudraPriceCache.getIfPresent(attributeItem)

        return Skytils.client.get("https://${Skytils.domain}/api/auctions/kuudra/item_price") {
            attributes.keySet.forEachIndexed { i, attr ->
                parameter("attr${i+1}", "${attr}_${attributes.getInteger(attr)}")
            }
            parameter("item", itemId)
        }.body<AttributePricedItem>().also {
            kuudraPriceCache.put(attributeItem, it)
        }
    }

    /**
     * @param id the ID of the lowest priced auction
     * @param price the price of the lowest priced auction
     * @param timestamp if [id] is `null`, the timestamp of when [price] was last updated
     */
    @Serializable
    data class AttributePricedItem(
        val id: String,
        val price: Double,
        val timestamp: Long?
    )
}