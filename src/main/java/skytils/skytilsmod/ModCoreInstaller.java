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

package skytils.skytilsmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
    Created by Sk1er for use in all mods. Install under exact package name each time.
 */
public class ModCoreInstaller {


    private static final String VERSION_URL = "https://api.sk1er.club/modcore_versions";
    private static final String className = "club.sk1er.mods.core.ModCore";
    private static boolean errored = false;
    private static String error;
    private static File dataDir = null;
    private static boolean isRunningModCore = false;

    public static boolean isIsRunningModCore() {
        return isRunningModCore;
    }

    private static boolean isInitalized() {
        try {
            LinkedHashSet<String> objects = new LinkedHashSet<>();
            objects.add(className);
            Launch.classLoader.clearNegativeEntries(objects);
            Field invalidClasses = LaunchClassLoader.class.getDeclaredField("invalidClasses");
            invalidClasses.setAccessible(true);
            Object obj = invalidClasses.get(ModCoreInstaller.class.getClassLoader());
            ((Set<String>) obj).remove(className);
            return Class.forName("club.sk1er.mods.core.ModCore") != null;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
            ignored.printStackTrace();
        }
        return false;
    }

    public static boolean isErrored() {
        return errored;
    }

    public static String getError() {
        return error;
    }

    private static void bail(String error) {
        errored = true;
        ModCoreInstaller.error = error;
    }

    private static JsonHolder readFile(File in) {
        try {
            return new JsonHolder(FileUtils.readFileToString(in));
        } catch (IOException ignored) {

        }
        return new JsonHolder();
    }

    public static void initializeModCore(File gameDir) {
        if (!isIsRunningModCore()) {
            return;
        }
        try {
            Class<?> modCore = Class.forName(className);
            Method instanceMethod = modCore.getMethod("getInstance");
            Method initialize = modCore.getMethod("initialize", File.class);
            Object modCoreObject = instanceMethod.invoke(null);
            initialize.invoke(modCoreObject, gameDir);
            System.out.println("Loaded ModCore Successfully");
            return;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        System.out.println("Did NOT ModCore Successfully");
    }

    public static int initialize(File gameDir, String minecraftVersion) {
        if (isInitalized()) return -1;
        dataDir = new File(gameDir, "modcore");
        if (!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                bail("Unable to create necessary files");
                return 1;
            }
        }
        JsonHolder jsonHolder = fetchJSON(VERSION_URL);
        String latestRemote = jsonHolder.optString(minecraftVersion);
        boolean failed = jsonHolder.getKeys().size() == 0 || (jsonHolder.has("success") && !jsonHolder.optBoolean("success"));

        File metadataFile = new File(dataDir, "metadata.json");
        JsonHolder localMetadata = readFile(metadataFile);
        if (failed) latestRemote = localMetadata.optString(minecraftVersion);
        File modcoreFile = new File(dataDir, "Sk1er Modcore-" + latestRemote + " (" + minecraftVersion + ").jar");

        if (!modcoreFile.exists() || !localMetadata.optString(minecraftVersion).equalsIgnoreCase(latestRemote) && !failed) {
            //File does not exist, or is out of date, download it
            File old = new File(dataDir, "Sk1er Modcore-" + localMetadata.optString(minecraftVersion) + " (" + minecraftVersion + ").jar");
            if (old.exists()) old.delete();

            if (!download("https://static.sk1er.club/repo/mods/modcore/" + latestRemote + "/" + minecraftVersion + "/ModCore-" + latestRemote + " (" + minecraftVersion + ").jar", latestRemote, modcoreFile, minecraftVersion, localMetadata)) {
                bail("Unable to download");
                return 2;
            }

        }

        addToClasspath(modcoreFile);

        if (!isInitalized()) {
            bail("Something went wrong and it did not add the jar to the class path. Local file exists? " + modcoreFile.exists());
            return 3;
        }
        isRunningModCore = true;
        return 0;
    }


    public static void addToClasspath(File file) {
        try {
            URL url = file.toURI().toURL();

            ClassLoader classLoader = ModCoreInstaller.class.getClassLoader();
            Method method = classLoader.getClass().getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(classLoader, url);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    private static boolean download(String url, String version, File file, String mcver, JsonHolder versionData) {
        url = url.replace(" ", "%20");
        System.out.println("Downloading ModCore " + " version " + version + " from: " + url);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("ModCore Initializer");
        JProgressBar bar = new JProgressBar();
        JLabel label = new JLabel("Downloading ModCore " + version, SwingConstants.CENTER);
        label.setSize(600, 120);
        frame.getContentPane().add(label);
        frame.getContentPane().add(bar);
        GroupLayout layout = new GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(label, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                                .addComponent(bar, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(label, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bar, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        frame.setResizable(false);
        bar.setBorderPainted(true);
        bar.setMinimum(0);
        bar.setStringPainted(true);
        Font font = bar.getFont();
        bar.setFont(new Font(font.getName(), font.getStyle(), font.getSize() * 2));
        label.setFont(new Font(font.getName(), font.getStyle(), font.getSize() * 2));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        HttpURLConnection connection = null;
        InputStream is = null;
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(true);
            connection.addRequestProperty("User-Agent", "Mozilla/4.76 (Sk1er Modcore Initializer)");
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setDoOutput(true);
            is = connection.getInputStream();
            int contentLength = connection.getContentLength();
            byte[] buffer = new byte[1024];
            System.out.println("MAX: " + contentLength);
            bar.setMaximum(contentLength);
            int read;
            bar.setValue(0);
            while ((read = is.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
                bar.setValue(bar.getValue() + 1024);
            }
            FileUtils.write(new File(dataDir, "metadata.json"), versionData.put(mcver, version).toString());
        } catch (Exception e) {
            e.printStackTrace();
            frame.dispose();
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }

                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                System.out.println("Failed cleaning up ModCoreInstaller#download");
                e.printStackTrace();
            }
        }

        frame.dispose();
        return true;
    }

    public static JsonHolder fetchJSON(String url) {
        return new JsonHolder(fetchString(url));
    }

    public static String fetchString(String url) {
        url = url.replace(" ", "%20");
        System.out.println("Fetching " + url);

        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(true);
            connection.addRequestProperty("User-Agent", "Mozilla/4.76 (Sk1er ModCore)");
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setDoOutput(true);
            is = connection.getInputStream();
            return IOUtils.toString(is, Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.disconnect();
                }

                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                System.out.println("Failed cleaning up ModCoreInstaller#fetchString");
                e.printStackTrace();
            }
        }

        return "Failed to fetch";
    }


    //Added because we need to use before ModCore is loaded
    static class JsonHolder {
        private JsonObject object;

        public JsonHolder(JsonObject object) {
            this.object = object;
        }

        public JsonHolder(String raw) {
            if (raw == null)
                object = new JsonObject();
            else
                try {
                    this.object = new JsonParser().parse(raw).getAsJsonObject();
                } catch (Exception e) {
                    this.object = new JsonObject();
                    e.printStackTrace();
                }
        }

        public JsonHolder() {
            this(new JsonObject());
        }

        @Override
        public String toString() {
            if (object != null)
                return object.toString();
            return "{}";
        }

        public JsonHolder put(String key, boolean value) {
            object.addProperty(key, value);
            return this;
        }

        public void mergeNotOverride(JsonHolder merge) {
            merge(merge, false);
        }

        public void mergeOverride(JsonHolder merge) {
            merge(merge, true);
        }

        public void merge(JsonHolder merge, boolean override) {
            JsonObject object = merge.getObject();
            for (String s : merge.getKeys()) {
                if (override || !this.has(s))
                    put(s, object.get(s));
            }
        }

        private void put(String s, JsonElement element) {
            this.object.add(s, element);
        }

        public JsonHolder put(String key, String value) {
            object.addProperty(key, value);
            return this;
        }

        public JsonHolder put(String key, int value) {
            object.addProperty(key, value);
            return this;
        }

        public JsonHolder put(String key, double value) {
            object.addProperty(key, value);
            return this;
        }

        public JsonHolder put(String key, long value) {
            object.addProperty(key, value);
            return this;
        }

        private JsonHolder defaultOptJSONObject(String key, JsonObject fallBack) {
            try {
                return new JsonHolder(object.get(key).getAsJsonObject());
            } catch (Exception e) {
                return new JsonHolder(fallBack);
            }
        }

        public JsonArray defaultOptJSONArray(String key, JsonArray fallback) {
            try {
                return object.get(key).getAsJsonArray();
            } catch (Exception e) {
                return fallback;
            }
        }

        public JsonArray optJSONArray(String key) {
            return defaultOptJSONArray(key, new JsonArray());
        }


        public boolean has(String key) {
            return object.has(key);
        }

        public long optLong(String key, long fallback) {
            try {
                return object.get(key).getAsLong();
            } catch (Exception e) {
                return fallback;
            }
        }

        public long optLong(String key) {
            return optLong(key, 0);
        }

        public boolean optBoolean(String key, boolean fallback) {
            try {
                return object.get(key).getAsBoolean();
            } catch (Exception e) {
                return fallback;
            }
        }

        public boolean optBoolean(String key) {
            return optBoolean(key, false);
        }

        public JsonObject optActualJSONObject(String key) {
            try {
                return object.get(key).getAsJsonObject();
            } catch (Exception e) {
                return new JsonObject();
            }
        }

        public JsonHolder optJSONObject(String key) {
            return defaultOptJSONObject(key, new JsonObject());
        }


        public int optInt(String key, int fallBack) {
            try {
                return object.get(key).getAsInt();
            } catch (Exception e) {
                return fallBack;
            }
        }

        public int optInt(String key) {
            return optInt(key, 0);
        }


        public String defaultOptString(String key, String fallBack) {
            try {
                return object.get(key).getAsString();
            } catch (Exception e) {
                return fallBack;
            }
        }

        public String optString(String key) {
            return defaultOptString(key, "");
        }


        public double optDouble(String key, double fallBack) {
            try {
                return object.get(key).getAsDouble();
            } catch (Exception e) {
                return fallBack;
            }
        }

        public List<String> getKeys() {
            List<String> tmp = new ArrayList<>();
            for (Map.Entry<String, JsonElement> e : object.entrySet()) {
                tmp.add(e.getKey());
            }
            return tmp;
        }

        public double optDouble(String key) {
            return optDouble(key, 0.0);
        }


        public JsonObject getObject() {
            return object;
        }

        public boolean isNull(String key) {
            return object.has(key) && object.get(key).isJsonNull();
        }

        public JsonHolder put(String values, JsonHolder values1) {
            return put(values, values1.getObject());
        }

        public JsonHolder put(String values, JsonObject object) {
            this.object.add(values, object);
            return this;
        }

        public void put(String blacklisted, JsonArray jsonElements) {
            this.object.add(blacklisted, jsonElements);
        }

        public void remove(String header) {
            object.remove(header);
        }
    }


}