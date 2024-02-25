/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.funny.skytilsplus

import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEntity
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEnumChatFormatting
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEnumDyeColor
import gg.skytils.skytilsmod.utils.ReflectionHelper.getClassHelper
import gg.skytils.skytilsmod.utils.ReflectionHelper.getFieldHelper
import gg.skytils.skytilsmod.utils.ReflectionHelper.getMethodHelper
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import kotlinx.serialization.Serializable
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.passive.EntityOcelot
import net.minecraft.entity.passive.EntitySheep
import net.minecraft.item.EnumDyeColor
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SheepifyRebellion {
    val fakeWorld by lazy {
        getClassHelper("gg.essential.gui.common.EmulatedUI3DPlayer\$FakeWorld")?.let { clazz ->
            val instance = clazz.getFieldHelper("INSTANCE")?.get(null)
            clazz.getMethodHelper("getFakeWorld")?.invoke(instance) as? WorldClient
        } ?: error("Failed to get FakeWorld")
    }

    val dummyModelMap = hashMapOf<AbstractClientPlayer, EntityLivingBase>()

    val skytilsPlusUsernames = mutableSetOf<String>()
    val skytilsPlusColors = mutableMapOf<String, EnumDyeColor>()

    val lookup = EnumDyeColor.entries.associateBy { ((it as AccessorEnumDyeColor).chatColor as AccessorEnumChatFormatting).formattingCode }

    val isEnabled by lazy {
        Utils.isBSMod || SuperSecretSettings.sheepifyRebellion
    }

    @SubscribeEvent
    fun playerSpawn(event: EntityJoinWorldEvent) {
        if (event.entity !is AbstractClientPlayer || event.entity.uniqueID.version() != 4) return

        var fakeEntity: EntityLivingBase? = null

        if (SuperSecretSettings.catGaming && event.entity is EntityPlayerSP) {
            fakeEntity = EntityOcelot(fakeWorld)
            fakeEntity.tameSkin = 3
        } else if (Utils.inSkyblock && isEnabled) {
            val color = skytilsPlusColors[event.entity.name] ?: return
            fakeEntity = EntitySheep(fakeWorld)
            fakeEntity.fleeceColor = color

            if (skytilsPlusUsernames.contains(event.entity.name)) {
                fakeEntity.customNameTag = "jeb_"
                fakeEntity.alwaysRenderNameTag = false
            }
        }

        if (fakeEntity != null) {
            dummyModelMap[event.entity as AbstractClientPlayer] = fakeEntity
            fakeEntity.forceSpawn = true
            fakeEntity.motionX = 0.0
            fakeEntity.motionY = 0.0
            fakeEntity.motionZ = 0.0
            fakeEntity.noClip = true
        }
    }

    @SubscribeEvent
    fun playerLeave(event: LivingDeathEvent) {
        dummyModelMap.remove(event.entity)?.setDead()
    }

    @SubscribeEvent
    fun onWorldUnload(event: WorldEvent.Unload) {
        dummyModelMap.clear()
    }

    @SubscribeEvent
    fun onRender(event: RenderLivingEvent.Pre<*>) {
        val fakeEntity = dummyModelMap[event.entity] ?: return
        event.isCanceled = true
        val renderer = event.renderer.renderManager.getEntityRenderObject<EntityLivingBase>(fakeEntity)
        copyProperties(fakeEntity, event.entity)
        renderer.doRender(fakeEntity, event.x, event.y, event.z, event.entity.rotationYaw, 1f)
        (event.renderer as RendererLivingEntity<EntityLivingBase>).renderName(event.entity, event.x, event.y, event.z)
    }

    fun copyProperties(entity: EntityLivingBase, originalEntity: EntityLivingBase) {
        entity as AccessorEntity
        originalEntity as AccessorEntity

        entity.copyLocationAndAnglesFrom(originalEntity)
        entity.setRotationYawHead(originalEntity.rotationYawHead)
        entity.prevLimbSwingAmount = originalEntity.prevLimbSwingAmount
        entity.limbSwingAmount = originalEntity.limbSwingAmount
        entity.limbSwing = originalEntity.limbSwing
        entity.prevSwingProgress = originalEntity.prevSwingProgress
        entity.swingProgress = originalEntity.swingProgress
        entity.isSwingInProgress = originalEntity.isSwingInProgress
        entity.setRenderYawOffset(originalEntity.renderYawOffset)
        entity.hurtTime = originalEntity.hurtTime
        entity.fire = originalEntity.fire
        entity.deathTime = originalEntity.deathTime
        entity.arrowCountInEntity = originalEntity.arrowCountInEntity
    }

    @Serializable
    data class SkytilsPlusData(val active: List<String>, val inactive: List<String>)
}