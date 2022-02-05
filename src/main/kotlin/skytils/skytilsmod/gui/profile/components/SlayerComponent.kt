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
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.provideDelegate
import gg.essential.elementa.state.State
import skytils.hylin.skyblock.Member
import skytils.skytilsmod.utils.NumberUtil

class SlayerComponent(private val profileState: State<Member?>) : UIComponent() {
    init {
        profileState.onSetValue { m ->
            clearChildren()
            if (m == null) return@onSetValue
            Window.enqueueRenderOperation {
                val rev = m.slayers.revenant
                val tara = m.slayers.tarantula
                val sven = m.slayers.sven
                val eman = m.slayers.enderman
                UIText("§7Total Slayer XP: §f${NumberUtil.nf.format(rev.xp + tara.xp + sven.xp + eman.xp)}").constrain {
                    x = 0.pixels
                    y = 0.pixels
                } childOf this
                val slayers by UIContainer().constrain {
                    x = 0.pixels
                    y = SiblingConstraint(5f)
                    width = RelativeConstraint()
                    height = ChildBasedSizeConstraint()
                } childOf this

                SlayerBossComponent(m.slayers::revenant, "zombie").constrain {
                    x = CramSiblingConstraint(10f)
                    y = CramSiblingConstraint(10f)
                    width = RelativeConstraint()
                    height = ChildBasedSizeConstraint()
                } childOf slayers

                SlayerBossComponent(m.slayers::tarantula, "spider").constrain {
                    x = CramSiblingConstraint(10f)
                    y = CramSiblingConstraint(10f)
                    width = RelativeConstraint()
                    height = ChildBasedSizeConstraint()
                } childOf slayers

                SlayerBossComponent(m.slayers::sven, "wolf").constrain {
                    x = CramSiblingConstraint(10f)
                    y = CramSiblingConstraint(10f)
                    width = RelativeConstraint()
                    height = ChildBasedSizeConstraint()
                } childOf slayers

                SlayerBossComponent(m.slayers::enderman, "enderman").constrain {
                    x = CramSiblingConstraint(10f)
                    y = CramSiblingConstraint(10f)
                    width = RelativeConstraint()
                    height = ChildBasedSizeConstraint()
                } childOf slayers
            }
        }
    }
}