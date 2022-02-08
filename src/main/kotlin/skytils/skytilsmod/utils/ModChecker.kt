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

package skytils.skytilsmod.utils

import net.minecraft.client.ClientBrandRetriever
import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Loader
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.tweaker.SkytilsLoadingPlugin
import skytils.skytilsmod.tweaker.createButton
import java.awt.Desktop
import java.awt.Image
import java.awt.Toolkit
import java.net.URI
import javax.swing.*


object ModChecker {
    val isModded by lazy {
        ClientBrandRetriever.getClientModName()?.startsWith("fml,forge") != true ||
                ClientBrandRetriever.getClientModName() != FMLCommonHandler.instance().modName ||
                Loader.isModLoaded("feather") ||
                ForgeVersion.getStatus().ordinal > 3
    }

    fun checkModdedForge() {
        if (!isModded) return
        showMessage()
    }

    private fun showMessage() {
        Skytils.threadPool.submit {
            runCatching {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
            }
            // This makes the JOptionPane show on taskbar and stay on top
            val frame = JFrame()
            frame.isUndecorated = true
            frame.isAlwaysOnTop = true
            frame.setLocationRelativeTo(null)
            frame.isVisible = true
            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            var icon: Icon? = null
            try {
                val url = SkytilsLoadingPlugin::class.java.getResource("/assets/skytils/sychicpet.gif")
                if (url != null) {
                    icon = ImageIcon(
                        Toolkit.getDefaultToolkit().createImage(url).getScaledInstance(50, 50, Image.SCALE_DEFAULT)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val discordLink = createButton("Join the Discord") {
                Desktop.getDesktop().browse(URI("https://discord.gg/skytils"))
            }
            val close = createButton("Close") {
                val w = SwingUtilities.getWindowAncestor(this)
                JOptionPane.getRootFrame().dispose()
                if (w != null) {
                    w.isVisible = false
                    w.dispose()
                } else {
                    frame.isVisible = false
                    frame.dispose()
                }
            }
            val totalOptions = arrayOf(discordLink, close)
            JOptionPane.showOptionDialog(
                frame,
                """
                    #You're using a modded version of Minecraft Forge! (${ClientBrandRetriever.getClientModName()})
                    #This is currently unsupported due to possible incompatibilities.
                    #For any issues you encounter with Skytils, please contact
                    #your mod loader's developer first for instructions.
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
        }
    }
}