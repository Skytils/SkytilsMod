package skytils.skytilsmod.core;

import club.sk1er.vigilance.Vigilant;
import club.sk1er.vigilance.data.*;

import java.io.File;

public class Config extends Vigilant {

    @Property(
            type = PropertyType.TEXT,
            name = "Skytils Data",
            description = "URL for Skytils data.",
            category = "General",
            subcategory = "API",
            hidden = true
    )
    public String dataURL = "https://raw.githubusercontent.com/Skytils/SkytilsMod-Data/main/";

    @Property(
            type = PropertyType.TEXT,
            name = "Hypixel API Key",
            description = "Your Hypixel API key, which can be obtained from /api new. Required for some features.\nSet this with /skytils setkey <key>.",
            category = "General",
            subcategory = "API"
    )
    public String apiKey = "";

    @Property(
            type = PropertyType.SELECTOR,
            name = "Command Alias Mode",
            description = "Choose which mode to use for Command Aliases.",
            category = "General",
            subcategory = "Command Aliases",
            options = {"Simple", "Advanced"}
    )
    public int commandAliasMode = 0;

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
            name = "Auto Copy Fails to Clipboard",
            description = "Copies deaths and fails in dungeons to your clipboard.",
            category = "Dungeons",
            subcategory = "Miscellaneous"
    )
    public boolean autoCopyFailToClipboard = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Dungeon Timer",
            description = "Shows the time taken for certain actions in dungeons.",
            category = "Dungeons",
            subcategory = "Miscellaneous"
    )
    public boolean dungeonTimer = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Necron Phase Timer",
            description = "Shows the time taken for each phase in the Necron boss fight.",
            category = "Dungeons",
            subcategory = "Miscellaneous"
    )
    public boolean necronPhaseTimer = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Dungeon Score Estimate",
            description = "Shows an estimate of the current dungeon score.\nRequires the Dungeon Rooms mod in order to use.",
            category = "Dungeons",
            subcategory = "Score Calculation"
    )
    public boolean showScoreCalculation = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Score Calculation Party Assist",
            description = "Helps your party determine the amount of secrets in the dungeon by sending room info in party chat.\n\u00a7cThis feature is use at your own risk.",
            category = "Dungeons",
            subcategory = "Score Calculation"
    )
    public boolean scoreCalculationAssist = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Receive Help for Secret Counts",
            description = "Receive help from your party in order to determine the amount of secrets in the dungeon.",
            category = "Dungeons",
            subcategory = "Score Calculation"
    )
    public boolean scoreCalculationReceiveAssist = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Remove Party Chat Notification Sounds",
            description = "Removes party chat notification sounds caused by score calculation.\n\u00a7cDo not turn this on if you do not use the Hypixel feature.",
            category = "Dungeons",
            subcategory = "Score Calculation"
    )
    public boolean removePartyChatNotifFromScoreCalc = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Dungeon Chest Reroll Confirmation",
            description = "Requires you to click 3 times in order to reroll a chest.",
            category = "Dungeons",
            subcategory = "Quality of Life",
            max = 5
    )
    public int kismetRerollConfirm = 0;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Floor 4 Crowd Messages",
            description = "Hides the messages from the Crowd on Floor 4.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean hideF4Spam = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Spirit Animal Nametags",
            description = "Removes the nametags above spirit animals on Floor 4.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean hideF4Nametags = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Terminal Completion Titles",
            description = "Removes the title that shows up when a terminal is completed.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean hideTerminalCompletionTitles = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Wither Miner Nametags",
            description = "Removes the nametags above Wither Miners on Floor 7.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean hideWitherMinerNametags = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Terracotta Nametags",
            description = "Hides the nametags of the Terracotta while in Dungeons",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean hideTerracotaNametags = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Non-Starred Mobs Nametags",
            description = "Hides the nametags of non-starred mobs while in Dungeons",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean hideNonStarredNametags = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Larger Bat Models",
            description = "Increases the size of bat models.\nThe hitbox of the bat may be offset from what is shown.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean biggerBatModels = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Revive Stone Names",
            description = "Shows names next to the heads on the Revive Stone menu.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean reviveStoneNames = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Bat Hitboxes",
            description = "Draws the outline of a bat's bounding box.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean showBatHitboxes = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Giant HP",
            description = "Shows the HP of Giants in your HUD.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean showGiantHP = false;

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
            name = "Show Hidden Shadow Assassins",
            description = "Make Shadow Assassins in dungeons visible.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean showHiddenShadowAssassins = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Sadan's Interest",
            description = "Replace Sadan's interest display with Skytils' own.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean showSadanInterest = false;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Show Necron's HP",
            description = "Shows additional info about Necron's health.",
            category = "Dungeons",
            subcategory = "Quality of Life",
            options = {"None", "HP", "Percentage Health"}
    )
    public Integer necronHealth = 0;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Stealthy Watcher Undeads",
            description = "Makes stealthy undeads spawned by The Watcher visible.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean showStealthyBloodMobs = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Spirit Leap Names",
            description = "Shows names next to the heads on the Spirit Leap menu.",
            category = "Dungeons",
            subcategory = "Quality of Life"
    )
    public boolean spiritLeapNames = false;

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
            name = "Show Next Blaze",
            description = "Colors the next blaze to shoot in Higher or Lower yellow.",
            category = "Dungeons",
            subcategory = "Solvers"
    )
    public boolean showNextBlaze = false;

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
            name = "Ice Fill Solver",
            description = "\u00a7b[WIP] \u00a7rShows the path to take on the Ice Fill puzzle.",
            category = "Dungeons",
            subcategory = "Solvers"
    )
    public boolean iceFillSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Ice Path Solver",
            description = "Show the path for the silverfish to follow on the Ice Path puzzle.",
            category = "Dungeons",
            subcategory = "Solvers"
    )
    public boolean icePathSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Teleport Maze Solver",
            description = "Shows which pads you've stepped on in the Teleport Maze puzzle.",
            category = "Dungeons",
            subcategory = "Solvers"
    )
    public boolean teleportMazeSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Three Weirdos Solver",
            description = "Shows which chest to click in the Three Weirdos puzzle.",
            category = "Dungeons",
            subcategory = "Solvers"
    )
    public boolean threeWeirdosSolver = false;

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
            name = "Water Board Solver",
            description = "\u00a7b[WIP] \u00a7rDisplays which levers to flip for the Water Board puzzle.",
            category = "Dungeons",
            subcategory = "Solvers"
    )
    public boolean waterBoardSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Block Incorrect Terminal Clicks",
            description = "Blocks incorrect clicks on terminals.",
            category = "Dungeons",
            subcategory = "Terminal Solvers"
    )
    public boolean blockIncorrectTerminalClicks = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Click in Order Solver",
            description = "Shows the items to click on the Click in Order terminal.",
            category = "Dungeons",
            subcategory = "Terminal Solvers"
    )
    public boolean clickInOrderTerminalSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Select All Colors Solver",
            description = "Shows the items to click on the Select All Color terminal.",
            category = "Dungeons",
            subcategory = "Terminal Solvers"
    )
    public boolean selectAllColorTerminalSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Starts With Sequence Solver",
            description = "Shows the items to click on the What starts with? terminal.",
            category = "Dungeons",
            subcategory = "Terminal Solvers"
    )
    public boolean startsWithSequenceTerminalSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Simon Says Solver",
            description = "\u00a7b[WIP] \u00a7rShow which buttons to press on the Simon Says device in Floor 7.\n\u00a7cKnown bug, if a teammate clicks a button it will not register.",
            category = "Dungeons",
            subcategory = "Terminal Solvers"
    )
    public boolean simonSaysSolver = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hidden Jerry Alert",
            description = "Displays an alert when you find a hidden Jerry.",
            category = "Events",
            subcategory = "Mayor Jerry"
    )
    public boolean hiddenJerryAlert = false;

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
            name = "Burrow Particle Add-on",
            description = "\u00a7b[WIP] \u00a7rAdd-on for Show Griffin Burrows. Uses particles in addition to the API.\nIt's recommended you only use this feature when the API is not working.",
            category = "Events",
            subcategory = "Mythological"
    )
    public boolean particleBurrows = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Time Until Burrow Refresh",
            description = "Displays the amount of time until the next refresh.",
            category = "Events",
            subcategory = "Mythological"
    )
    public boolean showGriffinCountdown = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Fast-Travel Addon",
            description = "Shows the closest travel scroll to the burrow.\nRequires MVP+ rank and the travel scroll unlocked.",
            category = "Events",
            subcategory = "Mythological"
    )
    public boolean showBurrowFastTravel = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Block Mathematical Hoe Right Clicks",
            description = "Prevents accidentally viewing the recipe for the Mathematical Hoes.",
            category = "Farming",
            subcategory = "Quality of Life"
    )
    public boolean blockMathHoeClicks = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Farming RNG Titles",
            description = "Removes the titles that show up after getting a drop with Pumpkin Dicer / Melon Dicer",
            category = "Farming",
            subcategory = "Quality of Life"
    )
    public boolean hideFarmingRNGTitles = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Prevent Breaking Farms",
            description = "Prevents you from breaking parts of your farm while holding an axe or a hoe.",
            category = "Farming",
            subcategory = "Quality of Life"
    )
    public boolean preventBreakingFarms = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Disable Pickaxe Ability on Private Island",
            description = "Prevents you from using pickaxe abilities on your island.",
            category = "Mining",
            subcategory = "Quality of Life"
    )
    public boolean noPickaxeAbilityOnPrivateIsland = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Sneaky Creeper",
            description = "Makes Sneaky Creepers in the Deep Caverns visible.",
            category = "Mining",
            subcategory = "Quality of Life"
    )
    public boolean showSneakyCreeper = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Powder Ghast Ping",
            description = "Displays a title on your screen when a Powder Ghast spawns.",
            category = "Mining",
            subcategory = "Quality of Life"
    )
    public boolean powerGhastPing = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Raffle Warning",
            description = "Displays a title on your screen 15 seconds from the ending of the raffle.",
            category = "Mining",
            subcategory = "Quality of Life"
    )
    public boolean raffleWarning = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Raffle Waypoint",
            description = "Displays a waypoint on your screen to the raffle box after you deposit a ticket.",
            category = "Mining",
            subcategory = "Quality of Life"
    )
    public boolean raffleWaypoint = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Ghosts",
            description = "Makes the ghosts in The Mist visible.",
            category = "Mining",
            subcategory = "Quality of Life"
    )
    public boolean showGhosts = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Ghosts' Health",
            description = "Displays the health bar of ghosts in The Mist.",
            category = "Mining",
            subcategory = "Quality of Life"
    )
    public boolean showGhostHealth = false;

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
            name = "Block Useless Zombie Sword",
            description = "Prevents you from using the Zombie Sword when at full health.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean blockUselessZombieSword = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Compact Item Stars",
            description = "Shortens item names with stars in them.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean compactStars = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Implosion Particles",
            description = "Removes the explosion created by the Implosion ability.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean hideImplosionParticles = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Midas Staff Gold",
            description = "Prevents the gold blocks from Molten Wave from rendering, leaving only the particles.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean hideMidasStaffGoldBlocks = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Dropped Item Scale",
            description = "Change the size of dropped items.",
            category = "Miscellaneous",
            subcategory = "Items",
            max = 500
    )
    public Integer itemDropScale = 100;

    @Property(
            type = PropertyType.SLIDER,
            name = "Larger Heads",
            description = "Change the size of heads in your inventory.",
            category = "Miscellaneous",
            subcategory = "Items",
            max = 200
    )
    public Integer largerHeadScale = 100;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Enchanted Book Tier",
            description = "Shows the tier of books with only 1 enchantment.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean showEnchantedBookTier = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Potion Tier",
            description = "Shows the tier of potions as the stack size.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean showPotionTier = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Pet Candies",
            description = "Shows the number of candies used as the stack size",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean showPetCandies = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Soul Eater Bonus",
            description = "Shows the current Soul Eater bonus from the last mob kill.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean showSoulEaterBonus = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Item Rarity",
            description = "Shows the rarity of an item in the color",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    public boolean showItemRarity = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Item Rarity Opacity",
            description = "How opaque the rarity color will be",
            category = "Miscellaneous",
            subcategory = "Items",
            max = 100
    )
    public int itemRarityOpacity = 75;

    @Property(
            type = PropertyType.SWITCH,
            name = "Only Collect Enchanted Items",
            description = "Prevents you from collecting unenchanted items from minions if there is a Super Compactor.",
            category = "Miscellaneous",
            subcategory = "Minions"
    )
    public boolean onlyCollectEnchantedItems = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Minion Tier",
            description = "Shows the tier of minions as the stack size.",
            category = "Miscellaneous",
            subcategory = "Minions"
    )
    public boolean showMinionTier = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Endstone Protector Spawn Timer",
            description = "Counts down the time until the Endstone Protector spawns.",
            category = "Miscellaneous",
            subcategory = "Other"
    )
    public boolean golemSpawnTimer = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Legion Player Display",
            description = "Shows the amount of players within range of the Legion enchantment.",
            category = "Miscellaneous",
            subcategory = "Other"
    )
    public boolean legionPlayerDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Cap Legion Display",
            description = "Caps the legion display to the effective maximum(20)",
            category = "Miscellaneous",
            subcategory = "Other"
    )
    public boolean legionCap = true;

    @Property(
            type = PropertyType.SWITCH,
            name = "Boss Bar Fix",
            description = "Hides the Witherborn boss bars.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean bossBarFix = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Custom Damage Splash",
            description = "\u00a7b[WIP] \u00a7rReplaces Skyblock damage splashes with custom rendered ones.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean customDamageSplash = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Disable Night Vision",
            description = "Removes the vanilla effects of Night Vision.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean disableNightVision = false;

    @Property(
            type = PropertyType.SLIDER,
            name = "Dungeon Pot Lock",
            description = "Only allows you to purchase this dungeon pot from Ophelia, no other items.",
            category = "Miscellaneous",
            subcategory = "Quality of Life",
            max = 7
    )
    public int dungeonPotLock = 0;

    @Property(
            type = PropertyType.SWITCH,
            name = "Enchant Glint Fix",
            description = "Fixes some items not having the enchantment glint.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean enchantGlintFix = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Air Display",
            description = "Prevents the game from rendering the air bubbles while underwater.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideAirDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Creeper Veil Near NPCs",
            description = "Stops the Creeper Veil from blocking interaction with NPCs.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideCreeperVeilNearNPCs = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Fire on Entities",
            description = "Prevents the game from rendering fire on burning entities.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideEntityFire = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Jerry Rune",
            description = "Prevents the game from rendering the items spawned by the Jerry rune.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideJerryRune = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Lightning",
            description = "Prevents all lightning from rendering.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideLightning = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Mob Death Particles",
            description = "Hides the smoke particles created when mobs die.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideDeathParticles = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Highlight Salvageable Items",
            description = "Highlights items that can be salvaged.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean highlightSalvageableItems = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Slayer Miniboss Spawn Alert",
            description = "Displays a title when a slayer miniboss spawns.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean slayerMinibossSpawnAlert = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "No Hurtcam",
            description = "Removes the screen shake when you are hurt.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean noHurtcam = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Ping when in Atoned Horror Danger Zone",
            description = "Pings when you are standing on the Atoned Horror's TNT target.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean rev5TNTPing = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Power Orb Lock",
            description = "Prevents placing the power orb if the same or better power orb is within range.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean powerOrbLock = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Prevent Log Spam",
            description = "Prevents your logs from being spammed with exceptions while on Hypixel.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean preventLogSpam = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Prioritize Item Abilities",
            description = "Prioritize right click abilities over the profile viewer.\n\u00a7cThis feature is use at your own risk and may be removed later!",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean prioritizeItemAbilities = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Protect Starred Items",
            description = "Prevents you from dropping, salvaging, or selling starred dungeon items.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean protectStarredItems = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Arachne Spawn",
            description = "Shows the location of the Arachne Altar when a fragment is placed.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean showArachneSpawn = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Show Arachne HP",
            description = "Shows the HP of Arachne on your HUD.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean showArachneHP = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Stop Clicking Non-Salvageable Items",
            description = "Stops you from clicking Non-Salvageable items while in the Salvage menu",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean stopClickingNonSalvageable = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "View Relic Waypoints",
            description = "Shows the location of all the relics at the Spider's Den.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean relicWaypoints = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Find Rare Relics",
            description = "Finds rare relics at the Spider's Den as you walk near them.",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean rareRelicFinder = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Dolphin Pet Display",
            description = "Shows the players within the range of the Dolphin pet.",
            category = "Pets",
            subcategory = "Displays"
    )
    public boolean dolphinPetDisplay = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Cap Dolphin Pet Display",
            description = "Caps the doplhin pet display to the effective maximum(5)",
            category = "Pets",
            subcategory = "Displays"
    )
    public boolean dolphinCap = true;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Autopet Message Hider",
            description = "Removes autopet messages from your chat.",
            category = "Pets",
            subcategory = "Quality of Life",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int hideAutopetMessages = 0;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide Pet Nametags",
            description = "Hides the nametags above pets.",
            category = "Pets",
            subcategory = "Quality of Life"
    )
    public boolean hidePetNametags = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Pet Item Confirmation",
            description = "Requires a confirmation before using a pet item.",
            category = "Pets",
            subcategory = "Quality of Life"
    )
    public boolean petItemConfirmation = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Hide fishing hooks",
            description = "Hides fishing hooks from other players",
            category = "Miscellaneous",
            subcategory = "Quality of Life"
    )
    public boolean hideFishingHooks = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Disable Cooldown Sounds",
            description = "Blocks the sound effect played while an item is on cooldown.",
            category = "Sounds",
            subcategory = "Abilities"
    )
    public boolean disableCooldownSounds = false;

    @Property(
            type = PropertyType.SWITCH,
            name = "Disable Terracotta Sounds",
            description = "Prevent the game from playing the loud sounds created by the Terracotta.",
            category = "Sounds",
            subcategory = "Dungeons"
    )
    public boolean disableTerracottaSounds = false;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Text Shadow",
            description = "Changes the shadow type for the text displayed.",
            category = "Spam",
            subcategory = "Display",
            options = {"Normal", "None", "Outline"}
    )
    public int spamShadow = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Implosion Hider",
            description = "Removes Implosion messages from your chat.",
            category = "Spam",
            subcategory = "Abilities",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int implosionHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Midas Staff Hider",
            description = "Removes Midas Staff messages from your chat.",
            category = "Spam",
            subcategory = "Abilities",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int midasStaffHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Spirit Sceptre Hider",
            description = "Removes Spirit Sceptre messages from your chat.",
            category = "Spam",
            subcategory = "Abilities",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int spiritSceptreHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Giant Sword Hider",
            description = "Removes Giant Sword messages from your chat.",
            category = "Spam",
            subcategory = "Abilities",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int giantSwordHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Livid Dagger Hider",
            description = "Removes Livid Dagger messages from your chat.",
            category = "Spam",
            subcategory = "Abilities",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int lividHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Mana Use Hider",
            description = "Removes mana usage updates from the action bar.\nWorks best with SkyblockAddons.",
            category = "Spam",
            subcategory = "Abilities",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int manaUseHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Blessing Hider",
            description = "Removes blessing messages from your chat.",
            category = "Spam",
            subcategory = "Dungeons",
            options = {"Normal", "Hidden", "Toasts"}
    )
    public int blessingHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Blood Key Hider",
            description = "Removes Blood Key messages from your chat.",
            category = "Spam",
            subcategory = "Dungeons",
            options = {"Normal", "Hidden", "Separate Gui", "Toasts"}
    )
    public int bloodKeyHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Boss Messages Hider",
            description = "Hides Boss Messages",
            category = "Spam",
            subcategory = "Dungeons",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int hideBossMessages = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Countdown and Ready Messages Hider",
            description = "Hides the Dungeon countdown and ready messages",
            category = "Spam",
            subcategory = "Dungeons",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int hideDungeonCountdownAndReady = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Dungeon Abilities Messages Hider",
            description = "Hides dungeon abilities messages and ultimates messages in chat",
            category = "Spam",
            subcategory = "Dungeons",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int hideDungeonAbilities = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Mort Messages Hider",
            description = "Hides Mort's messages while in dungeons",
            category = "Spam",
            subcategory = "Dungeons",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int hideMortMessages = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Superboom Pickup Hider",
            description = "Removes Superboom pickup messages from your chat.",
            category = "Spam",
            subcategory = "Dungeons",
            options = {"Normal", "Hidden", "Separate Gui", "Toasts"}
    )
    public int superboomHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Revive Stone Pickup Hider",
            description = "Removes Revive Stone pickup messages from your chat.",
            category = "Spam",
            subcategory = "Dungeons",
            options = {"Normal", "Hidden", "Separate Gui", "Toasts"}
    )
    public int reviveStoneHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Wither Key Hider",
            description = "Removes Wither Key messages from your chat.",
            category = "Spam",
            subcategory = "Dungeons",
            options = {"Normal", "Hidden", "Separate Gui", "Toasts"}
    )
    public int witherKeyHider = 0;

    @Property(
            type = PropertyType.SLIDER,
            name = "Toast Time",
            description = "Number of milliseconds that toasts are displayed for.",
            category = "Spam",
            subcategory = "Dungeons",
            max = 10_000
    )
    public int toastTime = 2500;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Combo Hider",
            description = "Removes combo messages from your chat.",
            category = "Spam",
            subcategory = "Miscellaneous",
            options = {"Normal", "Hidden", "Separate GUI", "Toasts"}
    )
    public int comboHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Blocks in the way Hider",
            description = "Removes blocks in the way messages from your chat.",
            category = "Spam",
            subcategory = "Miscellaneous",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int inTheWayHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Can't use Ability Hider",
            description = "Hides the you can't use abilities in this room message ",
            category = "Spam",
            subcategory = "Miscellaneous",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int hideCantUseAbility = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Cooldown Hider",
            description = "Removes ability still on cooldown messages from your chat.",
            category = "Spam",
            subcategory = "Miscellaneous",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int cooldownHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "No Enemies Nearby Hider",
            description = "Hides the 'There are no enemies nearby!' message",
            category = "Spam",
            subcategory = "Miscellaneous",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int hideNoEnemiesNearby = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Out of mana Hider",
            description = "Removes out of mana messages from your chat.",
            category = "Spam",
            subcategory = "Miscellaneous",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int manaMessages = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Blessing Enchant Hider",
            description = "Removes blessing enchant message from fishing.",
            category = "Spam",
            subcategory = "Fishing",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int blessingEnchantHider = 0;

    @Property(
            type = PropertyType.SELECTOR,
            name = "Blessing Bait Hider",
            description = "Removes blessing bait message from fishing.",
            category = "Spam",
            subcategory = "Fishing",
            options = {"Normal", "Hidden", "Separate GUI"}
    )
    public int blessingBaitHider = 0;

    public Config() {
        super(new File("./config/skytils/config.toml"));
        initialize();
    }
}
