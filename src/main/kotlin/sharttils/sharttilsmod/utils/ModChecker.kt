/*
 * Sharttils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Sharttils
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

package sharttils.sharttilsmod.utils

import net.minecraft.client.ClientBrandRetriever
import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.Loader
import sharttils.sharttilsmod.Sharttils
import sharttils.sharttilsmod.tweaker.createButton
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
                ForgeVersion.getStatus().ordinal > 3 ||
                (!Sharttils.deobfEnvironment && Loader.instance().activeModList.filter { it.modId == "Forge" || it.modId == "FML" }
                    .any { it.signingCertificate == null })
    }

    fun checkModdedForge() {
        if (!isModded) return
        showMessage()
    }

    private fun showMessage() {
        Sharttils.threadPool.submit {
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
                    Toolkit.getDefaultToolkit().createImage(this::class.java.getResource("/assets/sharttils/logo.png"))
                        .getScaledInstance(50, 50, Image.SCALE_DEFAULT)
                )
            }.getOrNull()
            val discordLink = createButton("Join the Discord") {
                Desktop.getDesktop().browse(URI("https://discord.gg/sharttils"))
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
                    #For any issues you encounter with Sharttils, please contact
                    #your 'custom' mod loader's developer first for instructions.
                    #You will not receive support from Sharttils staff for issues.
                    #Thanks! 💕
                    #
                    #Was I wrong? Report this at discord.gg/sharttils.
                """.trimMargin("#"),
                "Sharttils Message",
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