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
A Kotlin object by Erymanthus / RayDeeUx (original code base, math, timestamp wrangling) + nea89 (regex wrangling, full code rewrite, cleanup)
[written for the Kotlin-based Minecraft Forge 1.8.9 mod Skytils by Sychic and My-Name-Is-Jeff / Lily]

Intended to detect countdowns within item tooltips and adding the value of such countdowns in units of seconds
to the user's current system time in Unix epoch form, then converting that value to a human readable timestamp.
 *********************/

object CountdownCalculator {

    val regex =
        "(?:(?<days>\\d+)d)? ?(?:(?<hours>\\d+)h)? ?(?:(?<minutes>\\d+)m)? ?(?:(?<seconds>\\d+)s)?\\b".toRegex()
    val formatter12h = DateTimeFormatter.ofPattern("EEEE, MMM d h:mm:ss a z")!!
    val formatter24h = DateTimeFormatter.ofPattern("EEEE, MMM d HH:mm:ss z")!! //we love our non 12 hour timestamp format users <3

    /********************
    note to future contributors:
    to add a new timestamp, type make sure you have /sba dev enabled first and inspect the item lore
    with a countdown before adding it to the list of substrings seen here.
    
    the format is as follows:
    DESCRIPTIVECOUNTDOWNTYPE(substringFromItemLore, desiredPrefixOfYourChoice),
    
    or, alternaitvely, as per the variable names in the enum class:
    DESCRIPTIVECOUNTDOWNTYPE(match, label),

    example:
    TIMEUNTILTECHNOBLADEDIES("Time until Technoblade dies", "Against all odds, he will die at"),

    if there are any formatting codes in the item lore line within the prefix of the countdown in question,
    you must include them in your `substringFromItemLore` (or `match`) value. there will be some section signs within the
    line of your item lore's line for you to copy if necessary.

    conversely, there is no need to include any formatting codes (or colons) for your `desiredPrefixOfYourChoice` (or `label`) value.
    this kotlin object will automatically handle that for you in-game. make sure your `desiredPrefixOfYourChoice` (or `label`)
    value is descriptive, but not too lengthy.
    ********************/
    @Suppress("unused")
    private enum class CountdownTypes(
        val match: String,
        val label: String,
        val isRelative: Boolean = false,
    ) {
        STARTING("Starting in:", "Starts at"),
        STARTS("Starts in:", "Starts at"),
        INTEREST("Interest in:", "Interest at"),
        UNTILINTEREST("Until interest:", "Interest at"),
        ENDS("Ends in:", "Ends at"),
        REMAINING("Remaining:", "Ends at"),
        DURATION("Duration:", "Finishes at"),
        TIMELEFT("Time left:", "Ends at"),
        EVENTTIMELEFT("Event lasts for", "Ends at", isRelative = true),
        HOLLOWSPASSVARIANTONE("another§6 ", "Your pass expires at"), //why variant one? see below
        HOLLOWSPASSVARIANTTWO("another §6", "Your pass expires at"), //variant two because hypixel admins have a VERY inconsistent record of placing color codes and I (erymanthus) am NOT taking any risks of this feature breaking over some inconsistency. i mean have you seen how shit goes down when mod devs don't account for variances
        CALENDARDETAILS("(§e", "Starts at"); // Calendar details
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (!Utils.inSkyblock) return
        val useFormatter = when (Skytils.config.showWhenCountdownEnds) {
            1 -> formatter24h
            2 -> formatter12h
            else -> return
        }
        if (event.itemStack != null && mc.thePlayer?.openContainer != null) {
            var i = -1
            var lastTimer: ZonedDateTime? = null
            while (++i < event.toolTip.size) {
                val tooltipLine = event.toolTip[i]
                val countdownKind = CountdownTypes.values().find { it.match in tooltipLine } ?: continue
                val match = regex.findAll(tooltipLine).maxByOrNull { it.value.length } ?: continue
                val days = match.groups["days"]?.value?.toInt() ?: 0
                val hours = match.groups["hours"]?.value?.toInt() ?: 0
                val minutes = match.groups["minutes"]?.value?.toInt() ?: 0
                val seconds = match.groups["seconds"]?.value?.toInt() ?: 0
                val totalSeconds = days * 86400L + hours * 3600L + minutes * 60L + seconds
                if (totalSeconds == 0L) continue
                val countdownTarget = if (countdownKind.isRelative) {
                    if (lastTimer == null) {
                        event.toolTip.add(
                            ++i,
                            "§r§cThe above countdown is relative, but Skytils couldn't find another countdown."
                        )
                        continue
                    } else lastTimer.plusSeconds(totalSeconds)
                } else ZonedDateTime.now().plusSeconds(totalSeconds)
                val countdownTargetFormatted = useFormatter.format(countdownTarget)
                event.toolTip.add(
                    ++i,
                    "§r§b${countdownKind.label} $countdownTargetFormatted"
                )
                lastTimer = countdownTarget
            }
        }
    }
}
