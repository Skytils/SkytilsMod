package skytils.skytilsmod.core;

import club.sk1er.vigilance.Vigilant;
import club.sk1er.vigilance.data.*;

import java.io.File;

public class Config extends Vigilant {
    public Config() {
        super(new File("./config/skytils.toml"));
        initialize();
    }
}
