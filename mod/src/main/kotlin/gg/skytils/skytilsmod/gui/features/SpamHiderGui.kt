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

package gg.skytils.skytilsmod.gui.features

import gg.essential.api.EssentialAPI
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.universal.UChat
import gg.essential.vigilance.data.PropertyItem
import gg.essential.vigilance.data.PropertyType
import gg.essential.vigilance.gui.settings.DropDown
import gg.essential.vigilance.utils.onLeftClick
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.SpamHider
import gg.skytils.skytilsmod.gui.components.AccordionComponent
import gg.skytils.skytilsmod.gui.components.CustomFilterComponent
import gg.skytils.skytilsmod.gui.components.RepoFilterComponent
import gg.skytils.skytilsmod.gui.components.SimpleButton
import gg.skytils.skytilsmod.utils.ModChecker
import java.awt.Color

class SpamHiderGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    @Suppress("UNCHECKED_CAST")
    val configHiders = Skytils.config.getCategories()
        .flatMap { category ->
            category.items.filter { item ->
                item is PropertyItem && item.data.getDataType() == PropertyType.SELECTOR && item.data.attributesExt.category == "Spam"
            }
        } as List<PropertyItem>


    val saveButton = SimpleButton("Save & Exit")
        .constrain {
            x = CenterConstraint()
            y = RelativeConstraint() - 5.pixels() - basicYConstraint { this.getHeight() }
        } childOf window

    init {
        saveButton.onLeftClick {
            mc.displayGuiScreen(null)
        }
    }

    val scrollContainer = UIContainer()
        .constrain {
            x = 5.pixels()
            y = 5.pixels()
            width = basicWidthConstraint { window.getWidth() - 10 }
            height = basicHeightConstraint { window.getHeight() - 15 - saveButton.getHeight() }
        } childOf window
    val container = ScrollComponent(pixelsPerScroll = 30f)
        .constrain {
            width = RelativeConstraint()
            height = RelativeConstraint()
        } childOf scrollContainer
    val scrollbar = UIRoundedRectangle(5f)
        .constrain {
            x = RelativeConstraint(0.975f)
            width = RelativeConstraint(0.025f)
            color = Color(255, 255, 255, 80).toConstraint()
        } childOf scrollContainer

    // Accordions
    val toasts = AccordionComponent("Toasts")
        .constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = RelativeConstraint()
            height = RelativeConstraint(0.1f)
        } childOf container effect ScissorEffect()
    val toastInfo = AccordionComponent("What's a toast?")
        .constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = RelativeConstraint()
            height = 20.pixels()
        } childOf toasts effect ScissorEffect()
    val legacyHiders = AccordionComponent("Legacy Hiders")
        .constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = RelativeConstraint()
            height = RelativeConstraint(0.1f)
        } childOf container effect ScissorEffect()
    val repoHiders = AccordionComponent("Repo Hiders")
        .constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = RelativeConstraint()
            height = RelativeConstraint(0.1f)
        } childOf container effect ScissorEffect()
    val customHiders = AccordionComponent("Custom Hiders")
        .constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = RelativeConstraint()
            height = RelativeConstraint(0.1f)
        } childOf container effect ScissorEffect()
    val dummyBox = UIContainer()
        .constrain {
            x = CenterConstraint()
            y = SiblingConstraint()
            width = RelativeConstraint()
            height = 1.pixel()
        }

    init {
        // Force scroll bar update on accordion open/close
        container.children.filterIsInstance<UIContainer>().first().children.filterIsInstance<AccordionComponent>()
            .forEach {
                it.afterHeightChange {
                    container.addChild(dummyBox)
                    container.removeChild(dummyBox)
                }
            }
        // Scroll bar
        container.setVerticalScrollBarComponent(scrollbar, true)
        scrollbar.onMouseEnter {
            this.animate {
                setColorAnimation(
                    Animations.IN_OUT_QUAD,
                    0.25f,
                    Color(255, 255, 255, 120).toConstraint()
                )
            }
        }.onMouseLeave {
            this.animate {
                setColorAnimation(
                    Animations.IN_OUT_QUAD,
                    0.25f,
                    Color(255, 255, 255, 80).toConstraint()
                )
            }
        }

        // Toast Info
        UIContainer()
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = RelativeConstraint()
                height = 5.pixels()
            } childOf toastInfo
        UIText("A toast is a little pop up notification that appears in the top right of your screen.")
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
            } childOf toastInfo
        UIText("It appears like a default Minecraft notification. An example is shown below.")
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
            } childOf toastInfo
        UIContainer()
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = RelativeConstraint()
                height = 5.pixels()
            } childOf toastInfo
        val demoToast = UIImage.ofResource("/assets/skytils/gui/toast.png")
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = 160.pixels()
                height = 32.pixels()
            } childOf toastInfo
        UIImage.ofResource("/assets/skytils/toasts/revive.png")
            .constrain {
                x = 8.pixels()
                y = 8.pixels()
                width = 16.pixels()
                height = 16.pixels()
            } childOf demoToast
        UIText("§6§lPlayer")
            .constrain {
                x = 30.pixels()
                y = 7.pixels()
            } childOf demoToast
        UIText("§dHello World!")
            .constrain {
                x = 30.pixels()
                y = 18.pixels()
            } childOf demoToast
        UIContainer()
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = RelativeConstraint()
                height = 5.pixels()
            } childOf toastInfo

        // Show Toast Time
        val toastTimeContainer = UIContainer()
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = RelativeConstraint()
                height = ChildBasedSizeConstraint() + 10.pixels()
            } childOf toasts
        Skytils.config.getCategories()
            .flatMap { category ->
                category.items.filter { item ->
                    item is PropertyItem && item.data.attributesExt.name == "Toast Time"
                }
            }.first().toSettingsObject()!!
            .constrain {
                x = CenterConstraint()
                y = 5.pixels()
                width = RelativeConstraint(0.95f)
            } childOf toastTimeContainer

        // Show toast config options
        configHiders.filter { it.data.attributesExt.options.contains("Toasts") }
            .forEach { property ->
                val container = UIContainer()
                    .constrain {
                        x = CenterConstraint()
                        y = SiblingConstraint()
                        width = RelativeConstraint()
                        height = ChildBasedSizeConstraint() + 10.pixels()
                    } childOf toasts
                property.toSettingsObject()
                    .constrain {
                        x = CenterConstraint()
                        y = 5.pixels()
                        width = RelativeConstraint(0.95f)
                    } childOf container
            }

        // Add Compact Building Tools
        val compactBuildingToolsContainer = UIContainer()
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = RelativeConstraint()
                height = ChildBasedSizeConstraint() + 10.pixels()
            } childOf legacyHiders
        Skytils.config.getCategories()
            .flatMap { category ->
                category.items.filter { item ->
                    item is PropertyItem && item.data.attributesExt.name == "Compact Building Tools"
                }
            }.first().toSettingsObject()!!
            .constrain {
                x = CenterConstraint()
                y = 5.pixels()
                width = RelativeConstraint(0.95f)
            } childOf compactBuildingToolsContainer

        // Show legacy hiders
        configHiders.filter { !it.data.attributesExt.options.contains("Toasts") }
            .forEach { propertyItem ->
                val container = UIContainer()
                    .constrain {
                        x = CenterConstraint()
                        y = SiblingConstraint()
                        width = RelativeConstraint()
                        height = ChildBasedSizeConstraint() + 10.pixels()
                    } childOf legacyHiders
                propertyItem.toSettingsObject()
                    .constrain {
                        x = CenterConstraint()
                        y = 5.pixels()
                        width = RelativeConstraint(0.95f)
                    } childOf container
            }

        // Show Repo Hiders
        SpamHider.repoFilters.forEach { filter ->
            @Suppress("SENSELESS_COMPARISON")
            if (filter == null) return@forEach
            val container = UIContainer()
                .constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint()
                    width = RelativeConstraint()
                    height = ChildBasedSizeConstraint() + 10.pixels()
                } childOf repoHiders
            RepoFilterComponent(filter, DropDown(filter.state, listOf("Normal", "Hidden", "Separate Gui")))
                .constrain {
                    x = CenterConstraint()
                    y = 5.pixels()
                    width = RelativeConstraint(0.95f)
                } childOf container
        }

        // Custom Hider Info
        UIWrappedText(
            "In order to account for color codes in a message, you must use a Pilcrow character(¶)",
            centered = true
        )
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = RelativeConstraint(0.8f)
            } childOf customHiders
        val copy = SimpleButton("Click here to copy!")
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
            } childOf customHiders
        copy.onMouseClick {
            setClipboardString("¶")
            copy.text.setText("Copied!")
        }.onMouseLeave {
            copy.text.setText("Click here to copy!")
        }

        // Show Custom Hiders
        val customHiderContainer by UIContainer()
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = RelativeConstraint()
                height = ChildBasedSizeConstraint()
            } childOf customHiders
        SpamHider.filters.forEach { filter ->
            val container = UIContainer()
                .constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint()
                    width = RelativeConstraint()
                    height = ChildBasedSizeConstraint() + 10.pixels()
                } childOf customHiderContainer
            CustomFilterComponent(filter, DropDown(filter.state, listOf("Normal", "Hidden", "Separate Gui")))
                .constrain {
                    x = CenterConstraint()
                    y = 5.pixels()
                    width = RelativeConstraint(0.95f)
                } childOf container
        }
        val addCustomHider = SimpleButton("Add Custom Hider")
            .constrain {
                x = CenterConstraint()
                y = SiblingConstraint()
                width = RelativeConstraint(0.75f)
            } childOf customHiders

        addCustomHider.onMouseClick {
            val filter =
                SpamHider.Filter(
                    "Enter Unique Filter Name Here",
                    0,
                    true,
                    "Input Pattern Here".toRegex(),
                    SpamHider.FilterType.STARTSWITH,
                    true
                )
            SpamHider.filters.add(filter)
            val container = UIContainer()
                .constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint()
                    width = RelativeConstraint()
                    height = ChildBasedSizeConstraint() + 10.pixels()
                } childOf customHiderContainer
            CustomFilterComponent(filter, DropDown(filter.state, listOf("Normal", "Hidden", "Separate Gui")))
                .constrain {
                    x = CenterConstraint()
                    y = 5.pixels()
                    width = RelativeConstraint(0.95f)
                } childOf container
        }
    }

    override fun onScreenClose() {
        if (SpamHider.filters.mapTo(hashSetOf()) { it.name }.size != SpamHider.filters.size) {
            if (ModChecker.canShowNotifications) {
                EssentialAPI.getNotifications().push("Warning!", "Your spam hider filters must have unique names!\nThey will not save correctly upon exiting.")
            } else {
                UChat.chat("${Skytils.failPrefix} §cYour spam hider filters must have unique names!\nThey will not save correctly upon exiting.")
            }
        }
        PersistentSave.markDirty<SpamHider>()
        Skytils.config.markDirty()
    }
}