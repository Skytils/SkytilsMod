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

package gg.skytils.skytilsmod.utils

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import gg.essential.universal.UChat
import gg.essential.universal.wrappers.UPlayer
import gg.skytils.skytilsmod.Skytils
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.init.Items
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.regex.Pattern

object Internationalization {
//    @Serializable
//    data class Language(
//        val name: String,
//        val locale: String,
//        @SerialName("hypixel_name") val hypixelName: String,
//        val available: Boolean
//    )
    val languages = arrayOf("English", "简体中文", "繁體中文", "Čeština", "Nederlands", "Suomi", "Français", "Deutsch", "Ελληνικά", "Italiano", "日本語", "한국어", "Norsk", "Pirate Speak", "Polski", "Português", "Português do Brasil", "Русский", "Español", "Türkçe", "Українська")

    var translations: Map<String, Map<String, String>> = emptyMap() // language -> key -> translation
    var patterns = emptyMap<String, Map<String, Pattern>>() // language -> key -> pattern
    val replacePattern = Pattern.compile("\\{\\{(.+?)}}")
//    var shouldDetect = false

    init {
        Skytils.IO.launch {
            Skytils.config.language = languages.indexOf(
                Skytils.client.get("https://api.slothpixel.me/api/players/${UPlayer.getUUID()}")
                    .body<JsonObject>()["language"].asString
            )
        }
    }

//    @SubscribeEvent
//    fun onTick(event: TickEvent.ClientTickEvent) {
//        if (!Skytils.config.autoDetectLanguage || !shouldDetect || event.phase != TickEvent.Phase.START || Skytils.mc.thePlayer == null || Skytils.mc.theWorld == null || translations == null || Utils.inSkyblock || SBInfo.locraw == null) return
//        shouldDetect = false
//        if (SBInfo.locraw!!.server.contains("lobby")) {
//            val gameMenu = Skytils.mc.thePlayer.inventory.mainInventory.first { it != null && it.item == Items.compass } ?: return
//            val lang = getLanguageForTranslation(gameMenu.displayName)
//            if (lang != null) {
//                Skytils.config.language = languages.indexOf(lang)
//            }
//        }
//    }
//
//    @SubscribeEvent
//    fun onWorldLoad(event: WorldEvent.Load) {
//        if (Skytils.config.autoDetectLanguage) shouldDetect = true
//    }

    fun getLanguage(): String {
        return languages[Skytils.config.language]
    }

    fun getPattern(key: String): Pattern {
        return patterns[getLanguage()]?.get(key) ?: Pattern.compile(key)
    }
}

fun String.localized(): String {
    val matcher = Internationalization.replacePattern.matcher(this)
    val sb = StringBuffer()
    while (matcher.find()) {
        matcher.appendReplacement(sb, Internationalization.translations[Internationalization.getLanguage()]?.get(matcher.group(1)) ?: matcher.group(1))
    }
    matcher.appendTail(sb)
    return sb.toString()
}
