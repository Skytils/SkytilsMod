/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.core

import gg.skytils.skytilsmod.gui.OptionsGui
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.client.IModGuiFactory
import net.minecraftforge.fml.client.IModGuiFactory.RuntimeOptionGuiHandler

class ForgeGuiFactory : IModGuiFactory {
    override fun initialize(minecraft: Minecraft) {
    }

    override fun mainConfigGuiClass(): Class<out GuiScreen> = OptionsGui::class.java

    override fun runtimeGuiCategories(): Set<IModGuiFactory.RuntimeOptionCategoryElement> = emptySet()

    override fun getHandlerFor(runtimeOptionCategoryElement: IModGuiFactory.RuntimeOptionCategoryElement): RuntimeOptionGuiHandler =
        object : RuntimeOptionGuiHandler {
            override fun addWidgets(list: MutableList<Gui>?, i: Int, j: Int, k: Int, l: Int) {}

            override fun paint(i: Int, j: Int, k: Int, l: Int) {}

            override fun actionCallback(i: Int) {}

            override fun close() {}
        }

}