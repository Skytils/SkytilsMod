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

package gg.skytils.skytilsmod.gui

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.IO
import gg.skytils.skytilsmod.gui.components.SimpleButton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.awt.Color
import java.util.jar.JarFile

class LegalGui : WindowScreen(ElementaVersion.V5, newGuiScale = 2), ReopenableGUI {
    companion object {
        val licenses: MutableMap<String, String> = sortedMapOf()

        private suspend fun loadLicenses() = coroutineScope {
            if (licenses.isNotEmpty()) return@coroutineScope
            val file = Skytils.jarFile ?: return@coroutineScope

            if (file.isDirectory) {
                file.resolve("assets/skytils/legal").walkTopDown().forEach {
                    if (it.isFile) {
                        async {
                            licenses[it.nameWithoutExtension] = it.readText()
                        }.start()
                    }
                }
            } else {
                JarFile(file).use { jar ->
                    jar.entries().iterator().forEach { entry ->
                        if (entry.name.startsWith("assets/skytils/legal/") && !entry.isDirectory) {
                            async {
                                licenses[entry.name.substringAfterLast("/").substringBeforeLast(".")] = jar.getInputStream(entry).use { it.bufferedReader().readText() }
                            }.start()
                        }
                    }
                }
            }
        }
    }

    init {
        UIText("Licenses").childOf(window).constrain {
            x = CenterConstraint()
            y = 5.percent()
            textScale = 2.pixels
        }

        val buttonScrollComponent = ScrollComponent(
            innerPadding = 4f,
        ).childOf(window).constrain {
            x = 5.percent()
            y = 15.percent()
            width = 20.percent()
            height = 70.percent() + 2.pixels()
        }

        val licenseTextScroll = ScrollComponent(
            innerPadding = 4f,
        ).childOf(window).constrain {
            x = 30.percent()
            y = 15.percent()
            width = 50.percent()
            height = 70.percent() + 2.pixels()
        }

        val block = UIBlock(Color.gray.constraint).childOf(licenseTextScroll).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent
            height = 100.percent
        }

        val text = UIWrappedText().childOf(block).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 97.5.percent
            height = 97.5.percent
        }

        IO.launch {
            loadLicenses()
        }.invokeOnCompletion {
            licenses.forEach { (name, license) ->
                SimpleButton(name).childOf(buttonScrollComponent).constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint(5f)
                    width = 90.percent()
                    height = 20.pixels()
                }.onMouseClick {
                    text.setText(license)
                }

                if (name == "skytils") {
                    text.setText(license)
                }
            }
        }
    }
}