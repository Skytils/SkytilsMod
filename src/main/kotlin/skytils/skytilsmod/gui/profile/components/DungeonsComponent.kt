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
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.State
import gg.essential.universal.UMatrixStack
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import skytils.hylin.player.Player
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.dungeons.DungeonClass
import skytils.skytilsmod.utils.SkillUtils
import java.awt.Color

class DungeonsComponent(private val playerState: State<Player?>, private val profileState: State<Member?>) :
    UIComponent() {

    val classes by UIContainer().constrain {
        width = RelativeConstraint()
        height = ChildBasedRangeConstraint() + 5.pixels
    } childOf this

    val catacombs by UIContainer().constrain {
        y = SiblingConstraint(10f)
        width = RelativeConstraint()
        height = ChildBasedRangeConstraint() + 5.pixels
    } childOf this

    var needsSetup = false

    init {
        profileState.onSetValue {
            if (it != null) {
                needsSetup = true
            }
        }

    }

    override fun draw(matrixStack: UMatrixStack) {
        if (needsSetup) {
            val profile = profileState.get()!!
            clearChildren()
            classes childOf this
            catacombs childOf this

            val classesTitle by UIText("Classes: ").constrain {
                x = 0.pixels
                y = 5.pixels
            } childOf classes

            val selectedClass by UIText("Selected Class: ${profile.dungeons.selectedClass?.className ?: "None"}").constrain {
                x = 0.pixels
                y = SiblingConstraint(5f)
            } childOf classes

            val healer by DungeonClassComponent(
                ItemComponent(ItemStack(Items.potionitem.setPotionEffect(Potion.heal.name))),
                Color(65, 102, 245).toConstraint(),
                DungeonClass.HEALER,
                profileState
            ).constrain {
                x = 0.pixels
                y = SiblingConstraint(5f)
                height = 20.pixels
                width = 42.5.percent
            } childOf classes

            val mage by DungeonClassComponent(
                ItemComponent(ItemStack(Items.blaze_rod)),
                CopyConstraintColor() boundTo healer,
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
                CopyConstraintColor() boundTo healer,
                DungeonClass.BERSERK,
                profileState
            ).constrain {
                x = 0.pixels
                y = SiblingConstraint(5f)
                height = 20.pixels
                width = 42.5.percent
            } childOf classes

            val archer by DungeonClassComponent(
                ItemComponent(ItemStack(Items.bow)),
                CopyConstraintColor() boundTo healer,
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
                CopyConstraintColor() boundTo healer,
                DungeonClass.TANK,
                profileState
            ).constrain {
                x = 0.pixels
                y = SiblingConstraint(5f)
                height = 20.pixels
                width = 42.5.percent
            } childOf classes

            val cataData = profile.dungeons.dungeons["catacombs"]
            val normalData = cataData?.normal
            val masterData = cataData?.master

            val cataXp =
                SkillUtils.calcXpWithOverflowAndProgress(
                    cataData?.experience ?: 0.0,
                    SkillUtils.dungeoneeringXp.size,
                    SkillUtils.dungeoneeringXp.values
                )
            val progress = run {
                if (cataXp.first == SkillUtils.dungeoneeringXp.size) return@run 1f
                return@run (cataXp.third % 1).toFloat()
            }

            val cataTitle by UIText("Catacombs: ").constrain {
                x = 0.pixels
                y = 5.pixels
            } childOf catacombs

            val cata by XPComponent(
                ItemComponent(ItemStack(Items.skull, 1, 1)),
                "Catacombs ${cataXp.first}",
                progress,
                cataXp.second.toLong()
            ).constrain {
                x = 0.pixels
                y = SiblingConstraint(5f)
                height = 20.pixels
                width = 42.5.percent
            } childOf catacombs

            val highestFloor = normalData?.highestCompletion
            val highestMasterFloor = masterData?.highestCompletion

            val highestFloorBeaten by UIText("Highest Floor Beaten: ${highestFloor?.let { if (it == 0) "Entrance" else "Floor $it" } ?: "None"}").constrain {
                x = 0.pixels
                y = SiblingConstraint(5f)
            } childOf catacombs
            val highestMasterFloorBeaten by UIText("Highest Master Floor Beaten: ${highestMasterFloor?.let { if (it == 0) "Entrance" else "Floor $it" } ?: "None"}").constrain {
                x = 0.pixels
                y = SiblingConstraint(5f)
            } childOf catacombs
            val secretsFound by UIText("${
                playerState.map { it?.achievements?.getOrDefault("skyblock_treasure_hunter", 0) }
                    .getOrDefault(0)
            }").constrain {
                x = 0.pixels
                y = SiblingConstraint(5f)
            } childOf catacombs
            val floors by UIContainer().constrain {
                x = 0.pixels
                y = SiblingConstraint(10f)
                width = RelativeConstraint()
                height = ChildBasedSizeConstraint()
            } childOf catacombs
            val masterFloors by UIContainer().constrain {
                x = 0.pixels
                y = SiblingConstraint(5f)
                width = RelativeConstraint()
                height = ChildBasedSizeConstraint()
            } childOf catacombs

            if (normalData != null && highestFloor != null) for (i in 0..highestFloor) {
                DungeonFloorComponent(normalData, i).constrain {
                    x = CramSiblingConstraint(5f)
                    y = CramSiblingConstraint(5f)
                } childOf floors
            }
            if (masterData != null && highestMasterFloor != null) for (i in 1..highestMasterFloor) {
                DungeonFloorComponent(masterData, i).constrain {
                    x = CramSiblingConstraint(5f)
                    y = CramSiblingConstraint(5f)
                } childOf masterFloors
            }
            needsSetup = false
        }
        super.draw(matrixStack)
    }
}