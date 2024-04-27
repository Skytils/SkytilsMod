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

import swing.RoundedRectanglePanel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;


/**
 * Modified from SkyblockAddons under MIT License
 * <a href="https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE">...</a>
 *
 * @author BiscuitDevelopment
 */
public class SkytilsInstallerFrame extends JFrame implements ActionListener, MouseListener {

    private static final Pattern IN_MODS_SUBFOLDER = Pattern.compile("1\\.8\\.9[/\\\\]?$");

    private static final int w = 550;
    private static final int h = 500;

    private DescriptionPanel descriptionPanel;
    private FolderPanel chooser;
    private FooterPanel footer;

    public SkytilsInstallerFrame() {
        try {
            // Frame Code
            setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("assets/skytils/logo.png")));
            setName("SkytilsInstallerFrame");
            setTitle("Skytils Installer");
            setUndecorated(true);
            setResizable(false);
            setSize(w, h);
            setShape(new RoundRectangle2D.Double(0, 0, w, h, 16, 16));

            JPanel testPanel = new JPanel();
            testPanel.setBackground(new Color(0x4166f5));

            // Logo
            LogoPanel logo = new LogoPanel(w * 3 / 7, w * 3 / 7, 16, 16, new Color(0x8193ff));
            testPanel.add(logo, BorderLayout.PAGE_START);

            // Description
            descriptionPanel = new DescriptionPanel(w * 4 / 5, h / 4, 16, 16, new Color(0x8193ff));
            descriptionPanel.forge.addMouseListener(this);
            testPanel.add(descriptionPanel, BorderLayout.CENTER);

            // Chooser
            chooser = new FolderPanel(w * 4 / 5, h / 7, 16, 16, new Color(0x8193ff));
            chooser.folderButton.addActionListener(this);
            testPanel.add(chooser, BorderLayout.EAST);

            // Footer
            footer = new FooterPanel(w / 2 + (getOperatingSystem() == OperatingSystem.LINUX ? 60 : 0), h / 11, 16, 16, new Color(0x8193ff));
            footer.openFolder.addActionListener(this);
            footer.install.addActionListener(this);
            footer.install.setEnabled(true);
            footer.install.requestFocus();
            footer.close.addActionListener(this);
            testPanel.add(footer, BorderLayout.PAGE_END);

            setContentPane(testPanel);
        } catch (Exception ex) {
            showErrorPopup(ex);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SkytilsInstallerFrame frame = new SkytilsInstallerFrame();
            frame.centerFrame(frame);
            frame.setVisible(true);
        } catch (Exception ex) {
            showErrorPopup(ex);
        }
    }

    public static OperatingSystem getOperatingSystem() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.US);
        // macos can sometimes be reported as "darwin" which will be interpreted as windows
        // windows: https://github.com/openjdk/jdk/blob/6d5699617ff0985104a8bb5f2c9eb8887cb0961e/src/java.base/windows/native/libjava/java_props_md.c#L418-L446
        // else: https://en.wikipedia.org/wiki/Uname#Examples
        // MacOS is always "Mac OS X"
        // see: https://github.com/openjdk/jdk/blob/master/src/java.base/macosx/native/libjava/java_props_macosx.c#L235
        if (osName.startsWith("win")) {
            return OperatingSystem.WINDOWS;

        } else if (osName.contains("mac")) {
            return OperatingSystem.MACOS;

        } else if (osName.contains("solaris") || osName.contains("sunos")) {

            return OperatingSystem.SOLARIS;
        } else if (osName.contains("linux") || osName.contains("unix")) {

            return OperatingSystem.LINUX;
        }
        return OperatingSystem.UNKNOWN;
    }

    private static String getStacktraceText(Throwable ex) {
        StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().replace("\t", "  ");
    }

    private static void showErrorPopup(Throwable ex) {
        ex.printStackTrace();

        JTextArea textArea = new JTextArea(getStacktraceText(ex));
        textArea.setEditable(false);
        Font currentFont = textArea.getFont();
        Font newFont = new Font(Font.MONOSPACED, currentFont.getStyle(), currentFont.getSize());
        textArea.setFont(newFont);

        JScrollPane errorScrollPane = new JScrollPane(textArea);
        errorScrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(null, errorScrollPane, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static File getThisFile() {
        try {
            return new File(SkytilsInstallerFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            showErrorPopup(ex);
        }
        return null;
    }

    public void onFolderSelect() {
        File currentDirectory = new File(chooser.folderLocation.getText());

        JFileChooser jFileChooser = new JFileChooser(currentDirectory);
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setAcceptAllFileFilterUsed(false);
        if (jFileChooser.showOpenDialog(this) == 0) {
            File newDirectory = jFileChooser.getSelectedFile();
            chooser.folderLocation.setText(newDirectory.getPath());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("an action happened");
        if (e.getSource() == footer.close) {
            dispose();
            System.exit(0);
        }
        if (e.getSource() == chooser.folderButton) {
            System.out.println("selecting!");
            onFolderSelect();
        }
        if (e.getSource() == footer.install) {
            onInstall();
        }
        if (e.getSource() == footer.openFolder) {
            onOpenFolder();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        String forgeUrl = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.8.9.html";
        if (e.getSource() == descriptionPanel.forge) {
            try {
                Desktop.getDesktop().browse(new URI(forgeUrl));
            } catch (IOException | URISyntaxException ex) {
                showErrorPopup(ex);
            } catch (/* Throws on linux */UnsupportedOperationException ex) {
                ex.printStackTrace();
                JTextArea textArea = new JTextArea("Failed to open url open it manually instead\n" + forgeUrl);
                textArea.setEditable(false);
                Font currentFont = textArea.getFont();
                Font newFont = new Font(Font.MONOSPACED, currentFont.getStyle(), currentFont.getSize());
                textArea.setFont(newFont);
                JScrollPane errorScrollPane = new JScrollPane(textArea);
                errorScrollPane.setPreferredSize(new Dimension(600, 400));
                JOptionPane.showMessageDialog(null, errorScrollPane, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void onInstall() {
        try {
            File modsFolder = new File(chooser.folderLocation.getText());
            if (!modsFolder.exists()) {
                showErrorMessage("Folder not found: " + modsFolder.getPath());
                return;
            }
            if (!modsFolder.isDirectory()) {
                showErrorMessage("Not a folder: " + modsFolder.getPath());
                return;
            }
            tryInstall(modsFolder);
        } catch (Exception e) {
            showErrorPopup(e);
        }
    }

    private void tryInstall(File modsFolder) {
        File thisFile = getThisFile();

        if (thisFile != null) {
            boolean inSubFolder = IN_MODS_SUBFOLDER.matcher(modsFolder.getPath()).find();

            File newFile = new File(modsFolder, "Skytils-" + getVersionFromMcmodInfo() + ".jar");
            if (thisFile.equals(newFile)) {
                showErrorMessage("You are opening this file from where the file should be installed... there's nothing to be done!");
                return;
            }

            boolean deletingFailure = false;
            if (modsFolder.isDirectory()) { // Delete in this current folder.
                boolean failed = findSkytilsAndDelete(modsFolder.listFiles());
                if (failed) deletingFailure = true;
            }
            if (inSubFolder) { // We are in the 1.8.9 folder, delete in the parent folder as well.
                if (modsFolder.getParentFile().isDirectory()) {
                    boolean failed = findSkytilsAndDelete(modsFolder.getParentFile().listFiles());
                    if (failed) deletingFailure = true;
                }
            } else { // We are in the main mods folder, but the 1.8.9 subfolder exists... delete in there too.
                File subFolder = new File(modsFolder, "1.8.9");
                if (subFolder.exists() && subFolder.isDirectory()) {
                    boolean failed = findSkytilsAndDelete(subFolder.listFiles());
                    if (failed) deletingFailure = true;
                }
            }

            if (deletingFailure) return;

            if (thisFile.isDirectory()) {
                showErrorMessage("This file is a directory... Are we in a development environment?");
                return;
            }

            try {
                Files.copy(thisFile.toPath(), newFile.toPath());
            } catch (Exception ex) {
                showErrorPopup(ex);
                return;
            }

            showMessage("Skytils has been successfully installed into your mods folder.");
            dispose();
            System.exit(0);
        }
    }

    private boolean findSkytilsAndDelete(File[] files) {
        if (files == null) return false;

        for (File file : files) {
            if (!file.isDirectory() && file.getPath().endsWith(".jar")) {
                try {
                    JarFile jarFile = new JarFile(file);
                    ZipEntry mcModInfo = jarFile.getEntry("mcmod.info");
                    if (mcModInfo != null) {
                        InputStream inputStream = jarFile.getInputStream(mcModInfo);
                        String modID = getModIDFromInputStream(inputStream);
                        if (modID.equals("skytils")) {
                            jarFile.close();
                            try {
                                boolean deleted = file.delete();
                                if (!deleted) {
                                    throw new Exception();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                showErrorMessage("Was not able to delete the other Skytils files found in your mods folder!" + System.lineSeparator() +
                                        "Please make sure that your minecraft is currently closed and try again, or feel" + System.lineSeparator() +
                                        "free to open your mods folder and delete those files manually.");
                                return true;
                            }
                            continue;
                        }
                    }
                    jarFile.close();
                } catch (Exception ex) {
                    // Just don't check the file I guess, move on to the next...
                }
            }
        }
        return false;
    }

    public void onOpenFolder() {
        try {
            Desktop.getDesktop().open(getModsFolder());
        } catch (Exception e) {
            showErrorPopup(e);
        }
    }

    public File getModsFolder() {
        String userHome = System.getProperty("user.home", ".");

        File modsFolder = getFile(userHome, "minecraft/mods/1.8.9");
        if (!modsFolder.exists()) {
            modsFolder = getFile(userHome, "minecraft/mods");
        }

        if (!modsFolder.exists() && !modsFolder.mkdirs()) {
            throw new RuntimeException("The working directory could not be created: " + modsFolder);
        }
        return modsFolder;
    }

    public File getFile(String userHome, String minecraftPath) {
        File workingDirectory;
        switch (getOperatingSystem()) {
            case LINUX:
            case SOLARIS: {
                workingDirectory = new File(userHome, '.' + minecraftPath + '/');
                break;
            }
            case WINDOWS: {
                String applicationData = System.getenv("APPDATA");
                if (applicationData != null) {
                    workingDirectory = new File(applicationData, "." + minecraftPath + '/');
                    break;
                }
                workingDirectory = new File(userHome, '.' + minecraftPath + '/');
                break;
            }
            case MACOS: {
                workingDirectory = new File(userHome, "Library/Application Support/" + minecraftPath);
                break;
            }
            default: {
                workingDirectory = new File(userHome, minecraftPath + '/');
                break;
            }
        }
        return workingDirectory;
    }

    public void centerFrame(JFrame frame) {
        Rectangle rectangle = frame.getBounds();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(0, 0, screenSize.width, screenSize.height);

        int newX = screenRectangle.x + (screenRectangle.width - rectangle.width) / 2;
        int newY = screenRectangle.y + (screenRectangle.height - rectangle.height) / 2;

        if (newX < 0) newX = 0;
        if (newY < 0) newY = 0;

        frame.setBounds(newX, newY, rectangle.width, rectangle.height);
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Skytils", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Skytils - Error", JOptionPane.ERROR_MESSAGE);
    }

    private String getVersionFromMcmodInfo() {
        String version = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().
                    getClassLoader().getResourceAsStream("mcmod.info"), "mcmod.info not found.")));
            while ((version = bufferedReader.readLine()) != null) {
                if (version.contains("\"version\": \"")) {
                    version = version.split(Pattern.quote("\"version\": \""))[1];
                    version = version.substring(0, version.length() - 2);
                    break;
                }
            }
        } catch (Exception ex) {
            // It's okay, I guess just don't use the version lol.
        }
        return version;
    }

    private String getModIDFromInputStream(InputStream inputStream) {
        String version = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((version = bufferedReader.readLine()) != null) {
                if (version.contains("\"modid\": \"")) {
                    version = version.split(Pattern.quote("\"modid\": \""))[1];
                    version = version.substring(0, version.length() - 2);
                    break;
                }
            }
        } catch (Exception ex) {
            // RIP, couldn't find the modid...
        }
        return version;
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public enum OperatingSystem {
        LINUX,
        SOLARIS,
        WINDOWS,
        MACOS,
        UNKNOWN
    }

    private class LogoPanel extends RoundedRectanglePanel {

        public LogoPanel(int width, int height, int radiusW, int radiusH, Color color) throws IOException {
            super(radiusW, radiusH);
            int img = (int) (height * 0.8f);
            BufferedImage myPicture = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader()
                    .getResourceAsStream("assets/skytils/logo.png"), "Logo not found."));
            Image scaled = myPicture.getScaledInstance(img, img, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(scaled));
            logo.setName("SkytilsLogo");
            logo.setBounds(0, 0, img, img);
            logo.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
            logo.setHorizontalAlignment(SwingConstants.CENTER);
            logo.setPreferredSize(new Dimension(img, img));

            JLabel version = new JLabel("Skytils v" + getVersionFromMcmodInfo() + " for MC 1.8.9");
            version.setName("Mod Version");
            version.setFont(new Font(Font.DIALOG, Font.BOLD, 16));
            version.setHorizontalAlignment(SwingConstants.CENTER);

            this.add(logo);
            this.add(version);
            this.setBackground(color);
            this.setForeground(color);
            this.setPreferredSize(new Dimension(width, height));
        }
    }

    private static class DescriptionPanel extends RoundedRectanglePanel {
        public final JTextArea forge;

        public DescriptionPanel(int width, int height, int radiusW, int radiusH, Color color) {
            super(radiusW, radiusH);

            JTextArea desc = new JTextArea();
            desc.setName("DescriptionText");
            desc.setText("This installer will copy Skytils into your forge mods folder for you, and replace any old versions that already exist. " +
                    "Close this if you prefer to do this yourself!");
            desc.setEditable(false);
            desc.setHighlighter(null);
            desc.setEnabled(true);
            desc.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            desc.setOpaque(false);
            desc.setBounds(radiusW, radiusH, width - radiusW * 2, height - radiusH * 2);

            // Forge Link
            forge = new JTextArea();
            forge.setName("Forge Text");
            forge.setText("However, you still need to install Forge client in order to be able to run this mod. Click here to visit the download page for Forge 1.8.9!");
            forge.setForeground(new Color(0x550ef2));
            forge.setEditable(false);
            forge.setHighlighter(null);
            forge.setEnabled(true);
            forge.setFont(new Font(Font.DIALOG, Font.PLAIN, 13));
            forge.setLineWrap(true);
            forge.setWrapStyleWord(true);
            forge.setOpaque(false);
            forge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            forge.setBounds(radiusW, height - radiusH, width - radiusW * 2, height - radiusH * 2);

            this.add(desc, BorderLayout.PAGE_START);
            this.add(forge, BorderLayout.PAGE_END);
            this.setBackground(color);
            this.setForeground(color);
            this.setPreferredSize(new Dimension(width, height));
        }
    }

    private class FolderPanel extends RoundedRectanglePanel {
        public final JButton folderButton;
        public final JTextField folderLocation;

        public FolderPanel(int width, int height, int radiusW, int radiusH, Color color) throws IOException {
            super(radiusW, radiusH);

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);

            JLabel folderLabel = new JLabel();
            folderLabel.setName("Folder Label");
            folderLabel.setText("Mods Folder");
            folderLabel.setFont(new Font(Font.DIALOG, Font.BOLD, 12));

            this.add(folderLabel);

            folderLocation = new JTextField();
            folderLocation.setName("Folder Field");
            folderLocation.setText(getModsFolder().getAbsolutePath());
            folderLocation.setEditable(false);
            folderLocation.setPreferredSize(new Dimension(width * 2 / 3, 20));

            this.add(folderLocation);

            BufferedImage myPicture = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader()
                    .getResourceAsStream("assets/skytils/gui/folder.png"), "Folder icon not found."));
            Image scaled = myPicture.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            folderButton = new JButton(new ImageIcon(scaled));
            folderButton.setName("ButtonFolder");

            this.add(folderButton);

            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(
                    layout.createSequentialGroup()
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(folderLabel)
                                            .addComponent(folderLocation)
                            )
                            .addComponent(folderButton)
            );
            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addComponent(folderLabel)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                    .addComponent(folderLocation)
                                    .addComponent(folderButton)
                            )
            );

            this.setBackground(color);
            this.setForeground(color);
            this.setPreferredSize(new Dimension(width, height));
        }
    }

    private static class FooterPanel extends RoundedRectanglePanel {

        public final JButton install;
        public final JButton openFolder;
        public final JButton close;

        public FooterPanel(int width, int height, int radiusW, int radiusH, Color color) {
            super(radiusW, radiusH);

            GroupLayout layout = new GroupLayout(this);
            this.setLayout(layout);

            install = new JButton();
            install.setName("Install Button");
            install.setText("Install");

            this.add(install);

            openFolder = new JButton();
            openFolder.setName("Open Folder Button");
            openFolder.setText("Open Mods Folder");

            this.add(openFolder);

            close = new JButton();
            close.setName("Close Button");
            close.setText("Cancel");

            this.add(close);

            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            layout.setHorizontalGroup(
                    layout.createSequentialGroup()
                            .addComponent(install)
                            .addComponent(openFolder)
                            .addComponent(close)
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                            .addComponent(install)
                            .addComponent(openFolder)
                            .addComponent(close)
            );

            this.setBackground(color);
            this.setForeground(color);
            this.setPreferredSize(new Dimension(width, height));
        }
    }
}
