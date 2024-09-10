package gg.skytils.skytilsmod.mixins.hooks.renderer

import gg.skytils.skytilsmod.core.Config.threeWeirdosSolverColor
import gg.skytils.skytilsmod.features.impl.dungeons.solvers.ThreeWeirdosSolver
import gg.skytils.skytilsmod.utils.bindColor
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.tileentity.TileEntityChest
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

fun setChestColor(
    te: TileEntityChest,
    x: Double,
    y: Double,
    z: Double,
    partialTicks: Float,
    destroyStage: Int,
    ci: CallbackInfo
) {
    if (te.pos == ThreeWeirdosSolver.riddleChest) {
        threeWeirdosSolverColor.bindColor()
        GlStateManager.disableTexture2D()
    }
}

fun setChestColorPost(
    te: TileEntityChest,
    x: Double,
    y: Double,
    z: Double,
    partialTicks: Float,
    destroyStage: Int,
    ci: CallbackInfo
) {
    GlStateManager.enableTexture2D()
}