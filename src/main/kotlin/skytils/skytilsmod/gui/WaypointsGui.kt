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

package skytils.skytilsmod.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UKeyboard
import gg.essential.vigilance.gui.settings.*
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.util.BlockPos
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.core.TickTask
import skytils.skytilsmod.features.impl.handlers.Waypoint
import skytils.skytilsmod.features.impl.handlers.Waypoints
import skytils.skytilsmod.gui.components.SimpleButton
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.SkyblockIsland
import skytils.skytilsmod.utils.setState
import skytils.skytilsmod.utils.toggle
import java.awt.Color

class WaypointsGui : WindowScreen(ElementaVersion.V1, newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent

    private val islandDropdown: DropDown
    private val sortingOrder: DropDown
    private val searchBar: UITextInput

    private val entries = HashMap<UIContainer, Entry>()
    private val categoryContainers = HashMap<UIContainer, Category>()

    init {
        scrollComponent = ScrollComponent(
            innerPadding = 4f,
        ).childOf(window).constrain {
            x = CenterConstraint()
            y = 15.percent()
            width = 90.percent()
            height = 70.percent() + 2.pixels()
        }

        val topButtons = UIContainer().childOf(window).constrain {
            x = 0.pixels()
            y = 5.percent()
            width = 100.percent()
            height = 50.pixels()
        }

        islandDropdown = DropDown(SkyblockIsland.values().indexOfFirst {
            SBInfo.mode == it.mode
        }.run { if (this == -1) 0 else this }, SkyblockIsland.values().map { it.formattedName }).childOf(topButtons)
            .constrain {
                x = 5.pixels(true)
            }.also {
                it.onValueChange { s ->
                    loadWaypointsForSelection(s)
                    expandAll()
                }
            }

        sortingOrder =
            DropDown(SortingOptions.lastSelected, SortingOptions.values().map { it.displayName }).childOf(topButtons)
                .constrain {
                    x = SiblingConstraint(10f, true)
                }.apply {
                    onValueChange {
                        expandAll()
                        SortingOptions.lastSelected = it
                        val sorter = SortingOptions.values()[it]
                        scrollComponent.allChildren.forEach { category ->
                            category as UIContainer
                            val uiContainers = category.childContainers.sortedBy { w ->
                                val entry = entries[w] ?: error("no entry found for child")
                                return@sortedBy sorter.sortingBy(entry.toWaypoint(SkyblockIsland.PrivateIsland))
                            }
                            // Remove and re-add the waypoints in the correct order
                            category.children.removeAll(uiContainers)
                            category.children.addAll(uiContainers)
                        }
                    }
                }

        searchBar = UITextInput("Search").childOf(topButtons).constrain {
            x = 2.pixels()
            y = 2.pixels()
            width = 20.percent()
            height = 23.pixels()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            onKeyType { _, _ ->
                expandAll()
                scrollComponent.allChildren.forEach { category ->
                    category.childContainers.forEach { w ->
                        val entry = entries[w] ?: error("no entry found for child")
                        if (entry.name.getText().contains(this@apply.getText())) {
                            w.unhide()
                        } else {
                            w.hide()
                        }
                    }
                }
            }
        }

        SimpleButton("Toggle Visible").childOf(topButtons).constrain {
            x = 2.pixels()
            y = 30.pixels()
            width = 100.pixels()
            height = 20.pixels()
        }.apply {
            onLeftClick {
                if (expandAll()) { // if not all of the categories are expanded, expand them. this will run on the next button press.
                    val valid = scrollComponent.allChildren.map { c ->
                        c.childContainers.mapNotNull { w ->
                            val entry = entries[w] ?: error("no entry found for child")
                            if (entry.name.getText().contains(searchBar.getText())) return@mapNotNull entry
                            else return@mapNotNull null
                        }
                    }.flatten()
                    if (valid.any { it.enabled.checked }) {
                        valid.forEach {
                            if (it.enabled.checked) it.enabled.toggle()
                        }
                    } else {
                        valid.forEach {
                            if (!it.enabled.checked) it.enabled.toggle()
                        }
                    }
                }
            }
        }

        SimpleButton("Remove Visible").childOf(topButtons).constrain {
            x = 107.pixels()
            y = 30.pixels()
            width = 100.pixels()
            height = 20.pixels()
        }.apply {
            onLeftClick {
                scrollComponent.allChildren.forEach { c ->
                    c as UIContainer
                    val category = categoryContainers[c] ?: error("no category found for UIContainer")
                    if (category.isCollapsed) return@forEach // Collapsed categories aren't visible
                    c.childContainers.forEach { w ->
                        val entry = entries[w] ?: error("no entry found for child")
                        if (entry.name.getText().contains(searchBar.getText())) {
                            c.removeChild(w)
                            category.children.remove(w)
                            entries.remove(w)
                            if (category.children.isEmpty()) {
                                scrollComponent.removeChild(c)
                                categoryContainers.remove(c)
                            }
                        }
                    }
                }
            }
        }

        SimpleButton("Expand All").childOf(topButtons).constrain {
            x = 2.pixels(alignOpposite = true)
            y = 30.pixels()
            width = 100.pixels()
            height = 20.pixels()
        }.onLeftClick { expandAll() }

        SimpleButton("Collapse All").childOf(topButtons).constrain {
            x = SiblingConstraint(5f, alignOpposite = true)
            y = 30.pixels()
            width = 100.pixels()
            height = 20.pixels()
        }.onLeftClick {
            categoryContainers.values.forEach { collapse(it) }
        }

        UIText("Waypoints").childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            height = 14.pixels()
        }

        val bottomButtons = UIContainer().childOf(window).constrain {
            x = CenterConstraint()
            y = 90.percent()
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        SimpleButton("Save and Exit").childOf(bottomButtons).constrain {
            x = 0.pixels()
            y = 0.pixels()
        }.onLeftClick {
            mc.displayGuiScreen(null)
        }

        SimpleButton("New Waypoints").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            addNewWaypoint()
        }

        SimpleButton("New Category").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            // Add a new category and a new blank waypoint inside it.
            val category = addNewCategory(name = "")
            addNewWaypoint(category)
        }

        SimpleButton("Share").childOf(window).constrain {
            x = 5.pixels()
            y = 5.pixels(alignOpposite = true)
        }.onLeftClick {
            mc.displayGuiScreen(null)
            TickTask(2) {
                Skytils.displayScreen = WaypointShareGui()
            }
        }

        loadWaypointsForSelection(islandDropdown.getValue(), savePrev = false)
    }

    private fun loadWaypointsForSelection(selection: Int, savePrev: Boolean = true, isClosing: Boolean = false) {
        if (savePrev) {
            val current = SkyblockIsland.values().find {
                it.formattedName == islandDropdown.childrenOfType<UIText>()
                    .find { it.componentName == "currentSelectionText" }?.getText()
            } ?: error("previous selected island not found")
            Waypoints.waypoints.removeAll {
                it.island == current
            }
            for (entry in entries.values) {
                runCatching {
                    Waypoints.waypoints.add(
                        entry.toWaypoint(current)
                    )
                }.onFailure {
                    it.printStackTrace()
                }
            }
            PersistentSave.markDirty<Waypoints>()
        }
        entries.clear()
        categoryContainers.clear()
        scrollComponent.clearChildren()
        if (!isClosing) {
            val island = SkyblockIsland.values()[selection]
            Waypoints.waypoints.filter {
                it.island == island
            }.sortedBy { SortingOptions.values()[SortingOptions.lastSelected].sortingBy(it) }.forEach {
                addNewWaypoint(it.category ?: "Uncategorized", it.name, it.pos, it.enabled, it.color, it.addedAt)
            }
        }
    }

    private fun addNewCategory(
        name: String = "",
        enabled: Boolean = true
    ): Category {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 90.percent()
            height = ChildBasedRangeConstraint()
        }.effect(OutlineEffect(Color(255, 255, 255, 100), 1f))

        val enabledComponent = CheckboxComponent(enabled).childOf(container).constrain {
            x = 7.5.pixels()
            y = 0.pixels()
        }.apply {
            onValueChange { newValue: Any? ->
                val categoryObj = categoryContainers[container] ?: error("no category found for UIContainer")
                // If this value change was triggered while updating the checkbox, don't update the child checkboxes.
                // see `updateCheckbox()`
                if(categoryObj.ignoreCheckboxValueChange) return@onValueChange
                // When the category is checked or unchecked, all child waypoints will follow this change.
                (this.parent as UIContainer).childContainers.forEach {
                    it.childrenOfType<CheckboxComponent>().firstOrNull()?.setState(newValue as Boolean)
                }
            }
        }

        SimpleButton("Remove").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            container.childContainers.forEach { entries.remove(it) } // remove all waypoints in this category from the list of entries
            container.children.clear() // clear the children of this category's UIContainer
            scrollComponent.removeChild(container) // remove this category's UIContainer from the ScrollComponent
            categoryContainers.remove(container) // remove this category from the list of categories
        }

        val newWaypointButton = SimpleButton("New Waypoint").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }

        val nameComponent = UITextInput("Category Name").childOf(container).constrain {
            x = CenterConstraint()
            y = 0.pixels()
            width = 30.percent()
            height = 24.pixels()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(name)
        }

        val expandComponent = SimpleButton("Collapse").childOf(container).constrain {
            x = 5.pixels(true)
            y = 0.pixels()
        }.apply {
            onLeftClick {
                val categoryObj = categoryContainers[container] ?: error("no category found for name")
                if (categoryObj.isCollapsed) expand(categoryObj) else collapse(categoryObj)
            }
        }

        newWaypointButton.onLeftClick {
            expand(categoryContainers[container] ?: error("no category found for UIContainer"))
            addNewWaypoint(categoryName = nameComponent.getText())
        }

        categoryContainers[container] = Category(
            container,
            enabledComponent,
            nameComponent,
            expandComponent,
            newWaypointButton
        )

        container.children.addObserver { _, _ ->
            // When children are added or removed, the category's checkbox should be updated to reflect those changes.
            updateCheckbox(categoryContainers[container] ?: return@addObserver)
        }

        return categoryContainers[container]!!
    }

    private fun addNewWaypoint(
        categoryName: String = "Uncategorized",
        name: String = "",
        pos: BlockPos = mc.thePlayer.position,
        enabled: Boolean = true,
        color: Color = Color.RED,
        addedAt: Long = System.currentTimeMillis()
    ) {
        addNewWaypoint(
            categoryContainers.entries.firstOrNull {
                it.value.name.getText() == categoryName
            }?.value ?: addNewCategory(categoryName),
            name, pos, enabled, color, addedAt
        )
    }

    private fun addNewWaypoint(
        category: Category,
        name: String = "",
        pos: BlockPos = mc.thePlayer.position,
        enabled: Boolean = true,
        color: Color = Color.RED,
        addedAt: Long = System.currentTimeMillis(),
    ) {
        val container = UIContainer().childOf(category.container).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 90.percent()
            height = ChildBasedMaxSizeConstraint()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f)).apply {
            animateBeforeHide {
                setHeightAnimation(Animations.IN_SIN, 0.2f, 0.pixels)
            }
            animateAfterUnhide {
                setHeightAnimation(Animations.IN_SIN, 0.2f, ChildBasedMaxSizeConstraint())
            }
        }

        val enabled = CheckboxComponent(enabled).childOf(container).constrain {
            x = 7.5.pixels()
            y = CenterConstraint()
        }.apply {
            onValueChange {
                Window.enqueueRenderOperation {
                    // Update the checkbox *after* the `checked` state is updated.
                    updateCheckbox(category)
                }
            }
        }

        val nameComponent = UITextInput("Waypoint Name").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 30.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(name)
        }

        val xComponent = UITextInput("X").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(pos.x.toString())
        }

        val yComponent = UITextInput("Y").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(pos.y.toString())
        }

        val zComponent = UITextInput("Z").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(pos.z.toString())
        }

        val colorComponent = ColorComponent(color, true).childOf(container).constrain {
            x = SiblingConstraint(25f)
            y = 0.pixels()
            width = CoerceAtLeastConstraint(AspectConstraint(), 10.percentOfWindow)
        }.apply {
            setColor(color)
            onValueChange { c ->
                setColor(c as Color)
            }
        }

        SimpleButton("Remove").childOf(container).constrain {
            x = 85.percent()
            y = CenterConstraint()
        }.onLeftClick {
            category.children.remove(container)
            category.container.removeChild(container)
            entries.remove(container)

            // If the last waypoint in a category is deleted, remove that category.
            if (category.children.isEmpty()) {
                categoryContainers.remove(category.container)
                scrollComponent.removeChild(category.container)
            }
        }

        nameComponent.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) xComponent.grabWindowFocus()
            }
        }

        xComponent.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) yComponent.grabWindowFocus()
                setText(getText().filter { c -> c.isDigit() || c == '-' })
            }
        }

        yComponent.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) zComponent.grabWindowFocus()
                setText(getText().filter { c -> c.isDigit() || c == '-' })
            }
        }

        zComponent.apply {
            onKeyType { _, keyCode ->
                if (keyCode == UKeyboard.KEY_TAB) nameComponent.grabWindowFocus()
                setText(getText().filter { c -> c.isDigit() || c == '-' })
            }
        }

        category.children.add(container)
        entries[container] =
            Entry(category, enabled, nameComponent, xComponent, yComponent, zComponent, colorComponent, addedAt)
    }

    override fun onScreenClose() {
        super.onScreenClose()
        loadWaypointsForSelection(-1, isClosing = true)
    }

    private fun updateCheckbox(category: Category) {
        category.ignoreCheckboxValueChange = true
        category.enabled.setState(
            category.children.all {
                entries[it]?.enabled?.checked == true
            }
        )
        category.ignoreCheckboxValueChange = false
    }

    private fun expandAll(): Boolean {
        val allExpanded = categoryContainers.values.all { !it.isCollapsed }
        categoryContainers.values.forEach(::expand)
        return allExpanded
    }

    private fun expand(category: Category) = category.apply {
        children.forEach {
            if (entries[it]!!.name.getText().contains(this@WaypointsGui.searchBar.getText())) {
                it.unhide(true)
            } else it.hide()
        }
        isCollapsed = false
        expandComponent.text.setText("Collapse")
    }

    private fun collapse(category: Category) = category.apply {
        children.forEach { it.hide() }
        isCollapsed = true
        expandComponent.text.setText("Expand")
    }

    internal data class Category(
        val container: UIContainer,
        val enabled: CheckboxComponent,
        val name: UITextInput,
        val expandComponent: SimpleButton,
        val newWaypointButton: SimpleButton,
        val children: MutableList<UIContainer> = mutableListOf(),
        var isCollapsed: Boolean = false,
        var ignoreCheckboxValueChange: Boolean = false
    )

    private data class Entry(
        val category: Category,
        val enabled: CheckboxComponent,
        val name: UITextInput,
        val x: UITextInput,
        val y: UITextInput,
        val z: UITextInput,
        val color: ColorComponent,
        val addedAt: Long,
        var colorPickerUp: Boolean = false,
        var colorPickerDown: Boolean = false,
    ) {
        fun toWaypoint(island: SkyblockIsland) = Waypoint(
            category.name.getText(),
            name.getText(),
            BlockPos(
                x.getText().toInt(),
                y.getText().toInt(),
                z.getText().toInt()
            ),
            island,
            enabled.checked,
            color.getColor(),
            addedAt
        )
    }

    enum class SortingOptions(val displayName: String, val sortingBy: (Waypoint) -> String) {
        AZ("A-Z", { "${it.name} ${it.pos} ${it.enabled}" }),
        CLOSEST("Closest", { "${mc.thePlayer?.getDistanceSq(it.pos)} ${AZ.sortingBy(it)}" }),
        RECENT("Recent", { "${Long.MAX_VALUE - it.addedAt} ${AZ.sortingBy(it)}" });

        companion object {
            var lastSelected = 0
        }
    }
}

// Gets the children of a UIContainer excluding all Vigilance components.
// Used to get a list of all waypoints in a category, because categories contain
// controls as well as multiple `UIContainer`s for the category's waypoints.
internal val UIComponent.childContainers
    get() = this.childrenOfType<UIContainer>().filter { it !is SettingComponent }
