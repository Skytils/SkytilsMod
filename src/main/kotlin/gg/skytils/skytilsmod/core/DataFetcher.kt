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
package gg.skytils.skytilsmod.core

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Reference.dataUrl
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.client
import gg.skytils.skytilsmod.Skytils.Companion.domain
import gg.skytils.skytilsmod.Skytils.Companion.failPrefix
import gg.skytils.skytilsmod.Skytils.Companion.json
import gg.skytils.skytilsmod.features.impl.dungeons.solvers.ThreeWeirdosSolver
import gg.skytils.skytilsmod.features.impl.dungeons.solvers.TriviaSolver
import gg.skytils.skytilsmod.features.impl.farming.FarmingFeatures
import gg.skytils.skytilsmod.features.impl.farming.TreasureHunter
import gg.skytils.skytilsmod.features.impl.funny.skytilsplus.SheepifyRebellion
import gg.skytils.skytilsmod.features.impl.handlers.Mayor
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.features.impl.handlers.SpamHider
import gg.skytils.skytilsmod.features.impl.mining.MiningFeatures
import gg.skytils.skytilsmod.features.impl.misc.ItemFeatures
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.misc.SummonSkins
import gg.skytils.skytilsmod.features.impl.spidersden.RelicWaypoints
import gg.skytils.skytilsmod.utils.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import kotlin.concurrent.fixedRateTimer
import kotlin.reflect.jvm.jvmName

object DataFetcher {
    var job: Job? = null

    private fun loadData(): Job {
        return Skytils.IO.launch {
            try {
                get<String>("${dataUrl}constants/domain.txt") {
                    if (isNotBlank()) {
                        domain = trim()
                    }
                }
                get<JsonObject>("${dataUrl}constants/enchants.json") {
                    Utils.checkThreadAndQueue {
                        EnchantUtil.enchants.clear()
                        EnchantUtil.enchants.addAll(
                            json.decodeFromJsonElement<Map<String, NormalEnchant>>(get("NORMAL")!!).values
                        )
                        EnchantUtil.enchants.addAll(
                            json.decodeFromJsonElement<Map<String, NormalEnchant>>(get("ULTIMATE")!!).values
                        )
                        EnchantUtil.enchants.addAll(
                            json.decodeFromJsonElement<Map<String, StackingEnchant>>(get("STACKING")!!).values
                        )
                    }
                }
                get<Map<String, String>>("${dataUrl}solvers/fetchur.json") {
                    Utils.checkThreadAndQueue {
                        MiningFeatures.fetchurItems.clear()
                        MiningFeatures.fetchurItems.putAll(this)
                    }
                }
                get<Map<String, String>>("${dataUrl}solvers/hungryhiker.json") {
                    Utils.checkThreadAndQueue {
                        FarmingFeatures.hungerHikerItems.clear()
                        FarmingFeatures.hungerHikerItems.putAll(this)
                    }
                }
                get<LevelingXPData>("${dataUrl}constants/levelingxp.json") {
                    Utils.checkThreadAndQueue {
                        SkillUtils.maxSkillLevels.clear()
                        SkillUtils.maxSkillLevels.putAll(defaultCaps)
                        SkillUtils.skillXp.clear()
                        SkillUtils.skillXp.putAll(levelingXP)
                        SkillUtils.dungeoneeringXp.clear()
                        SkillUtils.dungeoneeringXp.putAll(dungeonXp)
                        SkillUtils.slayerXp.clear()
                        SkillUtils.slayerXp.putAll(slayerXp)
                        SkillUtils.runeXp.clear()
                        SkillUtils.runeXp.putAll(runeXp)
                        SkillUtils.hotmXp.clear()
                        SkillUtils.runeXp.putAll(hotmXp)
                    }
                }
                get<List<Mayor>>("${dataUrl}constants/mayors.json") {
                    Utils.checkThreadAndQueue {
                        MayorInfo.mayorData.clear()
                        MayorInfo.mayorData.addAll(this)
                    }
                }
                get<List<String>>("${dataUrl}solvers/threeweirdos.json") {
                    Utils.checkThreadAndQueue {
                        ThreeWeirdosSolver.solutions.clear()
                        ThreeWeirdosSolver.solutions.addAll(this)
                    }
                }
                get<Map<String, String>>("${dataUrl}solvers/treasurehunter.json") {
                    Utils.checkThreadAndQueue {
                        TreasureHunter.treasureHunterLocations.clear()
                        entries.associateTo(TreasureHunter.treasureHunterLocations) { (key, value) ->
                            key to value.split(",").map { it.toDouble() }
                                .run { BlockPos(this[0], this[1], this[2]) }
                        }
                    }
                }
                get<Map<String, List<String>>>("${dataUrl}solvers/oruotrivia.json") {
                    Utils.checkThreadAndQueue {
                        TriviaSolver.triviaSolutions.clear()
                        TriviaSolver.triviaSolutions.putAll(this)
                    }
                }
                get<List<JsonElement>>("${dataUrl}constants/relics.json") {
                    Utils.checkThreadAndQueue {
                        RelicWaypoints.relicLocations.clear()
                        mapTo(RelicWaypoints.relicLocations) {
                            json.decodeFromJsonElement(BlockPosArraySerializer, it)
                        }
                    }
                }
                // no key required
                get<JsonObject>("https://api.hypixel.net/resources/skyblock/items") {
                    if (get("success")?.jsonPrimitive?.booleanOrNull == true) {
                        val items: List<ItemFeatures.APISBItem> = json.decodeFromJsonElement(
                            get("items")!!
                        )
                        val sellPrices =
                            items.filter { it.npcSellPrice != null }.associate { it.id to it.npcSellPrice!! }
                        val idToName = items.associate { it.id to it.name }
                        Utils.checkThreadAndQueue {
                            ItemFeatures.sellPrices.clear()
                            ItemFeatures.sellPrices.putAll(sellPrices)
                            ItemFeatures.itemIdToNameLookup.clear()
                            ItemFeatures.itemIdToNameLookup.putAll(idToName)
                        }
                    }
                }
                get<Map<String, HashMap<String, Int>>>("${dataUrl}constants/slayerhealth.json") {
                    Utils.checkThreadAndQueue {
                        SlayerFeatures.BossHealths.clear()
                        SlayerFeatures.BossHealths.putAll(this)
                    }
                }
                get<List<SpamHider.Filter>>("${dataUrl}SpamFilters.json") {
                    Utils.checkThreadAndQueue {
                        SpamHider.repoFilters.clear()
                        SpamHider.repoFilters.addAll(this)
                    }
                }
                get<Map<String, String>>("${dataUrl}constants/summons.json") {
                    Utils.checkThreadAndQueue {
                        SummonSkins.skinMap.clear()
                        SummonSkins.skinMap.putAll(this)
                        SummonSkins.loadSkins()
                    }
                }

                if (SheepifyRebellion.isSkytilsPlus) {
                    get<Map<String, String>>("https://gist.githubusercontent.com/My-Name-Is-Jeff/4ddf4e88360c34a8582b82834b3511c8/raw/coloreddata.json") {
                        Utils.checkThreadAndQueue {
                            SheepifyRebellion.skytilsPlusColors.clear()
                            SheepifyRebellion.skytilsPlusColors.putAll(this.entries.associate {
                                it.key to (SheepifyRebellion.lookup[it.value.firstOrNull()] ?: EnumDyeColor.WHITE)
                            })
                        }
                    }

                    get<SheepifyRebellion.SkytilsPlusData>("https://gist.githubusercontent.com/My-Name-Is-Jeff/4ddf4e88360c34a8582b82834b3511c8/raw/plusdata.json") {
                        Utils.checkThreadAndQueue {
                            SheepifyRebellion.skytilsPlusUsernames.clear()
                            SheepifyRebellion.skytilsPlusUsernames.addAll(active)
                            SheepifyRebellion.skytilsPlusUsernames.addAll(inactive)
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                UChat.chat("$failPrefix §cSkytils ran into an error while fetching data. Please report this to our Discord server!")
            }
        }
    }

    private suspend inline fun <reified T> get(url: String, crossinline block: T.() -> Unit) =
        Skytils.IO.launch {
            runCatching {
                client.get(url).body<T>().apply(block)
            }.onFailure {
                it.printStackTrace()
                UChat.chat("""
                    |$failPrefix §cFailed to fetch data! Some features may not work as expected.
                    | URL: $url
                    | §c${it::class.qualifiedName ?: it::class.jvmName}: ${it.message ?: "Unknown"}
                """.trimMargin())
            }
        }

    @JvmStatic
    fun reloadData() {
        if (job?.isActive != true) {
            job = loadData()
        } else {
            UChat.chat("$failPrefix §cData fetch requested while already fetching!")
        }
    }

    internal fun preload() {}

    init {
        fixedRateTimer(name = "Skytils-Reload-Data", period = 60 * 60 * 1000L) {
            reloadData()
        }
    }
}

@Serializable
private data class LevelingXPData(
    @SerialName("default_skill_caps")
    val defaultCaps: LinkedHashMap<String, Int>,
    @SerialName("leveling_xp")
    val levelingXP: LinkedHashMap<Int, Long>,
    @SerialName("dungeoneering_xp")
    val dungeonXp: LinkedHashMap<Int, Long>,
    @SerialName("slayer_xp")
    val slayerXp: LinkedHashMap<String, LinkedHashMap<Int, Long>>,
    @SerialName("runecrafting_xp")
    val runeXp: LinkedHashMap<Int, Long>,
    @SerialName("hotm_xp")
    val hotmXp: LinkedHashMap<Int, Long>
)