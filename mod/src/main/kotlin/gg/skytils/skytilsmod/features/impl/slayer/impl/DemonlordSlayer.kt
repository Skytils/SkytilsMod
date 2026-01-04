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

import gg.skytils.event.impl.TickEvent
import gg.skytils.event.impl.entity.EntityJoinWorldEvent
import gg.skytils.event.impl.world.BlockStateUpdateEvent
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.core.GuiManager
import gg.skytils.skytilsmod.core.SoundQueue
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.slayer.base.ThrowingSlayer
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.formattedText
import gg.skytils.skytilsmod.utils.multiplatform.armorItems
import gg.skytils.skytilsmod.utils.multiplatform.blockPos
import gg.skytils.skytilsmod.utils.printDevMessage
import gg.skytils.skytilsmod.utils.stripControlCodes
import gg.skytils.skytilsmod.utils.toVec3
import net.minecraft.block.AirBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.BlazeEntity
import net.minecraft.entity.mob.ZombifiedPiglinEntity
import net.minecraft.entity.mob.AbstractSkeletonEntity
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.util.DyeColor
import net.minecraft.item.PlayerHeadItem
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.math.BlockPos
import java.awt.Color

class DemonlordSlayer(entity: BlazeEntity) :
    ThrowingSlayer<BlazeEntity>(entity, "Inferno Demonlord", "§c☠ §bInferno Demonlord") {
    var totemEntity: ArmorStandEntity? = null
    var totemPos: BlockPos? = null

    private var lastTickInvis = false
    val relevantEntity: Entity?
        get() {
            return if (entity.isInvisible) {
                if (quazii == null || typhoeus == null) {
                    null
                } else if (typhoeusTimer?.displayName?.string?.contains("IMMUNE") == true
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
                } else if (typhoeusTimer?.displayName?.string?.contains("IMMUNE") == true
                    || (typhoeus?.health ?: 0f) <= 0f
                ) {
                    quaziiTimer
                } else {
                    typhoeusTimer
                }
            } else {
                timerEntity
            } ?: return null
            val attunement = relevantTimer.displayName?.string?.substringBefore(" ")?.stripControlCodes()
            return attunementColors[attunement]
        }

    // Is there a point making a class for the demons and storing the entity and the timer in the same place?
    var quazii: AbstractSkeletonEntity? = null
    var quaziiTimer: ArmorStandEntity? = null
    var typhoeus: ZombifiedPiglinEntity? = null
    var typhoeusTimer: ArmorStandEntity? = null

    val activeFire = mutableSetOf<BlockPos>()

    companion object {
        private const val thrownTexture =
            "eyJ0aW1lc3RhbXAiOjE0NzkxODY4NTAxNDQsInByb2ZpbGVJZCI6ImQzMGRjYzE3NzlmOTRlYTdhYTdiMTg4ZGU1N2E0M2FkIiwicHJvZmlsZU5hbWUiOiJoYW9oYW5rbGxpdSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWMyZTlkODM5NWNhY2Q5OTIyODY5YzE1MzczY2Y3Y2IxNmRhMGE1Y2U1ZjNjNjMyYjE5Y2ViMzkyOWM5YTExIn19fQ=="
        private const val quaziiTexture = // this the wither skeleton
            "ewogICJ0aW1lc3RhbXAiIDogMTU4ODE3NzgxODc4MywKICAicHJvZmlsZUlkIiA6ICIzMDQzZjNkOTliMGY0MDI2OTQwNzcyZDNkZDE2MjRiYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJERU1PTjMxOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xNmNhMTQ1YmE0MzViMzc1Zjc2M2ZmNTNiNGNlMDRiMmEwYzg3M2U4ZmY1NDdlOGIxNGIzOTJmZGU2ZmJmZDk0IgogICAgfQogIH0KfQ=="
        private const val typhoeusTexture = // and this is the pig
            "ewogICJ0aW1lc3RhbXAiIDogMTYzMzk2MDM1MDkxNywKICAicHJvZmlsZUlkIiA6ICIzYTNmNzhkZmExZjQ0OTllYjE5NjlmYzlkOTEwZGYwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb19jcmVyYXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTJmMjk5NDVhYTUzY2Q5NWEwOTc4YTYyZWYxYThjMTk3ODgwMzM5NWE4YWQ1YzA5MjFkOWNiZTVlMTk2YmI4YiIKICAgIH0KICB9Cn0="

        // Taken directly from https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes
        private val attunementColors = mapOf(
            "ASHEN" to Color(85, 85, 85),
            "CRYSTAL" to Color(85, 255, 255),
            "AURIC" to Color(255, 255, 85),
            "SPIRIT" to Color(255, 255, 255)
        )

        private fun createBlazeFirePingTask() =
            tickTimer(4, repeats = true, register = false) {
                if (Utils.inSkyblock && Config.blazeFireWarning && Skytils.mc.player != null) {
                    (SlayerFeatures.slayer as? DemonlordSlayer)?.let {
                        val player = Skytils.mc.player ?: return@tickTimer
                        if (!player.isOnGround) return@tickTimer
                        val under = blockPos(
                            player.x,
                            player.y - 0.5,
                            player.z
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

    override fun tick(event: TickEvent) {
        if (entity.isInvisible && !lastTickInvis) {
            lastTickInvis = true
            val prevBB = entity.boundingBox.expand(3.0, 1.5, 3.0)
            tickTimer(10) {
                val demons = entity.entityWorld.getOtherEntities(
                    entity, prevBB
                ) { it.type == EntityType.ZOMBIFIED_PIGLIN || (it.type == EntityType.WITHER_SKELETON) }
                for (demon in demons) {
                    val helmet = ItemUtil.getSkullTexture((demon as LivingEntity).armorItems.getOrNull(4) ?: continue)
                    val helmetTexture = if (demon is AbstractSkeletonEntity) {
                        quaziiTexture
                    } else {
                        typhoeusTexture
                    }
                    if (helmet == helmetTexture) {
                        demon.entityWorld.getOtherEntities(
                            demon, demon.boundingBox.expand(0.2, 3.0, 0.2)
                        ) {
                            it is ArmorStandEntity && it.isInvisible && it.hasCustomName()
                                    && it.displayName?.formattedText?.matches(SlayerFeatures.timerRegex) == true
                        }.firstOrNull()?.let {
                            if (demon is AbstractSkeletonEntity) {
                                quazii = demon
                                quaziiTimer = it as ArmorStandEntity
                                printDevMessage({ "Quazii" }, "slayer")
                            } else if (demon is ZombifiedPiglinEntity) {
                                typhoeus = demon
                                typhoeusTimer = it as ArmorStandEntity
                                printDevMessage({ "Typhoeus" }, "slayer")
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
        (event.entity as? ArmorStandEntity)?.let { e ->
            tickTimer(1) {
                if (e.armorItems[0]?.takeIf { it.item is PlayerHeadItem }
                        ?.let { ItemUtil.getSkullTexture(it) == thrownTexture } == true) {
                    printDevMessage(
                        "Found skull armor stand",
                        "slayer",
                    )
                    thrownEntity = e
                    return@tickTimer
                } else if (totemPos?.let { e.name.formattedText.matches(SlayerFeatures.totemRegex) && e.squaredDistanceTo(it.toVec3()) < 9 } == true) {
                    totemEntity = e
                }
            }
        }
    }

    override fun blockChange(event: BlockStateUpdateEvent) {
        if (totemEntity != null && event.old.isIn(BlockTags.TERRACOTTA) && event.update.block is AirBlock) {
            totemEntity = null
            printDevMessage({ "removed totem entity" }, "totem")
            return
        } else if ((thrownEntity?.blockPos?.getSquaredDistance(event.pos) ?: 0.0) < 9.0
            && event.old.block is AirBlock && event.update.isIn(BlockTags.TERRACOTTA)
        ) {
            thrownEntity = null
            totemPos = event.pos
        }

        // This also triggers on the totem, could check for yellow clay replacing red clay,
        // but might be better to not delay anything
        if (event.update.isIn(BlockTags.TERRACOTTA)
            && event.update.block.defaultMapColor == DyeColor.RED.mapColor
        ) {
            activeFire.add(event.pos)
        } else if (event.old.block == Blocks.FIRE && event.update.block == Blocks.AIR) {
            activeFire.remove(event.pos.down())
        }
    }
}