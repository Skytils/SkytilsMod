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

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEntity
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEntitySlime
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEnumChatFormatting
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorEnumDyeColor
import gg.skytils.skytilsmod.utils.ReflectionHelper.getClassHelper
import gg.skytils.skytilsmod.utils.ReflectionHelper.getFieldHelper
import gg.skytils.skytilsmod.utils.ReflectionHelper.getMethodHelper
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.hasMoved
import kotlinx.serialization.Serializable
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.entity.EntityAgeable
import net.minecraft.entity.EntityList
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.*
import net.minecraft.item.EnumDyeColor
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs

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

    val isSkytilsPlus by lazy {
        Utils.isBSMod || SuperSecretSettings.sheepifyRebellion
    }

    val palEntities: List<Int> by lazy {
        mutableListOf<Int>().apply {
            addAll(50..68)
            addAll(90..101)
            remove(63) // vault the ender dragon cause the model is different
            if (isSkytilsPlus) {
                remove(91)
            }
        }
    }

    @SubscribeEvent
    fun playerSpawn(event: EntityJoinWorldEvent) {
        if (event.entity !is AbstractClientPlayer || event.entity.uniqueID.version() == 2) return

        if (Utils.inSkyblock) {
            checkForFakeModel(event.entity as AbstractClientPlayer)
        } else if (event.entity is EntityPlayerSP) {
            val world = event.world
            tickTimer(5) {
                if (Utils.inSkyblock && mc.theWorld == world) {
                    event.world.playerEntities.forEach {
                        if (it is AbstractClientPlayer && it.uniqueID.version() != 2 && it !in dummyModelMap) {
                            checkForFakeModel(it)
                        }
                    }
                }
            }
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

    fun checkForFakeModel(entity: AbstractClientPlayer) {
        var fakeEntity: EntityLivingBase? = null
        if ((SuperSecretSettings.catGaming || (Utils.isBSMod && SkytilsPlus.redeemed)) && entity is EntityPlayerSP) {
            fakeEntity = EntityOcelot(fakeWorld)
            fakeEntity.tameSkin = 3
        } else if (SuperSecretSettings.palworld) {
            val uuid = entity.uniqueID
            val most = abs(uuid.mostSignificantBits)
            val least = abs(uuid.leastSignificantBits)
            fakeEntity = EntityList.createEntityByID(if (SuperSecretSettings.cattiva) 98 else palEntities[(most % palEntities.size).toInt()], fakeWorld) as EntityLivingBase
            when (fakeEntity) {
                is EntityOcelot -> fakeEntity.tameSkin = (least % 4).toInt()
                is EntitySheep -> fakeEntity.fleeceColor = EnumDyeColor.byDyeDamage((least % 16).toInt())
                is EntityWolf -> {
                    fakeEntity.isTamed = true
                    fakeEntity.collarColor = EnumDyeColor.byDyeDamage((least % 16).toInt())
                }
                is EntitySkeleton -> fakeEntity.skeletonType = (least % 2).toInt()
                is EntityHorse -> {
                    fakeEntity.horseType = (least % 5).toInt()
                    fakeEntity.horseVariant = (most % 7).toInt() or ((least % 5).toInt() shl 8)
                    fakeEntity.isHorseSaddled = (most % 2).toInt() == 1
                }
                is EntityRabbit -> {
                    fakeEntity.rabbitType = (least % 7).toInt().takeIf { it != 6 } ?: 99
                }
                is EntitySlime -> {
                    (fakeEntity as AccessorEntitySlime).invokeSetSlimeSize((least % 3).toInt())
                }
                is EntityBat -> {
                    fakeEntity.isBatHanging = false
                }
            }
        } else if (isSkytilsPlus) {
            val color = skytilsPlusColors[entity.name] ?: return
            fakeEntity = EntitySheep(fakeWorld)
            fakeEntity.fleeceColor = color

            if (skytilsPlusUsernames.contains(entity.name)) {
                fakeEntity.customNameTag = "jeb_"
                fakeEntity.alwaysRenderNameTag = false
            }
        }

        if (fakeEntity != null) {
            dummyModelMap[entity] = fakeEntity
            fakeEntity.forceSpawn = true
            fakeEntity.motionX = 0.0
            fakeEntity.motionY = 0.0
            fakeEntity.motionZ = 0.0
            fakeEntity.noClip = true
        }
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
        entity.isSprinting = originalEntity.isSprinting
        entity.isInvisible = originalEntity.isInvisible

        if (entity.inventory.size >= originalEntity.inventory.size) {
            for (i in originalEntity.inventory.indices) {
                entity.inventory[i] = originalEntity.inventory[i]
            }
        }

        if (originalEntity.isChild && entity is EntityAgeable) {
            entity.growingAge = -1
        }

        if (entity is EntityTameable) {
            entity.isSitting = originalEntity.isSneaking && !originalEntity.hasMoved
        } else {
            entity.isSneaking = originalEntity.isSneaking
        }

        entity.ticksExisted = originalEntity.ticksExisted
    }

    @Serializable
    data class SkytilsPlusData(val active: List<String>, val inactive: List<String>)
}