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
package skytils.skytilsmod.core

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import gg.essential.universal.UChat
import net.minecraft.util.BlockPos
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.gson
import skytils.skytilsmod.features.impl.dungeons.solvers.ThreeWeirdosSolver
import skytils.skytilsmod.features.impl.dungeons.solvers.TriviaSolver
import skytils.skytilsmod.features.impl.farming.FarmingFeatures
import skytils.skytilsmod.features.impl.farming.TreasureHunter
import skytils.skytilsmod.features.impl.handlers.Mayor
import skytils.skytilsmod.features.impl.handlers.MayorInfo
import skytils.skytilsmod.features.impl.handlers.MayorPerk
import skytils.skytilsmod.features.impl.handlers.SpamHider
import skytils.skytilsmod.features.impl.mining.MiningFeatures
import skytils.skytilsmod.features.impl.misc.ItemFeatures
import skytils.skytilsmod.features.impl.misc.SlayerFeatures
import skytils.skytilsmod.features.impl.misc.SummonSkins
import skytils.skytilsmod.features.impl.spidersden.RelicWaypoints
import skytils.skytilsmod.utils.*
import java.util.concurrent.Future
import kotlin.concurrent.fixedRateTimer

object DataFetcher {
    private fun loadData(): Future<*> {
        val dataUrl = Skytils.config.dataURL
        return Skytils.threadPool.submit {
            try {
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
                        get("runecrafting_xp").asJsonObject.entrySet().associateTo(SkillUtils.runeXp) { it.key.toInt() to it.value.asLong }
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
                APIUtil.getArrayResponse("${Skytils.config.dataURL}SpamFilters.json").apply {
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
                UChat.chat("§cSkytils ran into an error while fetching data. Please report this to our Discord server!")
            }
        }
    }

    @JvmStatic
    fun reloadData(): Future<*> {
        return loadData()
    }

    internal fun preload() {}

    init {
        fixedRateTimer(name = "Skytils-Reload-Data", period = 60 * 60 * 1000L) {
            reloadData()
        }
    }
}