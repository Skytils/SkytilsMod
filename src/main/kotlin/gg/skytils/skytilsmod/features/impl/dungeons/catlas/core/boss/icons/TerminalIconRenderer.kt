/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2025 Skytils
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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.boss.icons

import gg.essential.universal.UGraphics
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.utils.F7Terminals
import gg.skytils.skytilsmod.utils.RenderUtil

object TerminalIconRenderer : IconRenderer() {
    override fun draw(iconCtx: IconContext) {
        if (CatlasConfig.bossMapTerminalIcons) return
        F7Terminals.entries.forEach { terminal ->
            UGraphics.pushMatrix()
            UGraphics.translate(iconCtx.worldToIconX(terminal.pos.x), iconCtx.worldToIconY(terminal.pos.z), 0.0)
            RenderUtil.renderItem(terminal.type.item, 0, 0)
            UGraphics.popMatrix()
        }
    }
}