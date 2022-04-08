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
package sharttils.sharttilsmod.core

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import gg.essential.universal.UChat
import net.minecraft.util.BlockPos
import sharttils.sharttilsmod.Reference
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.Sharttils.Companion.gson
import sharttils.sharttilsmod.features.impl.dungeons.solvers.ThreeWeirdosSolver
import sharttils.sharttilsmod.features.impl.dungeons.solvers.TriviaSolver
import sharttils.sharttilsmod.features.impl.farming.FarmingFeatures
import sharttils.sharttilsmod.features.impl.farming.TreasureHunter
import sharttils.sharttilsmod.features.impl.handlers.Mayor
import sharttils.sharttilsmod.features.impl.handlers.MayorInfo
import sharttils.sharttilsmod.features.impl.handlers.MayorPerk
import sharttils.sharttilsmod.features.impl.handlers.SpamHider
import sharttils.sharttilsmod.features.impl.mining.MiningFeatures
import sharttils.sharttilsmod.features.impl.misc.ItemFeatures
import sharttils.sharttilsmod.features.impl.misc.SlayerFeatures
import sharttils.sharttilsmod.features.impl.misc.SummonSkins
import sharttils.sharttilsmod.features.impl.spidersden.RelicWaypoints
import sharttils.sharttilsmod.utils.*
import java.util.concurrent.Future
import kotlin.concurrent.fixedRateTimer

object DataFetcher {
    private fun loadData(): Future<*> {
        return Sharttils.threadPool.submit {
            val dataUrl = Reference.dataUrl
            try {
                APIUtil.getResponse("${dataUrl}constants/domain.txt").apply {
                    if (isNotBlank()) {
                        Sharttils.domain = trim()
                    }
                }
                APIUtil.getJSONResponse("${dataUrl}constants/enchants.json").apply {
                    Utils.checkThreadAndQueue {
                        EnchantUtil.enchants.clear()
                        val normal = this.getAsJsonObject("NORMAL")
                        val ultimate = this.getAsJsonObject("ULTIMATE")
                        val stacking = this.getAsJsonObject("STACKING")

                        normal.entrySet().mapTo(EnchantUtil.enchants) {
                            gson.fromJson(it.value, Enchant::class.java)
                        }
                        ultimate.entrySet().mapTo(EnchantUtil.enchants) {
                            gson.fromJson(it.value, Enchant::class.java)
                        }
                        stacking.entrySet().mapTo(EnchantUtil.enchants) {
                            gson.fromJson(it.value, Enchant::class.java)
                        }
                    }
                }

                APIUtil.getJSONResponse("${dataUrl}solvers/fetchur.json").apply {
                    Utils.checkThreadAndQueue {
                        MiningFeatures.fetchurItems.clear()
                        entrySet().associateTo(MiningFeatures.fetchurItems) { it.key to it.value.asString }
                    }
                }
                APIUtil.getJSONResponse("${dataUrl}solvers/hungryhiker.json").apply {
                    Utils.checkThreadAndQueue {
                        FarmingFeatures.hungerHikerItems.clear()
                        entrySet().associateTo(FarmingFeatures.hungerHikerItems) { it.key to it.value.asString }
                    }
                }
                APIUtil.getJSONResponse("${dataUrl}constants/levelingxp.json").apply {
                    Utils.checkThreadAndQueue {
                        SkillUtils.maxSkillLevels.clear()
                        get("default_skill_caps").asJsonObject.entrySet()
                            .associateTo(SkillUtils.maxSkillLevels) { it.key to it.value.asInt }
                        SkillUtils.skillXp.clear()
                        get("leveling_xp").asJsonObject.entrySet()
                            .associateTo(SkillUtils.skillXp) { it.key.toInt() to it.value.asLong }
                        SkillUtils.dungeoneeringXp.clear()
                        get("dungeoneering_xp").asJsonObject.entrySet()
                            .associateTo(SkillUtils.dungeoneeringXp) { it.key.toInt() to it.value.asLong }
                        SkillUtils.slayerXp.clear()
                        get("slayer_xp").asJsonObject.entrySet().associateTo(SkillUtils.slayerXp) { (key, element) ->
                            key to element.asJsonObject.entrySet()
                                .associateTo(LinkedHashMap()) { it.key.toInt() to it.value.asLong }
                        }
                        SkillUtils.runeXp.clear()
                        get("runecrafting_xp").asJsonObject.entrySet()
                            .associateTo(SkillUtils.runeXp) { it.key.toInt() to it.value.asLong }
                        SkillUtils.hotmXp.clear()
                        get("hotm_xp").asJsonObject.entrySet()
                            .associateTo(SkillUtils.hotmXp) { it.key.toInt() to it.value.asLong }
                    }
                }
                APIUtil.getArrayResponse("${dataUrl}constants/mayors.json").apply {
                    Utils.checkThreadAndQueue {
                        MayorInfo.mayorData.clear()
                        mapTo(MayorInfo.mayorData) {
                            it as JsonObject
                            Mayor(
                                it["name"].asString,
                                it["role"].asString,
                                it["perks"].asJsonArray.map { p ->
                                    val obj = p.asJsonObject
                                    MayorPerk(obj["name"].asString, obj["description"].asString)
                                },
                                it["special"].asBoolean
                            )
                        }
                    }
                }
                APIUtil.getArrayResponse("${dataUrl}solvers/threeweirdos.json").apply {
                    Utils.checkThreadAndQueue {
                        ThreeWeirdosSolver.solutions.clear()
                        mapTo(ThreeWeirdosSolver.solutions) { it.asString }
                    }
                }
                APIUtil.getJSONResponse("${dataUrl}solvers/treasurehunter.json").apply {
                    Utils.checkThreadAndQueue {
                        TreasureHunter.treasureHunterLocations.clear()
                        entrySet().associateTo(TreasureHunter.treasureHunterLocations) { (key, value) ->
                            key to value.asString.split(",").map { it.toDouble() }
                                .run { BlockPos(this[0], this[1], this[2]) }
                        }
                    }
                }
                APIUtil.getJSONResponse("${dataUrl}solvers/oruotrivia.json").apply {
                    Utils.checkThreadAndQueue {
                        TriviaSolver.triviaSolutions.clear()
                        entrySet().associateTo(TriviaSolver.triviaSolutions) { (key, value) ->
                            key to value.asJsonArray.map { it.asString }.toTypedArray()
                        }
                    }
                }
                APIUtil.getArrayResponse("${dataUrl}constants/relics.json").apply {
                    Utils.checkThreadAndQueue {
                        RelicWaypoints.relicLocations.clear()
                        mapTo(RelicWaypoints.relicLocations) {
                            it as JsonArray
                            BlockPos(it[0].asInt, it[1].asInt, it[2].asInt)
                        }
                    }
                }
                APIUtil.getJSONResponse("${dataUrl}constants/sellprices.json").apply {
                    Utils.checkThreadAndQueue {
                        ItemFeatures.sellPrices.clear()
                        entrySet().associateTo(ItemFeatures.sellPrices) {
                            it.key to it.value.asDouble
                        }
                    }
                }
                APIUtil.getJSONResponse("${dataUrl}constants/slayerhealth.json").apply {
                    Utils.checkThreadAndQueue {
                        SlayerFeatures.BossHealths.clear()
                        entrySet().associateTo(SlayerFeatures.BossHealths) { it.key to it.value.asJsonObject }
                    }
                }
                APIUtil.getArrayResponse("${dataUrl}SpamFilters.json").apply {
                    Utils.checkThreadAndQueue {
                        val filters = SpamHider.repoFilters.toHashSet()
                        SpamHider.repoFilters.clear()
                        mapTo(SpamHider.repoFilters) {
                            it as JsonObject
                            SpamHider.Filter(
                                it["name"].asString,
                                filters.find { f -> f.name == it["name"].asString }?.state ?: 0,
                                true,
                                it["pattern"].asString.toRegex(),
                                when (it["type"].asString) {
                                    "STARTSWITH" -> SpamHider.FilterType.STARTSWITH
                                    "CONTAINS" -> SpamHider.FilterType.CONTAINS
                                    "REGEX" -> SpamHider.FilterType.REGEX
                                    else -> SpamHider.FilterType.CONTAINS
                                },
                                it["formatted"].asBoolean
                            )
                        }
                    }
                }
                APIUtil.getJSONResponse("${dataUrl}constants/summons.json").apply {
                    Utils.checkThreadAndQueue {
                        SummonSkins.skinMap.clear()
                        entrySet().associateTo(SummonSkins.skinMap) {
                            it.key to it.value.asString
                        }
                        SummonSkins.loadSkins()
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                UChat.chat("§cSharttils ran into an error while fetching data. Please report this to our Discord server!")
            }
        }
    }

    @JvmStatic
    fun reloadData(): Future<*> {
        return loadData()
    }

    internal fun preload() {}

    init {
        fixedRateTimer(name = "Sharttils-Reload-Data", period = 60 * 60 * 1000L) {
            reloadData()
        }
    }
}