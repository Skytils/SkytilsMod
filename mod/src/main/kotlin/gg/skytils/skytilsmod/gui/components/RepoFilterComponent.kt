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

package gg.skytils.skytilsmod.gui.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.font.DefaultFonts
import gg.essential.elementa.state.BasicState
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.gui.settings.DropDown
import gg.skytils.skytilsmod.features.impl.handlers.SpamHider
import gg.skytils.skytilsmod.utils.toTitleCase

/**
 * Based on Vigilance under LGPL 3.0 license
 * Modified
 * https://github.com/Sk1erLLC/Vigilance/blob/master/LICENSE
 * @author Sk1er LLC
 */
class RepoFilterComponent(filter: SpamHider.Filter, dropDown: DropDown) : UIComponent() {
    private val boundingBox by UIBlock(VigilancePalette.getDarkHighlight().toConstraint()).constrain {
        x = 1.pixels()
        y = 1.pixels()
        width = RelativeConstraint(1f) - 10.pixels()
        height = ChildBasedMaxSizeConstraint() + INNER_PADDING.pixels()
    } childOf this effect OutlineEffect(
        VigilancePalette.getDivider(),
        1f
    ).bindColor(BasicState(VigilancePalette.getDivider()))

    private val textBoundingBox by UIContainer().constrain {
        x = INNER_PADDING.pixels()
        y = INNER_PADDING.pixels()
        width = RelativeConstraint(0.5f)
        height = ChildBasedSizeConstraint(3f) + INNER_PADDING.pixels()
    } childOf boundingBox

    private val filterName by UIWrappedText(filter.name).constrain {
        width = RelativeConstraint(1f)
        height = basicHeightConstraint { this.getTextScale() * 10 }
        textScale = 1.5f.pixels()
        color = VigilancePalette.getBrightText().toConstraint()
        fontProvider = DefaultFonts.VANILLA_FONT_RENDERER
    } childOf textBoundingBox

    init {
        UIWrappedText("${filter.type.name.toTitleCase()} ${filter.regex.pattern}")
            .constrain {
                y = SiblingConstraint() + 3.pixels()
                width = basicWidthConstraint {
                    fontProvider.getStringWidth(
                        SpamHider.FilterType.entries.map { it.name.toTitleCase() }
                            .maxByOrNull { it.length }!!,
                        this.getTextScale()
                    ) * 1.5f
                }
                fontProvider = DefaultFonts.VANILLA_FONT_RENDERER
            } childOf textBoundingBox
    }

    init {
        onMouseEnter {
            filterName.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, VigilancePalette.getAccent().toConstraint())
            }
        }

        onMouseLeave {
            filterName.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, VigilancePalette.getBrightText().toConstraint())
            }
        }

        dropDown.onValueChange {
            filter.state = it
        }
        dropDown.constrain {
            x = INNER_PADDING.pixels(true)
            basicYConstraint { parent.getTop() + 30f }
            width = 25.percent()
        }
        dropDown childOf boundingBox

        constrain {
            y = SiblingConstraint(10f)
            width = RelativeConstraint(1f)
            height = ChildBasedMaxSizeConstraint()
        }
    }

    companion object {
        const val INNER_PADDING = 15f
    }
}