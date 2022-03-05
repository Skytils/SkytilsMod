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

package skytils.skytilsmod.features.impl.dungeons

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.RenderDragon
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase
import skytils.skytilsmod.mixins.transformers.accessors.AccessorModelDragon
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.colors.ColorFactory
import java.awt.Color

object MasterMode7Features {
    fun onMobSpawned(entity: Entity) {
        if (DungeonTimer.phase4ClearTime != -1L && entity is EntityDragon) {
            val type = WitherKingDragons.values().minByOrNull { entity.getDistanceSq(it.blockPos) } ?: return
            (entity as ExtensionEntityLivingBase).skytilsHook.colorMultiplier = type.color
            (entity as ExtensionEntityLivingBase).skytilsHook.masterDragonType = type
        }
    }

    @SubscribeEvent
    fun onRenderLivingPost(event: RenderLivingEvent.Post<*>) {
        val entity = event.entity
        if (Skytils.config.showWitherKingDragonsHP && DungeonTimer.phase4ClearTime != -1L && entity is EntityDragon) {
            GlStateManager.disableCull()
            GlStateManager.disableDepth()
            val percentage = event.entity.health / event.entity.baseMaxHealth
            val color = when {
                percentage >= 0.75 -> ColorFactory.YELLOWGREEN
                percentage >= 0.5 -> ColorFactory.YELLOW
                percentage >= 0.25 -> ColorFactory.DARKORANGE
                else -> ColorFactory.CRIMSON
            }
            RenderUtil.drawLabel(
                Vec3(
                    RenderUtil.interpolate(entity.posX, entity.lastTickPosX, RenderUtil.getPartialTicks()),
                    RenderUtil.interpolate(entity.posY, entity.lastTickPosY, RenderUtil.getPartialTicks()) + 0.5f,
                    RenderUtil.interpolate(entity.posZ, entity.lastTickPosZ, RenderUtil.getPartialTicks())
                ),
                NumberUtil.format(event.entity.health),
                color,
                RenderUtil.getPartialTicks(),
                true,
                6f
            )
            GlStateManager.enableDepth()
            GlStateManager.enableCull()
        }
    }

    fun onRenderMainModel(
        entity: EntityDragon,
        f: Float,
        g: Float,
        h: Float,
        i: Float,
        j: Float,
        scaleFactor: Float,
        ci: CallbackInfo
    ) {
        if (!Skytils.config.recolorWitherKingsDragons) return
        entity as ExtensionEntityLivingBase
        entity.skytilsHook.colorMultiplier?.bindColor()
    }

    fun getHurtOpacity(
        renderDragon: RenderDragon,
        lastDragon: EntityDragon,
        value: Float
    ): Float {
        if (!Skytils.config.recolorWitherKingsDragons) return value
        lastDragon as ExtensionEntityLivingBase
        return if (lastDragon.skytilsHook.colorMultiplier != null) {
            val model = renderDragon.mainModel as AccessorModelDragon
            model.body.isHidden = true
            model.wing.isHidden = true
            0.03f
        } else value
    }

    fun getEntityTexture(entity: EntityDragon, cir: CallbackInfoReturnable<ResourceLocation>) {
        if (!Skytils.config.retextureWitherKingsDragons) return
        entity as ExtensionEntityLivingBase
        val type = entity.skytilsHook.masterDragonType ?: return
        cir.returnValue = type.texture
    }

    fun afterRenderHurtFrame(
        renderDragon: RenderDragon,
        entitylivingbaseIn: EntityDragon,
        f: Float,
        g: Float,
        h: Float,
        i: Float,
        j: Float,
        scaleFactor: Float,
        ci: CallbackInfo
    ) {
        val model = renderDragon.mainModel as AccessorModelDragon
        model.body.isHidden = false
        model.wing.isHidden = false
    }

    fun shouldHideDragonDeath() =
        Utils.inDungeons && DungeonTimer.phase4ClearTime != -1L && Skytils.config.hideDragonDeathAnimation
}

enum class WitherKingDragons(val blockPos: BlockPos, val color: Color) {
    POWER(BlockPos(27, 14, 59), ColorFactory.RED),
    APEX(BlockPos(27, 14, 94), ColorFactory.LIME),
    SOUL(BlockPos(56, 14, 125), ColorFactory.PURPLE),
    ICE(BlockPos(85, 14, 94), ColorFactory.CYAN),
    FLAME(BlockPos(85, 14, 56), ColorFactory.CORAL);

    val texture = ResourceLocation("skytils", "textures/dungeons/m7/dragon_${this.name.lowercase()}.png")
}