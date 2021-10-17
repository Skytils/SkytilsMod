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
package skytils.skytilsmod.core

import com.google.gson.JsonObject
import gg.essential.api.utils.Multithreading
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
import skytils.skytilsmod.utils.APIUtil
import skytils.skytilsmod.utils.Enchant
import skytils.skytilsmod.utils.EnchantUtil
import skytils.skytilsmod.utils.SkillUtils
import java.util.concurrent.Future
import kotlin.concurrent.fixedRateTimer

object DataFetcher {
    private fun loadData(): Future<*> {
        val dataUrl = Skytils.config.dataURL
        return Multithreading.submit {
            try {

                APIUtil.getJSONResponse("${dataUrl}constants/enchants.json").apply {
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

                val fetchurData = APIUtil.getJSONResponse("${dataUrl}solvers/fetchur.json")
                for ((key, value) in fetchurData.entrySet()) {
                    MiningFeatures.fetchurItems[key] = value.asString
                }
                val hikerData = APIUtil.getJSONResponse("${dataUrl}solvers/hungryhiker.json")
                for ((key, value) in hikerData.entrySet()) {
                    FarmingFeatures.hungerHikerItems[key] = value.asString
                }
                val levelingData = APIUtil.getJSONResponse("${dataUrl}constants/levelingxp.json")
                for ((key, value) in levelingData.get("default_skill_caps").asJsonObject.entrySet()) {
                    SkillUtils.maxSkillLevels[key] = value.asInt
                }
                for ((key, value) in levelingData.get("leveling_xp").asJsonObject.entrySet()) {
                    SkillUtils.skillXp[key.toInt()] = value.asLong
                }
                for ((key, value) in levelingData.get("dungeoneering_xp").asJsonObject.entrySet()) {
                    SkillUtils.dungeoneeringXp[key.toInt()] = value.asLong
                }
                for ((key, value) in levelingData.get("slayer_xp").asJsonObject.entrySet()) {
                    SkillUtils.slayerXp[key] =
                        value.asJsonObject.entrySet().associateTo(LinkedHashMap()) { it.key.toInt() to it.value.asLong }
                }
                val mayorData = APIUtil.getArrayResponse("${dataUrl}constants/mayors.json")
                for (m in mayorData) {
                    val mayorObj = m.asJsonObject
                    MayorInfo.mayorData.add(
                        Mayor(
                            mayorObj["name"].asString,
                            mayorObj["role"].asString,
                            mayorObj["perks"].asJsonArray.map {
                                val obj = it.asJsonObject
                                MayorPerk(obj["name"].asString, obj["description"].asString)
                            },
                            mayorObj["special"].asBoolean
                        )
                    )
                }
                val threeWeirdosSolutions = APIUtil.getArrayResponse("${dataUrl}solvers/threeweirdos.json")
                for (solution in threeWeirdosSolutions) {
                    ThreeWeirdosSolver.solutions.add(solution.asString)
                }
                val treasureData = APIUtil.getJSONResponse("${dataUrl}solvers/treasurehunter.json")
                for ((key, value) in treasureData.entrySet()) {
                    val parts = value.asString.split(",").map { it.toDouble() }
                    TreasureHunter.treasureHunterLocations[key] = BlockPos(parts[0], parts[1], parts[2])
                }
                val triviaData = APIUtil.getJSONResponse("${dataUrl}solvers/oruotrivia.json")
                for ((key, value) in triviaData.entrySet()) {
                    TriviaSolver.triviaSolutions[key] = value.asJsonArray.map { it.asString }.toTypedArray()
                }
                val relicData = APIUtil.getArrayResponse("${dataUrl}constants/relics.json")
                for (i in 0 until relicData.size()) {
                    val coordsArr = relicData[i].asJsonArray
                    RelicWaypoints.relicLocations.add(
                        BlockPos(
                            coordsArr[0].asInt,
                            coordsArr[1].asInt,
                            coordsArr[2].asInt
                        )
                    )
                }
                for ((key, value) in APIUtil.getJSONResponse("${dataUrl}constants/sellprices.json").entrySet()) {
                    ItemFeatures.sellPrices[key] = value.asDouble
                }
                val slayerHealthData = APIUtil.getJSONResponse("${dataUrl}constants/slayerhealth.json")
                for ((key, value) in slayerHealthData.entrySet()) {
                    SlayerFeatures.BossHealths[key] = value.asJsonObject
                }
                APIUtil.getArrayResponse("${Skytils.config.dataURL}SpamFilters.json").mapTo(SpamHider.repoFilters) {
                    it as JsonObject
                    SpamHider.Filter(
                        it["name"].asString, 0, true, it["pattern"].asString, when (it["type"].asString) {
                            "STARTSWITH" -> SpamHider.FilterType.STARTSWITH
                            "CONTAINS" -> SpamHider.FilterType.CONTAINS
                            "REGEX" -> SpamHider.FilterType.REGEX
                            else -> SpamHider.FilterType.CONTAINS
                        }, it["formatted"].asBoolean
                    )
                }
                APIUtil.getJSONResponse("${dataUrl}constants/summons.json").entrySet().forEach {
                    SummonSkins.skinMap[it.key] = it.value.asString
                }
                SummonSkins.loadSkins()
            } catch (e: Throwable) {
                e.printStackTrace()
                UChat.chat("§cSkytils ran into an error while fetching data. Please report this to our Discord server!")
            }
        }
    }

    private fun clearData() {
        EnchantUtil.enchants.clear()
        ItemFeatures.sellPrices.clear()
        MayorInfo.mayorData.clear()
        MiningFeatures.fetchurItems.clear()
        RelicWaypoints.relicLocations.clear()
        SkillUtils.dungeoneeringXp.clear()
        SkillUtils.slayerXp.clear()
        ThreeWeirdosSolver.solutions.clear()
        TriviaSolver.triviaSolutions.clear()
        SlayerFeatures.BossHealths.clear()
        SpamHider.repoFilters.clear()
        SummonSkins.skinMap.clear()
    }

    @JvmStatic
    fun reloadData(): Future<*> {
        clearData()
        return loadData()
    }

    internal fun preload() {}

    init {
        fixedRateTimer(name = "Skytils-Reload-Data", period = 60 * 60 * 1000L) {
            reloadData()
        }
    }
}