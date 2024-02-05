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

package gg.skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.State
import gg.skytils.hypixel.types.player.Player
import gg.skytils.hypixel.types.skyblock.Member
import gg.skytils.skytilsmod.gui.constraints.FixedChildBasedRangeConstraint
import gg.skytils.skytilsmod.utils.NumberUtil
import gg.skytils.skytilsmod.utils.SkillUtils
import gg.skytils.skytilsmod.utils.toTitleCase
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import java.awt.Color

class DungeonsComponent(private val playerState: State<Player?>, private val profileState: State<Member?>) :
    UIContainer() {

    val classes by UIContainer().constrain {
        width = 100.percent
        height = FixedChildBasedRangeConstraint() + 5.pixels
    } childOf this

    val catacombs by UIContainer().constrain {
        y = SiblingConstraint(2.5f)
        width = RelativeConstraint()
        height = FixedChildBasedRangeConstraint()
    } childOf this

    // Classes stuff
    val classesTitle by UIText("Classes: ").constrain {
        x = 5.percent
        y = 5.pixels
    } childOf classes

    val selectedClass by UIText().bindText(profileState.map { "Selected Class: ${it?.dungeons?.selected_dungeon_class?.toTitleCase() ?: "None"}" })
        .constrain {
            x = 5.percent
            y = SiblingConstraint(5f)
        } childOf classes

    val healer by DungeonClassComponent(
        ItemComponent(ItemStack(Items.potionitem.setPotionEffect(Potion.heal.name), 1, 8261)),
        Color(65, 102, 245).constraint,
        "healer",
        profileState
    ).constrain {
        x = 5.percent
        y = SiblingConstraint(5f)
        height = 20.pixels
        width = 42.5.percent
    } childOf classes

    val mage by DungeonClassComponent(
        ItemComponent(ItemStack(Items.blaze_rod)),
        Color(65, 102, 245).constraint,
        "mage",
        profileState
    ).constrain {
        x = 52.5.percent
        y = CopyConstraintFloat() boundTo healer
        height = 20.pixels
        width = 42.5.percent
    } childOf classes

    val berserk by DungeonClassComponent(
        ItemComponent(ItemStack(Items.iron_sword)),
        Color(65, 102, 245).constraint,
        "berserk",
        profileState
    ).constrain {
        x = 5.percent
        y = SiblingConstraint(5f)
        height = 20.pixels
        width = 42.5.percent
    } childOf classes

    val archer by DungeonClassComponent(
        ItemComponent(ItemStack(Items.bow)),
        Color(65, 102, 245).constraint,
        "archer",
        profileState
    ).constrain {
        x = 52.5.percent
        y = CopyConstraintFloat() boundTo berserk
        height = 20.pixels
        width = 42.5.percent
    } childOf classes

    val tank by DungeonClassComponent(
        ItemComponent(ItemStack(Items.leather_chestplate)),
        Color(65, 102, 245).constraint,
        "tank",
        profileState
    ).constrain {
        x = 5.percent
        y = SiblingConstraint(5f)
        height = 20.pixels
        width = 42.5.percent
    } childOf classes

    // cata stuff
    val cataTitle by UIText("Catacombs: ").constrain {
        x = 5.percent
        y = 0.pixels
    } childOf catacombs

    private val cataData = profileState.map {
        it?.dungeons?.dungeon_types?.get("catacombs")
    }

    private val cataXp = cataData.map {
        SkillUtils.calcXpWithOverflowAndProgress(
            it?.experience ?: 0.0,
            SkillUtils.dungeoneeringXp.size,
            SkillUtils.dungeoneeringXp.values
        )
    }

    val cata by XPComponent(
        ItemComponent(ItemStack(Items.skull, 1, 1)),
        "Catacombs",
        0f,
        0
    ).bindText(
        cataXp.map { (level, _, _) -> "Catacombs $level" }
    ).bindPercent(
        cataXp.map { (level, _, progress) ->
            if (level == SkillUtils.dungeoneeringXp.size) return@map 1f
            return@map (progress % 1).toFloat()
        }
    ).bindOverflow(
        cataXp.map { (_, overflow, _) -> overflow.toLong() }
    ).constrain {
        x = 5.percent
        y = SiblingConstraint(5f)
        height = 20.pixels
        width = 42.5.percent
    } childOf catacombs

    val floors by UIContainer().constrain {
        y = SiblingConstraint(5f)
        height = ChildBasedRangeConstraint()
        width = 100.percent
    } childOf catacombs

    val normalData = cataData.map {
        it?.normal
    }
    val masterData = cataData.map {
        it?.master
    }

    val floorData by UIContainer().constrain {
        height = ChildBasedRangeConstraint()
        width = 100.percent
    } childOf floors

    val highestFloorBeaten by UIText()
        .bindText(normalData.map { "Highest Floor Beaten: ${it?.highest_tier_completed?.let { if (it == 0) "Entrance" else "Floor $it" } ?: "None"}" })
        .constrain {
            x = 5.percent
        } childOf floorData

    val highestMasterFloorBeaten by UIText()
        .bindText(masterData.map { "Highest Master Floor Beaten: ${it?.highest_tier_completed?.let { "Floor $it" } ?: "None"}" })
        .constrain {
            x = 5.percent
            y = SiblingConstraint(5f)
        } childOf floorData

    val secretsFound by UIText()
        .bindText(playerState.map {
            "Secrets Found: ${
                NumberUtil.nf.format(
                    it?.achievements?.getOrDefault(
                        "skyblock_treasure_hunter",
                        0
                    ) ?: 0
                )
            }"
        })
        .constrain {
            x = 5.percent
            y = SiblingConstraint(5f)
        } childOf floorData

    val normalFloorContainer by UIContainer().constrain {
        x = 5.percent
        y = SiblingConstraint(5f)
        width = 90.percent
        height = ChildBasedRangeConstraint()
    } childOf floors

    val floorDivider by UIBlock(Color(0x4166f5).constraint).constrain {
        x = 5.pixels
        y = SiblingConstraint(5f)
        width = 90.percent
        height = 2.pixels
    } childOf floors

    val masterFloorContainer by UIContainer().constrain {
        x = 5.percent
        y = SiblingConstraint(5f)
        width = 90.percent
        height = ChildBasedRangeConstraint()
    } childOf floors

    init {
        normalData.onSetValue { dungeon ->
            Window.enqueueRenderOperation {
                normalFloorContainer.clearChildren()
                dungeon?.highest_tier_completed?.let { highest ->
                    (0..highest).forEach {
                        DungeonFloorComponent(dungeon, it, false).constrain {
                            x = CramSiblingConstraint(5f)
                            y = CramSiblingConstraint(5f)
                        } childOf normalFloorContainer
                    }
                }
            }
        }

        masterData.onSetValue { dungeon ->
            Window.enqueueRenderOperation {
                masterFloorContainer.clearChildren()
                dungeon?.highest_tier_completed?.let { highest ->
                    (1..highest).forEach {
                        DungeonFloorComponent(dungeon, it, true).constrain {
                            x = CramSiblingConstraint(5f)
                            y = CramSiblingConstraint(5f)
                        } childOf masterFloorContainer
                    }
                }
            }
        }
    }
}