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

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.State
import gg.essential.elementa.utils.withAlpha
import gg.essential.vigilance.gui.VigilancePalette
import gg.skytils.hypixel.types.skyblock.Member
import gg.skytils.skytilsmod.utils.NumberUtil

class SlayerComponent(profileState: State<Member?>) : UIContainer() {

    val slayers = profileState.map { it?.slayer?.slayer_bosses }
    val rev = slayers.map { it?.get("zombie") }
    val tara = slayers.map { it?.get("spider") }
    val sven = slayers.map { it?.get("wolf") }
    val eman = slayers.map { it?.get("enderman") }
    val blaze = slayers.map { it?.get("blaze") }
    val vampire = slayers.map { it?.get("vampire") }
    val totalXp = slayers.map { slayer ->
        val revXp = rev.get()?.xp ?: 0.0
        val taraXp = tara.get()?.xp ?: 0.0
        val svenXp = sven.get()?.xp ?: 0.0
        val emanXp = eman.get()?.xp ?: 0.0
        val blazeXp = blaze.get()?.xp ?: 0.0
        val vampireXp = vampire.get()?.xp ?: 0.0

        revXp + taraXp + svenXp + emanXp + blazeXp + vampireXp
    }

    val totalXpText by UIText().constrain {
        x = 0.pixels
        y = 0.pixels
    }.bindText(totalXp.map { xp ->
        "§7Total Slayer XP: §f${NumberUtil.nf.format(xp)}"
    }) childOf this

    val slayersContainer by UIContainer().constrain {
        x = 0.pixels
        y = SiblingConstraint(5f)
        width = RelativeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf this

    val zom by SlayerBossComponent(rev, "zombie").constrain {
        x = 0.pixels
        y = 0.pixels
        color = VigilancePalette.getBackground().withAlpha(120).constraint
    } childOf slayersContainer

    val spi by SlayerBossComponent(tara, "spider").constrain {
        x = CramSiblingConstraint(10f)
        y = CramSiblingConstraint(5f)
        color = VigilancePalette.getBackground().withAlpha(120).constraint
    } childOf slayersContainer

    val wol by SlayerBossComponent(sven, "wolf").constrain {
        x = CramSiblingConstraint(10f)
        y = CramSiblingConstraint(5f)
        color = VigilancePalette.getBackground().withAlpha(120).constraint
    } childOf slayersContainer

    val end by SlayerBossComponent(eman, "enderman").constrain {
        x = CramSiblingConstraint(10f)
        y = CramSiblingConstraint(5f)
        color = VigilancePalette.getBackground().withAlpha(120).constraint
    } childOf slayersContainer

    val bla by SlayerBossComponent(blaze, "blaze").constrain {
        x = CramSiblingConstraint(10f)
        y = CramSiblingConstraint(5f)
        color = VigilancePalette.getBackground().withAlpha(120).constraint
    } childOf slayersContainer

    val vam by SlayerBossComponent(vampire, "vampire").constrain {
        x = CramSiblingConstraint(10f)
        y = CramSiblingConstraint(5f)
        color = VigilancePalette.getBackground().withAlpha(120).constraint
    } childOf slayersContainer
}