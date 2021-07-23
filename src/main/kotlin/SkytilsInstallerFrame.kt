/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
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

import javax.imageio.ImageIO
import javax.swing.*
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.io.*
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Files
import java.util.Objects
import java.util.jar.JarFile
import java.util.regex.Pattern
import kotlin.system.exitProcess


/**
 * Taken from SkyblockAddons under MIT License
 * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
 * @author BiscuitDevelopment
 */
class SkytilsInstallerFrame : JFrame(), ActionListener, MouseListener {

    private var logo: JLabel? = null
    private var versionInfo: JLabel? = null
    private var labelFolder: JLabel? = null

    private var panelCenter: JPanel? = null
    private var panelBottom: JPanel? = null
    private var totalContentPane: JPanel? = null

    private var descriptionText: JTextArea? = null
    private var forgeDescriptionText: JTextArea? = null

    private var textFieldFolderLocation: JTextField? = null
    private var buttonChooseFolder: JButton? = null

    private var buttonInstall: JButton? = null
    private var buttonOpenFolder: JButton? = null
    private var buttonClose: JButton? = null

    private var frameX: Int = 0
    private var frameY: Int = 0

    private var w = TOTAL_WIDTH
    private var h: Int = 0
    private var margin: Int = 0

    init {
        try {
            name = "SkytilsInstallerFrame"
            title = "Skytils Installer"
            isResizable = false
            setSize(TOTAL_WIDTH, TOTAL_HEIGHT)
            contentPane = getPanelContentPane()

            getButtonFolder().addActionListener(this)
            getButtonInstall().addActionListener(this)
            getButtonOpenFolder().addActionListener(this)
            getButtonClose().addActionListener(this)
            getForgeTextArea().addMouseListener(this)

            pack()
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

            getFieldFolder().text = getModsFolder().path
            getButtonInstall().isEnabled = true
            getButtonInstall().requestFocus()
        } catch (ex: Exception) {
            showErrorPopup(ex)
        }
    }

    private fun getPanelContentPane(): JPanel {
        if (totalContentPane == null) {
            try {
                totalContentPane = JPanel()
                totalContentPane!!.name = "PanelContentPane"
                totalContentPane!!.layout = BorderLayout(5, 5)
                totalContentPane!!.preferredSize = Dimension(TOTAL_WIDTH, TOTAL_HEIGHT)
                totalContentPane!!.add(getPanelCenter(), "Center")
                totalContentPane!!.add(getPanelBottom(), "South")
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return totalContentPane!!
    }

    private fun getPanelCenter(): JPanel {
        if (panelCenter == null) {
            try {
                panelCenter = JPanel()
                panelCenter!!.name = "PanelCenter"
                panelCenter!!.layout = null
                panelCenter!!.add(getPictureLabel(), getPictureLabel().name)
                panelCenter!!.add(getVersionInfo(), getVersionInfo().name)
                panelCenter!!.add(getTextArea(), getTextArea().name)
                panelCenter!!.add(getForgeTextArea(), getForgeTextArea().name)
                panelCenter!!.add(getLabelFolder(), getLabelFolder().name)
                panelCenter!!.add(getFieldFolder(), getFieldFolder().name)
                panelCenter!!.add(getButtonFolder(), getButtonFolder().name)
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return panelCenter!!
    }

    private fun getPictureLabel(): JLabel {
        if (logo == null) {
            try {
                h = w / 2
                margin = 5

                val myPicture = ImageIO.read(
                    Objects.requireNonNull(
                        javaClass.classLoader
                            .getResourceAsStream("assets/skytils/logo.png"), "Logo not found."
                    )
                )
                val scaled = myPicture.getScaledInstance(w / 2 - margin * 2, h - margin, Image.SCALE_SMOOTH)
                logo = JLabel(ImageIcon(scaled))
                logo!!.name = "Logo"
                logo!!.setBounds(frameX + margin, frameY + margin, w - margin * 2, h - margin)
                logo!!.font = Font(Font.DIALOG, Font.BOLD, 18)
                logo!!.horizontalAlignment = SwingConstants.CENTER
                logo!!.preferredSize = Dimension(w, h)

                frameY += h
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return logo!!
    }

    private fun getVersionInfo(): JLabel {
        if (versionInfo == null) {
            try {
                h = 25

                versionInfo = JLabel()
                versionInfo!!.name = "LabelMcVersion"
                versionInfo!!.setBounds(frameX, frameY, w, h)
                versionInfo!!.font = Font(Font.DIALOG, Font.BOLD, 14)
                versionInfo!!.horizontalAlignment = SwingConstants.CENTER
                versionInfo!!.preferredSize = Dimension(w, h)
                versionInfo!!.text = "v" + getVersionFromMcmodInfo() + " by the Skytils Team - for Minecraft 1.8.9"

                frameY += h
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return versionInfo!!
    }

    private fun getTextArea(): JTextArea {
        if (descriptionText == null) {
            try {
                h = 60
                margin = 10

                descriptionText = JTextArea()
                descriptionText!!.name = "TextArea"
                setTextAreaProperties(descriptionText!!)
                descriptionText!!.text =
                    "This installer will copy Skytils into your forge mods folder for you, and replace any old versions that already exist. " +
                            "Close this if you prefer to do this yourself!"
                descriptionText!!.wrapStyleWord = true

                frameY += h
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return descriptionText!!
    }

    private fun setTextAreaProperties(textArea: JTextArea) {
        textArea.setBounds(frameX + margin, frameY + margin, w - margin * 2, h - margin)
        textArea.isEditable = false
        textArea.highlighter = null
        textArea.isEnabled = true
        textArea.font = Font(Font.DIALOG, Font.PLAIN, 12)
        textArea.lineWrap = true
        textArea.isOpaque = false
        textArea.preferredSize = Dimension(w - margin * 2, h - margin)
    }

    private fun getForgeTextArea(): JTextArea {
        if (forgeDescriptionText == null) {
            try {
                h = 55
                margin = 10

                forgeDescriptionText = JTextArea()
                forgeDescriptionText!!.name = "TextAreaForge"
                setTextAreaProperties(forgeDescriptionText!!)
                forgeDescriptionText!!.text =
                    "However, you still need to install Forge client in order to be able to run this mod. Click here to visit the download page for Forge 1.8.9!"
                forgeDescriptionText!!.foreground = Color.BLUE.darker()
                forgeDescriptionText!!.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                forgeDescriptionText!!.wrapStyleWord = true

                frameY += h
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return forgeDescriptionText!!
    }

    private fun getLabelFolder(): JLabel {
        if (labelFolder == null) {
            h = 16
            w = 65

            frameX += 10 // Padding

            try {
                labelFolder = JLabel()
                labelFolder!!.name = "LabelFolder"
                labelFolder!!.setBounds(frameX, frameY + 2, w, h)
                labelFolder!!.preferredSize = Dimension(w, h)
                labelFolder!!.text = "Mods Folder"
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }

            frameX += w
        }
        return labelFolder!!
    }

    private fun getFieldFolder(): JTextField {
        if (textFieldFolderLocation == null) {
            h = 20
            w = 287

            try {
                textFieldFolderLocation = JTextField()
                textFieldFolderLocation!!.name = "FieldFolder"
                textFieldFolderLocation!!.setBounds(frameX, frameY, w, h)
                textFieldFolderLocation!!.isEditable = false
                textFieldFolderLocation!!.preferredSize = Dimension(w, h)
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }

            frameX += w
        }
        return textFieldFolderLocation!!
    }

    private fun getButtonFolder(): JButton {
        if (buttonChooseFolder == null) {
            h = 20
            w = 25

            frameX += 10 // Padding

            try {
                val myPicture = ImageIO.read(
                    Objects.requireNonNull(
                        javaClass.classLoader
                            .getResourceAsStream("assets/skytils/gui/folder.png"), "Folder icon not found."
                    )
                )
                val scaled = myPicture.getScaledInstance(w - 8, h - 6, Image.SCALE_SMOOTH)
                buttonChooseFolder = JButton(ImageIcon(scaled))
                buttonChooseFolder!!.name = "ButtonFolder"
                buttonChooseFolder!!.setBounds(frameX, frameY, w, h)
                buttonChooseFolder!!.preferredSize = Dimension(w, h)
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return buttonChooseFolder!!
    }

    private fun getPanelBottom(): JPanel {
        if (panelBottom == null) {
            try {
                panelBottom = JPanel()
                panelBottom!!.name = "PanelBottom"
                panelBottom!!.layout = FlowLayout(FlowLayout.CENTER, 15, 10)
                panelBottom!!.preferredSize = Dimension(390, 55)
                panelBottom!!.add(getButtonInstall(), getButtonInstall().name)
                panelBottom!!.add(getButtonOpenFolder(), getButtonOpenFolder().name)
                panelBottom!!.add(getButtonClose(), getButtonClose().name)
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return panelBottom!!
    }

    private fun getButtonInstall(): JButton {
        if (buttonInstall == null) {
            w = 100
            h = 26

            try {
                buttonInstall = JButton()
                buttonInstall!!.name = "ButtonInstall"
                buttonInstall!!.preferredSize = Dimension(w, h)
                buttonInstall!!.text = "Install"
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return buttonInstall!!
    }

    private fun getButtonOpenFolder(): JButton {
        if (buttonOpenFolder == null) {
            w = 130
            h = 26

            try {
                buttonOpenFolder = JButton()
                buttonOpenFolder!!.name = "ButtonOpenFolder"
                buttonOpenFolder!!.preferredSize = Dimension(w, h)
                buttonOpenFolder!!.text = "Open Mods Folder"
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return buttonOpenFolder!!
    }

    private fun getButtonClose(): JButton {
        if (buttonClose == null) {
            w = 100
            h = 26

            try {
                buttonClose = JButton()
                buttonClose!!.name = "ButtonClose"
                buttonClose!!.preferredSize = Dimension(w, h)
                buttonClose!!.text = "Cancel"
            } catch (ivjExc: Throwable) {
                showErrorPopup(ivjExc)
            }
        }
        return buttonClose!!
    }

    private fun onFolderSelect() {
        val currentDirectory = File(getFieldFolder().text)

        val jFileChooser = JFileChooser(currentDirectory)
        jFileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        jFileChooser.isAcceptAllFileFilterUsed = false
        if (jFileChooser.showOpenDialog(this) == 0) {
            val newDirectory = jFileChooser.selectedFile
            getFieldFolder().text = newDirectory.path
        }
    }

    override fun actionPerformed(e: ActionEvent) {
        if (e.source == getButtonClose()) {
            dispose()
            exitProcess(0)
        }
        if (e.source == getButtonFolder()) {
            onFolderSelect()
        }
        if (e.source == getButtonInstall()) {
            onInstall()
        }
        if (e.source == getButtonOpenFolder()) {
            onOpenFolder()
        }
    }

    override fun mouseClicked(e: MouseEvent) {
        if (e.source == getForgeTextArea()) {
            try {
                Desktop.getDesktop()
                    .browse(URI("https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html"))
            } catch (ex: Exception) {
                showErrorPopup(ex)
            }
        }
    }

    private fun onInstall() {
        try {
            val modsFolder = File(getFieldFolder().text)
            if (!modsFolder.exists()) {
                showErrorMessage("Folder not found: ${modsFolder.path}")
                return
            }
            if (!modsFolder.isDirectory) {
                showErrorMessage("Not a folder: ${modsFolder.path}")
                return
            }
            tryInstall(modsFolder)
        } catch (e: Exception) {
            showErrorPopup(e)
        }
    }

    private fun tryInstall(modsFolder: File) {
        val thisFile = getThisFile()

        if (thisFile != null) {
            val inSubFolder = IN_MODS_SUBFOLDER.matcher(modsFolder.path).find()

            val newFile = File(modsFolder, "Skytils-${getVersionFromMcmodInfo()}.jar")
            if (thisFile == newFile) {
                showErrorMessage("You are opening this file from where it should be installed... there's nothing to be done!")
                return
            }

            var deletingFailure = false
            if (modsFolder.isDirectory) { // Delete in this current folder.
                val failed = findSkytilsAndDelete(modsFolder.listFiles())
                if (failed) deletingFailure = true
            }
            if (inSubFolder) { // We are in the 1.8.9 folder, delete in the parent folder as well.
                if (modsFolder.parentFile.isDirectory) {
                    val failed = findSkytilsAndDelete(modsFolder.parentFile.listFiles())
                    if (failed) deletingFailure = true
                }
            } else { // We are in the main mods folder, but the 1.8.9 subfolder exists... delete in there too.
                val subFolder = File(modsFolder, "1.8.9")
                if (subFolder.exists() && subFolder.isDirectory) {
                    val failed = findSkytilsAndDelete(subFolder.listFiles())
                    if (failed) deletingFailure = true
                }
            }

            if (deletingFailure) return

            if (thisFile.isDirectory) {
                showErrorMessage("This file is a directory... are we in a development environment?")
                return
            }

            try {
                Files.copy(thisFile.toPath(), newFile.toPath())
            } catch (ex: Exception) {
                showErrorPopup(ex)
                return
            }

            showMessage("Skytils has been successfully installed to your mods folder.")
            dispose()
            exitProcess(0)
        }
    }

    private fun findSkytilsAndDelete(files: Array<File>?): Boolean {
        if (files == null) return false
        for (file in files) {
            if (!file.isDirectory && file.extension == "jar") {
                try {
                    val jarFile = JarFile(file)
                    val mcModInfo = jarFile.getEntry("mcmod.info")
                    if (mcModInfo != null) {
                        val inputStream = jarFile.getInputStream(mcModInfo)
                        val modID = getModIDFromInputStream(inputStream)
                        if (modID == "skytils") {
                            jarFile.close()
                            try {
                                val deleted = file.delete()
                                if (!deleted) {
                                    throw Exception()
                                }
                            } catch (ex: Exception) {
                                ex.printStackTrace()
                                showErrorMessage(
                                    "Was not able to delete the other Skytils files found in your mods folder!" + System.lineSeparator() +
                                            "Please make sure that your minecraft is currently closed and try again, or feel" + System.lineSeparator() +
                                            "free to open your mods folder and delete those files manually."
                                )
                                return true
                            }
                            continue
                        }
                    }
                    jarFile.close()
                } catch (ex: Exception) {
                    // Just don't check the file I guess, move on to the next...
                }
            }
        }
        return false
    }

    private fun onOpenFolder() {
        try {
            Desktop.getDesktop().open(getModsFolder())
        } catch (e: Exception) {
            showErrorPopup(e)
        }
    }

    private fun getModsFolder(): File {
        val userHome = System.getProperty("user.home", ".")

        var modsFolder = getFile(userHome, "minecraft/mods/1.8.9")
        if (!modsFolder.exists()) {
            modsFolder = getFile(userHome, "minecraft/mods")
        }

        if (!modsFolder.exists() && !modsFolder.mkdirs()) {
            throw RuntimeException("The working directory could not be created: $modsFolder")
        }
        return modsFolder
    }

    private fun getFile(userHome: String, minecraftPath: String): File {
        return when (getOperatingSystem()) {
            OperatingSystem.LINUX, OperatingSystem.SOLARIS -> File(userHome, ".$minecraftPath/")
            OperatingSystem.WINDOWS -> {
                val applicationData = System.getenv("APPDATA")
                if (applicationData != null) {
                    File(applicationData, ".$minecraftPath/")
                } else File(userHome, ".$minecraftPath/")
            }
            OperatingSystem.MACOS -> File(userHome, "Library/Application Support/$minecraftPath")
            else -> File(userHome, "$minecraftPath/")
        }
    }

    fun centerFrame(frame: JFrame) {
        val rectangle = frame.bounds
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val screenRectangle = Rectangle(0, 0, screenSize.width, screenSize.height)

        val newX = (screenRectangle.x + (screenRectangle.width - rectangle.width) / 2).coerceAtLeast(0)
        val newY = (screenRectangle.y + (screenRectangle.height - rectangle.height) / 2).coerceAtLeast(0)

        frame.setBounds(newX, newY, rectangle.width, rectangle.height)
    }

    private fun showMessage(message: String) {
        JOptionPane.showMessageDialog(null, message, "Skytils", JOptionPane.INFORMATION_MESSAGE)
    }

    private fun showErrorMessage(message: String) {
        JOptionPane.showMessageDialog(null, message, "Skytils - Error", JOptionPane.ERROR_MESSAGE)
    }

    private fun getVersionFromMcmodInfo(): String {
        var version = ""
        try {
            val bufferedReader = InputStreamReader(
                Objects.requireNonNull(
                    javaClass.classLoader.getResourceAsStream("mcmod.info"), "mcmod.info not found."
                )
            ).buffered()
            while ((bufferedReader.readLine().also { version = it }) != null) {
                if (version.contains("\"version\": \"")) {
                    version = version.split("\"version\": \"")[1]
                    version = version.substring(0, version.length - 2)
                    break
                }
            }
        } catch (ex: Exception) {
            // It's okay, I guess just don't use the version lol.
        }
        return version
    }

    private fun getModIDFromInputStream(inputStream: InputStream): String {
        var version = ""
        try {
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            while ((bufferedReader.readLine().also { version = it }) != null) {
                if (version.contains("\"modid\": \"")) {
                    version = version.split("\"modid\": \"")[1]
                    version = version.substring(0, version.length - 2)
                    break
                }
            }
        } catch (ex: Exception) {
            // RIP, couldn't find the modid...
        }
        return version
    }

    private fun getThisFile(): File? {
        try {
            return File(SkytilsInstallerFrame::class.java.protectionDomain.codeSource.location.toURI())
        } catch (ex: URISyntaxException) {
            showErrorPopup(ex)
        }
        return null
    }

    override fun mousePressed(e: MouseEvent) {
    }

    override fun mouseReleased(e: MouseEvent) {
    }

    override fun mouseEntered(e: MouseEvent) {
    }

    override fun mouseExited(e: MouseEvent) {
    }

    companion object {
        private val IN_MODS_SUBFOLDER = Pattern.compile("1\\.8\\.9[/\\\\]?$")
        private const val TOTAL_HEIGHT = 435
        private const val TOTAL_WIDTH = 404

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                val frame = SkytilsInstallerFrame()
                frame.centerFrame(frame)
                frame.isVisible = true

            } catch (ex: Exception) {
                showErrorPopup(ex)
            }
        }

        @JvmStatic
        fun getOperatingSystem(): OperatingSystem {
            val osName = System.getProperty("os.name").lowercase()
            if (osName.contains("win")) {
                return OperatingSystem.WINDOWS

            } else if (osName.contains("mac")) {
                return OperatingSystem.MACOS

            } else if (osName.contains("solaris") || osName.contains("sunos")) {

                return OperatingSystem.SOLARIS
            } else if (osName.contains("linux") || osName.contains("unix")) {

                return OperatingSystem.LINUX
            }
            return OperatingSystem.UNKNOWN
        }

        private fun getStacktraceText(ex: Throwable): String {
            val stringWriter = StringWriter()
            ex.printStackTrace(PrintWriter(stringWriter))
            return stringWriter.toString().replace("\t", "  ")
        }

        private fun showErrorPopup(ex: Throwable) {
            ex.printStackTrace()

            val textArea = JTextArea(getStacktraceText(ex))
            textArea.isEditable = false
            val currentFont = textArea.font
            val newFont = Font(Font.MONOSPACED, currentFont.style, currentFont.size)
            textArea.font = newFont

            val errorScrollPane = JScrollPane(textArea)
            errorScrollPane.preferredSize = Dimension(600, 400)
            JOptionPane.showMessageDialog(null, errorScrollPane, "Error", JOptionPane.ERROR_MESSAGE)
        }
    }

    enum class OperatingSystem {
        LINUX,
        SOLARIS,
        WINDOWS,
        MACOS,
        UNKNOWN
    }
}