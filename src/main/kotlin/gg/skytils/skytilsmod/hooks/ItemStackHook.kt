package gg.skytils.skytilsmod.hooks

import gg.skytils.skytilsmod.core.Config
import net.minecraft.text.Text
import net.minecraft.util.Formatting

const val star = "✪"
val masterStars = ('➊'..'➎').map { it.toString() }

fun modifyStarDisplay(displayName: Text): Text {
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
                    if (masters > 0) {
                        val masterStarText = Text.literal(star.repeat(masters)).formatted(Formatting.RED)
                        val normalStarText = Text.literal(star.repeat(stars - masters)).formatted(Formatting.GOLD)

                        out.append(masterStarText)
                        out.append(normalStarText)
                    } else {
                        out.append(current)
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
    return string.count { it.toString() == star }
}

private fun Text.masterStarCount(): Int {
    return masterStars.indexOfFirst { it == string } + 1
}