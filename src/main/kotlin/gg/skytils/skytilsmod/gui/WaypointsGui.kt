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

package gg.skytils.skytilsmod.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.gui.settings.ColorComponent
import gg.essential.vigilance.gui.settings.DropDown
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.core.tickTimer
import gg.skytils.skytilsmod.features.impl.handlers.Waypoint
import gg.skytils.skytilsmod.features.impl.handlers.WaypointCategory
import gg.skytils.skytilsmod.features.impl.handlers.Waypoints
import gg.skytils.skytilsmod.gui.components.HelpComponent
import gg.skytils.skytilsmod.gui.components.MultiCheckboxComponent
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.utils.*
import net.minecraft.util.BlockPos
import java.awt.Color

class WaypointsGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2), ReopenableGUI {

    private val scrollComponent: ScrollComponent

    private val islandDropdown: DropDown
    private val sortingOrder: DropDown
    private val searchBar: UITextInput

    private val entries = HashMap<UIContainer, Entry>()
    private val categoryContainers = HashMap<UIContainer, Category>()

    init {
        lastUpdatedPlayerPosition = mc.thePlayer?.position

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

        val isOnUnknownIsland = SkyblockIsland.values().none { it.mode == SBInfo.mode }
        val hasAnyUnknownWaypoint = Waypoints.categories.any { it.island == SkyblockIsland.Unknown }
        val options = SkyblockIsland.values()
            .mapNotNull { if (it == SkyblockIsland.Unknown && !isOnUnknownIsland && !hasAnyUnknownWaypoint) null else it.displayName }
        islandDropdown = DropDown(
            SkyblockIsland.values().indexOfFirst {
                SBInfo.mode == it.mode
            }.run { if (this == -1) SkyblockIsland.values().indexOf(SkyblockIsland.Unknown) else this },
            options
        ).childOf(topButtons)
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
                    onValueChange { newValue ->
                        expandAll()
                        SortingOptions.lastSelected = newValue
                        val sorter = SortingOptions.values()[newValue]
                        val highestValues = mutableMapOf<UIContainer, Waypoint?>()
                        // First, sort each category in place
                        scrollComponent.allChildren.forEach { category ->
                            category as UIContainer
                            val uiContainers = category.childContainers.sortedWith { a, b ->
                                sorter.comparator.compare(entries[a]!!.toWaypoint(), entries[b]!!.toWaypoint())
                            }
                            highestValues[category] = entries[uiContainers.firstOrNull()]?.toWaypoint()
                            // Remove and re-add the waypoints in the correct order to their category
                            category.children.removeAll(uiContainers)
                            category.children.addAll(uiContainers)
                        }
                        // Then, sort the categories by their highest value according to the sorting function.
                        scrollComponent.clearChildren()
                        categoryContainers.values.sortedWith { a: Category, b: Category ->
                            sorter.comparator.compare(highestValues[a.container], highestValues[b.container])
                        }.forEach {
                            scrollComponent.addChild(it.container)
                        }
                    }
                }

        searchBar = UITextInput("Search").childOf(topButtons).constrain {
            x = 2.pixels()
            y = 2.pixels()
            width = 20.percent()
            height = 23.pixels()
        }.apply {
            val searchBar = this
            onLeftClick {
                grabWindowFocus()
            }
            onKeyType { _, _ ->
                expandAll()
                scrollComponent.allChildren.forEach { category ->
                    category.childContainers.forEach { w ->
                        val entry = entries[w] ?: throwNoEntryFoundError()
                        if (entry.name.getText().contains(searchBar.getText())) {
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
                            val entry = entries[w] ?: throwNoEntryFoundError()
                            if (entry.name.getText().contains(searchBar.getText())) return@mapNotNull entry
                            else return@mapNotNull null
                        }
                    }.flatten()
                    if (valid.any { it.enabled.checked!! }) {
                        valid.forEach {
                            if (it.enabled.checked!!) it.enabled.toggle()
                        }
                    } else {
                        valid.forEach {
                            if (!it.enabled.checked!!) it.enabled.toggle()
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
                scrollComponent.allChildren.forEach { container ->
                    container as UIContainer
                    val category = categoryContainers[container] ?: throwCategoryNotFoundError()
                    if (!category.isCollapsed) // Collapsed categories aren't visible
                        container.childContainers.forEach { waypoint ->
                            val entry = entries[waypoint] ?: throwNoEntryFoundError()
                            if (entry.name.getText().contains(searchBar.getText())) {
                                container.removeChild(waypoint)
                                category.children.remove(waypoint)
                                entries.remove(waypoint)
                                if (category.children.isEmpty()) {
                                    scrollComponent.removeChild(container)
                                    categoryContainers.remove(container)
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

        SimpleButton("New Category").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            // Add a new category and a new blank waypoint inside it.
            val category = addNewCategory(name = "", isExpanded = true)
            addNewWaypoint(category)
        }

        HelpComponent(
            window,
            "Waypoints are organized by category. To add a category, click the 'New Category' button. To create a new waypoint in a category, click the 'New Waypoint' button at the top of any category. Clicking the 'Remove' button at the top of a category will remove all waypoints in the category. Waypoints are separated by the island that they are displayed on, so if you don't see the waypoint that you're looking for, try changing the island in the dropdown menu in the top right corner. To share waypoints with someone else, click the 'Share' button in the bottom left corner."
        )

        SimpleButton("Share").childOf(window).constrain {
            x = SiblingConstraint(5f)
            y = 5.pixels(alignOpposite = true)
        }.onLeftClick {
            mc.displayGuiScreen(null)
            tickTimer(2) {
                Skytils.displayScreen = WaypointShareGui()
            }
        }

        loadWaypointsForSelection(islandDropdown.getValue(), savePrev = false)
    }

    private fun loadWaypointsForSelection(selection: Int, savePrev: Boolean = true, isClosing: Boolean = false) {
        if (savePrev) {
            val current = SkyblockIsland.values().find {
                it.displayName == islandDropdown.childrenOfType<UIText>()
                    .find { it.componentName == "currentSelectionText" }?.getText()
            } ?: error("previous selected island not found")
            Waypoints.categories.removeAll {
                it.island == current
            }
            for (category in categoryContainers.values) {
                Waypoints.categories.add(
                    WaypointCategory(
                        category.name.getText(),
                        category.children.mapNotNull { entries[it]?.toWaypoint() }.toHashSet(),
                        !category.isCollapsed,
                        current
                    )
                )
            }
            PersistentSave.markDirty<Waypoints>()
        }
        entries.clear()
        categoryContainers.clear()
        scrollComponent.clearChildren()
        if (!isClosing) {
            val island = SkyblockIsland.values()[selection]
            val comparator = SortingOptions.values()[SortingOptions.lastSelected].comparator
            // Sort the categories by their highest value, and then add the waypoints in each category sorted by their values
            Waypoints.categories.sortedWith { a, b ->
                comparator.compare(
                    a.waypoints.maxWithOrNull(comparator) ?: return@sortedWith 0,
                    b.waypoints.maxWithOrNull(comparator) ?: return@sortedWith 0
                )
            }.filter {
                it.island == island
            }.forEach {
                val category = addNewCategory(it.name ?: "", isExpanded = it.isExpanded)
                for (waypoint in it.waypoints.sortedWith(comparator)) {
                    addNewWaypoint(
                        category, waypoint.name, waypoint.pos, waypoint.enabled, waypoint.color, waypoint.addedAt
                    )
                }
            }
        }
    }

    private fun addNewCategory(
        name: String = "",
        enabled: Boolean = true,
        isExpanded: Boolean
    ): Category {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 90.percent()
            height = ChildBasedRangeConstraint() + (CATEGORY_INNER_PADDING * 2).pixels()
        }.effect(OutlineEffect(Color(255, 255, 255, 100), 1f))

        val enabledComponent = MultiCheckboxComponent(enabled).childOf(container).constrain {
            x = 7.5.pixels()
            y = CATEGORY_INNER_PADDING.pixels()
        }.apply {
            onValueChange { newValue: Any? ->
                val categoryObj = categoryContainers[container] ?: throwCategoryNotFoundError()
                if (newValue != null) categoryObj.children.forEach {
                    // When the category is checked or unchecked, all child waypoints will follow this change.
                    entries[it]?.enabled?.setState(newValue as Boolean)
                }
            }
        }

        val nameComponent = UITextInput("Category Name").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = (CATEGORY_INNER_PADDING + 5).pixels()
            width = 30.percent()
            height = 24.pixels()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(name)
        }

        val expandComponent = SimpleButton("Collapse").childOf(container).constrain {
            x = PixelConstraint(5f, alignOpposite = true)
            y = CATEGORY_INNER_PADDING.pixels()
        }.apply {
            onLeftClick {
                val categoryObj = categoryContainers[container] ?: throwCategoryNotFoundError()
                if (categoryObj.isCollapsed) expand(categoryObj) else collapse(categoryObj)
            }
        }

        SimpleButton("Remove").childOf(container).constrain {
            x = SiblingConstraint(5f, alignOpposite = true)
            y = CATEGORY_INNER_PADDING.pixels()
        }.onLeftClick {
            container.childContainers.forEach { entries.remove(it) } // remove all waypoints in this category from the list of entries
            container.children.clear() // clear the children of this category's UIContainer
            scrollComponent.removeChild(container) // remove this category's UIContainer from the ScrollComponent
            categoryContainers.remove(container) // remove this category from the list of categories
        }

        val newWaypointButton = SimpleButton("New Waypoint").childOf(container).constrain {
            x = SiblingConstraint(5f, alignOpposite = true)
            y = CATEGORY_INNER_PADDING.pixels()
        }

        newWaypointButton.onLeftClick {
            expand(categoryContainers[container] ?: throwCategoryNotFoundError())
            addNewWaypoint(category = categoryContainers[container]!!)
        }

        val category = Category(
            container = container,
            enabled = enabledComponent,
            name = nameComponent,
            expandComponent = expandComponent,
            newWaypointButton = newWaypointButton,
            isCollapsed = !isExpanded
        )
        categoryContainers[container] = category

        // When children are added or removed, the category's checkbox should be updated to reflect those changes.
        container.children.addObserver { _, _ ->
            Window.enqueueRenderOperation { // Update after the child is added so that its checked state can be used
                if (!container.children.isEmpty() && !category.isCollapsed) updateCheckbox(categoryContainers[container])
            }
        }
        if (!isExpanded) collapse(categoryContainers[container]!!) // Update the "Collapse" button text if the category is collapsed to begin with

        return category
    }

    private var lastHoveredCategory: Category? = null

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
            width = 95.percent()
            height = ChildBasedMaxSizeConstraint() + 10.pixels()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f)).apply {
            animateBeforeHide {
                setHeightAnimation(Animations.IN_SIN, 0.2f, 0.pixels)
            }
            animateAfterUnhide {
                setHeightAnimation(Animations.IN_SIN, 0.2f, ChildBasedMaxSizeConstraint() + 10.pixels())
            }
        }

        val enabled = MultiCheckboxComponent(enabled).childOf(container).constrain {
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

        val xComponent = container.createSmallTextBox("X", pos.x.toString())
        val yComponent = container.createSmallTextBox("Y", pos.y.toString())
        val zComponent = container.createSmallTextBox("Z", pos.z.toString())

        listOf(xComponent, yComponent, zComponent).forEach {
            it.limitToNumericalCharacters().colorIfNumeric(Color.WHITE, Color(170, 0, 0))
        }

        val colorComponent = ColorComponent(color, true).childOf(container).constrain {
            x = SiblingConstraint(25f)
            y = CenterConstraint()
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

        MoveComponent().childOf(container).constrain {
            x = 5.pixels(alignOpposite = true)
            y = CenterConstraint()
        }.onMouseClick { event ->
            val entry = entries[container] ?: throwNoEntryFoundError()
            val currentCategory = categoryContainers[container.parent] ?: return@onMouseClick
            entry.isDragging = true
            entry.lastDragPos = event.absoluteX to event.absoluteY
            entry.previousCategory = currentCategory

            // Remove this waypoint from its old category and make it a child of the Window so it can be freely moved around
            // prevent the container's width from changing when it is removed from the container
            val absoluteWidth = container.getWidth()
            val absoluteHeight = container.getHeight()
            container.setWidth(absoluteWidth.pixels())
            currentCategory.container.removeChild(container)
            currentCategory.children.remove(container)
            window.addChild(container)
            val offsetX = getLeft() - container.getRight() + event.relativeX
            val offsetY = getTop() - container.getBottom() + event.relativeY
            Window.enqueueRenderOperation {
                // This prevents an edge case where the waypoint would appear almost off the screen
                container.setX(basicXConstraint { event.absoluteX - absoluteWidth - offsetX })
                container.setY(basicYConstraint { event.absoluteY - absoluteHeight - offsetY })
            }
        }.onMouseDrag { mouseX, mouseY, _ ->
            val entry = entries[container] ?: throwNoEntryFoundError()
            if (!entry.isDragging) return@onMouseDrag
            val (startX, startY) = entry.lastDragPos ?: return@onMouseDrag

            val absX = mouseX + getLeft()
            val absY = mouseY + getTop()
            val dx = absX - startX
            val dy = absY - startY

            entry.lastDragPos = absX to absY

            val newX = entry.container.getLeft() + dx
            val newY = entry.container.getTop() + dy
            container.setX(newX.pixels)
            container.setY(newY.pixels)

            // If the mouse is hovered over a new category, create a placeholder that would be replaced
            // with the waypoint if the mouse were to be released.
            val hovered = categoryContainers.entries.firstOrNull { it.key.isHovered() }?.value ?: return@onMouseDrag
            lastHoveredCategory = hovered

            for (categoryObj in categoryContainers.values) {
                if (categoryObj.dragGuideElement != null && hovered != categoryObj) {
                    // Remove drag placeholder objects that don't belong to the currently hovered category.
                    categoryObj.container.removeChild(categoryObj.dragGuideElement!!)
                    categoryObj.dragGuideElement = null
                }
            }

            if (hovered.dragGuideElement == null) {
                hovered.dragGuideElement = UIBlock(VigilancePalette.getSuccess()).constrain {
                    x = 0.pixels()
                    y = SiblingConstraint(5f)
                    width = 100.percent()
                    height = 4.pixels()
                }.childOf(hovered.container)
            }
        }.onMouseRelease {
            val entry = entries[container] ?: throwNoEntryFoundError()
            if (!entry.isDragging || entry.previousCategory == null || lastHoveredCategory == null) return@onMouseRelease
            entry.isDragging = false
            entry.lastDragPos = null

            val newCategory = lastHoveredCategory!!
            // Remove the container as a child of the Window because it will be added to a UIContainer below:
            Window.enqueueRenderOperation { // This all must be run during the next frame so that we don't modify children while iterating over them.
                window.removeChild(container)
                if (newCategory.dragGuideElement != null) {
                    // Remove the dragging guide because the drag has ended
                    newCategory.container.removeChild(newCategory.dragGuideElement!!)
                    newCategory.dragGuideElement = null
                }
                // Add the waypoint to the category that it was dragged to
                newCategory.container.addChild(container)
                newCategory.children.add(container)
                // Set it back to its original constraints so it fits back into the category
                container.constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint(5f)
                    width = 95.percent()
                }
                entry.previousCategory = null
            }
        }

        // When pressing the TAB key, cycle between the different input fields.
        nameComponent.setTabTarget(xComponent)
        xComponent.setTabTarget(yComponent)
        yComponent.setTabTarget(zComponent)
        zComponent.setTabTarget(nameComponent)

        category.children.add(container)
        entries[container] =
            Entry(
                category,
                container,
                enabled,
                nameComponent,
                xComponent,
                yComponent,
                zComponent,
                colorComponent,
                addedAt
            )

        if (category.isCollapsed) container.hide(true)
    }

    private fun UIContainer.createSmallTextBox(placeholder: String, defaultText: String): UITextInput =
        UITextInput(placeholder).childOf(this).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 5.percent()
        }.apply {
            onLeftClick {
                grabWindowFocus()
            }
            setText(defaultText)
        }

    override fun onScreenClose() {
        super.onScreenClose()
        loadWaypointsForSelection(-1, isClosing = true)
        Waypoints.computeVisibleWaypoints()
    }

    private fun updateCheckbox(category: Category?) {
        category ?: return
        val anyEnabled = category.children.any {
            entries[it]?.enabled?.checked == true
        }
        val allEnabled = anyEnabled && category.children.all {
            entries[it]?.enabled?.checked == true
        }
        category.enabled.setState(
            // If all checkboxes are checked, set the state to on.
            // If some but not all of the checkboxes are checked, set the state to an indeterminate state.
            // If none are checked, uncheck the checkbox.
            if (allEnabled) true else if (anyEnabled) null else false
        )
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

    private data class Category(
        val container: UIContainer,
        val enabled: MultiCheckboxComponent,
        val name: UITextInput,
        val expandComponent: SimpleButton,
        val newWaypointButton: SimpleButton,
        val children: MutableList<UIContainer> = mutableListOf(),
        var isCollapsed: Boolean = false,
        var ignoreCheckboxValueChange: Boolean = false,
        var dragGuideElement: UIBlock? = null
    )

    private data class Entry(
        val category: Category,
        val container: UIContainer,
        val enabled: MultiCheckboxComponent,
        val name: UITextInput,
        val x: UITextInput,
        val y: UITextInput,
        val z: UITextInput,
        val color: ColorComponent,
        val addedAt: Long,
        var isDragging: Boolean = false,
        var lastDragPos: Pair<Float, Float>? = null,
        var previousCategory: Category? = null
    ) {
        fun toWaypoint() = Waypoint(
            name.getText(),
            x.getText().toIntOrNull() ?: 0,
            y.getText().toIntOrNull() ?: 0,
            z.getText().toIntOrNull() ?: 0,
            enabled.checked!!,
            color.getColor(),
            addedAt
        )
    }

    enum class SortingOptions(val displayName: String, val comparator: Comparator<Waypoint>) {
        AZ("A-Z", { a, b ->
            a.name.compareTo(b.name)
        }),
        CLOSEST("Closest", { a, b ->
            val distanceA = lastUpdatedPlayerPosition?.distanceSq(a.pos) ?: 0.0
            val distanceB = lastUpdatedPlayerPosition?.distanceSq(b.pos) ?: 0.0
            distanceA.compareTo(distanceB)
        }),
        FARTHEST("Farthest", { a, b ->
            val distanceA = lastUpdatedPlayerPosition?.distanceSq(a.pos) ?: 0.0
            val distanceB = lastUpdatedPlayerPosition?.distanceSq(b.pos) ?: 0.0
            distanceB.compareTo(distanceA)
        }),
        RECENT("Recent", { a, b ->
            a.addedAt.compareTo(b.addedAt)
        });

        companion object {
            var lastSelected = 0
        }
    }

    private fun throwCategoryNotFoundError(): Nothing = error("no category found for UIContainer")
    private fun throwNoEntryFoundError(): Nothing = error("no entry found for child")

    companion object {
        const val CATEGORY_INNER_PADDING = 7.5
        var lastUpdatedPlayerPosition: BlockPos? = null
    }
}

/**
 * A component with three lines that signifies a button which allows the user to move the waypoint to another category.
 */
private class MoveComponent : UIContainer() {
    init {

        constrain {
            width = 12.pixels()
            height = 10.pixels()
        }

        for (i in 0..2)
            UIBlock().constrain {
                y = (1.0 + 3.5 * i).pixels()
                x = 1.pixels()
                width = 10.pixels()
                height = 1.pixels()
            } childOf this
    }
}