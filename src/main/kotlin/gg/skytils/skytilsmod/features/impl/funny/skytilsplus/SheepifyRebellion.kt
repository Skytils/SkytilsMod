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

import gg.essential.universal.UChat
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.tickTask
import gg.skytils.skytilsmod.events.impl.CheckRenderEntityEvent
import gg.skytils.skytilsmod.utils.ReflectionHelper.getClassHelper
import gg.skytils.skytilsmod.utils.ReflectionHelper.getFieldHelper
import gg.skytils.skytilsmod.utils.ReflectionHelper.getMethodHelper
import gg.skytils.skytilsmod.utils.RenderUtil
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.network.NetHandlerPlayClient
import net.minecraft.client.renderer.entity.RenderPlayer
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.passive.EntitySheep
import net.minecraft.item.EnumDyeColor
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object SheepifyRebellion {
    val fakeWorld by lazy {
        getClassHelper("gg.essential.gui.common.EmulatedUI3DPlayer\$FakeWorld")?.let { clazz ->
            val instance = clazz.getFieldHelper("INSTANCE")?.get(null)
            clazz.getMethodHelper("getFakeWorld")?.invoke(instance) as? WorldClient
        } ?: error("Failed to get FakeWorld")
    }

    val sheepMap = hashMapOf<AbstractClientPlayer, EntitySheep>()

    @SubscribeEvent
    fun onCheckRender(event: CheckRenderEntityEvent<*>) {
/*        sheepMap[event.entity]?.let {
            event.isCanceled = true
        }*/
    }

    @SubscribeEvent
    fun playerSpawn(event: EntityJoinWorldEvent) {
        if (event.entity is AbstractClientPlayer) {
            val sheep = EntitySheep(fakeWorld)
            sheepMap[event.entity as AbstractClientPlayer] = sheep
            sheep.forceSpawn = true
            sheep.motionX = 0.0
            sheep.motionY = 0.0
            sheep.motionZ = 0.0
            sheep.setLocationAndAngles(event.entity.posX, event.entity.posY, event.entity.posZ, event.entity.rotationYaw, event.entity.rotationPitch)
            sheep.fleeceColor = EnumDyeColor.RED
            //event.world.spawnEntityInWorld(sheep)
            //sheep.mountEntity(event.entity)
            //sheep.customNameTag = event.entity.name
            //sheep.alwaysRenderNameTag = true
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) {
            sheepMap.forEach { (player, sheep) ->
                //sheep.setPositionAndRotation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch)
/*                sheep.copyLocationAndAnglesFrom(player)
                sheep.setRotationYawHead(player.rotationYawHead)
                sheep.prevLimbSwingAmount = player.prevLimbSwingAmount
                sheep.limbSwingAmount = player.limbSwingAmount
                sheep.limbSwing = player.limbSwing*/
            }
        }
    }

    @SubscribeEvent
    fun playerLeave(event: LivingDeathEvent) {
        sheepMap.remove(event.entity)?.setDead()
    }

    @SubscribeEvent
    fun onRender(event: RenderLivingEvent.Pre<*>) {
        val sheep = sheepMap[event.entity] ?: return
        event.isCanceled = true
        val renderSheep = event.renderer.renderManager.getEntityRenderObject<EntitySheep>(sheep)
        copyProperties(sheep, event.entity)
        renderSheep.doRender(sheep, event.x, event.y, event.z, event.entity.rotationYaw, 1f)
        (event.renderer as RendererLivingEntity<EntityLivingBase>).renderName(event.entity, event.x, event.y, event.z)
    }

    fun copyProperties(entity: EntityLivingBase, originalEntity: EntityLivingBase) {
        entity.copyLocationAndAnglesFrom(originalEntity)
        entity.setRotationYawHead(originalEntity.rotationYawHead)
        entity.prevLimbSwingAmount = originalEntity.prevLimbSwingAmount
        entity.limbSwingAmount = originalEntity.limbSwingAmount
        entity.limbSwing = originalEntity.limbSwing
        entity.prevSwingProgress = originalEntity.prevSwingProgress
        entity.swingProgress = originalEntity.swingProgress
        entity.isSwingInProgress = originalEntity.isSwingInProgress
        entity.setRenderYawOffset(originalEntity.renderYawOffset)
        /*        for (i in event.entity.inventory.indices) {
            entity.inventory[i] = originalEntity.inventory[i]
        }*/
    }
}