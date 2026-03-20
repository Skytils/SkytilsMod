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
import gg.skytils.skytilsmod.Skytils.client
import gg.skytils.skytilsmod.Skytils.domain
import gg.skytils.skytilsmod.Skytils.failPrefix
import gg.skytils.skytilsmod.features.impl.handlers.Mayor
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo
import gg.skytils.skytilsmod.utils.Utils
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
                get<List<Mayor>>("${dataUrl}constants/mayors.json") {
                    Utils.checkThreadAndQueue {
                        MayorInfo.mayorData.clear()
                        MayorInfo.mayorData.addAll(this)
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