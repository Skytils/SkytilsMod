package skytils.skytilsmod.core;

import club.sk1er.vigilance.Vigilant;
import club.sk1er.vigilance.data.*;

import java.io.File;

public class Config extends Vigilant {

    @Property(
            type = PropertyType.TEXT,
            name = "Hypixel API Key",
            description = "Your Hypixel API key, which can be obtained from /api new. Required for some features.",
            category = "General",
            subcategory = "API"
    )
    public String apiKey = "";

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Griffin Burrows",
            description = "Shows the location of burrows during the event.",
            category = "Events",
            subcategory = "Mythological"
    )
    public boolean showGriffinBurrows = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Hidden Fels",
            description = "Make Fels in dungeons visible.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean showHiddenFels = false;

    public Config() {
        super(new File("./config/skytils.toml"));
        initialize();
    }
}
