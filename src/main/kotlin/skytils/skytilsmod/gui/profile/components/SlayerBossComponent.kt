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

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CramSiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import skytils.hylin.skyblock.slayer.Slayers
import skytils.hylin.skyblock.slayer.StandardSlayer
import skytils.skytilsmod.utils.ItemUtil
import skytils.skytilsmod.utils.SkillUtils
import skytils.skytilsmod.utils.toTitleCase
import kotlin.reflect.KProperty

class SlayerBossComponent<T : StandardSlayer>(slayerField: KProperty<T>, type: String) : UIRoundedRectangle(2f) {
    init {
        constrain {
            width = ChildBasedSizeConstraint(5f)
            height = ChildBasedSizeConstraint(5f)
        }

        val slayer: T = slayerField.call()
        val xpSet = SkillUtils.slayerXp[type]!!
        val (level, overflow, percent) = SkillUtils.calcXpWithOverflowAndProgress(
            slayer.xp.toDouble(),
            xpSet.size,
            xpSet.values
        )

        val stack = when (slayerField) {
            Slayers::revenant -> ItemUtil.setSkullTexture(
                ItemStack(Items.skull, level, 3),
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWZjMDE4NDQ3M2ZlODgyZDI4OTVjZTdjYmM4MTk3YmQ0MGZmNzBiZjEwZDM3NDVkZTk3YjZjMmE5YzVmYzc4ZiJ9fX0=",
                "063f9bdf-047b-47ef-85b6-533ff1dfd69b"
            )
            Slayers::tarantula -> ItemUtil.setSkullTexture(
                ItemStack(Items.skull, level, 3),
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ3ZTNiMTlhYzRmM2RlZTljNTY3N2MxMzUzMzNiOWQzNWE3ZjU2OGI2M2QxZWY0YWRhNGIwNjhiNWEyNSJ9fX0=",
                "063f9bdf-047b-47ef-85b6-533ff1dfd69b"
            )
            Slayers::sven -> ItemUtil.setSkullTexture(
                ItemStack(Items.skull, level, 3),
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjgzYTJhYTlkMzczNGI5MTlhYzI0Yzk2NTllNWUwZjg2ZWNhZmJmNjRkNDc4OGNmYTQzM2JiZWMxODllOCJ9fX0=",
                "063f9bdf-047b-47ef-85b6-533ff1dfd69b"
            )
            Slayers::enderman -> ItemUtil.setSkullTexture(
                ItemStack(Items.skull, level, 3),
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWIwOWEzNzUyNTEwZTkxNGIwYmRjOTA5NmIzOTJiYjM1OWY3YThlOGE5NTY2YTAyZTdmNjZmYWZmOGQ2Zjg5ZSJ9fX0=",
                "063f9bdf-047b-47ef-85b6-533ff1dfd69b"
            )
            else -> ItemStack(Items.skull, -1, 1)
        }

        XPComponent(
            ItemComponent(stack),
            "${slayerField.name.toTitleCase()} Slayer $level",
            if (level == xpSet.size) 1f else (percent % 1).toFloat(),
            overflow.toLong()
        ).constrain {
            x = 5.pixels
            y = 5.pixels
            width = 42.5.percent()
            height = 20.pixels()
        } childOf this

        for (field in slayer::class.java.declaredFields.filter { it.name.endsWith("Kills") }) {
            field.isAccessible = true
            UIWrappedText(
                """
                    #Tier ${(field.name.substringBeforeLast("Kills").substringAfter("t").toIntOrNull() ?: 0) + 1}
                    #${field.get(slayer) ?: 0}
                """.trimMargin("#"), centered = true
            ).constrain {
                x = CramSiblingConstraint(10f)
                y = CramSiblingConstraint(10f)
            } childOf this
        }
    }
}