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
import gg.skytils.skytilsmod.Skytils.IO
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.ifNull
import gg.skytils.skytilsmod.utils.set
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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
    private val fetching = mutableMapOf<String, Deferred<AttributePricedItem>>()

    fun getAttributePricedItemId(item: ItemStack): String? {
        val extraAttr = ItemUtil.getExtraAttributes(item) ?: return null
        val attributes = extraAttr.getCompoundTag("attributes")
        if (attributes.keySet.size < 2) return null
        val itemId = ItemUtil.getSkyBlockItemID(extraAttr) ?: return null
        return "$itemId ${attributes.keySet.joinToString(" ") { "${it}_${attributes.getInteger(it)}" }}".intern()
    }

    fun getOrRequestAttributePricedItem(attrId: String): AttributePricedItem? {
        return kuudraPriceCache.getIfPresent(attrId).ifNull {
            if (attrId !in fetching) {
                IO.launch {
                    fetchAttributePricedItem(attrId)
                }
            }
        }
    }

    fun getAttributePricedItem(attrId: String): AttributePricedItem? {
        return kuudraPriceCache.getIfPresent(attrId)
    }

    suspend fun fetchAttributePricedItem(attrId: String): AttributePricedItem = coroutineScope {
        fetching[attrId]?.let {
            return@coroutineScope it.await()
        }

        return@coroutineScope fetching.getOrPut(attrId) {
            async {
                val parts = attrId.split(" ") as MutableList<String>
                val response = Skytils.client.get("https://${Skytils.domain}/api/auctions/kuudra/item_price") {
                    parameter("item", parts.removeAt(0))
                    parts.forEachIndexed { i, attr ->
                        parameter("attr${i+1}", attr)
                    }
                    expectSuccess = false
                }
                when (response.status) {
                    HttpStatusCode.NotFound -> AttributePricedItem.EMPTY
                    HttpStatusCode.OK -> response.body<AttributePricedItem>()
                    else -> AttributePricedItem.FAILURE
                }.also {
                    if (it != AttributePricedItem.FAILURE) {
                        kuudraPriceCache[attrId] = it
                    }
                    fetching -= attrId
                }
            }
        }.await()
    }

    suspend fun getOrFetchAttributePricedItem(item: ItemStack): AttributePricedItem? {
        val itemId = getAttributePricedItemId(item) ?: return null

        return getAttributePricedItem(itemId) ?: fetchAttributePricedItem(itemId)
    }

    /**
     * @param uuid the UUID of the lowest priced auction
     * @param id the Skyblock ID of the item
     * @param price the price of the lowest priced auction
     * @param timestamp if [uuid] is `null`, the timestamp of when [price] was last updated
     */
    @Serializable
    data class AttributePricedItem(
        val uuid: String? = null,
        val id: String,
        val price: Double,
        val timestamp: Long? = null
    ) {
        companion object {
            val EMPTY = AttributePricedItem(id = "", price = -1.0)
            val FAILURE = AttributePricedItem(id = "", price = -1.0)
        }
    }
}