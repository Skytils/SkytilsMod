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
package gg.skytils.skytilsmod.features.impl.misc

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.utils.Utils
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/*********************
CountdownCalculator
A Kotlin class by Erymanthus / RayDeeUx (code base, math, timestamp wrangling) + nea89 (regex wrangling + cleanup)
[designed for the Skytils mod by Sychic and My-Name-Is-Jeff / Lily]

Intended to detect countdowns within item tooltips and adding the value of such countdowns
to the user's current system time in Unix epoch form, then converting that value to a human readable timestamp.
 *********************/

object CountdownCalculator {

    val regex =
        ".*§.(?:(?<days>\\d+)d)? ?(?:(?<hours>\\d+)h)? ?(?:(?<minutes>\\d+)m)? ?(?:(?<seconds>\\d+)s)?\\b.*".toRegex()
    val formatter12h = DateTimeFormatter.ofPattern("EEEE, MMM d h:mm:ss a z")!!
    val formatter24h = DateTimeFormatter.ofPattern("EEEE, MMM d HH:mm:ss z")!!

    data class Countdown(
        val match: String,
        val label: String,
        val isRelative: Boolean = false,
    )

    val countdownTypes = listOf(
        Countdown("Starting in:", "Starts at"),
        Countdown("Starts in:", "Starts at"),
        Countdown("Interest in:", "Interest at"),
        Countdown("Until interest:", "Interest at"),
        Countdown("Ends in:", "Ends at"),
        Countdown("Remaining:", "Ends at"),
        Countdown("Duration:", "Finishes at"),
        Countdown("Time left:", "Ends at"),
        Countdown("Event lasts for", "Ends at", isRelative = true),
        Countdown("(§e", "Starts at"), // Calendar details
    )


    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inSkyblock) return
        val useFormatter = when(Skytils.config.showWhenCountdownEnds) {
            0 -> return
            1 -> formatter24h
            2 -> formatter12h
            else -> return
        }
        if (event.itemStack != null && mc.thePlayer?.openContainer != null) {
            var i = -1
            var lastTimer: ZonedDateTime? = null
            while (++i < event.toolTip.size) {
                val tooltipLine = event.toolTip[i]
                val countdownKind = countdownTypes.find { it.match in tooltipLine } ?: continue
                val match = regex.matchEntire(tooltipLine) ?: continue

                val days = match.groups["days"]?.value?.toInt() ?: 0
                val hours = match.groups["hours"]?.value?.toInt() ?: 0
                val minutes = match.groups["minutes"]?.value?.toInt() ?: 0
                val seconds = match.groups["seconds"]?.value?.toInt() ?: 0
                val totalSeconds = days * 86400L + hours * 3600L + minutes * 60L + seconds
                val countdownTarget = if (countdownKind.isRelative) {
                    if (lastTimer == null) {
                        event.toolTip.add(
                            ++i,
                            "§r§cThe above countdown is relative, but I can't find another countdown. (Skytils)"
                        )
                        continue
                    } else {
                        lastTimer.plusSeconds(totalSeconds)
                    }
                } else {
                    ZonedDateTime.now().plusSeconds(totalSeconds)
                }
                val countdownTargetFormatted = useFormatter.format(countdownTarget)
                event.toolTip.add(
                    ++i,
                    "§r§b${countdownKind.label}: $countdownTargetFormatted"
                )
                lastTimer = countdownTarget
            }
        }
    }
}
