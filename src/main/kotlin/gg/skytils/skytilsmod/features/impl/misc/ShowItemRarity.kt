package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.ChatColor
import gg.essential.universal.utils.toFormattedString
import gg.skytils.event.EventSubscriber
import gg.skytils.event.register
import gg.skytils.skytilsmod._event.DrawSlotEvent
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.util.SBInfo
import net.minecraft.component.DataComponentTypes

object ShowItemRarity : EventSubscriber {
    private val RARITY_REGEX =
        Regex("(?:§.|§la§r )+(?:SHINY )?(?<rarity>NONE|COMMON|UNCOMMON|RARE|EPIC|LEGENDARY|MYTHIC|DIVINE|SUPREME|ULTIMATE|SPECIAL|VERY SPECIAL)")

    private val RARITY_COLOR_MAP = mapOf(
        "NONE" to ChatColor.GRAY,
        "COMMON" to ChatColor.WHITE,
        "UNCOMMON" to ChatColor.GREEN,
        "RARE" to ChatColor.BLUE,
        "EPIC" to ChatColor.DARK_PURPLE,
        "LEGENDARY" to ChatColor.GOLD,
        "MYTHIC" to ChatColor.LIGHT_PURPLE,
        "DIVINE" to ChatColor.AQUA,
        "SUPREME" to ChatColor.DARK_RED,
        "ULTIMATE" to ChatColor.DARK_RED,
        "SPECIAL" to ChatColor.RED,
        "VERY SPECIAL" to ChatColor.RED
    )

    override fun setup() {
        register(::onDrawSlot)
    }

    fun onDrawSlot(event: DrawSlotEvent) {
        if (!SBInfo.skyblockState.getUntracked() || !Config.showItemRarity) return

        val stack = event.slot.stack
        val lines = stack.components.get(DataComponentTypes.LORE)?.lines ?: emptyList()
        lines.asReversed().firstNotNullOfOrNull { RARITY_REGEX.find(it.toFormattedString()) }?.let { match ->
            val (rarity) = match.destructured
            val color = RARITY_COLOR_MAP.getOrDefault(rarity, ChatColor.GRAY).color ?: return

            event.context.fill(
                event.slot.x,
                event.slot.y,
                event.slot.x + 16,
                event.slot.y + 16,
                color.withAlpha(Config.itemRarityOpacity).rgb
            )
        }
    }
}