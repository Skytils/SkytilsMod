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

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.MappedState
import gg.essential.elementa.state.State
import gg.essential.universal.USound
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.gui.settings.SettingComponent
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color

/**
 * Based on Vigilance's DropDown
 * https://github.com/Sk1erLLC/Vigilance
 * Licensed under LGPL-3.0
 * Modified by the Skytils team
 */
class DropdownComponent(
    initialSelection: Int,
    options: List<String>,
    outlineEffect: OutlineEffect? = OutlineEffect(VigilancePalette.getDivider(), 1f),
    optionPadding: Float = 6f
) : UIBlock() {
    private var onValueChange: (Int) -> Unit = { }
    private var active = false

    private var selectionState: State<Int> = BasicState(initialSelection)
    private var optionsState: State<List<String>> = BasicState(options)

    var collapsedHeight: HeightConstraint = 20.pixels

    fun height(newHeight: HeightConstraint) = apply {
        collapsedHeight = newHeight
    }

    fun bindSelection(newState: State<Int>) = apply {
        selectionState = newState
        currentSelectionState.rebind(optionsState.zip(selectionState))
        mappedOptions.rebind(optionsState)
        onValueChange(selectionState.get())
    }

    fun bindOptions(newState: State<List<String>>) = apply {
        optionsState = newState
        mappedOptions.rebind(optionsState)
        heightState.rebind(optionsState)
        currentSelectionState.rebind(optionsState.zip(selectionState))
    }

    private val currentSelectionState: MappedState<Pair<List<String>, Int>, String> =
        optionsState.zip(selectionState).map { (options, selection) -> options[selection] }
            .also {
                it.onSetValue {
                    readOptionComponents()
                }
            }
    private val currentSelectionText by UIText().bindText(currentSelectionState).constrain {
        x = 5.pixels()
        y = 6.pixels()
        color = VigilancePalette.getMidText().constraint
        fontProvider = getFontProvider()
    } childOf this

    private val downArrow by UIImage.ofResourceCached(SettingComponent.DOWN_ARROW_PNG).constrain {
        x = 5.pixels(true)
        y = 7.5.pixels()
        width = 9.pixels()
        height = 5.pixels()
    } childOf this

    private val upArrow by UIImage.ofResourceCached(SettingComponent.UP_ARROW_PNG).constrain {
        x = 5.pixels(true)
        y = 7.5.pixels()
        width = 9.pixels()
        height = 5.pixels()
    }

    private val scrollContainer by UIContainer().constrain {
        x = 5.pixels()
        y = SiblingConstraint(optionPadding) boundTo currentSelectionText
        width = ChildBasedMaxSizeConstraint()
        height = ChildBasedSizeConstraint() + optionPadding.pixels()
    } childOf this

    private val heightState: MappedState<List<String>, Number> = optionsState.map { options ->
        (options.size - 1) * (getFontProvider().getStringHeight(
            "Text",
            getTextScale()
        ) + optionPadding) - optionPadding
    }

    private val optionsHolder by ScrollComponent(customScissorBoundingBox = scrollContainer).constrain {
        x = 0.pixels()
        y = 0.pixels()
        height = PixelConstraint(1f) * heightState coerceAtMost
                basicHeightConstraint {
                    Window.of(this@DropdownComponent).getBottom() - this@DropdownComponent.getTop() - 31
                }
    } childOf scrollContainer

    private val mappedOptions = optionsState.map {
        it.mapIndexed { index, option ->
            // TODO: Wrap this somehow
            UIText(option).constrain {
                y = SiblingConstraint(optionPadding)
                color = Color(0, 0, 0, 0).toConstraint()
                fontProvider = getFontProvider()
            }.onMouseEnter {
                hoverText(this)
            }.onMouseLeave {
                unHoverText(this)
            }.onMouseClick { event ->
                event.stopPropagation()
                select(index)
            }
        }
    }.apply {
        onSetValue {
            optionsHolder.clearChildren()
            it.forEachIndexed { index, component ->
                if (index != selectionState.get())
                    component childOf optionsHolder
            }
        }
    }

    private val collapsedWidth = 22.pixels() + CopyConstraintFloat().to(currentSelectionText)

    private val expandedWidth =
        22.pixels() + (ChildBasedMaxSizeConstraint().to(optionsHolder) coerceAtLeast CopyConstraintFloat().to(
            currentSelectionText
        ))

    init {
        constrain {
            width = collapsedWidth
            height = collapsedHeight
            color = VigilancePalette.getDarkHighlight().toConstraint()
        }

        readOptionComponents()

        optionsHolder.hide(instantly = true)

        outlineEffect?.let(::enableEffect)

        val outlineContainer = UIContainer().constrain {
            x = (-1).pixels()
            y = (-1).pixels()
            width = RelativeConstraint(1f) + 2.pixels()
            height = RelativeConstraint(1f) + 3f.pixels()
        }
        outlineContainer.parent = this
        children.add(0, outlineContainer)
        enableEffect(ScissorEffect(outlineContainer))

        onMouseEnter {
            hoverText(currentSelectionText)
        }

        onMouseLeave {
            if (active) return@onMouseLeave

            unHoverText(currentSelectionText)
        }

        onLeftClick { event ->
            USound.playButtonPress()
            event.stopPropagation()

            if (active) {
                collapse()
            } else {
                expand()
            }
        }
    }

    fun select(index: Int) {
        if (index in optionsState.get().indices) {
            selectionState.set(index)
            onValueChange(index)
            collapse()
            readOptionComponents()
        }
    }

    fun onValueChange(listener: (Int) -> Unit) {
        onValueChange = listener
    }

    fun getValue() = selectionState.get()

    private fun expand() {
        active = true
        mappedOptions.get().forEach {
            it.setColor(VigilancePalette.getMidText().constraint)
        }

        animate {
            setHeightAnimation(
                Animations.IN_SIN,
                0.35f,
                collapsedHeight + RelativeConstraint(1f).boundTo(scrollContainer)
            )
        }

        optionsHolder.scrollToTop(false)

        replaceChild(upArrow, downArrow)
        setFloating(true)
        optionsHolder.unhide(useLastPosition = true)
        setWidth(expandedWidth)
    }

    fun collapse(unHover: Boolean = false, instantly: Boolean = false) {
        if (active)
            replaceChild(downArrow, upArrow)
        active = false

        fun animationComplete() {
            mappedOptions.get().forEach {
                it.setColor(Color(0, 0, 0, 0).toConstraint())
            }
            setFloating(false)
            optionsHolder.hide(instantly = true)
        }

        if (instantly) {
            setHeight(collapsedHeight)
            animationComplete()
        } else {
            animate {
                setHeightAnimation(Animations.OUT_SIN, 0.35f, collapsedHeight)

                onComplete(::animationComplete)
            }
        }

        if (unHover)
            unHoverText(currentSelectionText)

        setWidth(collapsedWidth)
    }

    private fun hoverText(text: UIComponent) {
        text.animate {
            setColorAnimation(Animations.OUT_EXP, 0.25f, VigilancePalette.getBrightText().constraint)
        }
    }

    private fun unHoverText(text: UIComponent) {
        text.animate {
            setColorAnimation(Animations.OUT_EXP, 0.25f, VigilancePalette.getMidText().constraint)
        }
    }

    private fun readOptionComponents() {
        optionsHolder.clearChildren()
        mappedOptions.get().forEachIndexed { index, component ->
            if (index != selectionState.get())
                component childOf optionsHolder
        }
    }
}