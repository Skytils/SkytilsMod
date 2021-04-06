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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
 * Taken from SkyblockAddons under MIT License
 * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
 * @author BiscuitDevelopment
 */
public class SkytilsInstallerFrame extends JFrame implements ActionListener, MouseListener {

    private static final Pattern IN_MODS_SUBFOLDER = Pattern.compile("1\\.8\\.9[/\\\\]?$");

    private JLabel logo = null;
    private JLabel versionInfo = null;
    private JLabel labelFolder = null;

    private JPanel panelCenter = null;
    private JPanel panelBottom = null;
    private JPanel totalContentPane = null;

    private JTextArea descriptionText = null;
    private JTextArea forgeDescriptionText = null;

    private JTextField textFieldFolderLocation = null;
    private JButton buttonChooseFolder = null;

    private JButton buttonInstall = null;
    private JButton buttonOpenFolder = null;
    private JButton buttonClose = null;

    private static final int TOTAL_HEIGHT = 435;
    private static final int TOTAL_WIDTH = 404;

    private int x = 0;
    private int y = 0;

    private int w = TOTAL_WIDTH;
    private int h;
    private int margin;

    public SkytilsInstallerFrame() {
        try {
            setName("SkytilsInstallerFrame");
            setTitle("Skytils Installer");
            setResizable(false);
            setSize(TOTAL_WIDTH, TOTAL_HEIGHT);
            setContentPane(getPanelContentPane());

            getButtonFolder().addActionListener(this);
            getButtonInstall().addActionListener(this);
            getButtonOpenFolder().addActionListener(this);
            getButtonClose().addActionListener(this);
            getForgeTextArea().addMouseListener(this);

            pack();
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            getFieldFolder().setText(getModsFolder().getPath());
            getButtonInstall().setEnabled(true);
            getButtonInstall().requestFocus();
        } catch (Exception ex) {
            showErrorPopup(ex);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SkytilsInstallerFrame frame = new SkytilsInstallerFrame();
            frame.centerFrame(frame);
            frame.show();

        } catch (Exception ex) {
            showErrorPopup(ex);
        }
    }

    private JPanel getPanelContentPane() {
        if (totalContentPane == null) {
            try {
                totalContentPane = new JPanel();
                totalContentPane.setName("PanelContentPane");
                totalContentPane.setLayout(new BorderLayout(5, 5));
                totalContentPane.setPreferredSize(new Dimension(TOTAL_WIDTH, TOTAL_HEIGHT));
                totalContentPane.add(getPanelCenter(), "Center");
                totalContentPane.add(getPanelBottom(), "South");
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return totalContentPane;
    }

    private JPanel getPanelCenter() {
        if (panelCenter == null) {
            try {
                (panelCenter = new JPanel()).setName("PanelCenter");
                panelCenter.setLayout(null);
                panelCenter.add(getPictureLabel(), getPictureLabel().getName());
                panelCenter.add(getVersionInfo(), getVersionInfo().getName());
                panelCenter.add(getTextArea(), getTextArea().getName());
                panelCenter.add(getForgeTextArea(), getForgeTextArea().getName());
                panelCenter.add(getLabelFolder(), getLabelFolder().getName());
                panelCenter.add(getFieldFolder(), getFieldFolder().getName());
                panelCenter.add(getButtonFolder(), getButtonFolder().getName());
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return panelCenter;
    }

    private JLabel getPictureLabel() {
        if (logo == null) {
            try {
                h = w/2;
                margin = 5;

                BufferedImage myPicture = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader()
                        .getResourceAsStream("assets/skytils/logo.png"), "Logo not found."));
                Image scaled = myPicture.getScaledInstance(w/2-margin*2, h-margin, Image.SCALE_SMOOTH);
                logo = new JLabel(new ImageIcon(scaled));
                logo.setName("Logo");
                logo.setBounds(x+margin, y+margin, w-margin*2, h-margin);
                logo.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
                logo.setHorizontalAlignment(SwingConstants.CENTER);
                logo.setPreferredSize(new Dimension(w, h));

                y += h;
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return logo;
    }

    private JLabel getVersionInfo() {
        if (versionInfo == null) {
            try {
                h = 25;

                versionInfo = new JLabel();
                versionInfo.setName("LabelMcVersion");
                versionInfo.setBounds(x, y, w, h);
                versionInfo.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
                versionInfo.setHorizontalAlignment(SwingConstants.CENTER);
                versionInfo.setPreferredSize(new Dimension(w, h));
                versionInfo.setText("v"+getVersionFromMcmodInfo()+" by the Skytils Team - for Minecraft 1.8.9");

                y += h;
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return versionInfo;
    }

    private JTextArea getTextArea() {
        if (descriptionText == null) {
            try {
                h = 60;
                margin = 10;

                descriptionText = new JTextArea();
                descriptionText.setName("TextArea");
                setTextAreaProperties(descriptionText);
                descriptionText.setText("This installer will copy Skytils into your forge mods folder for you, and replace any old versions that already exist. " +
                        "Close this if you prefer to do this yourself!");
                descriptionText.setWrapStyleWord(true);

                y += h;
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return descriptionText;
    }

    private void setTextAreaProperties(JTextArea textArea) {
        textArea.setBounds(x+margin, y+margin, w-margin*2, h-margin);
        textArea.setEditable(false);
        textArea.setHighlighter(null);
        textArea.setEnabled(true);
        textArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setPreferredSize(new Dimension(w-margin*2, h-margin));
    }

    private JTextArea getForgeTextArea() {
        if (forgeDescriptionText == null) {
            try {
                h = 55;
                margin = 10;

                forgeDescriptionText = new JTextArea();
                forgeDescriptionText.setName("TextAreaForge");
                setTextAreaProperties(forgeDescriptionText);
                forgeDescriptionText.setText("However, you still need to install Forge client in order to be able to run this mod. Click here to visit the download page for Forge 1.8.9!");
                forgeDescriptionText.setForeground(Color.BLUE.darker());
                forgeDescriptionText.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                forgeDescriptionText.setWrapStyleWord(true);

                y += h;
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return forgeDescriptionText;
    }

    private JLabel getLabelFolder() {
        if (labelFolder == null) {
            h = 16;
            w = 65;

            x += 10; // Padding

            try {
                labelFolder = new JLabel();
                labelFolder.setName("LabelFolder");
                labelFolder.setBounds(x, y+2, w, h);
                labelFolder.setPreferredSize(new Dimension(w, h));
                labelFolder.setText("Mods Folder");
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }

            x += w;
        }
        return labelFolder;
    }

    private JTextField getFieldFolder() {
        if (textFieldFolderLocation == null) {
            h = 20;
            w = 287;

            try {
                textFieldFolderLocation = new JTextField();
                textFieldFolderLocation.setName("FieldFolder");
                textFieldFolderLocation.setBounds(x, y, w, h);
                textFieldFolderLocation.setEditable(false);
                textFieldFolderLocation.setPreferredSize(new Dimension(w, h));
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }

            x += w;
        }
        return textFieldFolderLocation;
    }

    private JButton getButtonFolder() {
        if (buttonChooseFolder == null) {
            h = 20;
            w = 25;

            x += 10; // Padding

            try {
                BufferedImage myPicture = ImageIO.read(Objects.requireNonNull(getClass().getClassLoader()
                        .getResourceAsStream("assets/skytils/gui/folder.png"), "Folder icon not found."));
                Image scaled = myPicture.getScaledInstance(w-8, h-6, Image.SCALE_SMOOTH);
                buttonChooseFolder = new JButton(new ImageIcon(scaled));
                buttonChooseFolder.setName("ButtonFolder");
                buttonChooseFolder.setBounds(x, y, w, h);
                buttonChooseFolder.setPreferredSize(new Dimension(w, h));
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return buttonChooseFolder;
    }

    private JPanel getPanelBottom() {
        if (panelBottom == null) {
            try {
                panelBottom = new JPanel();
                panelBottom.setName("PanelBottom");
                panelBottom.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
                panelBottom.setPreferredSize(new Dimension(390, 55));
                panelBottom.add(getButtonInstall(), getButtonInstall().getName());
                panelBottom.add(getButtonOpenFolder(), getButtonOpenFolder().getName());
                panelBottom.add(getButtonClose(), getButtonClose().getName());
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return panelBottom;
    }

    private JButton getButtonInstall() {
        if (buttonInstall == null) {
            w = 100;
            h = 26;

            try {
                buttonInstall = new JButton();
                buttonInstall.setName("ButtonInstall");
                buttonInstall.setPreferredSize(new Dimension(w, h));
                buttonInstall.setText("Install");
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return buttonInstall;
    }

    private JButton getButtonOpenFolder() {
        if (buttonOpenFolder == null) {
            w = 130;
            h = 26;

            try {
                buttonOpenFolder = new JButton();
                buttonOpenFolder.setName("ButtonOpenFolder");
                buttonOpenFolder.setPreferredSize(new Dimension(w, h));
                buttonOpenFolder.setText("Open Mods Folder");
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return buttonOpenFolder;
    }

    private JButton getButtonClose() {
        if (buttonClose == null) {
            w = 100;
            h = 26;

            try {
                (buttonClose = new JButton()).setName("ButtonClose");
                buttonClose.setPreferredSize(new Dimension(w, h));
                buttonClose.setText("Cancel");
            } catch (Throwable ivjExc) {
                showErrorPopup(ivjExc);
            }
        }
        return buttonClose;
    }

    public void onFolderSelect() {
        File currentDirectory = new File(getFieldFolder().getText());

        JFileChooser jFileChooser = new JFileChooser(currentDirectory);
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setAcceptAllFileFilterUsed(false);
        if (jFileChooser.showOpenDialog(this) == 0) {
            File newDirectory = jFileChooser.getSelectedFile();
            getFieldFolder().setText(newDirectory.getPath());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == getButtonClose()) {
            dispose();
            System.exit(0);
        }
        if (e.getSource() == getButtonFolder()) {
            onFolderSelect();
        }
        if (e.getSource() == getButtonInstall()) {
            onInstall();
        }
        if (e.getSource() == getButtonOpenFolder()) {
            onOpenFolder();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == getForgeTextArea()) {
            try {
                Desktop.getDesktop().browse(new URI("http://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.8.9.html"));
            } catch (IOException | URISyntaxException ex) {
                showErrorPopup(ex);
            }
        }
    }

    public void onInstall() {
        try {
            File modsFolder = new File(getFieldFolder().getText());
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
            boolean inSubFolder = false;
            if (IN_MODS_SUBFOLDER.matcher(modsFolder.getPath()).find()) {
                inSubFolder = true;
            }

            File newFile = new File(modsFolder, "Skytils-"+getVersionFromMcmodInfo()+".jar");
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

    public OperatingSystem getOperatingSystem() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.US);
        if (osName.contains("win")) {
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

    public enum OperatingSystem {
        LINUX,
        SOLARIS,
        WINDOWS,
        MACOS,
        UNKNOWN
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

    private File getThisFile() {
        try {
            return new File(SkytilsInstallerFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            showErrorPopup(ex);
        }
        return null;
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}
}