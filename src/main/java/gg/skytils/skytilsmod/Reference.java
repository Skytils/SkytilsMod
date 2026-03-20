package gg.skytils.skytilsmod;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

public class Reference {
    public static String dataUrl = "https://data.skytils.gg/";
    public static final String MOD_ID = "skytils";
    public static final String MOD_NAME = "Skytils";
    @NotNull
    public static final String VERSION = getVersion();
    public static final String UNKNOWN_VERSION = "unknown";
    public static final int apiVersion = 7;

    private static String getVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse(UNKNOWN_VERSION);
    }
}
