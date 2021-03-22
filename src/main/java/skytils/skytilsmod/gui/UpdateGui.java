package skytils.skytilsmod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.UpdateChecker;
import skytils.skytilsmod.utils.MathUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class UpdateGui extends GuiScreen {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final String[] DOTS = { ".", "..", "...", "...", "..." };
    private static final int DOT_TIME = 200;  // ms between "." -> ".." -> "..."

    private boolean failed = false;
    private boolean complete = false;
    private GuiButton backButton;
    private float progress = 0f;

    public UpdateGui() {
        doUpdate(true);
    }

    @Override
    public void initGui() {
        this.buttonList.add(backButton = new GuiButton(0, this.width / 2 - 100, this.height / 4 + 132, 200, 20, ""));
        updateText();
    }

    private void doUpdate(boolean restartNow) {
        try {
            File directory = new File(Skytils.modDir, "updates");
            String url = UpdateChecker.getUpdateDownloadURL();
            String jarName = UpdateChecker.getJarNameFromUrl(url);

            new Thread(() -> {
                downloadUpdate(url, directory, jarName);
                if (!failed) {
                    UpdateChecker.scheduleCopyUpdateAtShutdown(jarName);
                    if (restartNow) {
                        mc.shutdown();
                    }
                    complete = true;
                    updateText();
                }
            }, "Skytils-update-downloader-thread").start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateText() {
        backButton.displayString = (failed || complete) ? "Back" : "Cancel";
    }

    private void downloadUpdate(String url, File directory, String jarName) {
        try {
            HttpURLConnection st = (HttpURLConnection) new URL(url).openConnection();
            st.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            st.connect();

            if (st.getResponseCode() != HttpURLConnection.HTTP_OK) {
                failed = true;
                updateText();
                System.out.println(url + " returned status code " + st.getResponseCode());
                return;
            }

            if (!directory.exists() && !directory.mkdirs()) {
                failed = true;
                updateText();
                System.out.println("Couldn't create update file directory");
                return;
            }

            String[] urlParts = url.split("/");

            float fileLength = st.getContentLength();

            File fileSaved = new File(directory, URLDecoder.decode(urlParts[urlParts.length - 1], "UTF-8"));

            InputStream fis = st.getInputStream();
            OutputStream fos = new FileOutputStream(fileSaved);

            byte[] data = new byte[1024];
            long total = 0;
            int count;

            while ((count = fis.read(data)) != -1) {
                if (mc.currentScreen != UpdateGui.this) {
                    // Cancelled
                    fos.close();
                    fis.close();
                    failed = true;
                    return;
                }

                total += count;
                progress = total / fileLength;
                fos.write(data, 0, count);
            }

            fos.flush();
            fos.close();
            fis.close();

            if (mc.currentScreen != UpdateGui.this) {
                failed = true;
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            failed = true;
            updateText();
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        if (failed) {
            drawCenteredString(mc.fontRendererObj, EnumChatFormatting.RED + "Update download failed", this.width/2, this.height/2, 0xFFFFFFFF);
        } else if (complete) {
            drawCenteredString(mc.fontRendererObj, EnumChatFormatting.GREEN + "Update download complete", this.width/2, this.height/2, 0xFFFFFF);
        } else {
            int left = Math.max(this.width/2 - 100, 10);
            int right = Math.min(this.width/2 + 100, this.width - 10);
            int top = this.height/2 - 2 - MathUtil.ceil(mc.fontRendererObj.FONT_HEIGHT / 2f);
            int bottom = this.height/2 + 2 + MathUtil.floor(mc.fontRendererObj.FONT_HEIGHT / 2f);
            drawRect(left - 1, top - 1, right + 1, bottom + 1, 0xFFC0C0C0);
            int progressPoint = MathUtil.clamp(MathUtil.floor(progress * (right - left) + left), left, right);
            drawRect(left, top, progressPoint, bottom, 0xFFCB3D35);
            drawRect(progressPoint, top, right, bottom, 0xFFFFFFFF);

            String label = String.format("%d%%", MathUtil.clamp(MathUtil.floor(progress * 100), 0, 100));
            mc.fontRendererObj.drawString(label, (this.width - mc.fontRendererObj.getStringWidth(label))/2, top + 3, 0xFF000000);
            int x = (this.width - mc.fontRendererObj.getStringWidth(String.format("Downloading %s", DOTS[DOTS.length - 1]))) / 2;
            String title = String.format("Downloading %s", DOTS[((int) (System.currentTimeMillis() % (DOT_TIME * DOTS.length))) / DOT_TIME]);
            drawString(mc.fontRendererObj, title, x, top - mc.fontRendererObj.FONT_HEIGHT - 2, 0xFFFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
