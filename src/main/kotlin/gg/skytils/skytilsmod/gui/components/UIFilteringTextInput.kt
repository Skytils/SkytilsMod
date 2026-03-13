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

package gg.skytils.skytilsmod.gui.components

import gg.essential.elementa.components.input.UITextInput
import java.awt.Color

class UIFilteringTextInput(
    placeholder: String = "",
    shadow: Boolean = true,
    selectionBackgroundColor: Color = Color.WHITE,
    selectionForegroundColor: Color = Color(64, 139, 229),
    allowInactiveSelection: Boolean = false,
    inactiveSelectionBackgroundColor: Color = Color(176, 176, 176),
    inactiveSelectionForegroundColor: Color = Color.WHITE,
    cursorColor: Color = Color.WHITE
) : UITextInput(
    placeholder,
    shadow,
    selectionBackgroundColor,
    selectionForegroundColor,
    allowInactiveSelection,
    inactiveSelectionBackgroundColor,
    inactiveSelectionForegroundColor,
    cursorColor
) {
    private var textFilter: (String) -> String = { it }

    fun filter(block: (String) -> String) {
        textFilter = block
    }

    override fun commitTextOperation(operation: TextOperation) {
        var newOperation = operation
        val originalLength = getText().length
        // temporarily apply operation to check the new text
        operation.redo()
        val unfiltered = getText()
        val filtered = textFilter(unfiltered)
        if (filtered != unfiltered) {
            if (originalLength == filtered.length) {
                // Because our filter creates a no-op, we simply do not commit
                // the operation and return early
                operation.undo()
                return
            }
            val start = LinePosition(0, 0, false)
            newOperation = CompoundTextOperation(
                newOperation,
                ReplaceTextOperation(
                    AddTextOperation(filtered, start),
                    RemoveTextOperation(start, LinePosition(0, unfiltered.length, false), false)
                )
            )
        }
        operation.undo()
        super.commitTextOperation(newOperation)
    }

    private inner class CompoundTextOperation(
        val old: TextOperation,
        val new: TextOperation,
    ) : TextOperation() {
        override fun redo() {
            old.redo()
            println(getText())

            val originalCursor = cursor
            val originalSelectionEnd = otherSelectionEnd
            new.redo()
            println(getText())
            cursor = originalCursor.coerceInBounds()
            otherSelectionEnd = originalSelectionEnd.coerceInBounds()
        }

        override fun undo() {
            new.undo()
            old.undo()
        }

    }

    private fun LinePosition.coerceInBounds(): LinePosition {
        val lines = if (isVisual) visualLines else textualLines
        val line = line.coerceIn(0, lines.lastIndex)
        return LinePosition(line, column.coerceIn(0, lines[line].length), isVisual)
    }
}