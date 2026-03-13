/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils

import gg.skytils.skytilsmod.Skytils.json
import gg.skytils.skytilsmod.Skytils.mc
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import kotlinx.serialization.json.*
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.FluidTags
import net.minecraft.state.property.Property
import net.minecraft.util.Identifier
import kotlin.jvm.optionals.getOrNull

object LegacyIdProvider {
    private val cache: MutableMap<Int, Int> = Int2IntOpenHashMap().also { it.defaultReturnValue(Integer.MIN_VALUE) }

    fun getLegacyId(state: BlockState): Int {
        if (state.isAir) return 0

        val stateId = Block.getRawIdFromState(state)
        return cache.getOrPut(stateId) {
            if (!state.fluidState.isEmpty) {
                if (state.fluidState.isIn(FluidTags.WATER)) {
                    return@getOrPut if (state.fluidState.isStill) 9 else 8
                } else if (state.fluidState.isIn(FluidTags.LAVA)) {
                    return@getOrPut if (state.fluidState.isStill) 11 else 10
                }
            }

            return@getOrPut block2Legacy[state.block] ?: run {
                val reqs = block2PropertyMatch[state.block] ?: return@getOrPut Integer.MIN_VALUE
                reqs.find { req ->
                    req.properties.all { (property, value) ->
                        val stateValue = state.get(property)
                        if (stateValue == null) {
                            return@find false
                        }
                        stateValue == value
                    }
                }?.legacyId ?: Integer.MIN_VALUE
            }
        }
    }

    private val block2Legacy = mutableMapOf<Block, Int>()
    private val block2PropertyMatch = mutableMapOf<Block, MutableSet<PropertyRequirement>>()

    data class PropertyRequirement(
        val block: Block,
        val properties: Map<Property<*>, Comparable<*>>,
        val legacyId: Int
    )

    init {
        runCatching {
            mc.resourceManager.getResourceOrThrow(
                Identifier.of("catlas:state2legacy.json")
            ).inputStream.use(json::decodeFromStream<JsonObject>)
        }.getOrNull()?.forEach { (k, v) ->
            if (k.startsWith("//")) return@forEach
            val id = when (v) {
                is JsonPrimitive -> v.intOrNull

                is JsonArray -> v.lastOrNull()?.jsonPrimitive?.intOrNull

                else -> null
            }

            if (id != null) {
                if ('[' !in k) {
                    val block = Registries.BLOCK.getOptionalValue(Identifier.of(k)).getOrNull() ?: return@forEach
                    block2Legacy[block] = id
                } else {
                    val key = Identifier.of(k.substringBefore('['))

                    val block = Registries.BLOCK.getOptionalValue(key).getOrNull() ?: return@forEach

                    val properties = k.substringAfter('[').substringBefore(']').split(',')
                    val requirements = properties.associate {
                        val (name, value) = it.split('=')
                        val prop: Property<*> = block.stateManager.getProperty(name) ?: return@forEach
                        val parsedValue: Comparable<*> = prop.parse(value).getOrNull() ?: return@forEach
                        prop to parsedValue
                    }

                    block2PropertyMatch.getOrPut(block) { mutableSetOf() }.add(
                        PropertyRequirement(block, requirements, id)
                    )
                }
            }
        }
    }

    private fun <T : Comparable<T>> getPropertyValueName(property: Property<T>, value: Comparable<*>): String {
        @Suppress("UNCHECKED_CAST")
        return property.name(value as T)
    }
}
