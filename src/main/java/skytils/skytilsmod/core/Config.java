package skytils.skytilsmod.core;

import club.sk1er.vigilance.Vigilant;
import club.sk1er.vigilance.data.*;

import java.io.File;

public class Config extends Vigilant {

    @Property(
            type = PropertyType.TEXT,
            name = "Hypixel API Key",
            description = "Your Hypixel API key, which can be obtained from /api new. Required for some features.\nSet this with /skytils setkey <key>.",
            category = "General",
            subcategory = "API"
    )
    public String apiKey = "";


    @Property(
            type = PropertyType.SWITCH,
            name = "Override other reparty commands",
            description = "Uses Skytils' reparty command instead of other mods'. \n\u00a7cRequires restart to work",
            category = "General",
            subcategory = "Reparty"
    )
    public boolean overrideReparty = true;


    @Property(
            type = PropertyType.SWITCH,
            name = "Auto-Accept Reparty",
            description = "Automatically accepts reparty invites",
            category = "General",
            subcategory = "Reparty"
    )
    public boolean autoReparty = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Auto-Accept Reparty Timeout",
            description = "Timeout in seconds for accepting a reparty invite",
            category = "General",
            subcategory = "Reparty",
            max = 120
    )
    public Integer autoRepartyTimeout = 60;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Update Channel",
            description = "Choose what type of updates you get notified for.",
            category = "General",
            subcategory = "Updates",
            options = {"None", "Pre-Release", "Release"}
    )
    public int updateChannel = 2;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Griffin Burrows",
            description = "\u00a7b[WIP] \u00a7rShows the location of burrows during the event.",
            category = "Events",
            subcategory = "Mythological"
    )
    public boolean showGriffinBurrows = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Spirit Leap Names",
            description = "Shows names next to the head when you click.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean spiritLeapNames = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Hidden Fels",
            description = "Make Fels in dungeons visible.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean showHiddenFels = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Blaze Solver",
            description = "Changes the color of the blaze to shoot on Higher or Lower.",
            category = "Dungeons",
            subcategory = "Solvers"
    )
    public boolean blazeSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Boulder Solver",
            description = "\u00a7b[WIP] \u00a7rShow which boxes to move on the Boulder puzzle.",
            category = "Dungeons",
            subcategory = "Solvers"
    )
    public boolean boulderSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Simon Says Solver",
            description = "\u00a7b[WIP] \u00a7rShow which buttons to press on the Simon Says device in Floor 7.\n\u00a7cKnown bug, if a teammate clicks a button it will not register.",
            category = "Dungeons",
            subcategory = "Solvers"
    )
    public boolean simonSaysSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Trivia Solver",
            description = "Shows the correct answer for the questions on the Trivia puzzle.",
            category = "Dungeons",
            subcategory = "Solvers"
    )
    public boolean triviaSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Ghosts",
            description = "Makes the ghosts in the Mist visible.",
            category = "Mining",
            subcategory = "Quality of Life"
    )
    public boolean showGhosts = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Fetchur Solver",
            description = "Tells you what item Fetchur wants.",
            category = "Mining",
            subcategory = "Solvers"
    )
    public boolean fetchurSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Puzzler Solver",
            description = "Shows which block to mine for Puzzler.",
            category = "Mining",
            subcategory = "Solvers"
    )
    public boolean puzzlerSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Boss Bar Fix",
            description = "Hides the Witherborn boss bars",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean bossBarFix = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Implosion Particles",
            description = "Removes the explosion created by the Implosion ability.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean hideImplosionParticles = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Dropped Item Scale",
            description = "Change the size of dropped items.",
            category = "Miscellaneous",
            subcategory = "Items",
            max = 400
    )
    public Integer itemDropScale = 100;

    @Property(
            type = PropertyType.SWITCH,
            name = "Soul Eater Bonus",
            description = "Shows the current Soul Eater bonus from the last mob kill.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean soulEaterLore = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Only Collect Enchanted Items",
            description = "Prevents you from collecting unenchanted items from minions if there is a Super Compactor.",
            category = "Miscellaneous",
            subcategory = "Minions"
    )
    public boolean onlyCollectEnchantedItems = false;

    public Config() {
        super(new File("./config/skytils.toml"));
        initialize();
    }
}
