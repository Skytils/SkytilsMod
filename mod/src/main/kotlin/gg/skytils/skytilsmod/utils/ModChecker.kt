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

package gg.skytils.skytilsmod.utils

import gg.essential.api.EssentialAPI
import gg.essential.universal.UDesktop
import gg.skytils.skytilsmod.Skytils
import kotlinx.coroutines.launch
import net.minecraft.client.ClientBrandRetriever
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.*


object ModChecker {
    val isModded by lazy {
        //#if FORGE
        //$$ ClientBrandRetriever.getClientModName()?.startsWith("fml,forge") != true ||
        //$$        ClientBrandRetriever.getClientModName() != FMLCommonHandler.instance().modName ||
        //$$        Loader.isModLoaded("feather") ||
        //$$        Loader.isModLoaded("labymod") ||
        //$$        ForgeVersion.getStatus().ordinal > 3
        //#else
        false
        //#endif
    }

    val canShowNotifications by lazy {
        !EssentialAPI.getConfig().disableAllNotifications && !EssentialAPI.getOnboardingData().hasDeniedEssentialTOS()
    }

    fun checkModdedForge() {
        if (!isModded) return
        showMessage()
    }

    private fun showMessage() {
        Skytils.launch {
            runCatching {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            }
            val frame = JFrame()
            frame.isUndecorated = true
            frame.isAlwaysOnTop = true
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            val icon = runCatching {
                ImageIcon(
                    Toolkit.getDefaultToolkit().createImage(this::class.java.getResource("/assets/skytils/logo.png"))
                        .getScaledInstance(50, 50, Image.SCALE_DEFAULT)
                )
            }.getOrNull()
            val discordLink = createButton("Join the Discord") {
                UDesktop.browse(URI("https://discord.gg/skytils"))
            }

            val close = createButton("Close") {
                frame.isVisible = false
                frame.dispose()
            }

            val totalOptions = arrayOf(discordLink, close)
            JOptionPane.showOptionDialog(
                frame,
                """
                    #You're using a 'custom' version of Minecraft Forge! (${ClientBrandRetriever.getClientModName()})
                    #This is currently unsupported due to guaranteed incompatibilities.
                    #For any issues you encounter with Skytils, please contact
                    #your 'custom' mod loader's developer first for instructions.
                    #You will not receive support from Skytils staff for issues.
                    #Thanks! 💕
                    #
                    #Was I wrong? Report this at discord.gg/skytils.
                """.trimMargin("#"),
                "Skytils Message",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                icon,
                totalOptions,
                totalOptions[1]
            )
            frame.isVisible = false
            frame.dispose()
        }
    }
}

private fun createButton(text: String, onClick: JButton.() -> Unit): JButton {
    return JButton(text).apply {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onClick(this@apply)
            }
        })
    }
}