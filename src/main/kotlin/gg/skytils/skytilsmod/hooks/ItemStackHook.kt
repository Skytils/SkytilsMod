package gg.skytils.skytilsmod.hooks

import gg.essential.universal.utils.toUnformattedString
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.util.SBInfo
import net.minecraft.text.Text
import net.minecraft.util.Formatting

const val star = '✪'
val masterStars = ('➊'..'➎').map { it.toString() }

fun modifyDisplayName(displayName: Text): Text {
    if (!SBInfo.skyblockState.getUntracked() || Config.starDisplayType == 0 || !displayName.toUnformattedString().contains(star)) return displayName

    val out = Text.empty().setStyle(displayName.style)
    val siblings = displayName.siblings

    var i = 0
    while (i < siblings.size) {
        val current = siblings[i]
        val stars = current.starCount()

        if (stars > 0) {
            val masters = siblings.getOrNull(i + 1)?.masterStarCount() ?: 0

            // 1 = Old, 2 = Compact, 0 = Disabled
            when (Config.starDisplayType) {
                1 -> {
                    out.append(current)

                    if (masters > 0) {
                        out.append(Text.literal(star.toString().repeat(masters)).formatted(Formatting.RED))
                    }
                }

                2 -> {
                    val total = stars + masters
                    val color = if (total > 5) Formatting.RED else Formatting.GOLD
                    out.append(Text.literal("$total$star").formatted(color))
                }
            }

            siblings.subList(i + if (masters > 0) 2 else 1, siblings.size).forEach { out.append(it) }

            break
        }

        out.append(current)
        i++
    }

    return out
}

private fun Text.starCount(): Int {
    return string.count { it == star }
}

private fun Text.masterStarCount(): Int {
    return masterStars.indexOfFirst { it == string } + 1
}