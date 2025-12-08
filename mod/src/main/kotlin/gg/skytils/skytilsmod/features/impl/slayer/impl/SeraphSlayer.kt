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
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures
import gg.skytils.skytilsmod.features.impl.slayer.base.ThrowingSlayer
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.multiplatform.armorItems
import gg.skytils.skytilsmod.utils.printDevMessage
import gg.skytils.skytilsmod.utils.toVec3
import net.minecraft.block.BeaconBlock
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.EndermanEntity
import net.minecraft.block.Blocks
import net.minecraft.item.Items
import net.minecraft.item.Item
import net.minecraft.util.math.Box
import kotlin.math.abs

class SeraphSlayer(entity: EndermanEntity) :
    ThrowingSlayer<EndermanEntity>(entity, "Voidgloom Seraph", "§bVoidgloom Seraph") {
    val nukekebiSkulls = mutableListOf<ArmorStandEntity>()
    var yangGlyphAdrenalineStressCount = -1L
    var lastYangGlyphSwitch = -1L
    var lastYangGlyphSwitchTicks = -1
    var thrownBoundingBox: Box? = null
    val hitPhase: Boolean
        get() = nameEntity?.customName?.string?.dropLastWhile { it == 's' }?.endsWith(" Hit") ?: false

    override fun tick(event: TickEvent) {
        if (lastYangGlyphSwitchTicks >= 0) lastYangGlyphSwitchTicks++
        if (lastYangGlyphSwitchTicks > 120) lastYangGlyphSwitchTicks = -1
        if (Config.experimentalYangGlyphDetection && lastYangGlyphSwitchTicks >= 0 && thrownEntity == null && thrownLocation == null) {
            Skytils.mc.world?.getEntitiesByClass(
                ArmorStandEntity::class.java,
                entity.boundingBox.expand(20.69, 20.69, 20.69)
            ) { e ->
                e as ArmorStandEntity
                e.age <= 300 && lastYangGlyphSwitchTicks + 5 > e.age &&
                        e.armorItems[4]?.item == Item.fromBlock(Blocks.BEACON)
            }?.minByOrNull {
                (abs(lastYangGlyphSwitchTicks - it.age) * 10) + SlayerFeatures.slayerEntity!!.squaredDistanceTo(
                    it
                )
            }?.let { suspect ->
                printDevMessage(
                    {
                        "Found suspect glyph, $lastYangGlyphSwitchTicks switched, ${suspect.age} existed, ${
                            entity.squaredDistanceTo(
                                suspect
                            )
                        } distance"
                    }, "slayer", "seraph", "seraphGlyph"
                )
                thrownEntity = suspect
            }
        }
    }

    override fun entityJoinWorld(event: EntityJoinWorldEvent) {
        tickTimer(1) {
            (event.entity as? ArmorStandEntity)?.let { e ->
                if (e.armorItems[4]?.item == Item.fromBlock(Blocks.BEACON)) {
                    val time = System.currentTimeMillis() - 50
                    printDevMessage(
                        { "Found beacon armor stand, time diff ${time - lastYangGlyphSwitch}" },
                        "slayer",
                        "seraph",
                        "seraphGlyph"
                    )
                    if (lastYangGlyphSwitch != -1L && time - lastYangGlyphSwitch < 300 && e.boundingBox.expand(
                            4.5,
                            4.0,
                            4.5
                        )
                            .intersects(thrownBoundingBox ?: e.boundingBox)
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
                } else if (e.boundingBox.expand(2.0, 3.0, 2.0)
                        .intersects(entity.boundingBox)
                ) {
                    printDevMessage({ "Found nearby armor stand" }, "slayer", "seraph", "seraphGlyph", "seraphFixation")
                    if (e.armorItems.any {
                            it?.takeIf { it.item == Items.PLAYER_HEAD }
                                ?.let { ItemUtil.getSkullTexture(it) } == "eyJ0aW1lc3RhbXAiOjE1MzQ5NjM0MzU5NjIsInByb2ZpbGVJZCI6ImQzNGFhMmI4MzFkYTRkMjY5NjU1ZTMzYzE0M2YwOTZjIiwicHJvZmlsZU5hbWUiOiJFbmRlckRyYWdvbiIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIwNzU5NGUyZGYyNzM5MjFhNzdjMTAxZDBiZmRmYTExMTVhYmVkNWI5YjIwMjllYjQ5NmNlYmE5YmRiYjRiMyJ9fX0="
                        }) {
                        nukekebiSkulls.add(e)
                    }
                    return@tickTimer
                }
            }
        }
    }

    override fun blockChange(event: BlockStateUpdateEvent) {
        if (event.pos == thrownLocation && event.old.block is BeaconBlock && event.update.block !is BeaconBlock) {
            thrownLocation = null
            thrownEntity = null
            return
        }
        thrownEntity?.let { entity ->
            printDevMessage({ "Glyph Entity exists" }, "slayer", "seraph", "seraphGlyph")
            if (event.update.block is BeaconBlock && entity.blockPos.getSquaredDistance(event.pos) <= 3.5 * 3.5) {
                printDevMessage({ "Beacon entity near beacon block!" }, "slayer", "seraph", "seraphGlyph")
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
            if (lastYangGlyphSwitchTicks in 0..5 && entity.squaredDistanceTo(event.pos.toVec3()) <= 5 * 5) {
                if (Config.yangGlyphPing && Config.yangGlyphPingOnLand) GuiManager.createTitle(
                    "§cYang Glyph!",
                    30
                )
                printDevMessage(
                    { "Beacon was close to slayer, $lastYangGlyphSwitchTicks" }, "slayer", "seraph", "seraphGlyph"
                )
                thrownLocation = event.pos
                lastYangGlyphSwitchTicks = -1
                yangGlyphAdrenalineStressCount = System.currentTimeMillis() + 5000L
            }
        }
    }
}