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

package skytils.skytilsmod.gui.profile.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.State
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import skytils.hylin.player.Player
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.dungeons.DungeonClass
import skytils.skytilsmod.gui.constraints.FixedChildBasedRangeConstraint
import skytils.skytilsmod.utils.NumberUtil
import skytils.skytilsmod.utils.SkillUtils
import java.awt.Color

class DungeonsComponent(private val playerState: State<Player?>, private val profileState: State<Member?>) :
    UIComponent() {

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

    val selectedClass by UIText().bindText(profileState.map { "Selected Class: ${it?.dungeons?.selectedClass?.className ?: "None"}" })
        .constrain {
            x = 5.percent
            y = SiblingConstraint(5f)
        } childOf classes

    val healer by DungeonClassComponent(
        ItemComponent(ItemStack(Items.potionitem.setPotionEffect(Potion.heal.name), 1, 8261)),
        Color(65, 102, 245).constraint,
        DungeonClass.HEALER,
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
        DungeonClass.MAGE,
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
        DungeonClass.BERSERK,
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
        DungeonClass.ARCHER,
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
        DungeonClass.TANK,
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
        it?.dungeons?.dungeons?.get("catacombs")
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

    // Normal floors
    val normalData = cataData.map {
        it?.normal
    }

    val normalFloors by UIContainer().constrain {
        height = ChildBasedRangeConstraint()
        width = 100.percent
    } childOf floors

    val highestFloorBeaten by UIText()
        .bindText(normalData.map { "Highest Floor Beaten: ${it?.highestCompletion?.let { if (it == 0) "Entrance" else "Floor $it" } ?: "None"}" })
        .constrain {
            x = 5.percent
        } childOf normalFloors

    val secretsFound by UIText()
        .bindText(playerState.map {
            "Secrets found: ${
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
        } childOf normalFloors

    val normalFloorContainer by UIContainer().constrain {
        x = 5.percent
        y = SiblingConstraint(5f)
        width = 90.percent
        height = ChildBasedRangeConstraint()
    } childOf normalFloors

    val masterData = cataData.map {
        it?.master
    }

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
                dungeon?.highestCompletion?.let { highest ->
                    (0 until highest).forEach {
                        DungeonFloorComponent(dungeon, it).constrain {
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
                dungeon?.highestCompletion?.let { highest ->
                    (0 until highest).forEach {
                        DungeonFloorComponent(dungeon, it).constrain {
                            x = CramSiblingConstraint(5f)
                            y = CramSiblingConstraint(5f)
                        } childOf masterFloorContainer
                    }
                }
            }
        }
    }
}