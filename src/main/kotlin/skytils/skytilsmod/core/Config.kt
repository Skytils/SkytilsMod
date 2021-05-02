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
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/
package skytils.skytilsmod.core

import club.sk1er.vigilance.Vigilant
import club.sk1er.vigilance.data.Category
import club.sk1er.vigilance.data.Property
import club.sk1er.vigilance.data.PropertyType
import club.sk1er.vigilance.data.SortingBehavior
import skytils.skytilsmod.Skytils.Companion.mc
import java.awt.Color
import java.io.File

class Config : Vigilant(File("./config/skytils/config.toml"), "Skytils", sortingBehavior = ConfigSorting) {

    @Property(
        type = PropertyType.TEXT,
        name = "Skytils Data",
        description = "URL for Skytils data.",
        category = "General",
        subcategory = "API",
        hidden = true
    )

    var dataURL = "https://cdn.jsdelivr.net/gh/Skytils/SkytilsMod-Data@main/"

    @Property(
        type = PropertyType.SWITCH,
        name = "Fetch Lowest BIN Prices",
        description = "Fetches the lowest BIN features for Skytils to use.\nSome features will be hidden and will not work if this switch isn't on.",
        category = "General",
        subcategory = "API"
    )
    var fetchLowestBINPrices = false

    @Property(
        type = PropertyType.TEXT,
        name = "Hypixel API Key",
        description = "Your Hypixel API key, which can be obtained from /api new. Required for some features.",
        category = "General",
        subcategory = "API"
    )
    var apiKey = ""

    @Property(
        type = PropertyType.SELECTOR,
        name = "Command Alias Mode",
        description = "Choose which mode to use for Command Aliases.",
        category = "General",
        subcategory = "Command Aliases",
        options = ["Simple", "Advanced"]
    )
    var commandAliasMode = 0

    @Property(
        type = PropertyType.SWITCH,
        name = "First Launch",
        description = "Used to see if the user is a new user of Skytils.",
        category = "General",
        subcategory = "Other",
        hidden = true
    )
    var firstLaunch = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Config Button on Pause",
        description = "Adds a button to configure Skytils to the pause menu.",
        category = "General",
        subcategory = "Other"
    )
    var configButtonOnPause = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Reopen Options Menu",
        description = "Sets the menu to the Skytils options menu instead of exiting when on a Skytils config menu.",
        category = "General",
        subcategory = "Other"
    )
    var reopenOptionsMenu = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Override other reparty commands",
        description = "Uses Skytils' reparty command instead of other mods'. \n§cRequires restart to work",
        category = "General",
        subcategory = "Reparty"
    )
    var overrideReparty = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Auto-Accept Reparty",
        description = "Automatically accepts reparty invites",
        category = "General",
        subcategory = "Reparty"
    )
    var autoReparty = false

    @Property(
        type = PropertyType.SLIDER,
        name = "Auto-Accept Reparty Timeout",
        description = "Timeout in seconds for accepting a reparty invite",
        category = "General",
        subcategory = "Reparty",
        max = 120
    )
    var autoRepartyTimeout = 60

    @Property(
        type = PropertyType.SELECTOR,
        name = "Update Channel",
        description = "Choose what type of updates you get notified for.",
        category = "General",
        subcategory = "Updates",
        options = ["None", "Pre-Release", "Release"]
    )
    var updateChannel = 2

    @Property(
        type = PropertyType.SWITCH,
        name = "Auto Copy Fails to Clipboard",
        description = "Copies deaths and fails in dungeons to your clipboard.",
        category = "Dungeons",
        subcategory = "Miscellaneous"
    )
    var autoCopyFailToClipboard = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Dungeon Chest Profit",
        description = "Shows the estimated profit for items from chests in dungeons.",
        category = "Dungeons",
        subcategory = "Miscellaneous"
    )
    var dungeonChestProfit = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Dungeon Map",
        description = "Displays the vanilla map on your screen using vanilla rendering code.",
        category = "Dungeons",
        subcategory = "Miscellaneous"
    )
    var dungeonTrashMap = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Dungeon Timer",
        description = "Shows the time taken for certain actions in dungeons.",
        category = "Dungeons",
        subcategory = "Miscellaneous"
    )
    var dungeonTimer = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Necron Phase Timer",
        description = "Shows the time taken for each phase in the Necron boss fight.",
        category = "Dungeons",
        subcategory = "Miscellaneous"
    )
    var necronPhaseTimer = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Dungeon Score Estimate",
        description = "Shows an estimate of the current dungeon score.\nRequires the Dungeon Rooms mod in order to use.",
        category = "Dungeons",
        subcategory = "Score Calculation"
    )
    var showScoreCalculation = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Score Calculation Party Assist",
        description = "Helps your party determine the amount of secrets in the dungeon by sending room info in party chat.\n§cThis feature is use at your own risk.",
        category = "Dungeons",
        subcategory = "Score Calculation"
    )
    var scoreCalculationAssist = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Receive Help for Secret Counts",
        description = "Receive help from your party in order to determine the amount of secrets in the dungeon.",
        category = "Dungeons",
        subcategory = "Score Calculation"
    )
    var scoreCalculationReceiveAssist = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Remove Party Chat Notification Sounds",
        description = "Removes party chat notification sounds caused by score calculation.\n§cDo not turn this on if you do not use the Hypixel feature.",
        category = "Dungeons",
        subcategory = "Score Calculation"
    )
    var removePartyChatNotifFromScoreCalc = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Box Skeleton Masters",
        description = "Draws the bounding box for Skeleton Masters.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var boxSkeletonMasters = false

    @Property(
        type = PropertyType.NUMBER,
        name = "Dungeon Chest Reroll Confirmation",
        description = "Requires you to click multiple times in order to reroll a chest.",
        category = "Dungeons",
        subcategory = "Quality of Life",
        max = 5
    )
    var kismetRerollConfirm = 0

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Damage In Boss",
        description = "Removes damage numbers while in a boss fight. Requires the custom damage splash to be enabled.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var hideDamageInBoss = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Floor 4 Crowd Messages",
        description = "Hides the messages from the Crowd on Floor 4.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var hideF4Spam = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Oruo Messages",
        description = "Hides the messages from Oruo during the Trivia.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var hideOruoMessages = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Spirit Animal Nametags",
        description = "Removes the nametags above spirit animals on Floor 4.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var hideF4Nametags = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Terminal Completion Titles",
        description = "Removes the title that shows up when a terminal is completed.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var hideTerminalCompletionTitles = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Wither Miner Nametags",
        description = "Removes the nametags above Wither Miners on Floor 7.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var hideWitherMinerNametags = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Terracotta Nametags",
        description = "Hides the nametags of the Terracotta while in Dungeons",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var hideTerracotaNametags = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Non-Starred Mobs Nametags",
        description = "Hides the nametags of non-starred mobs while in Dungeons",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var hideNonStarredNametags = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Larger Bat Models",
        description = "Increases the size of bat models.\nThe hitbox of the bat may be offset from what is shown.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    @JvmField
    var biggerBatModels = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Revive Stone Names",
        description = "Shows names next to the heads on the Revive Stone menu.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var reviveStoneNames = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Bat Hitboxes",
        description = "Draws the outline of a bat's bounding box.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var showBatHitboxes = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Giant HP",
        description = "Shows the HP of Giants in your HUD.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var showGiantHP = false

    /* @Property(
   type = PropertyType.SWITCH,
   name = "Show Hidden Fels",
   description = "Make Fels in dungeons visible.",
   category = "Dungeons",
   subcategory = "Quality of Life"
   )*/
    var showHiddenFels = false

    /* @Property(
   type = PropertyType.SWITCH,
   name = "Show Hidden Shadow Assassins",
   description = "Make Shadow Assassins in dungeons visible.",
   category = "Dungeons",
   subcategory = "Quality of Life"
   )*/
    var showHiddenShadowAssassins = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Sadan's Interest",
        description = "Replace Sadan's interest display with Skytils' own.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var showSadanInterest = false

    @Property(
        type = PropertyType.SELECTOR,
        name = "Show Necron's HP",
        description = "Shows additional info about Necron's health.",
        category = "Dungeons",
        subcategory = "Quality of Life",
        options = ["None", "HP", "Percentage Health"]
    )
    var necronHealth = 0

    /* @Property(
   type = PropertyType.SWITCH,
   name = "Show Stealthy Watcher Undeads",
   description = "Makes stealthy undeads spawned by The Watcher visible.",
   category = "Dungeons",
   subcategory = "Quality of Life"
   )*/
    var showStealthyBloodMobs = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Spirit Leap Names",
        description = "Shows names next to the heads on the Spirit Leap menu.",
        category = "Dungeons",
        subcategory = "Quality of Life"
    )
    var spiritLeapNames = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Blaze Solver",
        description = "Changes the color of the blaze to shoot on Higher or Lower.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    @JvmField
    var blazeSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Next Blaze",
        description = "Colors the next blaze to shoot in Higher or Lower yellow.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    @JvmField
    var showNextBlaze = false

    @Property(
        type = PropertyType.COLOR,
        name = "Lowest Blaze Color",
        description = "Color used to highlight the lowest blaze in.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    @JvmField
    var lowestBlazeColor = Color(255, 0, 0, 200)

    @Property(
        type = PropertyType.COLOR,
        name = "Highest Blaze Color",
        description = "Color used to highlight the highest blaze in.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    @JvmField
    var highestBlazeColor = Color(0, 255, 0, 200)

    @Property(
        type = PropertyType.COLOR,
        name = "Next Blaze Color",
        description = "Color used to highlight the next blaze in.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    @JvmField
    var nextBlazeColor = Color(255, 255, 0, 200)

    @Property(
        type = PropertyType.SWITCH,
        name = "Boulder Solver",
        description = "§b[WIP] §rShow which boxes to move on the Boulder puzzle.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    var boulderSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Creeper Beams Solver",
        description = "Shows pairs on the Creeper Beams puzzle.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    var creeperBeamsSolver = false


    @Property(
        type = PropertyType.SWITCH,
        name = "Ice Fill Solver",
        description = "§b[WIP] §rShows the path to take on the Ice Fill puzzle.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    var iceFillSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Ice Path Solver",
        description = "Show the path for the silverfish to follow on the Ice Path puzzle.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    var icePathSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Teleport Maze Solver",
        description = "Shows which pads you've stepped on in the Teleport Maze puzzle.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    var teleportMazeSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Three Weirdos Solver",
        description = "Shows which chest to click in the Three Weirdos puzzle.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    var threeWeirdosSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Trivia Solver",
        description = "Shows the correct answer for the questions on the Trivia puzzle.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    var triviaSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Water Board Solver",
        description = "§b[WIP] §rDisplays which levers to flip for the Water Board puzzle.",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    var waterBoardSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Find correct Livid",
        description = "Shows the hp of the correct livid on F5 and M5",
        category = "Dungeons",
        subcategory = "Solvers"
    )
    var findCorrectLivid = false

    @Property(
        type = PropertyType.SELECTOR,
        name = "Type of Livid Finder",
        category = "Dungeons",
        subcategory = "Solvers",
        options = ["Block", "Entity"]
    )
    var lividFinderType = 0

    /*
   @Property(
   type = PropertyType.SWITCH,
   name = "Block Incorrect Terminal Clicks",
   description = "Blocks incorrect clicks on terminals.",
   category = "Dungeons",
   subcategory = "Terminal Solvers"
   )*/
    var blockIncorrectTerminalClicks = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Click in Order Solver",
        description = "Shows the items to click on the Click in Order terminal.",
        category = "Dungeons",
        subcategory = "Terminal Solvers"
    )
    var clickInOrderTerminalSolver = false

    @Property(
        type = PropertyType.COLOR,
        name = "Click in Order First Color",
        category = "Dungeons",
        subcategory = "Terminal Solvers"
    )
    var clickInOrderFirst = Color(2, 62, 138, 255)

    @Property(
        type = PropertyType.COLOR,
        name = "Click in Order Second Color",
        category = "Dungeons",
        subcategory = "Terminal Solvers"
    )
    var clickInOrderSecond = Color(65, 102, 245, 255)

    @Property(
        type = PropertyType.COLOR,
        name = "Click in Order Third Color",
        category = "Dungeons",
        subcategory = "Terminal Solvers"
    )
    var clickInOrderThird = Color(144, 224, 239, 255)

    @Property(
        type = PropertyType.SWITCH,
        name = "Select All Colors Solver",
        description = "Shows the items to click on the Select All Color terminal.",
        category = "Dungeons",
        subcategory = "Terminal Solvers"
    )
    var selectAllColorTerminalSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Starts With Sequence Solver",
        description = "Shows the items to click on the What starts with? terminal.",
        category = "Dungeons",
        subcategory = "Terminal Solvers"
    )
    var startsWithSequenceTerminalSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Shoot the Target Solver",
        description = "Shows all the shot blocks on the device in Floor 7.",
        category = "Dungeons",
        subcategory = "Terminal Solvers"
    )
    var shootTheTargetSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Simon Says Solver",
        description = "Show which buttons to press on the Simon Says device in Floor 7.\n§cIf a teammate clicks a button it will not register.",
        category = "Dungeons",
        subcategory = "Terminal Solvers"
    )
    var simonSaysSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hidden Jerry Alert",
        description = "Displays an alert when you find a hidden Jerry.",
        category = "Events",
        subcategory = "Mayor Jerry"
    )
    var hiddenJerryAlert = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Griffin Burrows",
        description = "Shows the location of burrows during the event.\n§cThis feature requires your API key to be set in general settings.",
        category = "Events",
        subcategory = "Mythological"
    )
    var showGriffinBurrows = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Burrow Particle Add-on",
        description = "Uses particles in addition to the API in order to locate burrows.\nThis feature will help find burrows when the API isn't working.",
        category = "Events",
        subcategory = "Mythological"
    )
    var particleBurrows = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Time Until Burrow Refresh",
        description = "Displays the amount of time until the next refresh.",
        category = "Events",
        subcategory = "Mythological"
    )
    var showGriffinCountdown = false


    @Property(
        type = PropertyType.CHECKBOX,
        name = "Show Fast-Travel: Castle",
        description = "Shows the closest travel scroll to the burrow.\nThis allows the mod to show the Castle warp.",
        category = "Events",
        subcategory = "Mythological"
    )
    var burrowCastleFastTravel = false

    @Property(
        type = PropertyType.CHECKBOX,
        name = "Show Fast-Travel: Crypts",
        description = "Shows the closest travel scroll to the burrow.\nThis allows the mod to show the Crypts warp.",
        category = "Events",
        subcategory = "Mythological"
    )
    var burrowCryptsFastTravel = false

    @Property(
        type = PropertyType.CHECKBOX,
        name = "Show Fast-Travel: Dark Auction",
        description = "Shows the closest travel scroll to the burrow.\nThis allows the mod to show the DA warp.",
        category = "Events",
        subcategory = "Mythological"
    )
    var burrowDarkAuctionFastTravel = false

    @Property(
        type = PropertyType.CHECKBOX,
        name = "Show Fast-Travel: Hub",
        description = "Shows the closest travel scroll to the burrow.\nThis allows the mod to show the Hub warp.",
        category = "Events",
        subcategory = "Mythological"
    )
    var burrowHubFastTravel = false

    @Property(
        type = PropertyType.COLOR,
        name = "Empty/Start Burrow Color",
        description = "Color used to highlight the Griffin Burrows in.",
        category = "Events",
        subcategory = "Mythological"
    )
    var emptyBurrowColor = Color(173, 216, 230)

    @Property(
        type = PropertyType.COLOR,
        name = "Mob Burrow Color",
        description = "Color used to highlight the Griffin Burrows in.",
        category = "Events",
        subcategory = "Mythological"
    )
    var mobBurrowColor = Color(173, 216, 230)

    @Property(
        type = PropertyType.COLOR,
        name = "Treasure Burrow Color",
        description = "Color used to highlight the Griffin Burrows in.",
        category = "Events",
        subcategory = "Mythological"
    )
    var treasureBurrowColor = Color(173, 216, 230)

    @Property(
        type = PropertyType.SWITCH,
        name = "Broadcast Rare Drop Notifications",
        description = "Sends rare drop notification when you obtain a rare drop from a Mythological Creature.",
        category = "Events",
        subcategory = "Mythological"
    )
    var broadcastMythCreatureDrop = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Display Gaia Construct Hits",
        description = "Tracks the amount of times a Gaia Construct has been hit.",
        category = "Events",
        subcategory = "Mythological"
    )
    var trackGaiaHits = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Track Mythological Creatures",
        description = "Tracks and saves drops from Mythological Creatures.",
        category = "Events",
        subcategory = "Mythological"
    )
    var trackMythEvent = false

    /*    @Property(
        type = PropertyType.SWITCH,
        name = "Trick or Treat Chest Alert",
        description = "Displays a title when any trick or treat chest spawns near you.",
        category = "Events",
        subcategory = "Spooky"
        TODO Make this actually work
    )*/
    var trickOrTreatChestAlert = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Shiny Orb Waypoints",
        description = "Creates a waypoint of where your shiny orbs are",
        category = "Events",
        subcategory = "Technoblade"
    )
    var shinyOrbWaypoints = false

    /* @Property(
   type = PropertyType.SWITCH,
   name = "Block Mathematical Hoe Right Clicks",
   description = "Prevents accidentally viewing the recipe for the Mathematical Hoes.",
   category = "Farming",
   subcategory = "Quality of Life"
   )*/
    var blockMathHoeClicks = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Farming RNG Titles",
        description = "Removes the titles that show up after getting a drop with Pumpkin Dicer / Melon Dicer",
        category = "Farming",
        subcategory = "Quality of Life"
    )
    var hideFarmingRNGTitles = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hungry Hiker Solver",
        description = "Tells you what item the Hungry Hiker wants.",
        category = "Farming",
        subcategory = "Solvers"
    )
    var hungryHikerSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Treasure Hunter Solver",
        description = "Tells you where the Treasure Hunter's treasure is.",
        category = "Farming",
        subcategory = "Solvers"
    )
    var treasureHunterSolver = false

    /* @Property(
   type = PropertyType.SWITCH,
   name = "Prevent Breaking Farms",
   description = "Prevents you from breaking parts of your farm while holding an axe or a hoe.",
   category = "Farming",
   subcategory = "Quality of Life"
   )*/
    var preventBreakingFarms = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Click to Accept Trapper Task",
        description = "Open chat, then click anywhere on screen to accept Trapper Task.",
        category = "Farming",
        subcategory = "Quality of Life"
    )
    var acceptTrapperTask = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Trapper Cooldown Alarm",
        description = "Quickly plays five notes once the Trapper is off cooldown.",
        category = "Farming",
        subcategory = "Quality of Life"
    )
    var trapperPing = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Dark Mode Mist",
        description = "Replaces colors in The Mist with darker variants.",
        category = "Mining",
        subcategory = "Quality of Life"
    )
    var darkModeMist = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Highlight Completed Comissions",
        description = "Marks completed comissions in the menu with a red background.",
        category = "Mining",
        subcategory = "Quality of Life"
    )
    var highlightCompletedComissions = false

    /* @Property(
   type = PropertyType.SWITCH,
   name = "Disable Pickaxe Ability on Private Island",
   description = "Prevents you from using pickaxe abilities on your island.",
   category = "Mining",
   subcategory = "Quality of Life"
   )*/
    var noPickaxeAbilityOnPrivateIsland = false

    @Property(
        type = PropertyType.SWITCH,
        name = "More Visible Ghosts",
        description = "Makes ghosts more visible in the Dwarven Mines.\nThis is allowed on the Hypixel network and can be done in Vanilla.",
        category = "Mining",
        subcategory = "Quality of Life"
    )
    @JvmField
    var moreVisibleGhosts = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Powder Ghast Ping",
        description = "Displays a title on your screen when a Powder Ghast spawns.",
        category = "Mining",
        subcategory = "Quality of Life"
    )
    var powerGhastPing = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Raffle Warning",
        description = "Displays a title on your screen 15 seconds from the ending of the raffle.",
        category = "Mining",
        subcategory = "Quality of Life"
    )
    var raffleWarning = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Raffle Waypoint",
        description = "Displays a waypoint on your screen to the raffle box after you deposit a ticket.",
        category = "Mining",
        subcategory = "Quality of Life"
    )
    var raffleWaypoint = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Recolor Carpets",
        description = "Changes the color of carpets in the Dwarven Mines to red.",
        category = "Mining",
        subcategory = "Quality of Life"
    )
    var recolorCarpets = false

    /* @Property(
   type = PropertyType.SWITCH,
   name = "Show Ghosts",
   description = "Makes the ghosts in The Mist visible.",
   category = "Mining",
   subcategory = "Quality of Life"
   )*/
    var showGhosts = false

    /* @Property(
   type = PropertyType.SWITCH,
   name = "Show Ghosts' Health",
   description = "Displays the health bar of ghosts in The Mist.",
   category = "Mining",
   subcategory = "Quality of Life"
   )*/
    var showGhostHealth = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Skymall Reminder",
        description = "Reminds you every Skyblock day to check your Skymall perk while in the Dwarven Mines.",
        category = "Mining",
        subcategory = "Quality of Life"
    )
    var skymallReminder = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Fetchur Solver",
        description = "Tells you what item Fetchur wants.",
        category = "Mining",
        subcategory = "Solvers"
    )
    var fetchurSolver = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Puzzler Solver",
        description = "Shows which block to mine for Puzzler.",
        category = "Mining",
        subcategory = "Solvers"
    )
    var puzzlerSolver = false

    /*
   @Property(
   type = PropertyType.SWITCH,
   name = "Block Useless Zombie Sword",
   description = "Prevents you from using the Zombie Sword when at full health.",
   category = "Miscellaneous",
   subcategory = "Items"
   )*/
    var blockUselessZombieSword = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Compact Item Stars",
        description = "Shortens item names with stars in them.",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    @JvmField
    var compactStars = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Implosion Particles",
        description = "Removes the explosion created by the Implosion ability.",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var hideImplosionParticles = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Midas Staff Gold",
        description = "Prevents the gold blocks from Molten Wave from rendering, leaving only the particles.",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var hideMidasStaffGoldBlocks = false

    @Property(
        type = PropertyType.DECIMAL_SLIDER,
        name = "Dropped Item Scale",
        description = "Change the size of dropped items.",
        category = "Miscellaneous",
        subcategory = "Items",
        maxF = 5f,
        decimalPlaces = 2
    )
    @JvmField
    var itemDropScale = 1f

    @Property(
        type = PropertyType.DECIMAL_SLIDER,
        name = "Larger Heads",
        description = "Change the size of heads in your inventory.",
        category = "Miscellaneous",
        subcategory = "Items",
        maxF = 2f,
        decimalPlaces = 2
    )
    @JvmField
    var largerHeadScale = 1f

    @Property(
        type = PropertyType.SWITCH,
        name = "Prevent Placing Weapons",
        description = "Stops the game from trying to place the Flower of Truth and the Spirit Sceptre.",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var preventPlacingWeapons = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Enchanted Book Abbreviation",
        description = "Shows the abbreviated name of books with only 1 enchantment.",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var showEnchantedBookAbbreviation = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Enchanted Book Tier",
        description = "Shows the tier of books with only 1 enchantment.",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var showEnchantedBookTier = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show NPC Sell Price",
        description = "Shows the NPC Sell Price on certain items.",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var showNPCSellPrice = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Potion Tier",
        description = "Shows the tier of potions as the stack size.",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var showPotionTier = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Pet Candies",
        description = "Shows the number of candies used as the stack size",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var showPetCandies = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Soul Eater Bonus",
        description = "Shows the current Soul Eater bonus from the last mob kill.",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var showSoulEaterBonus = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Radioactive Bonus",
        description = "Shows the current Critical Damage bonus from Tarantula helmet",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var showRadioactiveBonus = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Item Rarity",
        description = "Shows the rarity of an item in the color",
        category = "Miscellaneous",
        subcategory = "Item Rarity"
    )
    @JvmField
    var showItemRarity = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Pet Rarity",
        description = "Shows the rarity of a pet in the color",
        category = "Miscellaneous",
        subcategory = "Item Rarity"
    )
    var showPetRarity = false

    @Property(
        type = PropertyType.PERCENT_SLIDER,
        name = "Item Rarity Opacity",
        description = "How opaque the rarity color will be",
        category = "Miscellaneous",
        subcategory = "Item Rarity"
    )
    var itemRarityOpacity = 0.75f

    @Property(
        type = PropertyType.SWITCH,
        name = "Only Collect Enchanted Items",
        description = "Prevents you from collecting unenchanted items from minions if there is a Super Compactor.",
        category = "Miscellaneous",
        subcategory = "Minions"
    )
    var onlyCollectEnchantedItems = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Minion Tier",
        description = "Shows the tier of minions as the stack size.",
        category = "Miscellaneous",
        subcategory = "Minions"
    )
    var showMinionTier = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Endstone Protector Spawn Timer",
        description = "Counts down the time until the Endstone Protector spawns.",
        category = "Miscellaneous",
        subcategory = "Other"
    )
    var golemSpawnTimer = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Legion Player Display",
        description = "Shows the amount of players within range of the Legion enchantment.",
        category = "Miscellaneous",
        subcategory = "Other"
    )
    var legionPlayerDisplay = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Cap Legion Display",
        description = "Caps the legion display to the effective maximum(20)",
        category = "Miscellaneous",
        subcategory = "Other"
    )
    var legionCap = true

    @Property(
        type = PropertyType.SWITCH,
        name = "Placed Summoning Eye Display",
        description = "Shows the amount of summoning eyes placed in the Dragon's Nest.",
        category = "Miscellaneous",
        subcategory = "Other"
    )
    var summoningEyeDisplay = false

    @Property(
        type = PropertyType.PERCENT_SLIDER,
        name = "Transparent Head Layer",
        description = "Changes the transparency of your head layer.",
        category = "Miscellaneous",
        subcategory = "Other"
    )
    @JvmField
    var transparentHeadLayer = 1f

    @Property(
        type = PropertyType.SWITCH,
        name = "Custom Auction Price Input",
        description = "Displays Skytils' own auction input GUI instead of a sign.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var betterAuctionPriceInput = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Boss Bar Fix",
        description = "Hides the Witherborn boss bars.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var bossBarFix = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Custom Damage Splash",
        description = "§b[WIP] §rReplaces Skyblock damage splashes with custom rendered ones.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var customDamageSplash = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Disable Night Vision",
        description = "Removes the vanilla effects of Night Vision.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    @JvmField
    var disableNightVision = false

    @Property(
        type = PropertyType.SLIDER,
        name = "Dungeon Pot Lock",
        description = "Only allows you to purchase this dungeon pot from Ophelia, no other items.",
        category = "Miscellaneous",
        subcategory = "Quality of Life",
        max = 7
    )
    var dungeonPotLock = 0

    @Property(
        type = PropertyType.SWITCH,
        name = "Enchant Glint Fix",
        description = "Fixes some items not having the enchantment glint.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    @JvmField
    var enchantGlintFix = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Air Display",
        description = "Prevents the game from rendering the air bubbles while underwater.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var hideAirDisplay = false

    /* @Property(
   type = PropertyType.SWITCH,
   name = "Hide Creeper Veil Near NPCs",
   description = "Stops the Creeper Veil from blocking interaction with NPCs.",
   category = "Miscellaneous",
   subcategory = "Quality of Life"
   )*/
    @JvmField
    var hideCreeperVeilNearNPCs = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Fire on Entities",
        description = "Prevents the game from rendering fire on burning entities.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    @JvmField
    var hideEntityFire = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Fishing Hooks",
        description = "Hides fishing hooks from other players",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var hideFishingHooks = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Jerry Rune",
        description = "Prevents the game from rendering the items spawned by the Jerry rune.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var hideJerryRune = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Lightning",
        description = "Prevents all lightning from rendering.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    @JvmField
    var hideLightning = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Mob Death Particles",
        description = "Hides the smoke particles created when mobs die.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    @JvmField
    var hideDeathParticles = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Potion Effects in Inventory",
        description = "Prevents the game from rendering the potion effects in inventories while in Skyblock.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    @JvmField
    var hidePotionEffects = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Highlight Disabled Potion Effects",
        description = "Marks disabled potion effects in the toggle menu.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var highlightDisabledPotionEffects = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Highlight Salvageable Items",
        description = "Highlights items that can be salvaged.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var highlightSalvageableItems = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Highlight Dungeon-Sellable Items",
        description = "Highlights dungeon-sellable items such as training weights in Ophelia NPC or Trades menu.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var highlightDungeonSellableItems = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Middle Click GUI Items",
        description = "Replaces left clicks on items with no Skyblock ID with middle clicks.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var middleClickGUIItems = false

    @Property(
        type = PropertyType.SWITCH,
        name = "No Fire",
        description = "Removes first-person fire overlay when you are burning.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var noFire = false

    @Property(
        type = PropertyType.SWITCH,
        name = "No Hurtcam",
        description = "Removes the screen shake when you are hurt.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    @JvmField
    var noHurtcam = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Power Orb Lock",
        description = "Prevents placing the power orb if the same or better power orb is within range.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var powerOrbLock = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Prevent Log Spam",
        description = "Prevents your logs from being spammed with exceptions while on Hypixel.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    @JvmField
    var preventLogSpam = false

    /* @Property(
   type = PropertyType.SWITCH,
   name = "Prioritize Item Abilities",
   description = "Prioritize right click abilities over the profile viewer.\n§cThis feature is use at your own risk and may be removed later!",
   category = "Miscellaneous",
   subcategory = "Quality of Life"
   )*/
    @JvmField
    var prioritizeItemAbilities = false

    // TODO get Sk1er LLC to make a number text box
    @Property(
        type = PropertyType.TEXT,
        name = "Protect Items Above Value",
        description = "Prevents you from dropping, salvaging, or selling items worth more than this value. Based on Lowest BIN price.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var protectItemBINThreshold = "0"

    @Property(
        type = PropertyType.SWITCH,
        name = "Protect Starred Items",
        description = "Prevents you from dropping, salvaging, or selling starred dungeon items.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var protectStarredItems = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Spider's Den Rain Timer",
        description = "Shows the duration of rain in the Spider's Den.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var rainTimer = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Arachne Spawn",
        description = "Shows the location of the Arachne Altar when a fragment is placed.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var showArachneSpawn = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Arachne HP",
        description = "Shows the HP of Arachne on your HUD.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var showArachneHP = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Coins per Bit",
        description = "Shows how many coins you will get per bit spent at the Community Shop.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var showCoinsPerBit = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Show Lowest BIN Price",
        description = "Shows the lowest Buy It Now price for various items in Skyblock.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var showLowestBINPrice = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Stop Clicking Non-Salvageable Items",
        description = "Stops you from clicking Non-Salvageable items while in the Salvage menu",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var stopClickingNonSalvageable = false

    @Property(
        type = PropertyType.SWITCH,
        name = "View Relic Waypoints",
        description = "Shows the location of all the relics at the Spider's Den.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var relicWaypoints = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Find Rare Relics",
        description = "Finds rare relics at the Spider's Den as you walk near them.",
        category = "Miscellaneous",
        subcategory = "Quality of Life"
    )
    var rareRelicFinder = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Dolphin Pet Display",
        description = "Shows the players within the range of the Dolphin pet.",
        category = "Pets",
        subcategory = "Displays"
    )
    var dolphinPetDisplay = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Cap Dolphin Pet Display",
        description = "Caps the doplhin pet display to the effective maximum(5)",
        category = "Pets",
        subcategory = "Displays"
    )
    var dolphinCap = true

    @Property(
        type = PropertyType.SELECTOR,
        name = "Autopet Message Hider",
        description = "Removes autopet messages from your chat.",
        category = "Pets",
        subcategory = "Quality of Life",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideAutopetMessages = 0

    @Property(
        type = PropertyType.SWITCH,
        name = "Hide Pet Nametags",
        description = "Hides the nametags above pets.",
        category = "Pets",
        subcategory = "Quality of Life"
    )
    var hidePetNametags = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Highlight Active Pet",
        description = "Highlights the current active pet.",
        category = "Pets",
        subcategory = "Quality of Life"
    )
    var highlightActivePet = false

    @Property(
        type = PropertyType.COLOR,
        name = "Active Pet Highlight Color",
        description = "Color used to highlight the active pet in.",
        category = "Pets",
        subcategory = "Quality of Life"
    )
    var activePetColor = Color(0, 255, 0)

    @Property(
        type = PropertyType.SWITCH,
        name = "Highlight Favorite Pets",
        description = "Highlight pets marked as favorite.",
        category = "Pets",
        subcategory = "Quality of Life"
    )
    var highlightFavoritePets = false

    @Property(
        type = PropertyType.COLOR,
        name = "Favorite Pet Highlight Color",
        description = "Color used to highlight the favorite pets in.",
        category = "Pets",
        subcategory = "Quality of Life"
    )
    var favoritePetColor = Color(0, 255, 255)

    @Property(
        type = PropertyType.SWITCH,
        name = "Pet Item Confirmation",
        description = "Requires a confirmation before using a pet item.",
        category = "Pets",
        subcategory = "Quality of Life"
    )
    var petItemConfirmation = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Ping when in Atoned Horror Danger Zone",
        description = "Pings when you are standing on the Atoned Horror's TNT target.",
        category = "Slayer",
        subcategory = "Quality of Life"
    )
    var rev5TNTPing = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Slayer Boss Hitbox",
        description = "Draws a box around slayer mini-bosses.",
        category = "Slayer",
        subcategory = "Quality of Life"
    )
    var slayerBossHitbox = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Slayer Miniboss Spawn Alert",
        description = "Displays a title when a slayer miniboss spawns.",
        category = "Slayer",
        subcategory = "Quality of Life"
    )
    var slayerMinibossSpawnAlert = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Disable Cooldown Sounds",
        description = "Blocks the sound effect played while an item is on cooldown.",
        category = "Sounds",
        subcategory = "Abilities"
    )
    var disableCooldownSounds = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Disable Jerry-chine Gun Sounds",
        description = "Blocks the villager hrmm noises that the Jerry-chine gun projectiles play.",
        category = "Sounds",
        subcategory = "Abilities"
    )
    var disableJerrygunSounds = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Disable Flower of Truth Sounds",
        description = "Blocks the eating noises that the Flower of Truth plays.",
        category = "Sounds",
        subcategory = "Abilities"
    )
    var disableTruthFlowerSounds = false

    @Property(
        type = PropertyType.SWITCH,
        name = "Disable Terracotta Sounds",
        description = "Prevent the game from playing the loud sounds created by the Terracotta.",
        category = "Sounds",
        subcategory = "Dungeons"
    )
    var disableTerracottaSounds = false

    @Property(
        type = PropertyType.SELECTOR,
        name = "Text Shadow",
        description = "Changes the shadow type for the text displayed.",
        category = "Spam",
        subcategory = "Display",
        options = ["Normal", "None", "Outline"]
    )
    var spamShadow = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Implosion Hider",
        description = "Removes Implosion messages from your chat.",
        category = "Spam",
        subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var implosionHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Midas Staff Hider",
        description = "Removes Midas Staff messages from your chat.",
        category = "Spam",
        subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var midasStaffHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Spirit Sceptre Hider",
        description = "Removes Spirit Sceptre messages from your chat.",
        category = "Spam",
        subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var spiritSceptreHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Giant Sword Hider",
        description = "Removes Giant Sword messages from your chat.",
        category = "Spam",
        subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var giantSwordHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Livid Dagger Hider",
        description = "Removes Livid Dagger messages from your chat.",
        category = "Spam",
        subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var lividHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Ray of Hope Hider",
        description = "Removes Ray of Hope messages from your chat.",
        category = "Spam",
        subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hopeHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Mana Use Hider",
        description = "Removes mana usage updates from the action bar.\nWorks best with SkyblockAddons.",
        category = "Spam",
        subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var manaUseHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Blessing Hider",
        description = "Removes blessing messages from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Toasts"]
    )
    var blessingHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Blood Key Hider",
        description = "Removes Blood Key messages from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"]
    )
    var bloodKeyHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Boss Messages Hider",
        description = "Hides Boss Messages.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideBossMessages = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Wither Essence Hider",
        description = "Removes Wither Essence unlock messages from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var witherEssenceHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Undead Essence Hider",
        description = "Removes Undead Essence unlock messages from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var undeadEssenceHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Countdown and Ready Messages Hider",
        description = "Hides the Dungeon countdown and ready messages",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideDungeonCountdownAndReady = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Dungeon Abilities Messages Hider",
        description = "Hides dungeon abilities messages and ultimates messages in chat",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideDungeonAbilities = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Mort Messages Hider",
        description = "Hides Mort's messages while in dungeons",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideMortMessages = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Superboom Pickup Hider",
        description = "Removes Superboom pickup messages from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"]
    )
    var superboomHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Revive Stone Pickup Hider",
        description = "Removes Revive Stone pickup messages from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"]
    )
    var reviveStoneHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Wither Key Hider",
        description = "Removes Wither Key messages from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"]
    )
    var witherKeyHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Tether Hider",
        description = "Removes Healer Tether messages from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"]
    )
    var tetherHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Self Orb Pickup Hider",
        description = "Removes Healer Orb messages that you pick up from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"]
    )
    var selfOrbHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Other Orb Pickup Hider",
        description = "Removes Healer Orb messages that others pick up from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"]
    )
    var otherOrbHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Trap Damage Hider",
        description = "Removes Trap Damage messages from your chat.",
        category = "Spam",
        subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"]
    )
    var trapDamageHider = 0

    @Property(
        type = PropertyType.SLIDER,
        name = "Toast Time",
        description = "Number of milliseconds that toasts are displayed for.",
        category = "Spam",
        subcategory = "Dungeons",
        max = 10000
    )
    var toastTime = 2500

    @Property(
        type = PropertyType.SELECTOR,
        name = "Blocks in the way Hider",
        description = "Removes blocks in the way messages from your chat.",
        category = "Spam",
        subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var inTheWayHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Can't use Ability Hider",
        description = "Hides the you can't use abilities in this room message ",
        category = "Spam",
        subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideCantUseAbility = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Combo Hider",
        description = "Removes combo messages from your chat.",
        category = "Spam",
        subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI", "Toasts"]
    )
    var comboHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Auto-Recombobulator Hider",
        description = "Removes Auto-Recombobulator messages from your chat.",
        category = "Spam",
        subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI", "Toasts"]
    )
    var autoRecombHider = 0

    @Property(
        type = PropertyType.SWITCH,
        name = "Compact Building Tools",
        description = "Compacts messages from the Block Zapper and the Builder's Wand.",
        category = "Spam",
        subcategory = "Miscellaneous"
    )
    var compactBuildingTools = false

    @Property(
        type = PropertyType.SELECTOR,
        name = "Cooldown Hider",
        description = "Removes ability still on cooldown messages from your chat.",
        category = "Spam",
        subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var cooldownHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "No Enemies Nearby Hider",
        description = "Hides the 'There are no enemies nearby!' message",
        category = "Spam",
        subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideNoEnemiesNearby = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Out of mana Hider",
        description = "Removes out of mana messages from your chat.",
        category = "Spam",
        subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var manaMessages = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Profile Message Hider",
        description = "Removes the \"§aYou are playing on profile: §eFruit§r\" messages from your chat.",
        category = "Spam",
        subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var profileHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Blessing Enchant Hider",
        description = "Removes blessing enchant message from fishing.",
        category = "Spam",
        subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var blessingEnchantHider = 0

    @Property(
        type = PropertyType.SELECTOR,
        name = "Blessed Bait Hider",
        description = "Removes blessed bait message from fishing.",
        category = "Spam",
        subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var blessedBaitHider = 0

    init {
        ::itemRarityOpacity dependsOn ::showItemRarity

        ::showLowestBINPrice dependsOn ::fetchLowestBINPrices
        ::betterAuctionPriceInput dependsOn ::fetchLowestBINPrices
        ::dungeonChestProfit dependsOn ::fetchLowestBINPrices
        ::showCoinsPerBit dependsOn ::fetchLowestBINPrices
        ::protectItemBINThreshold dependsOn ::fetchLowestBINPrices

        ::showNextBlaze dependsOn ::blazeSolver
        ::lowestBlazeColor dependsOn ::blazeSolver
        ::highestBlazeColor dependsOn ::blazeSolver
        ::nextBlazeColor dependsOn ::showNextBlaze
        ::clickInOrderFirst dependsOn ::clickInOrderTerminalSolver
        ::clickInOrderSecond dependsOn ::clickInOrderTerminalSolver
        ::clickInOrderThird dependsOn ::clickInOrderTerminalSolver
        ::lividFinderType dependsOn ::findCorrectLivid

        ::showGriffinCountdown dependsOn ::showGriffinBurrows
        ::particleBurrows dependsOn ::showGriffinBurrows

        ::emptyBurrowColor dependsOn ::showGriffinBurrows
        ::mobBurrowColor dependsOn ::showGriffinBurrows
        ::treasureBurrowColor dependsOn ::showGriffinBurrows

        ::burrowCastleFastTravel dependsOn ::showGriffinBurrows
        ::burrowCryptsFastTravel dependsOn ::showGriffinBurrows
        ::burrowDarkAuctionFastTravel dependsOn ::showGriffinBurrows
        ::burrowHubFastTravel dependsOn ::showGriffinBurrows

        ::activePetColor dependsOn ::highlightActivePet
        ::favoritePetColor dependsOn ::highlightFavoritePets

        registerListener(::protectItemBINThreshold) {
            val numeric = it.replace(Regex("[^0-9]"), "")
            protectItemBINThreshold = numeric.ifEmpty { "0" }
        }

        // asbyth cool code
        registerListener(::darkModeMist) { mc.renderGlobal.loadRenderers() }
        registerListener(::recolorCarpets) { mc.renderGlobal.loadRenderers() }

        this.dataURL = "https://cdn.jsdelivr.net/gh/Skytils/SkytilsMod-Data@main/"

        if (this.largerHeadScale > 2) {
            this.largerHeadScale /= 100
            markDirty()
        }
        if (this.itemDropScale > 5) {
            this.itemDropScale /= 100f
            markDirty()
        }
        if (this.itemRarityOpacity > 1) {
            this.itemRarityOpacity /= 100f
            markDirty()
        }

        initialize()
    }

    private object ConfigSorting : SortingBehavior() {
        override fun getCategoryComparator(): Comparator<in Category> {
            return Comparator { o1, o2 ->
                if (o1.name == "General") return@Comparator -1
                if (o2.name == "General") return@Comparator 1
                else compareValuesBy(o1, o2) {
                    it.name
                }
            }
        }
    }
}
