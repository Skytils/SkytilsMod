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

package gg.skytils.skytilsmod.core.structure.v2

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.dsl.provideDelegate
import gg.essential.elementa.layoutdsl.*
import gg.essential.elementa.state.v2.MutableState
import gg.essential.elementa.state.v2.memo
import gg.essential.elementa.state.v2.mutableStateOf
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UResolution
import gg.skytils.skytilsmod.gui.layout.modifier.OffsetMouseAlignment
import gg.skytils.skytilsmod.gui.layout.modifier.onLeftClickEvent
import gg.skytils.skytilsmod.gui.layout.modifier.onMouseRelease
import java.awt.Color

abstract class HudElement(
    val name: String,
    val x: MutableState<Float>,
    val y: MutableState<Float>,
) {
    constructor(name: String, x: Float, y: Float) :
            this(name, mutableStateOf(x), mutableStateOf(y))

    private val position = memo {
        Modifier.alignHorizontal(Alignment.Start(x()))
            .alignVertical(Alignment.Start(y()))
    }

    private val demoPosition = memo {
        Modifier
            .onLeftClickEvent { event ->
                Modifier.alignBoth(OffsetMouseAlignment(event.relativeX, event.relativeY))
            }.onMouseRelease {
                x.set(getLeft() / UResolution.scaledWidth)
                y.set(getTop() / UResolution.scaledHeight)
                position()
            }
    }

    val component
        get() = UIContainer()
            .apply {
                layout(Modifier.then(position).childBasedSize(2f)) { render() }
            }

    val demoComponent
        get() = UIBlock(Color.WHITE.withAlpha(40))
            .apply {
                layout(
                    Modifier
                        .then(demoPosition)
                        .childBasedSize(2f)
                        .hoverColor(Color.WHITE.withAlpha(100))
                    , block = { demoRender() }
                )
            }


    abstract fun LayoutScope.render()

    abstract fun LayoutScope.demoRender()
}