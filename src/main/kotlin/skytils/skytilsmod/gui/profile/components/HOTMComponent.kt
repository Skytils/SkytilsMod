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
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.state.State
import gg.essential.universal.UMatrixStack
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.mining.HOTM
import skytils.skytilsmod.utils.ItemUtil.setLore
import skytils.skytilsmod.utils.SkillUtils
import kotlin.math.ceil

class HOTMComponent(val profileState: State<Member?>) : UIComponent() {
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
            val hotmLevel = SkillUtils.calcXpWithProgress(profile.hotm.experience.toDouble(), SkillUtils.hotmXp.values)
            for (slot in HOTM.HOTMSlot.slots) {
                if (slot is HOTM.HOTMSlot.HOTMLevel) {
                    SlotComponent(
                        slot.getItem(if (slot.hotmLevel == ceil(hotmLevel).toInt()) -1 else hotmLevel.toInt())
                            .setLore(slot.getLore(slot.hotmLevel))
                    )
                        .constrain {
                            x = ((slot.slotNum % 9) * (16 + 2)).pixels
                            y = ((slot.slotNum / 9) * (16 + 2) - 18).pixels
                        } childOf this
                } else {
                    var level = profile.hotm.perks.getOrDefault(slot, 0)
                    if (slot is HOTM.HOTMSlot.PickaxeAbility && profile.hotm.perks.getOrDefault(
                            HOTM.HOTMSlot.Perk.SpecialPerk.PeakOfTheMountain,
                            0
                        ) >= 1
                    )
                        level++
                    SlotComponent(slot.getItem(level).setLore(slot.getLore(level))).constrain {
                        x = ((slot.slotNum % 9) * (16 + 2)).pixels
                        y = ((slot.slotNum / 9) * (16 + 2)).pixels
                    } childOf this
                }
            }
            needsSetup = false
        }
        super.draw(matrixStack)
    }
}