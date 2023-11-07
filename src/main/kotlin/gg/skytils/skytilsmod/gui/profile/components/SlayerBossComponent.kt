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

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ChildBasedRangeConstraint
import gg.essential.elementa.constraints.CopyConstraintFloat
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.State
import gg.essential.universal.UMinecraft
import gg.skytils.skytilsmod.utils.ItemUtil
import gg.skytils.skytilsmod.utils.SkillUtils
import gg.skytils.skytilsmod.utils.toTitleCase
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import skytils.hylin.skyblock.slayer.RevenantSlayer
import skytils.hylin.skyblock.slayer.StandardSlayer

class SlayerBossComponent<T : StandardSlayer>(slayer: State<T?>, type: String) : UIRoundedRectangle(2f) {
    val xpSet = SkillUtils.slayerXp[type] ?: SkillUtils.slayerXp.values.first()

    val slayerXp = slayer.map { it?.xp?.toDouble() ?: 0.0 }

    val levelData = slayerXp.map {
        SkillUtils.calcXpWithOverflowAndProgress(
            it,
            xpSet.size,
            xpSet.values
        )
    }

    val stack = levelData.map {
        return@map when (type) {
            "zombie" -> ItemUtil.setSkullTexture(
                ItemStack(Items.skull, it.first, 3),
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWZjMDE4NDQ3M2ZlODgyZDI4OTVjZTdjYmM4MTk3YmQ0MGZmNzBiZjEwZDM3NDVkZTk3YjZjMmE5YzVmYzc4ZiJ9fX0=",
                "063f9bdf-047b-47ef-85b6-533ff1dfd69b"
            )
            "spider" -> ItemUtil.setSkullTexture(
                ItemStack(Items.skull, it.first, 3),
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ3ZTNiMTlhYzRmM2RlZTljNTY3N2MxMzUzMzNiOWQzNWE3ZjU2OGI2M2QxZWY0YWRhNGIwNjhiNWEyNSJ9fX0=",
                "baf72192-7e1b-45a2-a80e-2e873bdbbacf"
            )
            "wolf" -> ItemUtil.setSkullTexture(
                ItemStack(Items.skull, it.first, 3),
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjgzYTJhYTlkMzczNGI5MTlhYzI0Yzk2NTllNWUwZjg2ZWNhZmJmNjRkNDc4OGNmYTQzM2JiZWMxODllOCJ9fX0=",
                "daf2a9b9-2326-4cc2-b1a9-d49194886c70"
            )
            "enderman" -> ItemUtil.setSkullTexture(
                ItemStack(Items.skull, it.first, 3),
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWIwOWEzNzUyNTEwZTkxNGIwYmRjOTA5NmIzOTJiYjM1OWY3YThlOGE5NTY2YTAyZTdmNjZmYWZmOGQ2Zjg5ZSJ9fX0=",
                "c4880012-4860-43d2-aaaf-51dfc00a8399"
            )
            else -> ItemStack(Items.skull, -1, 1)
        }
    }

    val progress = levelData.map { (level, _, percent) ->
        if (level == xpSet.size) 1f else (percent % 1).toFloat()
    }

    val textWidth = UMinecraft.getFontRenderer().getStringWidth("Tier III").pixels

    val xpComponent by XPComponent(
        ItemComponent(stack)
    ).constrain {
        x = 5.pixels
        y = 5.pixels
        width = ((textWidth + 2.5.pixels) * 4 + textWidth)
        height = 20.pixels()
    }.apply {
        bindText(levelData.map { (level, _, _) ->
            "${type.toTitleCase()} Slayer $level"
        })
        bindOverflow(levelData.map { it.second.toLong() })
        bindPercent(progress)
    } childOf this

    val t1Kills by UIWrappedText(centered = true).constrain {
        x = 5.pixels
        y = SiblingConstraint(2.5f)
        width = textWidth
    }.bindText(slayer.map {
        """
            #Tier I
            #${it?.t0Kills ?: 0}
        """.trimMargin("#")
    }) childOf this

    val t2Kills by UIWrappedText(centered = true).constrain {
        x = SiblingConstraint(2.5f) boundTo t1Kills
        y = CopyConstraintFloat() boundTo t1Kills
        width = textWidth
    }.bindText(slayer.map {
        """
            #Tier II
            #${it?.t1Kills ?: 0}
        """.trimMargin("#")
    }) childOf this

    val t3Kills by UIWrappedText(centered = true).constrain {
        x = SiblingConstraint(2.5f) boundTo t2Kills
        y = CopyConstraintFloat() boundTo t1Kills
        width = textWidth
    }.bindText(slayer.map {
        """
            #Tier III
            #${it?.t2Kills ?: 0}
        """.trimMargin("#")
    }) childOf this

    val t4Kills by UIWrappedText(centered = true).constrain {
        x = SiblingConstraint(2.5f) boundTo t3Kills
        y = CopyConstraintFloat() boundTo t1Kills
        width = textWidth
    }.bindText(slayer.map {
        """
            #Tier IV
            #${it?.t3Kills ?: 0}
        """.trimMargin("#")
    }) childOf this

    val t5Kills by UIWrappedText(centered = true).constrain {
        x = SiblingConstraint(2.5f) boundTo t4Kills
        y = CopyConstraintFloat() boundTo t1Kills
        width = textWidth
    }.bindText(slayer.map {
        if (it is RevenantSlayer) {
            """
                #Tier V
                #${it.t4Kills ?: 0}
            """.trimMargin("#")
        } else ""
    }) childOf this

    init {
        constrain {
            width = xpComponent.constraints.width + 10.pixels
            height = ChildBasedRangeConstraint() + 10.pixels
        }
    }
}