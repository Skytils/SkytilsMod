/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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

package sharttils.sharttilsmod.gui.profile.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.state.State
import sharttils.hylin.skyblock.Member
import sharttils.hylin.skyblock.mining.HOTM
import sharttils.sharttilsmod.utils.ItemUtil.setLore
import sharttils.sharttilsmod.utils.SkillUtils
import kotlin.math.ceil

class HOTMComponent(val profileState: State<Member?>) : UIComponent() {

    init {
        profileState.onSetValue {
            Window.enqueueRenderOperation {
                it?.let { profile ->
                    clearChildren()
                    val hotm: HOTM? = profile.hotm
                    val hotmLevel =
                        SkillUtils.calcXpWithProgress(hotm?.experience?.toDouble() ?: 0.0, SkillUtils.hotmXp.values)
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
                            var level = hotm?.perks?.getOrDefault(slot, 0) ?: 0
                            if (slot is HOTM.HOTMSlot.PickaxeAbility && level == 1 && (hotm?.perks?.getOrDefault(
                                    HOTM.HOTMSlot.Perk.SpecialPerk.PeakOfTheMountain,
                                    0
                                ) ?: 0) >= 1
                            )
                                level++
                            val item = slot.getItem(level).setLore(slot.getLore(level))
                            if (slot == hotm?.selectedPickaxeAbility) item.getSubCompound("ench", true)
                            SlotComponent(item).constrain {
                                x = ((slot.slotNum % 9) * (16 + 2)).pixels
                                y = ((slot.slotNum / 9) * (16 + 2)).pixels
                            } childOf this
                        }
                    }
                }
            }
        }
    }
}