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
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.events.impl.BlockChangeEvent
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.slayer.base.ThrowingSlayer
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.printDevMessage
import net.minecraft.block.BlockBeacon
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.abs

class SeraphSlayer(entity: EntityEnderman) :
    ThrowingSlayer<EntityEnderman>(entity, "Voidgloom Seraph", "§c☠ §bVoidgloom Seraph") {
    val nukekebiSkulls = mutableListOf<EntityArmorStand>()
    var yangGlyphAdrenalineStressCount = -1L
    var lastYangGlyphSwitch = -1L
    var lastYangGlyphSwitchTicks = -1
    var thrownBoundingBox: AxisAlignedBB? = null
    val hitPhase: Boolean
        get() = nameEntity?.customNameTag?.dropLastWhile { it == 's' }?.endsWith(" Hit") ?: false

    override fun tick(event: TickEvent.ClientTickEvent) {
        if (lastYangGlyphSwitchTicks >= 0) lastYangGlyphSwitchTicks++
        if (lastYangGlyphSwitchTicks > 120) lastYangGlyphSwitchTicks = -1
        if (Config.experimentalYangGlyphDetection && lastYangGlyphSwitchTicks >= 0 && thrownEntity == null && thrownLocation == null) {
            Skytils.mc.theWorld.getEntitiesWithinAABB(
                EntityArmorStand::class.java,
                entity.entityBoundingBox.expand(20.69, 20.69, 20.69)
            ) { e ->
                e as EntityArmorStand
                e.ticksExisted <= 300 && lastYangGlyphSwitchTicks + 5 > e.ticksExisted &&
                        e.inventory[4]?.item == Item.getItemFromBlock(Blocks.beacon)
            }.minByOrNull {
                (abs(lastYangGlyphSwitchTicks - it.ticksExisted) * 10) + SlayerFeatures.slayerEntity!!.getDistanceSqToEntity(
                    it
                )
            }?.let { suspect ->
                printDevMessage(
                    "Found suspect glyph, $lastYangGlyphSwitchTicks switched, ${suspect.ticksExisted} existed, ${
                        entity.getDistanceSqToEntity(
                            suspect
                        )
                    } distance", "slayer", "seraph", "seraphGlyph"
                )
                thrownEntity = suspect
            }
        }
    }

    override fun entityJoinWorld(event: EntityJoinWorldEvent) {
        tickTimer(1) {
            (event.entity as? EntityArmorStand)?.let { e ->
                if (e.inventory[4]?.item == Item.getItemFromBlock(Blocks.beacon)) {
                    val time = System.currentTimeMillis() - 50
                    printDevMessage(
                        "Found beacon armor stand, time diff ${time - lastYangGlyphSwitch}",
                        "slayer",
                        "seraph",
                        "seraphGlyph"
                    )
                    if (lastYangGlyphSwitch != -1L && time - lastYangGlyphSwitch < 300 && e.entityBoundingBox.expand(
                            4.5,
                            4.0,
                            4.5
                        )
                            .intersectsWith(thrownBoundingBox ?: e.entityBoundingBox)
                    ) {
                        printDevMessage(
                            "Beacon armor stand is close to slayer entity",
                            "slayer",
                            "seraph",
                            "seraphGlyph"
                        )
                        thrownEntity = e
                        lastYangGlyphSwitch = -1L
                        lastYangGlyphSwitchTicks = -1
                    }
                    return@tickTimer
                } else if (e.entityBoundingBox.expand(2.0, 3.0, 2.0)
                        .intersectsWith(entity.entityBoundingBox)
                ) {
                    printDevMessage("Found nearby armor stand", "slayer", "seraph", "seraphGlyph", "seraphFixation")
                    if (e.inventory.any {
                            it?.takeIf { it.item == Items.skull }
                                ?.let { ItemUtil.getSkullTexture(it) } == "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0="
                        }) {
                        nukekebiSkulls.add(e)
                    }
                    return@tickTimer
                }
            }
        }
    }

    override fun blockChange(event: BlockChangeEvent) {
        if (event.pos == thrownLocation && event.old.block is BlockBeacon && event.update.block !is BlockBeacon) {
            thrownLocation = null
            thrownEntity = null
            return
        }
        thrownEntity?.let { entity ->
            printDevMessage("Glyph Entity exists", "slayer", "seraph", "seraphGlyph")
            if (event.update.block is BlockBeacon && entity.position.distanceSq(event.pos) <= 3.5 * 3.5) {
                printDevMessage("Beacon entity near beacon block!", "slayer", "seraph", "seraphGlyph")
                thrownLocation = event.pos
                thrownEntity = null
                if (Config.yangGlyphPing && Config.yangGlyphPingOnLand) GuiManager.createTitle(
                    "§cYang Glyph!",
                    30
                )
                yangGlyphAdrenalineStressCount = System.currentTimeMillis() + 5000L
                lastYangGlyphSwitchTicks = -1
            }
        }
        if (Config.experimentalYangGlyphDetection && thrownLocation == null) {
            if (lastYangGlyphSwitchTicks in 0..5 && entity.getDistanceSq(event.pos) <= 5 * 5) {
                if (Config.yangGlyphPing && Config.yangGlyphPingOnLand) GuiManager.createTitle(
                    "§cYang Glyph!",
                    30
                )
                printDevMessage(
                    "Beacon was close to slayer, $lastYangGlyphSwitchTicks", "slayer", "seraph", "seraphGlyph"
                )
                thrownLocation = event.pos
                lastYangGlyphSwitchTicks = -1
                yangGlyphAdrenalineStressCount = System.currentTimeMillis() + 5000L
            }
        }
    }
}