package gg.skytils.skytilsmod.features.impl.misc

import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.utils.toFormattedString
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.InventoryDrawSlotEvent
import gg.skytils.event.impl.HotbarDrawSlotEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.util.ItemRarity
import gg.skytils.skytilsmod.util.ItemRarity.Companion.RARITY_REGEX
import gg.skytils.skytilsmod.util.PetInfo
import gg.skytils.skytilsmod.util.SBInfo
import net.minecraft.component.DataComponentTypes
import java.awt.Color

object ShowItemRarity : EventSubscriber {
    override fun setup() {
        register(::onDrawSlot)
        register(::onHotbarDrawSlot)
    }

    fun onDrawSlot(event: InventoryDrawSlotEvent) {
        if (!SBInfo.skyblockState.getUntracked() || !Config.showItemRarity) return

        if (Config.showPetRarity) {
            val rawPetInfo =
                event.slot.stack.components.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.get("petInfo")?.asString()
                    ?.get()

            if (rawPetInfo != null) {
                val petInfo = try {
                    Skytils.json.decodeFromString<PetInfo>(rawPetInfo)
                } catch (_: Exception) {
                    null
                } ?: return

                val color = ItemRarity.entries.find { it.rarityName == petInfo.tier }?.color ?: return

                drawRarityBackground(event, color)

                return
            }
        }

        val stack = event.slot.stack
        val lines = stack.components.get(DataComponentTypes.LORE)?.lines ?: emptyList()

        lines.asReversed().firstNotNullOfOrNull { line -> RARITY_REGEX.find(line.toFormattedString()) }?.let { match ->
            val (rarity) = match.destructured
            val color = ItemRarity.entries.find { it.rarityName == rarity }?.color ?: return

            drawRarityBackground(event, color)
        }
    }

    fun onHotbarDrawSlot(event: HotbarDrawSlotEvent) {
        if (!SBInfo.skyblockState.getUntracked() || !Config.showItemRarity) return

        val lines = event.stack.components.get(DataComponentTypes.LORE)?.lines ?: emptyList()

        lines.asReversed().firstNotNullOfOrNull { line -> RARITY_REGEX.find(line.toFormattedString()) }?.let { match ->
            val (rarity) = match.destructured
            val color = ItemRarity.entries.find { it.rarityName == rarity }?.color ?: return

            drawHotbarRarityBackground(event, color)
        }
    }

    private fun drawRarityBackground(event: InventoryDrawSlotEvent, color: Color) {
        // 0 = Square, 1 = Square Outline, 2 = Outline
        when (Config.itemRarityShape) {
            0 -> {
                event.fillSlot(color.withAlpha(Config.itemRarityOpacity))
            }

            1 -> {
                event.fillSlot(color.withAlpha((Config.itemRarityOpacity / 2)))
                event.outlineSlot(color.withAlpha(Config.itemRarityOpacity))
            }

            2 -> {
                event.outlineSlot(color.withAlpha(Config.itemRarityOpacity))
            }
        }
    }

    private fun drawHotbarRarityBackground(event: HotbarDrawSlotEvent, color: Color) {
        // 0 = Square, 1 = Square Outline, 2 = Outline
        when (Config.itemRarityShape) {
            0 -> {
                event.fillSlot(color.withAlpha(Config.itemRarityOpacity))
            }

            1 -> {
                event.fillSlot(color.withAlpha((Config.itemRarityOpacity / 2)))
                event.outlineSlot(color.withAlpha(Config.itemRarityOpacity))
            }

            2 -> {
                event.outlineSlot(color.withAlpha(Config.itemRarityOpacity))
            }
        }
    }
}

fun InventoryDrawSlotEvent.fillSlot(color: Color) {
    context.fill(
        slot.x,
        slot.y,
        slot.x + 16,
        slot.y + 16,
        color.rgb
    )
}

fun InventoryDrawSlotEvent.outlineSlot(color: Color) {
    context.drawStrokedRectangle(
        slot.x,
        slot.y,
        16,
        16,
        color.rgb
    )
}

fun HotbarDrawSlotEvent.fillSlot(color: Color) {
    context.fill(
        x,
        y,
        x + 16,
        y + 16,
        color.rgb
    )
}

fun HotbarDrawSlotEvent.outlineSlot(color: Color) {
    context.drawStrokedRectangle(
        x,
        y,
        16,
        16,
        color.rgb
    )
}