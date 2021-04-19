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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.features.impl.dungeons.solvers.ThreeWeirdosSolver
import skytils.skytilsmod.features.impl.dungeons.solvers.TriviaSolver
import skytils.skytilsmod.features.impl.mining.MiningFeatures
import skytils.skytilsmod.features.impl.misc.ItemFeatures
import skytils.skytilsmod.features.impl.spidersden.RelicWaypoints
import skytils.skytilsmod.utils.APIUtil
import skytils.skytilsmod.utils.Utils

class DataFetcher {
    @SubscribeEvent
    fun onTick(event: ClientTickEvent?) {
        if (!Utils.inSkyblock) return
        if (System.currentTimeMillis() - lastReload > 60 * 60 * 1000) {
            lastReload = System.currentTimeMillis()
            reloadData()
        }
    }

    companion object {
        private var lastReload: Long = 0
        private fun loadData() {
            val dataUrl: String = Skytils.config.dataURL
            Thread {
                val fetchurData = APIUtil.getJSONResponse(dataUrl + "solvers/fetchur.json")
                for ((key, value) in fetchurData.entrySet()) {
                    MiningFeatures.fetchurItems[key] = value.asString
                }
                val threeWeirdosSolutions = APIUtil.getArrayResponse(dataUrl + "solvers/threeweirdos.json")
                for (solution in threeWeirdosSolutions) {
                    ThreeWeirdosSolver.solutions.add(solution.asString)
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
    }
}