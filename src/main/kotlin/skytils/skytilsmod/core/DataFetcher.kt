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

import com.google.gson.JsonArray
import net.minecraft.util.BlockPos
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.features.impl.dungeons.solvers.ThreeWeirdosSolver
import skytils.skytilsmod.features.impl.dungeons.solvers.TriviaSolver
import skytils.skytilsmod.features.impl.farming.FarmingFeatures
import skytils.skytilsmod.features.impl.farming.TreasureHunter
import skytils.skytilsmod.features.impl.mining.MiningFeatures
import skytils.skytilsmod.features.impl.misc.ItemFeatures
import skytils.skytilsmod.features.impl.spidersden.RelicWaypoints
import skytils.skytilsmod.utils.APIUtil
import kotlin.concurrent.fixedRateTimer

object DataFetcher {
    private fun loadData() {
        val dataUrl: String = Skytils.config.dataURL
        Thread {
            val fetchurData = APIUtil.getJSONResponse(dataUrl + "solvers/fetchur.json")
            for ((key, value) in fetchurData.entrySet()) {
                MiningFeatures.fetchurItems[key] = value.asString
            }
            val hikerData = APIUtil.getJSONResponse(dataUrl + "solvers/hungryhiker.json")
            for ((key, value) in hikerData.entrySet()) {
                FarmingFeatures.hungerHikerItems[key] = value.asString
            }
            val threeWeirdosSolutions = APIUtil.getArrayResponse(dataUrl + "solvers/threeweirdos.json")
            for (solution in threeWeirdosSolutions) {
                ThreeWeirdosSolver.solutions.add(solution.asString)
            }
            val treasureData = APIUtil.getJSONResponse(dataUrl + "solvers/treasurehunter.json")
            for ((key, value) in treasureData.entrySet()) {
                val parts = value.asString.split(",").map { it.toDouble() }
                TreasureHunter.treasureHunterLocations[key] = BlockPos(parts[0], parts[1], parts[2])
            }
            val triviaData = APIUtil.getJSONResponse(dataUrl + "solvers/oruotrivia.json")
            for ((key, value) in triviaData.entrySet()) {
                TriviaSolver.triviaSolutions[key] = getStringArrayFromJsonArray(value.asJsonArray)
            }
            val relicData = APIUtil.getArrayResponse(dataUrl + "constants/relics.json")
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
            for ((key, value) in APIUtil.getJSONResponse(dataUrl + "constants/sellprices.json").entrySet()) {
                ItemFeatures.sellPrices[key] = value.asDouble
            }
        }.start()
    }

    private fun clearData() {
        ItemFeatures.sellPrices.clear()
        MiningFeatures.fetchurItems.clear()
        RelicWaypoints.relicLocations.clear()
        ThreeWeirdosSolver.solutions.clear()
        TriviaSolver.triviaSolutions.clear()
    }

    @JvmStatic
    fun reloadData() {
        clearData()
        loadData()
    }

    @JvmStatic
    fun getStringArrayFromJsonArray(jsonArray: JsonArray): Array<String> {
        val arraySize = jsonArray.size()
        val stringArray = arrayOfNulls<String>(arraySize)
        for (i in 0 until arraySize) {
            stringArray[i] = jsonArray[i].asString
        }
        return stringArray.filterNotNull().toTypedArray()
    }

    init {
        reloadData()
        fixedRateTimer(name = "Skytils-Reload-Data", period = 60 * 60 * 1000) {
            reloadData()
        }
    }
}