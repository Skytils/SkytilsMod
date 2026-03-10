package gg.skytils.skytilsmod.features.impl.dungeons

import gg.skytils.skytilsmod.Reference
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.util.SBInfo
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.FilledMapItem
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import kotlin.math.roundToInt

object CatlasDungeonMapOverlay {
    private const val baseMapSize = 16f
    private const val mapMargin = 8
    private val layerId = Identifier.of(Reference.MOD_ID, "catlas_dungeon_map")

    fun init() {
        HudLayerRegistrationCallback.EVENT.register { layeredDrawer ->
            layeredDrawer.addLayer(IdentifiedLayer.of(layerId) { context, _ ->
                render(context)
            })
        }
    }

    private fun render(context: DrawContext) {
        val mc = Skytils.mc
        if (!Config.enableCatlasDungeonMap || !SBInfo.dungeonsState.getUntracked() || mc.options.hudHidden) return

        val player = mc.player ?: return
        val mapStack = findDungeonMap(player.inventory.main.asSequence())
            ?: findDungeonMap(player.inventory.offHand.asSequence())
            ?: return

        val scale = Config.catlasDungeonMapScale.coerceAtLeast(1f)
        val mapSize = (baseMapSize * scale).roundToInt()
        val x = mc.window.scaledWidth - mapSize - mapMargin
        val y = mapMargin

        context.fill(x - 2, y - 2, x + mapSize + 2, y + mapSize + 2, 0x66000000)

        val matrices = context.matrices
        matrices.push()
        matrices.translate(x.toDouble(), y.toDouble(), 0.0)
        matrices.scale(scale, scale, 1f)
        context.drawItem(mapStack, 0, 0)
        matrices.pop()
    }

    private fun findDungeonMap(stacks: Sequence<ItemStack>): ItemStack? {
        return stacks.firstOrNull { stack ->
            !stack.isEmpty && stack.item is FilledMapItem
        }
    }
}
