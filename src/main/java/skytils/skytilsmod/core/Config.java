package skytils.skytilsmod.core;

import club.sk1er.vigilance.Vigilant;
import club.sk1er.vigilance.data.*;

import java.io.File;

public class Config extends Vigilant {

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Hidden Fels",
            description = "Make Fels in dungeons visible.",
            category = "Dungeons",
            subcategory = "QoL"
    )
    public boolean showHiddenFels = false;

    public Config() {
        super(new File("./config/skytils.toml"));
        initialize();
    }
}
