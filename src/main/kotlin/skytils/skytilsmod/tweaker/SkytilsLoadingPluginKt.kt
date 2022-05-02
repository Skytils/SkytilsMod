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

package skytils.skytilsmod.tweaker

import SkytilsInstallerFrame
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import skytils.skytilsmod.Skytils.Companion.client
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.startsWithAny
import java.awt.Desktop
import java.awt.Image
import java.awt.Toolkit
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.net.URI
import java.net.URL
import javax.swing.*

/**
 * Fake loading plugin, called from the java loading plugin
 * @see skytils.skytilsmod.tweaker.SkytilsLoadingPlugin
 */
class SkytilsLoadingPluginKt : IFMLLoadingPlugin {

    init {
        if (System.getProperty("skytils.skipStartChecks") == null) {
            // Must use reflection otherwise the "constant" value will be inlined by compiler
            val mixinVersion = runCatching {
                MixinBootstrap::class.java.getDeclaredField("VERSION").also { it.isAccessible = true }
                    .get(null) as String
            }.getOrDefault("unknown")
            if (!mixinVersion.startsWithAny("0.8")) {
                try {
                    Class.forName("com.mumfrey.liteloader.launch.LiteLoaderTweaker")
                    showMessage(SkytilsLoadingPlugin.liteloaderUserMessage)
                    SkytilsLoadingPlugin.exit()
                } catch (ignored: ClassNotFoundException) {
                    showMessage(
                        SkytilsLoadingPlugin.badMixinVersionMessage + "<br>The culprit seems to be " + File(
                            MixinEnvironment::class.java.protectionDomain.codeSource.location.toURI()
                        ).name + "</p></html>"
                    )
                    SkytilsLoadingPlugin.exit()
                }
            }

            val forgeVersion = ForgeVersion.getBuildVersion()
            // Asbyth's forge fork uses version 0
            if (!(forgeVersion >= 2318 || forgeVersion == 0)) {
                val forgeUrl =
                    "https://maven.minecraftforge.net/net/minecraftforge/forge/1.8.9-11.15.1.2318-1.8.9/forge-1.8.9-11.15.1.2318-1.8.9-installer.jar"
                val forgeUri =
                    URL("https://maven.minecraftforge.net/net/minecraftforge/forge/1.8.9-11.15.1.2318-1.8.9/forge-1.8.9-11.15.1.2318-1.8.9-installer.jar").toURI()
                val forgeButton = createButton("Get Forge") {
                    if (SkytilsInstallerFrame.getOperatingSystem() == SkytilsInstallerFrame.OperatingSystem.WINDOWS) {
                        runCatching {
                            val tempDir = System.getenv("TEMP")
                            val forgeFile = File(tempDir, "forge-1.8.9-11.15.1.2318-1.8.9-installer.jar")

                            val runtime = Utils.getJavaRuntime()

                            runBlocking {
                                val res = client.get(forgeUrl)
                                if (res.status == HttpStatusCode.OK) {
                                    res.bodyAsChannel().copyAndClose(forgeFile.writeChannel())
                                    Runtime.getRuntime()
                                        .exec("\"$runtime\" -jar \"${forgeFile.canonicalPath}\"")
                                    exit()
                                } else {
                                    Desktop.getDesktop().browse(forgeUri)
                                }
                            }
                        }.onFailure {
                            it.printStackTrace()
                            Desktop.getDesktop().browse(forgeUri)
                        }
                    } else Desktop.getDesktop().browse(forgeUri)
                }
                showMessage(
                    """
                #Skytils has detected that you are using an old version of Forge (build ${forgeVersion}).
                #In order to resolve this issue and launch the game,
                #please install Minecraft Forge build 2318 for Minecraft 1.8.9.
                #If you have already done this and are still getting this error,
                #ask for support in the Discord.
                """.trimMargin("#"),
                    forgeButton
                )
            }
            if (this::class.java.classLoader.getResource("patcher.mixins.json") == null && Package.getPackages()
                    .any { it.name.startsWith("club.sk1er.patcher") }
            ) {
                val sk1erClubButton = createButton("Go to Sk1er.Club") {
                    Desktop.getDesktop().browse(URL("https://sk1er.club/mods/patcher").toURI())
                }
                showMessage(
                    """
                #Skytils has detected that you are using an old version of Patcher.
                #You must update Patcher in order for your game to launch.
                #You can do so at https://sk1er.club/mods/patcher
                #If you have already done this and are still getting this error,
                #ask for support in the Discord.
                """.trimMargin("#"), sk1erClubButton
                )
                SkytilsLoadingPlugin.exit()
            }
        }
    }

    override fun getASMTransformerClass(): Array<String> {
        return arrayOf("skytils.skytilsmod.asm.SkytilsTransformer")
    }

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun injectData(data: MutableMap<String, Any>?) {
        Launch.classLoader.addTransformerExclusion("skytils.skytilsmod.utils.ModChecker")
    }

    override fun getAccessTransformerClass(): String? {
        return null
    }

    private fun showMessage(errorMessage: String, vararg options: Any) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // This makes the JOptionPane show on taskbar and stay on top
        val frame = JFrame()
        frame.isUndecorated = true
        frame.isAlwaysOnTop = true
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
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
            exit()
        }
        val totalOptions = arrayOf(discordLink, close, *options)
        JOptionPane.showOptionDialog(
            frame,
            errorMessage,
            "Skytils Error",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.ERROR_MESSAGE,
            icon,
            totalOptions,
            totalOptions[0]
        )
        exit()
    }

    private fun exit() {
        SkytilsLoadingPlugin.exit()
    }
}

fun createButton(text: String, onClick: JButton.() -> Unit): JButton {
    return JButton(text).apply {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                onClick(this@apply)
            }
        })
    }
}