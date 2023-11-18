/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.features.impl.slayer.impl

import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.SoundQueue
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.slayer.base.ThrowingSlayer
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.printDevMessage
import gg.skytils.skytilsmod.utils.stripControlCodes
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockColored
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.EntitySkeleton
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.ItemSkull
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color

class DemonlordSlayer(entity: EntityBlaze) :
    ThrowingSlayer<EntityBlaze>(entity, "Inferno Demonlord", "§c☠ §bInferno Demonlord") {
    var totemEntity: EntityArmorStand? = null
    var totemPos: BlockPos? = null

    private var lastTickInvis = false
    val relevantEntity: Entity?
        get() {
            return if (entity.isInvisible) {
                if (quazii == null || typhoeus == null) {
                    null
                } else if (typhoeusTimer?.displayName?.formattedText?.contains("IMMUNE") == true
                    || (typhoeus?.health ?: 0f) <= 0f
                ) {
                    quazii
                } else {
                    typhoeus
                }
            } else {
                entity
            }
        }
    val relevantColor: Color?
        get() {
            val relevantTimer = if (entity.isInvisible) {
                if (quazii == null || typhoeus == null) {
                    null
                } else if (typhoeusTimer?.displayName?.formattedText?.contains("IMMUNE") == true
                    || (typhoeus?.health ?: 0f) <= 0f
                ) {
                    quaziiTimer
                } else {
                    typhoeusTimer
                }
            } else {
                timerEntity
            } ?: return null
            val attunement = relevantTimer.displayName.unformattedText.substringBefore(" ").stripControlCodes()
            return attunementColors[attunement]
        }

    // Is there a point making a class for the demons and storing the entity and the timer in the same place?
    var quazii: EntitySkeleton? = null
    var quaziiTimer: EntityArmorStand? = null
    var typhoeus: EntityPigZombie? = null
    var typhoeusTimer: EntityArmorStand? = null

    val activeFire = mutableSetOf<BlockPos>()

    companion object {
        private const val thrownTexture =
            "InRleHR1cmVzIjogeyJTS0lOIjogeyJ1cmwiOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85YzJlOWQ4Mzk1Y2FjZDk5MjI4NjljMTUzNzNjZjdjYjE2ZGEwYTVjZTVmM2M2MzJiMTljZWIzOTI5YzlhMTEifX0="
        private const val quaziiTexture = // this the wither skeleton
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTZjYTE0NWJhNDM1YjM3NWY3NjNmZjUzYjRjZTA0YjJhMGM4NzNlOGZmNTQ3ZThiMTRiMzkyZmRlNmZiZmQ5NCJ9fX0="
        private const val typhoeusTexture = // and this is the pig
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTJmMjk5NDVhYTUzY2Q5NWEwOTc4YTYyZWYxYThjMTk3ODgwMzM5NWE4YWQ1YzA5MjFkOWNiZTVlMTk2YmI4YiJ9fX0="

        // Taken directly from https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes
        private val attunementColors = mapOf(
            "ASHEN" to Color(85, 85, 85),
            "CRYSTAL" to Color(85, 255, 255),
            "AURIC" to Color(255, 255, 85),
            "SPIRIT" to Color(255, 255, 255)
        )

        private fun createBlazeFirePingTask() =
            tickTimer(4, repeats = true, register = false) {
                if (Utils.inSkyblock && Config.blazeFireWarning && Skytils.mc.thePlayer != null) {
                    (SlayerFeatures.slayer as? DemonlordSlayer)?.let {
                        if (!Skytils.mc.thePlayer.onGround) return@tickTimer
                        val under = BlockPos(
                            Skytils.mc.thePlayer.posX,
                            Skytils.mc.thePlayer.posY - 0.5,
                            Skytils.mc.thePlayer.posZ
                        )
                        if (under in it.activeFire) {
                            // The reason this is a title and not just sound is because there is much less time
                            // to react to the pit warning than a rev5 tnt ping
                            GuiManager.createTitle("§c§lFire pit!", 4)
                            SoundQueue.addToQueue("random.orb", 1f)
                        }
                    }
                }
            }

        private var blazeFirePingTask = createBlazeFirePingTask()
    }

    override fun set() {
        blazeFirePingTask.start()
    }

    override fun unset() {
        blazeFirePingTask.cancel()
        blazeFirePingTask = createBlazeFirePingTask()
    }

    override fun tick(event: TickEvent.ClientTickEvent) {
        if (entity.isInvisible && !lastTickInvis) {
            lastTickInvis = true
            val prevBB = entity.entityBoundingBox.expand(3.0, 1.5, 3.0)
            tickTimer(10) {
                val demons = entity.entityWorld.getEntitiesInAABBexcluding(
                    entity, prevBB
                ) { it is EntityPigZombie || (it is EntitySkeleton && it.skeletonType == 1) }
                for (demon in demons) {
                    val helmet = ItemUtil.getSkullTexture(demon.inventory.getOrNull(4) ?: continue)
                    val helmetTexture = if (demon is EntitySkeleton) {
                        quaziiTexture
                    } else {
                        typhoeusTexture
                    }
                    if (helmet == helmetTexture) {
                        demon.entityWorld.getEntitiesInAABBexcluding(
                            demon, demon.entityBoundingBox.expand(0.2, 3.0, 0.2)
                        ) {
                            it is EntityArmorStand && it.isInvisible && it.hasCustomName()
                                    && it.displayName.formattedText.matches(SlayerFeatures.timerRegex)
                        }.firstOrNull()?.let {
                            if (demon is EntitySkeleton) {
                                quazii = demon
                                quaziiTimer = it as EntityArmorStand
                                printDevMessage("Quazii", "slayer")
                            } else if (demon is EntityPigZombie) {
                                typhoeus = demon
                                typhoeusTimer = it as EntityArmorStand
                                printDevMessage("Typhoeus", "slayer")
                            }
                        }
                    }
                }
            }
        } else if (!entity.isInvisible && lastTickInvis) {
            lastTickInvis = false
        }
    }

    override fun entityJoinWorld(event: EntityJoinWorldEvent) {
        (event.entity as? EntityArmorStand)?.let { e ->
            tickTimer(1) {
                if (e.inventory[4]?.takeIf { it.item is ItemSkull }
                        ?.let { ItemUtil.getSkullTexture(it) == thrownTexture } == true) {
                    printDevMessage(
                        "Found skull armor stand",
                        "slayer",
                    )
                    thrownEntity = e
                    return@tickTimer
                } else if (e.name.matches(SlayerFeatures.totemRegex) && e.getDistanceSq(totemPos) < 9) {
                    totemEntity = e
                }
            }
        }
    }

    override fun blockChange(event: BlockChangeEvent) {
        if (totemEntity != null && event.old.block == Blocks.stained_hardened_clay && event.update.block is BlockAir) {
            totemEntity = null
            printDevMessage("removed totem entity", "totem")
            return
        } else if ((thrownEntity?.position?.distanceSq(event.pos) ?: 0.0) < 9.0
            && event.old.block is BlockAir && event.update.block == Blocks.stained_hardened_clay
        ) {
            thrownEntity = null
            totemPos = event.pos
        }

        // This also triggers on the totem, could check for yellow clay replacing red clay,
        // but might be better to not delay anything
        if (event.update.block == Blocks.stained_hardened_clay
            && event.update.getValue(BlockColored.COLOR) == EnumDyeColor.RED
        ) {
            activeFire.add(event.pos)
        } else if (event.old.block == Blocks.fire && event.update.block == Blocks.air) {
            activeFire.remove(event.pos.down())
        }
    }
}