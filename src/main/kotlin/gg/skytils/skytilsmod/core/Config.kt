/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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
package gg.skytils.skytilsmod.core

import gg.essential.api.EssentialAPI
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UDesktop
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.Category
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType
import gg.essential.vigilance.data.SortingBehavior
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.commands.impl.RepartyCommand
import gg.skytils.skytilsmod.features.impl.trackers.Tracker
import gg.skytils.skytilsmod.gui.PotionNotificationsGui
import gg.skytils.skytilsmod.gui.SpiritLeapNamesGui
import gg.skytils.skytilsmod.mixins.transformers.accessors.AccessorCommandHandler
import gg.skytils.skytilsmod.utils.Utils
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.LoaderState
import java.awt.Color
import java.io.File
import java.net.URI

object Config : Vigilant(
    File("./config/skytils/config.toml"),
    if (Utils.isBSMod) "BSMod" else "Skytils",
    sortingBehavior = ConfigSorting
) {
    @Property(
        type = PropertyType.SWITCH, name = "Fetch Lowest BIN Prices",
        description = "Fetches the lowest BIN features for Skytils to use.\nSome features will be hidden and will not work if this switch isn't on.",
        category = "General", subcategory = "API",
        searchTags = ["BIN", "Bits", "Price Input", "Protect Items Above Value", "Chest Profit", "Dungeon Profit", "Container Sell Value", "Vistor Offer Helper", "Copper"]
    )
    var fetchLowestBINPrices = false

    @Property(
        type = PropertyType.SELECTOR, name = "Command Alias Mode",
        description = "Choose which mode to use for Command Aliases.",
        category = "General", subcategory = "Command Aliases",
        options = ["Simple", "Advanced"]
    )
    var commandAliasMode = 0

    @Property(
        type = PropertyType.SWITCH, name = "Auto Start Local API",
        description = "Automatically launches the Local API on game startup.",
        category = "General", subcategory = "Local API",
    )
    var localAPIAutoStart = false

    @Property(
        type = PropertyType.TEXT, name = "Local API Password",
        description = "Sets the password for the local API. No password will cause all requests to be rejected.",
        category = "General", subcategory = "Local API",
        protectedText = true
    )
    var localAPIPassword = ""

    @Property(
        type = PropertyType.BUTTON, name = "Join the Skytils Discord",
        description = "Join the Skytils Discord server for help using any of the features.",
        category = "General", subcategory = "Other",
        placeholder = "Join"
    )
    @Suppress("unused")
    fun openDiscordLink() {
        UDesktop.browse(URI.create("https://discord.gg/skytils"))
    }

    @Property(
        type = PropertyType.SWITCH, name = "First Launch",
        description = "Used to see if the user is a new user of Skytils.",
        category = "General", subcategory = "Other",
        hidden = true
    )
    var firstLaunch = true

    @Property(
        type = PropertyType.TEXT, name = "Last Launched Skytils Version",
        category = "General", subcategory = "Other",
        hidden = true
    )
    var lastLaunchedVersion = "0"

    @Property(
        type = PropertyType.SWITCH, name = "Config Button on Pause",
        description = "Adds a button to configure Skytils to the pause menu.",
        category = "General", subcategory = "Other"
    )
    var configButtonOnPause = true

    @Property(
        type = PropertyType.SWITCH, name = "Reopen Options Menu",
        description = "Sets the menu to the Skytils options menu instead of exiting when on a Skytils config menu.",
        category = "General", subcategory = "Other"
    )
    var reopenOptionsMenu = true

    @Property(
        type = PropertyType.SWITCH, name = "Override other reparty commands",
        description = "Uses Skytils' reparty command instead of other mods'. \n§cRequires restart to disable",
        category = "General", subcategory = "Reparty"
    )
    var overrideReparty = true

    @Property(
        type = PropertyType.SWITCH, name = "Coop Add Confirmation",
        description = "Requires you to run the /coopadd command twice to add a member.",
        category = "General", subcategory = "Hypixel"
    )
    var coopAddConfirmation = true

    @Property(
        type = PropertyType.SWITCH, name = "Guild Leave Confirmation",
        description = "Requires you to run the /g leave command twice to leave your guild.",
        category = "General", subcategory = "Hypixel"
    )
    var guildLeaveConfirmation = true

    @Property(
        type = PropertyType.SWITCH, name = "Multiple Party Invites Fix",
        description = "§b[WIP] Tries to fix the ghost party issue when inviting multiple in one command.",
        category = "General", subcategory = "Hypixel"
    )
    var multiplePartyInviteFix = false

    @Property(
        type = PropertyType.SWITCH, name = "Auto-Accept Reparty",
        description = "Automatically accepts reparty invites",
        category = "General", subcategory = "Reparty"
    )
    var autoReparty = false

    @Property(
        type = PropertyType.SLIDER, name = "Auto-Accept Reparty Timeout",
        description = "Timeout in seconds for accepting a reparty invite",
        category = "General", subcategory = "Reparty",
        max = 120
    )
    var autoRepartyTimeout = 60

    @Property(
        type = PropertyType.SELECTOR, name = "Update Channel",
        description = "Choose what type of updates you get notified for.",
        category = "General", subcategory = "Updates",
        options = ["None", "Pre-Release", "Release"]
    )
    var updateChannel = 2

    @Property(
        type = PropertyType.SWITCH, name = "Blood Room Portal Timer",
        description = "Displays time to portal on your HUD.",
        category = "Dungeons", subcategory = "HUD"
    )
    var bloodPortalTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Dungeon Crypts Counter",
        description = "Shows the amount of crypts destroyed on your HUD.",
        category = "Dungeons", subcategory = "HUD"
    )
    var bigCryptsCounter = false

    @Property(
        type = PropertyType.SWITCH, name = "Auto Copy Fails to Clipboard",
        description = "Copies deaths and fails in dungeons to your clipboard.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var autoCopyFailToClipboard = false

    @Property(
        type = PropertyType.SWITCH, name = "Auto-Reparty on Dungeon Ending",
        description = "Runs the reparty command when your dungeon ends.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var autoRepartyOnDungeonEnd = false

    @Property(
        type = PropertyType.SWITCH, name = "Death Counter",
        description = "Counts the amount of times each member of your team has died in a dungeon.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var dungeonDeathCounter = false

    @Property(
        type = PropertyType.SWITCH, name = "Party Finder Stats",
        description = "§b[WIP] Displays Stats about a Player who joined.",
        category = "Dungeons", subcategory = "Party Finder"
    )
    var partyFinderStats = false

    @Property(
        type = PropertyType.SWITCH, name = "Dungeon Chest Profit",
        description = "Shows the estimated profit for items from chests in dungeons.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var dungeonChestProfit = false

    @Property(
        type = PropertyType.SWITCH, name = "Dungeon Chest Profit Includes Essence",
        description = "Include essence when calculating Dungeon Chest Profit.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var dungeonChestProfitIncludesEssence = true

    @Property(
        type = PropertyType.SWITCH, name = "Dungeon Map",
        description = "Displays the vanilla map on your screen using vanilla rendering code.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var dungeonTrashMap = false

    @Property(
        type = PropertyType.SWITCH, name = "Dungeon Start Confirmation",
        description = "Requires a confirmation to start the dungeon when not in a full party.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var noChildLeftBehind = false

    @Property(
        type = PropertyType.SWITCH, name = "Dungeon Timer",
        description = "Shows the time taken for certain actions in dungeons.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var dungeonTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Necron Phase Timer",
        description = "Shows the time taken for each phase in the Necron boss fight.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var necronPhaseTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Red Screen Fix",
        description = "Fixes an issue in The Catacombs Floors 2 and 3 where the screen turns red on fancy graphics.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var worldborderFix = true

    @Property(
        type = PropertyType.SWITCH, name = "Show Decimal Seconds on Timers",
        description = "Adds decimals to the amount of seconds on the dungeon timers.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var showMillisOnDungeonTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Sadan Phase Timer",
        description = "Shows the time taken for each phase in the Sadan boss fight.",
        category = "Dungeons", subcategory = "Miscellaneous"
    )
    var sadanPhaseTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Dungeon Score Estimate",
        description = "Shows an estimate of the current dungeon score.",
        category = "Dungeons", subcategory = "Score Calculation"
    )
    var showScoreCalculation = false

    @Property(
        type = PropertyType.SWITCH, name = "Minimized Dungeon Score Estimate",
        description = "Only shows the dungeon score.",
        category = "Dungeons", subcategory = "Score Calculation"
    )
    var minimizedScoreCalculation = false

    @Property(
        type = PropertyType.SWITCH, name = "Score Calculation Party Assist",
        description = "Helps your party determine the state of the mimic in your dungeon by sending in party chat.\n§cThis feature is use at your own risk.",
        category = "Dungeons", subcategory = "Score Calculation"
    )
    var scoreCalculationAssist = false

    @Property(
        type = PropertyType.SWITCH, name = "Receive Score Calculation Party Assist",
        description = "Receive help from your party in order to determine the state of the mimic in the dungeon.",
        category = "Dungeons", subcategory = "Score Calculation"
    )
    var scoreCalculationReceiveAssist = false

    @Property(
        type = PropertyType.SWITCH, name = "Allow Mimic Dead! from other Mods",
        description = "Uses the Mimic dead! in order to determine the state of the mimic in the dungeon.",
        category = "Dungeons", subcategory = "Score Calculation"
    )
    var receiveHelpFromOtherModMimicDead = false

    @Property(
        type = PropertyType.SWITCH, name = "Send message on 270 score",
        description = "Send message on 270 score.",
        category = "Dungeons", subcategory = "Score Calculation"
    )
    var sendMessageOn270Score = false

    @Property(
        type = PropertyType.PARAGRAPH, name = "Message for 270 score",
        description = "Customize the message sent on hitting 270 score.",
        category = "Dungeons", subcategory = "Score Calculation",
        placeholder = "Skytils > 270 score"
    )
    var message270Score = ""

    @Property(
        type = PropertyType.SWITCH, name = "Create Title on 270 score",
        description = "Create title on 270 score.",
        category = "Dungeons", subcategory = "Score Calculation"
    )
    var createTitleOn270Score = false

    @Property(
        type = PropertyType.PARAGRAPH, name = "270 Title Message",
        description = "Customize the message that will be sent when the score reaches 270.",
        category = "Dungeons", subcategory = "Score Calculation",
        placeholder = "270"
    )
    var messageTitle270Score = ""

    @Property(
        type = PropertyType.SWITCH, name = "Send message on 300 score",
        description = "Send message on 300 score.",
        category = "Dungeons", subcategory = "Score Calculation"
    )
    var sendMessageOn300Score = false

    @Property(
        type = PropertyType.PARAGRAPH, name = "Message for 300 score",
        description = "Customize the message sent on hitting 300 score.",
        category = "Dungeons", subcategory = "Score Calculation",
        placeholder = "Skytils > 300 score"
    )
    var message300Score = ""

    @Property(
        type = PropertyType.SWITCH, name = "Create Title on 300 score",
        description = "Create title on 300 score.",
        category = "Dungeons", subcategory = "Score Calculation"
    )
    var createTitleOn300Score = false

    @Property(
        type = PropertyType.PARAGRAPH, name = "300 Title Message",
        description = "Customize the message that will be sent when the score reaches 300.",
        category = "Dungeons", subcategory = "Score Calculation",
        placeholder = "300"
    )
    var messageTitle300Score = ""

    @Property(
        type = PropertyType.SWITCH, name = "Blood Camp Helper",
        description = "Draws an outline where blood mobs spawn in after spinning as armor stands.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var bloodHelper = false

    @Property(
        type = PropertyType.COLOR, name = "Blood Camp Helper Color",
        description = "Changes the color of the outline.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var bloodHelperColor: Color = Color.RED

    @Property(
        type = PropertyType.SWITCH, name = "Box Starred Mobs",
        description = "Draws the bounding box for Starred Mobs.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var boxStarredMobs = false

    @Property(
        type = PropertyType.SWITCH, name = "Box Skeleton Masters",
        description = "Draws the bounding box for Skeleton Masters.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var boxSkeletonMasters = false

    @Property(
        type = PropertyType.SWITCH, name = "Box Spirit Bear",
        description = "Draws the bounding box for Spirit Bears.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var boxSpiritBears = false

    @Property(
        type = PropertyType.SWITCH, name = "Box Spirit Bow",
        description = "Draws a box around the Spirit Bow.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var boxSpiritBow = false

    @Property(
        type = PropertyType.NUMBER, name = "Dungeon Chest Reroll Confirmation",
        description = "Requires you to click multiple times in order to reroll a chest.",
        category = "Dungeons", subcategory = "Quality of Life",
        max = 5
    )
    var kismetRerollConfirm = 0

    @Property(
        type = PropertyType.NUMBER, name = "Dungeon Chest Reroll Protection Threshold",
        description = "Prevents rerolling if the value of the items is higher than this value in millions.",
        category = "Dungeons", subcategory = "Quality of Life",
        max = 1000
    )
    var kismetRerollThreshold = 0

    @Property(
        type = PropertyType.SWITCH, name = "Ghost Leap Names",
        description = "Shows names next to the heads on the Ghost Leap menu.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var ghostTeleportMenuNames = false


    @Property(
        type = PropertyType.SWITCH, name = "Hide Archer Bone Passive",
        description = "Hides the archer bone shield passive.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideArcherBonePassive = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Damage In Boss",
        description = "Removes damage numbers while in a boss fight. Requires the custom damage splash to be enabled.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideDamageInBoss = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Wither King Dragon Death",
        description = "Removes the dragon death animation from the Master Mode 7 boss fight.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideWitherKingDragonDeath = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Fairies",
        description = "Hides the fairies in dungeons.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideFairies = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Floor 4 Crowd Messages",
        description = "Hides the messages from the Crowd on Floor 4.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideF4Spam = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Oruo Messages",
        description = "Hides the messages from Oruo during the Trivia.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideOruoMessages = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Spirit Animal Nametags",
        description = "Removes the nametags above spirit animals on Floor 4.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideF4Nametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Terminal Completion Titles",
        description = "Removes the title that shows up when a terminal is completed.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideTerminalCompletionTitles = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Wither Miner Nametags",
        description = "Removes the nametags above Wither Miners on Floor 7.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideWitherMinerNametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Terracotta Nametags",
        description = "Hides the nametags of the Terracotta while in Dungeons",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideTerracotaNametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Non-Starred Mobs Nametags",
        description = "Hides the nametags of non-starred mobs while in Dungeons",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideNonStarredNametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Larger Bat Models",
        description = "Increases the size of bat models.\nThe hitbox of the bat may be offset from what is shown.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var biggerBatModels = false

    @Property(
        type = PropertyType.SWITCH, name = "Change Hurt Color on the Wither King's Dragons",
        description = "Reduces the tinting on hurting the wither king's dragons.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var changeHurtColorOnWitherKingsDragons = false

    @Property(
        type = PropertyType.SWITCH, name = "Retexture Wither King's Dragons",
        description = "Retextures the dragons in Master Mode 7 to their respective colors.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var retextureWitherKingsDragons = false

    @Property(
        type = PropertyType.SWITCH, name = "Revive Stone Names",
        description = "Shows names next to the heads on the Revive Stone menu.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var reviveStoneNames = false

    @Property(
        type = PropertyType.SWITCH, name = "Say Blaze Done",
        description = "Says in chat when blaze is done.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var sayBlazeDone = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Bat Hitboxes",
        description = "Draws the outline of a bat's bounding box.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var showBatHitboxes = false

    @Property(
        type = PropertyType.SWITCH, name = "Color Brewing Stands",
        description = "Color brewing stands if they are done or not.",
        category = "Miscellaneous", subcategory = "Brewing"
    )
    var colorBrewingStands = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Dungeon Floor as Stack Size",
        description = "Shows the dungeon floor as the stack size.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showDungeonFloorAsStackSize = false

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Held Item Scale",
        description = "Changes the size of your held item.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var itemScale = 1f

    @Property(
        type = PropertyType.SWITCH, name = "Show Giant HP",
        description = "Shows the HP of Giants in your HUD.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var showGiantHP = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Giant HP at Feet",
        description = "Shows the HP of giants' at the giant's feet.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var showGiantHPAtFeet = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Guardian Respawn Timer",
        description = "Shows the respawn timer for the Guardians in Floor 3.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var showGuardianRespawnTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Wither King Statue Box",
        description = "Draws a box around the Wither King Statues.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var showWitherKingStatueBox = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Sadan's Interest",
        description = "Replace Sadan's interest display with Skytils' own.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var showSadanInterest = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Terracotta Respawn Time",
        description = "Displays a timer until Terracotta respawn",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var terracottaRespawnTimer = false

    @Property(
        type = PropertyType.SELECTOR, name = "Show Necron's HP",
        description = "Shows additional info about Necron's health.",
        category = "Dungeons", subcategory = "Quality of Life",
        options = ["None", "HP", "Percentage Health"]
    )
    var necronHealth = 0

    @Property(
        type = PropertyType.SWITCH, name = "Show Wither King's Dragons' Color as Text",
        description = "Displays a more clear indicator of the dragons' text to make the game more accessible.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var showWitherDragonsColorBlind = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Wither King's Dragons' HP",
        description = "Displays a more clear indicator of the dragons' HP.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var showWitherKingDragonsHP = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Wither King's Dragons' Spawn Timer",
        description = "Displays a timer for when the dragons are about to spawn in.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var showWitherKingDragonsSpawnTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Spirit Bear Timer",
        description = "Shows the time it takes for the Spirit Bears to spawn.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var spiritBearTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Spirit Leap Names",
        description = "Shows names next to the heads on the Spirit Leap menu.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var spiritLeapNames = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Door Opener",
        description = "Highlight the player that most recently opened a Wither Door on the spirit leap menu.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var highlightDoorOpener = false

    @Property(
        type = PropertyType.BUTTON, name = "Spirit Leap Highlights",
        description = "Highlights names and classes on the Spirit Leap menu.",
        category = "Dungeons", subcategory = "Quality of Life",
        placeholder = "Open GUI"
    )
    @Suppress("unused")
    fun spiritLeapNameButton() {
        Skytils.displayScreen = SpiritLeapNamesGui()
    }

    @Property(
        type = PropertyType.SWITCH, name = "Spirit Pet Warning",
        description = "Lets you know if someone in your party has a Spirit pet equipped before the dungeon starts.\n§cYou must have pet visibility on in Skyblock.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var spiritPetWarning = false

    @Property(
        type = PropertyType.SWITCH, name = "Wither King Dragon Dimensional Slash Alert",
        description = "Creates a title when you are in range of dimensional slash.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var witherKingDragonSlashAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Wither King Dragon Spawn Alert",
        description = "Shows a message when the Wither King spawns a dragon.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var witherKingDragonSpawnAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Blaze Solver",
        description = "Changes the color of the blaze to shoot on Higher or Lower.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var blazeSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Next Blaze",
        description = "Colors the next blaze to shoot in Higher or Lower yellow.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var showNextBlaze = false

    @Property(
        type = PropertyType.COLOR, name = "Lowest Blaze Color",
        description = "Color used to highlight the lowest blaze in.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var lowestBlazeColor = Color(255, 0, 0, 200)

    @Property(
        type = PropertyType.COLOR, name = "Highest Blaze Color",
        description = "Color used to highlight the highest blaze in.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var highestBlazeColor = Color(0, 255, 0, 200)

    @Property(
        type = PropertyType.COLOR, name = "Next Blaze Color",
        description = "Color used to highlight the next blaze in.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var nextBlazeColor = Color(255, 255, 0, 200)

    @Property(
        type = PropertyType.SWITCH, name = "Boulder Solver",
        description = "§b[WIP] §rShow which boxes to move on the Boulder puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var boulderSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Creeper Beams Solver",
        description = "Shows pairs on the Creeper Beams puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var creeperBeamsSolver = false


    @Property(
        type = PropertyType.SWITCH, name = "Ice Fill Solver",
        description = "§b[WIP] §rShows the path to take on the Ice Fill puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var iceFillSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Ice Path Solver",
        description = "Show the path for the silverfish to follow on the Ice Path puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var icePathSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Teleport Maze Solver",
        description = "Shows which pads you've stepped on, and which pads to step on in the Teleport Maze puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var teleportMazeSolver = false

    @Property(
        type = PropertyType.COLOR, name = "Teleport Maze Solver Color",
        description = "Color of the thing that shows which pads you've stepped on in the Teleport Maze puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var teleportMazeSolverColor = Color(255, 0, 0, 200)

    @Property(
        type = PropertyType.SWITCH, name = "Three Weirdos Solver",
        description = "Shows which chest to click in the Three Weirdos puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var threeWeirdosSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Tic Tac Toe Solver",
        description = "§b[WIP] §rDisplays the best move on the Tic Tac Toe puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var ticTacToeSolver = false

    @Property(
        type = PropertyType.COLOR, name = "Tic Tac Toe Solver Color",
        description = "Color of the thing that displays the best move on the Tic Tac Toe puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var ticTacToeSolverColor = Color(23, 234, 99, 204)

    @Property(
        type = PropertyType.SWITCH, name = "Trivia Solver",
        description = "Shows the correct answer for the questions on the Trivia puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var triviaSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Water Board Solver",
        description = "§b[WIP] §rDisplays which levers to flip for the Water Board puzzle.",
        category = "Dungeons", subcategory = "Solvers"
    )
    var waterBoardSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Find correct Livid",
        description = "Shows the hp of the correct livid on F5 and M5",
        category = "Dungeons", subcategory = "Solvers"
    )
    var findCorrectLivid = false

    @Property(
        type = PropertyType.SELECTOR, name = "Type of Livid Finder",
        category = "Dungeons", subcategory = "Solvers",
        options = ["Block", "Entity"]
    )
    var lividFinderType = 0

    @Property(
        type = PropertyType.SWITCH, name = "Boxed Tanks",
        description = "Shows the bounding box of all tanks through walls.",
        category = "Dungeons", subcategory = "Tank Helper Tools"
    )
    var boxedTanks = false

    @Property(
        type = PropertyType.COLOR, name = "Boxed Tank Color",
        description = "Choose the color of the tanks in the bounding box",
        category = "Dungeons", subcategory = "Tank Helper Tools"
    )
    var boxedTankColor = Color(0, 255, 0)

    @Property(
        type = PropertyType.SWITCH, name = "Box Protected Teammates",
        description = "Shows the bounding box of protected teammates through walls.",
        category = "Dungeons", subcategory = "Tank Helper Tools"
    )
    var boxedProtectedTeammates = false

    @Property(
        type = PropertyType.COLOR, name = "Protected Teammate Box Color",
        description = "Choose the color of the teammates in the bounding box",
        category = "Dungeons", subcategory = "Tank Helper Tools"
    )
    var boxedProtectedTeammatesColor = Color(255, 0, 0)

    @Property(
        type = PropertyType.SWITCH, name = "Tank Protection Range Display",
        description = "Shows the range in which players will be protected by a tank.",
        category = "Dungeons", subcategory = "Tank Helper Tools"
    )
    var showTankRadius = false

    @Property(
        type = PropertyType.SWITCH, name = "Tank Range Wall",
        description = "Shows the range as a wall instead of a circle.",
        category = "Dungeons", subcategory = "Tank Helper Tools"
    )
    var showTankRadiusWall = true

    @Property(
        type = PropertyType.COLOR, name = "Tank Range Wall Color",
        description = "The color to display the Tank Range as.",
        category = "Dungeons", subcategory = "Tank Helper Tools"
    )
    var tankRadiusDisplayColor = Color(100, 255, 0, 50)

    @Property(
        type = PropertyType.SWITCH, name = "Middle Click on Terminals",
        description = "Replaces left clicks while on terminals with middle clicks.",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var middleClickTerminals = true

    @Property(
        type = PropertyType.SWITCH, name = "Change All to Same Color Solver",
        description = "Shows the best path of clicks for the Change All to Same Color Terminal.",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var changeAllSameColorTerminalSolver = false

    @Property(
        type = PropertyType.SELECTOR, name = "Change All to Same Color Solver Mode",
        description = "Changes the display mode of the solver.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        options = ["Normal", "LMB only"]
    )
    var changeToSameColorMode = 0

    @Property(
        type = PropertyType.SWITCH, name = "Click in Order Solver",
        description = "Shows the items to click on the Click in Order terminal.",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var clickInOrderTerminalSolver = false

    @Property(
        type = PropertyType.COLOR, name = "Click in Order First Color",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var clickInOrderFirst = Color(2, 62, 138, 255)

    @Property(
        type = PropertyType.COLOR, name = "Click in Order Second Color",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var clickInOrderSecond = Color(65, 102, 245, 255)

    @Property(
        type = PropertyType.COLOR, name = "Click in Order Third Color",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var clickInOrderThird = Color(144, 224, 239, 255)

    @Property(
        type = PropertyType.SWITCH, name = "Select All Colors Solver",
        description = "Shows the items to click on the Select All Color terminal.",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var selectAllColorTerminalSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Starts With Sequence Solver",
        description = "Shows the items to click on the What starts with? terminal.",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var startsWithSequenceTerminalSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Item Frame Alignment Solver",
        description = "Shows the amount of clicks needed on the device in Floor 7.",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var alignmentTerminalSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Shoot the Target Solver",
        description = "Shows all the shot blocks on the device in Floor 7.",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var shootTheTargetSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Simon Says Solver",
        description = "Show which buttons to press on the Simon Says device in Floor 7.\n§cIf a teammate clicks a button it will not register.",
        category = "Dungeons", subcategory = "Terminal Solvers"
    )
    var simonSaysSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Display Jerry Perks",
        description = "Displays the perks for Jerry.\nYou must visit Jerry in order for the display to function correctly.",
        category = "Events", subcategory = "Mayor Jerry"
    )
    var displayJerryPerks = false

    @Property(
        type = PropertyType.SWITCH, name = "Hidden Jerry Alert",
        description = "Displays an alert when you find a hidden Jerry.",
        category = "Events", subcategory = "Mayor Jerry"
    )
    var hiddenJerryAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Hidden Jerry Timer",
        description = "Displays a timer from when you last discovered a Hidden Jerry.",
        category = "Events", subcategory = "Mayor Jerry"
    )
    var hiddenJerryTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Track Mayor Jerry Items",
        description = "Tracks the amount of each type of Jerry that you've found, as well as drops obtained from Jerry Boxes.",
        category = "Events", subcategory = "Mayor Jerry"
    )
    var trackHiddenJerry = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Griffin Burrows",
        description = "Uses particles to locate nearby burrows.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Burrow", "Borrow", "Waypoint", "ESP"]
    )
    var showGriffinBurrows = false

    @Property(
        type = PropertyType.COLOR, name = "Empty/Start Burrow Color",
        description = "Color used to highlight the Griffin Burrows in.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Burrow", "Borrow"]
    )
    var emptyBurrowColor = Color(173, 216, 230)

    @Property(
        type = PropertyType.COLOR, name = "Mob Burrow Color",
        description = "Color used to highlight the Griffin Burrows in.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Burrow", "Borrow"]
    )
    var mobBurrowColor = Color(173, 216, 230)

    @Property(
        type = PropertyType.COLOR, name = "Treasure Burrow Color",
        description = "Color used to highlight the Griffin Burrows in.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Burrow", "Borrow"]
    )
    var treasureBurrowColor = Color(173, 216, 230)

    @Property(
        type = PropertyType.SWITCH, name = "Broadcast Rare Drop Notifications",
        description = "Sends rare drop notification when you obtain a rare drop from a Mythological Creature.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Tracker"]
    )
    var broadcastMythCreatureDrop = false

    @Property(
        type = PropertyType.SWITCH, name = "Display Gaia Construct Hits",
        description = "Tracks the amount of times a Gaia Construct has been hit.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth"]
    )
    var trackGaiaHits = false

    /*    @Property(
        type = PropertyType.SWITCH, name = "Hide Leftover Bleeds",
        description = "Removes the bleeds text left behind when a player dies to a Minotaur.",
        category = "Events", subcategory = "Mythological"
    )*/
    var removeLeftOverBleeds = false

    @Property(
        type = PropertyType.SWITCH, name = "Track Mythological Creatures",
        description = "Tracks and saves drops from Mythological Creatures.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Tracker"]
    )
    var trackMythEvent = false

    @Property(
        type = PropertyType.SWITCH, name = "Trick or Treat Chest Alert",
        description = "Displays a title when any trick or treat chest spawns near you.",
        category = "Events", subcategory = "Spooky",
        searchTags = ["Spooky Chest", "Spooky"]
    )
    var trickOrTreatChestAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Shiny Orb Waypoints",
        description = "Creates a waypoint of where your shiny orbs are",
        category = "Events", subcategory = "Technoblade"
    )
    var shinyOrbWaypoints = false

    @Property(
        type = PropertyType.SWITCH, name = "Plot Cleanup Helper",
        description = "Makes flowers and grass more visible by rendering them as sponges.",
        category = "Farming", subcategory = "Garden"
    )
    var gardenPlotCleanupHelper = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Sam's Scythe Blocks",
        description = "Shows the blocks which will be broken when using Sam's Scythe or Garden Scythe.",
        category = "Farming", subcategory = "Garden"
    )
    var showSamScytheBlocks = false

    @Property(
        type = PropertyType.COLOR, name = "Color of Sam's Scythe Marked Blocks",
        description = "Sets the color of the highlighted blocks.",
        category = "Farming", subcategory = "Garden"
    )
    var samScytheColor = Color(255, 0, 0, 50)

    @Property(
        type = PropertyType.SWITCH, name = "Visitor Offer Helper",
        description = "Displays information about visitor offers.",
        category = "Farming", subcategory = "Garden"
    )
    var visitorOfferHelper = false

    @Property(
        type = PropertyType.SWITCH, name = "Visitor Notifications",
        description = "Sends a message in chat when a visitor is at your garden.",
        category = "Farming", subcategory = "Garden"
    )
    var visitorNotifications = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Farming RNG Titles",
        description = "Removes the titles that show up after getting a drop with Pumpkin Dicer / Melon Dicer",
        category = "Farming", subcategory = "Quality of Life"
    )
    var hideFarmingRNGTitles = false

    @Property(
        type = PropertyType.SWITCH, name = "Hungry Hiker Solver",
        description = "Tells you what item the Hungry Hiker wants.",
        category = "Farming", subcategory = "Solvers"
    )
    var hungryHikerSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Treasure Hunter Solver",
        description = "Tells you where the Treasure Hunter's treasure is.",
        category = "Farming", subcategory = "Solvers"
    )
    var treasureHunterSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Click to Accept Trapper Task",
        description = "Open chat, then click anywhere on screen to accept Trapper Task.",
        category = "Farming", subcategory = "Quality of Life"
    )
    var acceptTrapperTask = true

    @Property(
        type = PropertyType.SWITCH, name = "Trapper Cooldown Alarm",
        description = "Quickly plays five notes once the Trapper is off cooldown.",
        category = "Farming", subcategory = "Quality of Life"
    )
    var trapperPing = false

    @Property(
        type = PropertyType.SWITCH, name = "Talbot's Theodolite Helper",
        description = "Shows Y coordinate bounds based on Talbot's Theodolite output",
        category = "Farming", subcategory = "Quality of Life"
    )
    var talbotsTheodoliteHelper = false

    @Property(
        type = PropertyType.TEXT, name = "Kuudra Auto-Reparty Player",
        description = "Automatically performs a reparty upon joining a Kuudra bossfight. Leave this blank to disable.",
        category = "Kuudra", subcategory = "Quality of Life"
    )
    var kuudraAutoRepartyPlayer = ""

    @Property(
        type = PropertyType.SWITCH, name = "Dark Mode Mist",
        description = "Replaces colors in The Mist with darker variants.",
        category = "Mining", subcategory = "Quality of Life"
    )
    var darkModeMist = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Completed Commissions",
        description = "Marks completed commissions in the menu with a red background.",
        category = "Mining", subcategory = "Quality of Life"
    )
    var highlightCompletedCommissions = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Disabled HOTM Perks",
        description = "Marks disabled perks in the menu with a red background.",
        category = "Mining", subcategory = "Quality of Life"
    )
    var highlightDisabledHOTMPerks = false

    @Property(
        type = PropertyType.SWITCH, name = "More Visible Ghosts",
        description = "Makes ghosts more visible in the Dwarven Mines.\nThis is allowed on the Hypixel network and can be done in Vanilla.",
        category = "Mining", subcategory = "Quality of Life"
    )
    var moreVisibleGhosts = false

    @Property(
        type = PropertyType.SWITCH, name = "Powder Ghast Ping",
        description = "Displays a title on your screen when a Powder Ghast spawns.",
        category = "Mining", subcategory = "Quality of Life"
    )
    var powerGhastPing = false

    @Property(
        type = PropertyType.SWITCH, name = "Raffle Warning",
        description = "Displays a title on your screen 15 seconds from the ending of the raffle.",
        category = "Mining", subcategory = "Quality of Life"
    )
    var raffleWarning = false

    @Property(
        type = PropertyType.SWITCH, name = "Raffle Waypoint",
        description = "Displays a waypoint on your screen to the raffle box after you deposit a ticket.",
        category = "Mining", subcategory = "Quality of Life"
    )
    var raffleWaypoint = false

    @Property(
        type = PropertyType.SWITCH, name = "Recolor Carpets",
        description = "Changes the color of carpets in the Dwarven Mines to red.",
        category = "Mining", subcategory = "Quality of Life"
    )
    var recolorCarpets = false

    @Property(
        type = PropertyType.SWITCH, name = "Skymall Reminder",
        description = "Reminds you every Skyblock day to check your Skymall perk while in the Dwarven Mines.",
        category = "Mining", subcategory = "Quality of Life"
    )
    var skymallReminder = false

    @Property(
        type = PropertyType.SWITCH, name = "Fetchur Solver",
        description = "Tells you what item Fetchur wants.",
        category = "Mining", subcategory = "Solvers"
    )
    var fetchurSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Puzzler Solver",
        description = "Shows which block to mine for Puzzler.",
        category = "Mining", subcategory = "Solvers"
    )
    var puzzlerSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows Death Waypoints",
        description = "Drops a waypoint to where you last died in the Crystal Hollows.",
        category = "Mining", subcategory = "Crystal Hollows"
    )
    var crystalHollowDeathWaypoint = false

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows map",
        description = "Shows a map to see in which part of the crystal hollows you are and saves locations of special places.",
        category = "Mining", subcategory = "Crystal Hollows"
    )
    var crystalHollowMap = false

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows map special places",
        description = "Show special places on the map (like Lost Precusor City).",
        category = "Mining", subcategory = "Crystal Hollows"
    )
    var crystalHollowMapPlaces = false

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows waypoints",
        description = "Shows waypoints to special places inside the Crystal Hollows.",
        category = "Mining", subcategory = "Crystal Hollows"
    )
    var crystalHollowWaypoints = false

    @Property(
        type = PropertyType.SWITCH, name = "King Yolkar waypoint",
        description = "Adds a waypoint for King Yolkar upon interacting with him",
        category = "Mining", subcategory = "Crystal Hollows"
    )
    var kingYolkarWaypoint = false

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows chat coordinates grabber",
        description = "When coordinates are shared in chat asks which one it is and displays a waypoint there and shows it on the map.",
        category = "Mining", subcategory = "Crystal Hollows"
    )
    var hollowChatCoords = false

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows Treasure Helper",
        description = "Helps you open treasure chests in the Crystal Hollows.",
        category = "Mining", subcategory = "Crystal Hollows"
    )
    var chTreasureHelper = false

    @Property(
        type = PropertyType.SWITCH, name = "Chat Tabs",
        description = "Creates various tabs to organize chat.",
        category = "Miscellaneous", subcategory = "Chat Tabs"
    )
    var chatTabs = false

    @Property(
        type = PropertyType.SWITCH, name = "Pre-fill Chat Commands",
        description = "Auto fills the respective command for each tab.",
        category = "Miscellaneous", subcategory = "Chat Tabs"
    )
    var preFillChatTabCommands = false

    @Property(
        type = PropertyType.SWITCH, name = "Auto Switch Chat Channel",
        description = "Automatically types the command to switch to a certain channel.",
        category = "Miscellaneous", subcategory = "Chat Tabs"
    )
    var autoSwitchChatChannel = false

    @Property(
        type = PropertyType.SWITCH, name = "Copy Chat Messages",
        description = "Copy chat messages with control + click.",
        category = "Miscellaneous", subcategory = "Chat Tabs"
    )
    var copyChat = false

    @Property(
        type = PropertyType.SWITCH, name = "Boss Bar Fix",
        description = "Hides the Witherborn boss bars.",
        category = "Miscellaneous", subcategory = "Fixes"
    )
    var bossBarFix = true

    @Property(
        type = PropertyType.SWITCH, name = "Fix Falling Sand Rendering",
        description = "Adds a check to rendering in order to prevent crashes.",
        category = "Miscellaneous", subcategory = "Fixes"
    )
    var fixFallingSandRendering = false

    @Property(
        type = PropertyType.SWITCH, name = "Fix World Time",
        description = "Fixes world time on other mods being messed up due to certain mods.",
        category = "Miscellaneous", subcategory = "Fixes"
    )
    var fixWorldTime = false

    @Property(
        type = PropertyType.SWITCH, name = "Prevent Log Spam",
        description = "Prevents your logs from being spammed with exceptions while on Hypixel.",
        category = "Miscellaneous", subcategory = "Fixes"
    )
    var preventLogSpam = true

    @Property(
        type = PropertyType.SWITCH, name = "Twitch Fix",
        description = "Fix twitch stuff.",
        category = "Miscellaneous", subcategory = "Fixes"
    )
    var twitchFix = true

    @Property(
        type = PropertyType.SWITCH, name = "Price Paid",
        description = "Records and shows the price you paid for certain items.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var pricePaid = false

    @Property(
        type = PropertyType.SWITCH, name = "Block Zapper Fatigue Timer",
        description = "Displays how long your block zapper is fatigued for.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var blockZapperFatigueTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Block Animation",
        description = "Removes the block animation on swords.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var disableBlockAnimation = false

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Dropped Item Size",
        description = "Change the size of dropped items.",
        category = "Miscellaneous", subcategory = "Items",
        maxF = 5f,
        decimalPlaces = 2
    )
    var itemDropScale = 1f

    @Property(
        type = PropertyType.SWITCH, name = "Hide Implosion Particles",
        description = "Removes the explosion created by the Implosion ability.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var hideImplosionParticles = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Midas Staff Gold",
        description = "Prevents the gold blocks from Molten Wave from rendering, leaving only the particles.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var hideMidasStaffGoldBlocks = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Filled Bazaar Orders",
        description = "Highlights 100%% filled orders in the bazaar.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var highlightFilledBazaarOrders = false

    @Property(
        type = PropertyType.SWITCH, name = "Item Cooldown Display",
        description = "Displays the cooldowns for your items. Items must be whitelisted with the /trackcooldown command.",
        category = "Miscellaneous", subcategory = "Items",
        searchTags = ["Wither Impact", "Hyperion", "Wither Shield"]
    )
    var itemCooldownDisplay = false

    @Property(
        type = PropertyType.SELECTOR, name = "Item Stars Display",
        description = "Changes the way Item Stars are displayed on Items.",
        category = "Miscellaneous", subcategory = "Items",
        options = ["Normal", "Old", "Compact"],
        searchTags = ["1.3.0-pre4"]
    )
    var starDisplayType = 0

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Head Display Size",
        description = "Change the size of heads in your inventory.",
        category = "Miscellaneous", subcategory = "Items",
        maxF = 2f,
        decimalPlaces = 2
    )
    var largerHeadScale = 1f

    @Property(
        type = PropertyType.SWITCH, name = "Prevent Placing Weapons",
        description = "Stops the game from trying to place the Flower of Truth, Moody Grappleshot, Spirit Sceptre, and Weird Tuba items.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var preventPlacingWeapons = false

    @Property(
        type = PropertyType.SWITCH, name = "Wither Shield Cooldown Tracker",
        description = "Displays the cooldowns for your wither shield (because apparently people can't follow directions)",
        category = "Miscellaneous", subcategory = "Items",
        searchTags = ["Wither Impact", "Hyperion", "Wither Shield", "Scylla", "Astraea", "Valkyrie"]
    )
    var witherShieldCooldown = false

    @Property(
        type = PropertyType.SWITCH, name = "Wither Shield has Wither Impact",
        description = "Get better tbh imagine only 1 scroll",
        category = "Miscellaneous", subcategory = "Items",
        searchTags = ["Wither Impact", "Hyperion", "Wither Shield", "Scylla", "Astraea", "Valkyrie"]
    )
    var assumeWitherImpact = true

    @Property(
        type = PropertyType.SWITCH, name = "Show Enchanted Book Abbreviation",
        description = "Shows the abbreviated name of books with only 1 enchantment.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showEnchantedBookAbbreviation = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Attribute Shard Abbreviation",
        description = "Shows the abbreivated name of shards with only 1 enchantment.",
        category = "Miscellaneous", subcategory = "Items",
        searchTags = ["1.3.0-pre1"]
    )
    var showAttributeShardAbbreviation = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Attribute Shard Level",
        description = "Shows the level of Attribute Shards as the stack size.",
        category = "Miscellaneous", subcategory = "Items",
        searchTags = ["1.3.0-pre1"]
    )
    var showAttributeShardLevel = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Enchanted Book Tier",
        description = "Shows the tier of books with only 1 enchantment.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showEnchantedBookTier = false

    @Property(
        type = PropertyType.SWITCH, name = "Combine Helper",
        description = "Shows if you're combining incompatible books or attribute shards",
        category = "Miscellaneous", subcategory = "Items"
    )
    var combineHelper = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Etherwarp Teleport Position",
        description = "Shows the block you will teleport to with the Etherwarp Transmission ability.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showEtherwarpTeleportPos = false

    @Property(
        type = PropertyType.COLOR, name = "Etherwarp Teleport Position Color",
        description = "Color the thing that shows the block you will teleport to with the Etherwarp Transmission ability.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showEtherwarpTeleportPosColor = Color(0, 0, 255, 69)

    @Property(
        type = PropertyType.SWITCH, name = "Show Gemstones",
        description = "Shows the added gemstones on items.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showGemstones = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Head Floor Number",
        description = "Shows the corresponding floor number for Diamond/Gold Catacombs Heads.",
        category = "Miscellaneous", subcategory = "Items",
        searchTags = ["Dungeons"]
    )
    var showHeadFloorNumber = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Item Origin",
        description = "Shows the origin on items.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showOrigin = false

    @Property(
        type = PropertyType.SWITCH, name = "Show NPC Sell Price",
        description = "Shows the NPC Sell Price on certain items.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showNPCSellPrice = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Potion Tier",
        description = "Shows the tier of potions as the stack size.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showPotionTier = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Pet Candies",
        description = "Shows the number of candies used as the stack size",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showPetCandies = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Item Star Count",
        description = "Shows the amount of stars on items as the stack size.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showStarCount = false

    @Property(
        type = PropertyType.SWITCH, name = "Stacking Enchant Progress Display",
        description = "Displays the progress for the held item's stacking enchant.",
        category = "Miscellaneous", subcategory = "Items"
    )
    var stackingEnchantProgressDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Radioactive Bonus",
        description = "Shows the current Critical Damage bonus from Tarantula helmet",
        category = "Miscellaneous", subcategory = "Items"
    )
    var showRadioactiveBonus = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Item Rarity",
        description = "Shows the rarity of an item in the color",
        category = "Miscellaneous", subcategory = "Item Rarity"
    )
    var showItemRarity = false

    @Property(
        type = PropertyType.SELECTOR, name = "Item Rarity Shape",
        description = "Select the shape of the item rarity's background.\n" +
                "§cCustom is made for Texture Pack makers, the png must be named customrarity.png.\n" +
                "§cDon't use it if you don't know what you are doing",
        category = "Miscellaneous", subcategory = "Item Rarity",
        options = ["Circle", "Square", "Square Outline", "Outline", "Custom", "Item Outline"]
    )
    var itemRarityShape = 0

    @Property(
        type = PropertyType.SWITCH, name = "Show Pet Rarity",
        description = "Shows the rarity of a pet in the color",
        category = "Miscellaneous", subcategory = "Item Rarity"
    )
    var showPetRarity = false

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Item Rarity Transparency",
        description = "How opaque the rarity color will be",
        category = "Miscellaneous", subcategory = "Item Rarity"
    )
    var itemRarityOpacity = 0.75f

    @Property(
        type = PropertyType.SWITCH, name = "Only Collect Enchanted Items",
        description = "Prevents you from collecting unenchanted items from minions if there is a Super Compactor.",
        category = "Miscellaneous", subcategory = "Minions"
    )
    var onlyCollectEnchantedItems = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Minion Tier",
        description = "Shows the tier of minions as the stack size.",
        category = "Miscellaneous", subcategory = "Minions"
    )
    var showMinionTier = false

    @Property(
        type = PropertyType.SWITCH, name = "Always Show Item Name Highlight",
        description = "Always shows the item name highlight.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var alwaysShowItemHighlight = false

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Low Health Vignette Threshold",
        description = "Render a red vignette on the edge of the screen when your health drops below this threshold. Set to 0.0%% to disable.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var lowHealthVignetteThreshold = 0.0f

    @Property(
        type = PropertyType.COLOR, name = "Low Health Vignette Color",
        description = "The color of the vignette that is shown when your health is below the threshold.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var lowHealthVignetteColor: Color = Color.RED

    @Property(
        type = PropertyType.SWITCH, name = "Hide Tooltips while on Storage",
        description = "Hides the tooltips of backpacks and ender chest while on the Storage GUI",
        category = "Miscellaneous", subcategory = "Other"
    )
    var hideTooltipsOnStorage = false

    @Property(
        type = PropertyType.SWITCH, name = "Copy Deaths to Clipboard",
        description = "Copies the deaths outside dungeons to your clipboard after clicking them in the chat.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var copyDeathToClipboard = false

    @Property(
        type = PropertyType.SWITCH, name = "Auto Copy RNG Drops to Clipboard",
        description = "Automatically copies RNG drops to your clipboard.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var autoCopyRNGDrops = false

    @Property(
        type = PropertyType.SWITCH, name = "Also Copy Very Rare Drops to Clipboard",
        description = "Automatically copies very rare drops to your clipboard.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var autoCopyVeryRareDrops = false

    @Property(
        type = PropertyType.SWITCH, name = "Dupe Tracker",
        description = "Tries to track duplicated items on the auction house.\nThis will not catch every single duped item.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var dupeTracker = false

    @Property(
        type = PropertyType.COLOR, name = "Dupe Tracker Overlay Color",
        description = "Changes the color of the Dupe Tracker Overlay.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var dupeTrackerOverlayColor = Color.BLACK.withAlpha(169)

    @Property(
        type = PropertyType.SWITCH, name = "Endstone Protector Spawn Timer",
        description = "Counts down the time until the Endstone Protector spawns.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var golemSpawnTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Players in Range Display",
        description = "Shows the amount of players within a 30 block radius.",
        category = "Miscellaneous", subcategory = "Other",
        searchTags = ["Dolphin", "Legion", "Bobbin' Time"]
    )
    var playersInRangeDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Placed Summoning Eye Display",
        description = "Shows the amount of summoning eyes placed in the Dragon's Nest.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var summoningEyeDisplay = false

    @Property(
        type = PropertyType.SELECTOR, name = "Ping Display",
        description = "Shows your ping to the current server, similar to the /skytils ping command.\nYou must be in a GUI or not moving in order to queue a ping.\nThere is a tiny chance that this will cause you to be punished.",
        category = "Miscellaneous", subcategory = "Other",
        options = ["Off", "Server List", "Packet"]
    )
    var pingDisplay = 0

    @Property(
        type = PropertyType.SWITCH, name = "Random Stuff",
        description = "Random stuff that may or may not increase your FPS.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var randomStuff = false

    @Property(
        type = PropertyType.SWITCH, name = "Scam Check",
        description = "Check if the other party is a known scammer when trading.\nThis relies on databases not controlled by Skytils and may not contain all scammers.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var scamCheck = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Bestiary Level",
        description = "Shows the bestiary level as the stack size.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var showBestiaryLevel = false

    @Property(
        PropertyType.SWITCH, name = "Show Selected Arrow",
        description = "Shows your current selected arrow.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var showSelectedArrowDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Show World Age",
        description = "Displays the day count of the current server.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var showWorldAge = false

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Transparent Armor Layer",
        description = "Changes the transparency of your armor layer.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var transparentArmorLayer = 1f

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Head Layer Transparency",
        description = "Changes the transparency of your head layer.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var transparentHeadLayer = 1f

    @Property(
        type = PropertyType.SWITCH, name = "Fix Summon Skin",
        description = "§c[WIP] §rChanges the summon's skin to the correct one.\n§cThis is very broken and may crash your game.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var fixSummonSkin = false

    @Property(
        type = PropertyType.SWITCH, name = "Use Player Skin",
        description = "Uses the player's skin for necromancy mobs.",
        category = "Miscellaneous", subcategory = "Other"
    )
    var usePlayerSkin = false

    @Property(
        type = PropertyType.SWITCH, name = "Custom Auction Price Input",
        description = "Displays Skytils' own auction input GUI instead of a sign.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var betterAuctionPriceInput = false

    @Property(
        type = PropertyType.SWITCH, name = "Better Stash",
        description = "Shows an improved view of your stash.\n&cWARNING: This feature runs /viewstash upon receiving a stash message.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var betterStash = false

    @Property(
        type = PropertyType.SWITCH, name = "Container Sell Value",
        description = "Display the lowest BIN prices for the most valuable items in backpacks, ender chest pages, minions, and island chests.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var containerSellValue = false

    @Property(
        type = PropertyType.SWITCH, name = "Include Item Modifiers",
        description = "Includes potato books, recombobulators, enchantments, and master stars in the item price calculations.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var includeModifiersInSellValue = true

    @Property(
        type = PropertyType.NUMBER, name = "Max Displayed Items",
        description = "The maximum amount of items to display in the Container Sell Value GUI.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        min = 5, max = 30, increment = 1
    )
    var containerSellValueMaxItems = 20

    @Property(
        type = PropertyType.SELECTOR, name = "Custom Damage Splash Style",
        description = "§b[WIP] §rReplaces Skyblock damage splashes with custom rendered ones.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        options = ["Off", "Comma", "Truncate"]
    )
    var customDamageSplash = 0

    @Property(
        type = PropertyType.SWITCH, name = "Disable Enderman Teleportation",
        description = "Removes the enderman teleport effect.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var disableEndermanTeleport = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Night Vision",
        description = "Removes the vanilla effects of Night Vision.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var disableNightVision = false

    @Property(
        type = PropertyType.SLIDER, name = "Dungeon Pot Lock",
        description = "Only allows you to purchase this dungeon pot from Ophelia, no other items.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        max = 7
    )
    var dungeonPotLock = 0

    @Property(
        type = PropertyType.SWITCH, name = "Enchant Glint Fix",
        description = "Fixes some items not having the enchantment glint.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var enchantGlintFix = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Absorption Hearts",
        description = "Prevents the game from rendering absorption hearts.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideAbsorption = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Air Display",
        description = "Prevents the game from rendering the air bubbles while underwater.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideAirDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Armor Display",
        description = "Prevents the game from rendering the vanilla armor points.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideArmorDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Cheap Coins",
        description = "Prevents the game from rendering cheap coins.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideCheapCoins = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Dying Mobs",
        description = "Removes dead/dying mobs from your screen.",
        category = "Dungeons", subcategory = "Quality of Life"
    )
    var hideDyingMobs = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Fire on Entities",
        description = "Prevents the game from rendering fire on burning entities.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideEntityFire = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Fishing Hooks",
        description = "Hides fishing hooks from other players",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideFishingHooks = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Hunger Display",
        description = "Prevents the game from rendering the vanilla hunger points.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideHungerDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Jerry Rune",
        description = "Prevents the game from rendering the items spawned by the Jerry rune.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideJerryRune = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Lightning",
        description = "Prevents all lightning from rendering.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideLightning = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Mob Death Particles",
        description = "Hides the smoke particles created when mobs die.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideDeathParticles = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Pet Health Display",
        description = "Hides the Vanilla pet hearts.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hidePetHealth = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Players in Spawn",
        description = "Hides players in the spawn area at the Hub.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hidePlayersInSpawn = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Potion Effects in Inventory",
        description = "Prevents the game from rendering the potion effects in inventories while in Skyblock.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hidePotionEffects = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Scoreboard Score",
        description = "Removes the red score numbers on the scoreboard.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideScoreboardScore = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Vanilla Health Display",
        description = "Prevents the game from rendering the vanilla heart points.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var hideHealthDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Disabled Potion Effects",
        description = "Marks disabled potion effects in the toggle menu.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var highlightDisabledPotionEffects = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Salvageable Items",
        description = "Highlights items that can be salvaged.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var highlightSalvageableItems = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Dungeon-Sellable Items",
        description = "Highlights dungeon-sellable items such as training weights in Ophelia NPC or Trades menu.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var highlightDungeonSellableItems = false

    @Property(
        type = PropertyType.SWITCH, name = "Lower Enderman Nametags",
        description = "Lowers the health and nametag of endermen so it's easier to see.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var lowerEndermanNametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Middle Click GUI Items",
        description = "Replaces left clicks on items with no Skyblock ID with middle clicks.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var middleClickGUIItems = false

    @Property(
        type = PropertyType.SWITCH, name = "Moveable Action Bar",
        description = "Allows you to move the action bar as if it were a Skytils HUD element.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var moveableActionBar = false

    @Property(
        type = PropertyType.SWITCH, name = "Moveable Item Name Highlight",
        description = "Allows you to move the item name highlight as if it were a Skytils HUD element.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var moveableItemNameHighlight = false

    @Property(
        type = PropertyType.SWITCH, name = "No Fire",
        description = "Removes first-person fire overlay when you are burning.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var noFire = false

    @Property(
        type = PropertyType.SWITCH, name = "No Hurtcam",
        description = "Removes the screen shake when you are hurt.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var noHurtcam = false

    @Property(
        type = PropertyType.SWITCH, name = "Party Addons",
        description = "Adds a few features to the party list.\n§eNote: Requires Hypixel Language to be set to English. §7(/lang en)",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var partyAddons = false

    @Property(
        type = PropertyType.SWITCH, name = "Prevent Cursor Reset",
        description = "Prevents the cursor from resetting to the center of the screen when you open a GUI.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var preventCursorReset = false

    @Property(
        type = PropertyType.SWITCH, name = "Prevent Moving on Death",
        description = "Unpresses all keys on death to prevent you from moving.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var preventMovingOnDeath = false

    @Property(
        type = PropertyType.SWITCH, name = "Power Orb Lock",
        description = "Prevents placing the power orb if the same or better power orb is within range.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var powerOrbLock = false

    @Property(
        type = PropertyType.NUMBER, name = "Power Orb Lock Duration",
        description = "Allows overwriting a power orb, if it has less time left than this option.",
        min = 1, max = 120,
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var powerOrbDuration = 10

    @Property(
        type = PropertyType.SWITCH, name = "Press Enter to confirm Sign Popups",
        description = "Allows pressing enter to confirm a sign popup, such as the bazaar or auction house prices.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var pressEnterToConfirmSignQuestion = false

    @Property(
        type = PropertyType.TEXT, name = "Protect Items Above Value",
        description = "Prevents you from dropping, salvaging, or selling items worth more than this value. Based on Lowest BIN price.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        searchTags = ["Lock"]
    )
    var protectItemBINThreshold = "0"

    @Property(
        type = PropertyType.SWITCH, name = "Protect Starred Items",
        description = "Prevents you from dropping, salvaging, or selling starred dungeon items.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        searchTags = ["Lock"]
    )
    var protectStarredItems = false

    @Property(
        type = PropertyType.SWITCH, name = "Spider's Den Rain Timer",
        description = "Shows the duration of rain in the Spider's Den.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var rainTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Arachne Spawn",
        description = "Shows the location of the Arachne Altar when a fragment is placed.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var showArachneSpawn = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Arachne HP",
        description = "Shows the HP of Arachne on your HUD.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var showArachneHP = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Coins per Bit",
        description = "Shows how many coins you will get per bit spent at the Community Shop.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var showCoinsPerBit = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Coins per Copper",
        description = "Shows how many coins you will get per copper spent at the SkyMart.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var showCoinsPerCopper = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Lowest BIN Price",
        description = "Shows the lowest Buy It Now price for various items in Skyblock.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var showLowestBINPrice = false

    @Property(
        type = PropertyType.SWITCH, name = "Stop Clicking Non-Salvageable Items",
        description = "Stops you from clicking Non-Salvageable items while in the Salvage menu",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var stopClickingNonSalvageable = false

    @Property(
        type = PropertyType.SWITCH, name = "View Relic Waypoints",
        description = "Shows the location of all the relics at the Spider's Den.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var relicWaypoints = false

    @Property(
        type = PropertyType.SWITCH, name = "Find Rare Relics",
        description = "Finds rare relics at the Spider's Den as you walk near them.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var rareRelicFinder = false

    @Property(
        type = PropertyType.BUTTON, name = "Reset Found Relic Waypoints",
        description = "Resets the state of all the relics at the Spider's Den.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    @Suppress("unused")
    fun resetRelicWaypoints() {
        Tracker.getTrackerById("found_spiders_den_relics")!!.doReset()
    }

    @Property(
        type = PropertyType.BUTTON, name = "Potion Duration Notifications",
        description = "Displays a notification when a Potion is about to expire.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    @Suppress("unused")
    fun openPotionEffectTimersGUI() {
        Skytils.displayScreen = PotionNotificationsGui()
    }


    @Property(
        type = PropertyType.SWITCH, name = "Stop Hook Sinking in Lava",
        description = "Stops your fishing hook from sinking in lava.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var lavaBobber = false

    @Property(
        type = PropertyType.SELECTOR, name = "Autopet Message Hider",
        description = "Removes autopet messages from your chat.",
        category = "Pets", subcategory = "Quality of Life",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideAutopetMessages = 0

    @Property(
        type = PropertyType.SWITCH, name = "Hide Pet Nametags",
        description = "Hides the nametags above pets.",
        category = "Pets", subcategory = "Quality of Life"
    )
    var hidePetNametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Active Pet",
        description = "Highlights the current active pet.",
        category = "Pets", subcategory = "Quality of Life"
    )
    var highlightActivePet = false

    @Property(
        type = PropertyType.COLOR, name = "Active Pet Highlight Color",
        description = "Color used to highlight the active pet in.",
        category = "Pets", subcategory = "Quality of Life"
    )
    var activePetColor = Color(0, 255, 0)

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Favorite Pets",
        description = "Highlight pets marked as favorite.",
        category = "Pets", subcategory = "Quality of Life"
    )
    var highlightFavoritePets = false

    @Property(
        type = PropertyType.COLOR, name = "Favorite Pet Highlight Color",
        description = "Color used to highlight the favorite pets in.",
        category = "Pets", subcategory = "Quality of Life"
    )
    var favoritePetColor = Color(0, 255, 255)

    @Property(
        type = PropertyType.SWITCH, name = "Pet Item Confirmation",
        description = "Requires a confirmation before using a pet item.",
        category = "Pets", subcategory = "Quality of Life"
    )
    var petItemConfirmation = false

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Revenant RNG Meter",
        description = "Internal value to store current Revenant RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true
    )
    var revRNG = 0f

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Tarantula RNG Meter",
        description = "Internal value to store current Tarantula RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true
    )
    var taraRNG = 0f

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Sven RNG Meter",
        description = "Internal value to store current Sven RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true
    )
    var svenRNG = 0f

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Voidgloom RNG Meter",
        description = "Internal value to store current Voidgloom Seraph RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true
    )
    var voidRNG = 0f

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Inferno RNG Meter",
        description = "Internal value to store current Inferno Demonlord RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true
    )
    var blazeRNG = 0f

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Bloodfiend RNG Meter",
        description = "Internal value to store current Riftstalker Bloodfiend RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true
    )
    var vampRNG = 0f

    @Property(
        type = PropertyType.SWITCH, name = "Click to Open Maddox Menu",
        description = "Open chat, then click anywhere on screen to open Maddox Menu.",
        category = "Slayer", subcategory = "Quality of Life"
    )
    var openMaddoxMenu = false

    @Property(
        type = PropertyType.SELECTOR, name = "Carry Mode",
        description = "Allow middle clicking to set your slayer boss.\nDisable this if you are doing your own boss.",
        category = "Slayer", subcategory = "General",
        options = ["Off", "T1", "T2", "T3", "T4", "T5"]
    )
    var slayerCarryMode = 0

    @Property(
        type = PropertyType.SWITCH, name = "Use Hits to Detect Slayer",
        description = "Finds your slayer based on the one you hit the most.",
        category = "Slayer", subcategory = "General"
    )
    var useSlayerHitMethod = true

    @Property(
        type = PropertyType.SWITCH, name = "Ping when in Atoned Horror Danger Zone",
        description = "Pings when you are standing on the Atoned Horror's TNT target.",
        category = "Slayer", subcategory = "Quality of Life"
    )
    var rev5TNTPing = false

    @Property(
        type = PropertyType.SWITCH, name = "Slayer Boss Hitbox",
        description = "Draws a box around slayer mini-bosses.",
        category = "Slayer", subcategory = "Quality of Life"
    )
    var slayerBossHitbox = false

    @Property(
        type = PropertyType.SWITCH, name = "Slayer Miniboss Spawn Alert",
        description = "Displays a title when a slayer miniboss spawns.",
        category = "Slayer", subcategory = "Quality of Life"
    )
    var slayerMinibossSpawnAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Show RNGesus Meter",
        description = "Shows your current RNGesus meter as the boss bar.",
        category = "Slayer", subcategory = "Quality of Life"
    )
    var showRNGMeter = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Slayer Armor Kills",
        description = "Displays the kills on your Final Destination Armor.",
        category = "Slayer", subcategory = "Quality of Life"
    )
    var showSlayerArmorKills = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Slayer Display",
        description = "Shows your current slayer's health and the time left",
        category = "Slayer", subcategory = "Quality of Life"
    )
    var showSlayerDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Slayer Time to Kill",
        description = "Shows the amount of time used to kill the slayer",
        category = "Slayer", subcategory = "Quality of Life"
    )
    var slayerTimeToKill = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Others' Broken Heart Radiation",
        description = "Removes Broken Heart Radiation from other slayer's while yours is spawned",
        category = "Slayer", subcategory = "Voidgloom Seraph",
    )
    var hideOthersBrokenHeartRadiation = false

    @Property(
        PropertyType.SWITCH, name = "Recolor Seraph Boss",
        description = "Changes the color of your Seraph boss based on the phase it is in.\nBeacon takes priority over the other colors.",
        category = "Slayer", subcategory = "Voidgloom Seraph"
    )
    var recolorSeraphBoss = false

    @Property(
        PropertyType.COLOR, name = "Seraph Beacon Phase Color",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        allowAlpha = false
    )
    var seraphBeaconPhaseColor = Color(255, 255, 255)

    @Property(
        PropertyType.COLOR, name = "Seraph Hits Phase Color",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        allowAlpha = false
    )
    var seraphHitsPhaseColor = Color(255, 255, 255)

    @Property(
        PropertyType.COLOR, name = "Seraph Normal Phase Color",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        allowAlpha = false
    )
    var seraphNormalPhaseColor = Color(255, 255, 255)

    @Property(
        PropertyType.SWITCH, name = "Show Seraph Display",
        description = "§b[WIP] §rShows info about your current Voidgloom Seraph boss.",
        category = "Slayer", subcategory = "Voidgloom Seraph"
    )
    var showSeraphDisplay = false


    /*    @Property(
            PropertyType.SWITCH, name = "Experimental Yang Glyph Detection",
            description = "Testing new detection for Yang Glyphs. Give us feedback on Discord!",
            category = "Slayer", subcategory = "Voidgloom Seraph"
        )*/
    var experimentalYangGlyphDetection = true

    @Property(
        PropertyType.SWITCH, name = "Yang Glyph Ping",
        description = "Alerts you when the Voidgloom Seraph throws down a Yang Glyph(beacon).",
        category = "Slayer", subcategory = "Voidgloom Seraph"
    )
    var yangGlyphPing = false

    @Property(
        PropertyType.SWITCH, name = "Yang Glyph Ping on Land",
        description = "Changes the Yang Glyph ping to ping on land rather than on throw.",
        category = "Slayer", subcategory = "Voidgloom Seraph"
    )
    var yangGlyphPingOnLand = false

    @Property(
        PropertyType.SWITCH, name = "Highlight Yang Glyph",
        description = "Highlights the Yang Glyph block.",
        category = "Slayer", subcategory = "Voidgloom Seraph"
    )
    var highlightYangGlyph = false

    @Property(
        PropertyType.SWITCH, name = "Point to Yang Glyph",
        description = "Draws an arrow in the direction of the Yang Glyph Block.",
        category = "Slayer", subcategory = "Voidgloom Seraph"
    )
    var pointYangGlyph = false

    @Property(
        PropertyType.COLOR, name = "Yang Glyph Highlight Color",
        description = "Changes the color for the Yang Glyph block",
        category = "Slayer", subcategory = "Voidgloom Seraph"
    )
    var yangGlyphColor = Color(65, 102, 245, 128)

    @Property(
        PropertyType.SWITCH, name = "Highlight Nukekebi Fixation Heads",
        description = "Draws the hitbox of Nukekebi Fixation heads",
        category = "Slayer", subcategory = "Voidgloom Seraph"
    )
    var highlightNukekebiHeads = false

    @Property(
        PropertyType.COLOR, name = "Nukekebi Fixation Head Color",
        description = "Changes the color for the Nukekebi Fixation Head Highlight",
        category = "Slayer", subcategory = "Voidgloom Seraph"
    )
    var nukekebiHeadColor = Color(65, 102, 245, 128)

    @Property(
        PropertyType.SWITCH, name = "Show Soulflow Display",
        description = "Shows your current internalized soulflow.\n" +
                "§cRequires your Soulflow battery to be in your inventory.",
        category = "Slayer", subcategory = "Voidgloom Seraph"
    )
    var showSoulflowDisplay = false

    @Property(
        PropertyType.NUMBER, name = "Low Soulflow Ping",
        description = "Alerts you when your soulflow is low.\n" +
                "§cRequires your Soulflow battery to be in your inventory.",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        min = 0,
        max = 500
    )
    var lowSoulflowPing = 0

    @Property(
        PropertyType.SWITCH, name = "Show Totem Display",
        description = "Shows the current totem's timer and hits.",
        category = "Slayer", subcategory = "Inferno Demonlord"
    )
    var showTotemDisplay = false

    @Property(
        PropertyType.NUMBER, name = "Totem Ping",
        description = "Alerts you of a specific time (seconds) on the Inferno Demonlord's Totem.",
        category = "Slayer", subcategory = "Inferno Demonlord",
        searchTags = ["1.3.0-pre1"],
        max = 8
    )
    var totemPing = 0

    @Property(
        PropertyType.SWITCH, name = "Hide Pacified Blazes",
        description = "Stops rendering faraway blazes when fighting the Inferno Demonlord if Smoldering Polarization is active.\n" +
                "Do note that you will still be able to interact with them! /skytilsupdatepotioneffects",
        category = "Slayer", subcategory = "Inferno Demonlord"
    )
    var ignorePacifiedBlazes = false

    @Property(
        type = PropertyType.SWITCH, name = "Ping when in Inferno Demonlord Fire",
        description = "Shows a warning when you are standing on Inferno Demonlord's fire.",
        category = "Slayer", subcategory = "Inferno Demonlord"
    )
    var blazeFireWarning = false

    @Property(
        PropertyType.SWITCH, name = "Recolor Demonlord Boss by Attunement",
        description = "Recolors the Inferno boss and demons depending on the correct dagger attunement.",
        category = "Slayer", subcategory = "Inferno Demonlord"
    )
    var attunementDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Vampire Slayer One Shot Alert",
        description = "Shows a title when you can one-shot the Vampire Slayer with Steak Stake",
        category = "Slayer"
    )
    var oneShotAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Twinclaw Alert",
        description = "Shows a title when the Vampire Slayer is about to do a Twinclaw attack",
        category = "Slayer"
    )
    var twinclawAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Cooldown Sounds",
        description = "Blocks the sound effect played while an item is on cooldown.",
        category = "Sounds", subcategory = "Abilities"
    )
    var disableCooldownSounds = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Jerry-chine Gun Sounds",
        description = "Blocks the villager hrmm noises that the Jerry-chine gun projectiles play.",
        category = "Sounds", subcategory = "Abilities"
    )
    var disableJerrygunSounds = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Flower of Truth Sounds",
        description = "Blocks the eating noises that the Flower of Truth plays.",
        category = "Sounds", subcategory = "Abilities"
    )
    var disableTruthFlowerSounds = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Terracotta Sounds",
        description = "Prevent the game from playing the loud sounds created by the Terracotta.",
        category = "Sounds", subcategory = "Dungeons"
    )
    var disableTerracottaSounds = false

    @Property(
        type = PropertyType.SELECTOR, name = "Text Shadow",
        description = "Changes the shadow type for the text displayed in the spam hider element.",
        category = "Spam", subcategory = "Display",
        options = ["Normal", "None", "Outline"]
    )
    var spamShadow = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Implosion Hider",
        description = "Removes Implosion messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var implosionHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Midas Staff Hider",
        description = "Removes Midas Staff messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var midasStaffHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Spirit Sceptre Hider",
        description = "Removes Spirit Sceptre messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var spiritSceptreHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Giant Sword Hider",
        description = "Removes Giant Sword messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var giantSwordHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Livid Dagger Hider",
        description = "Removes Livid Dagger messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var lividHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Ray of Hope Hider",
        description = "Removes Ray of Hope messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hopeHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Mining ability hider",
        description = "Removes Mining ability messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var miningAbilityHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Mana Use Hider",
        description = "Removes mana usage updates from the action bar.\nWorks best with SkyblockAddons.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var manaUseHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Healing Message Hider",
        description = "Removes Zombie Sword and Werewolf healing messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var healingHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Blessing Hider",
        description = "Removes blessing messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Toasts"]
    )
    var blessingHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Blood Key Hider",
        description = "Removes Blood Key messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"]
    )
    var bloodKeyHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Boss Messages Hider",
        description = "Hides Boss Messages.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideBossMessages = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Wither Essence Hider",
        description = "Removes Wither Essence unlock messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var witherEssenceHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Undead Essence Hider",
        description = "Removes Undead Essence unlock messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var undeadEssenceHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Countdown and Ready Messages Hider",
        description = "Hides the Dungeon countdown and ready messages",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideDungeonCountdownAndReady = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Dungeon Abilities Messages Hider",
        description = "Hides dungeon abilities messages and ultimates messages in chat",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideDungeonAbilities = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Mort Messages Hider",
        description = "Hides Mort's messages while in dungeons",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideMortMessages = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Superboom Pickup Hider",
        description = "Removes Superboom pickup messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"]
    )
    var superboomHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Revive Stone Pickup Hider",
        description = "Removes Revive Stone pickup messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"]
    )
    var reviveStoneHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Wither Key Hider",
        description = "Removes Wither Key messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"]
    )
    var witherKeyHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Tether Hider",
        description = "Removes Healer Tether messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"]
    )
    var tetherHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Self Orb Pickup Hider",
        description = "Removes Healer Orb messages that you pick up from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"]
    )
    var selfOrbHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Other Orb Pickup Hider",
        description = "Removes Healer Orb messages that others pick up from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"]
    )
    var otherOrbHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Trap Damage Hider",
        description = "Removes Trap Damage messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"]
    )
    var trapDamageHider = 0

    @Property(
        type = PropertyType.SLIDER, name = "Toast Time",
        description = "Number of milliseconds that toasts are displayed for.",
        category = "Spam", subcategory = "Dungeons",
        max = 10000
    )
    var toastTime = 2500

    @Property(
        type = PropertyType.SELECTOR, name = "Blocks in the way Hider",
        description = "Removes blocks in the way messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var inTheWayHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Can't use Ability Hider",
        description = "Hides the you can't use abilities in this room message ",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideCantUseAbility = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Combo Hider",
        description = "Removes combo messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI", "Toasts"]
    )
    var comboHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Auto-Recombobulator Hider",
        description = "Removes Auto-Recombobulator messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI", "Toasts"]
    )
    var autoRecombHider = 0

    @Property(
        type = PropertyType.SWITCH, name = "Compact Building Tools",
        description = "Compacts messages from the Block Zapper and the Builder's Wand.",
        category = "Spam", subcategory = "Miscellaneous"
    )
    var compactBuildingTools = false

    @Property(
        type = PropertyType.SWITCH, name = "Compact Mining Powder Gain",
        description = "Compacts messages from the chests when gaining powder",
        category = "Spam", subcategory = "Miscellaneous"
    )
    var compactPowderMessages = false

    @Property(
        type = PropertyType.SELECTOR, name = "Cooldown Hider",
        description = "Removes ability still on cooldown messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var cooldownHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "No Enemies Nearby Hider",
        description = "Hides the 'There are no enemies nearby!' message",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var hideNoEnemiesNearby = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Out of mana Hider",
        description = "Removes out of mana messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var manaMessages = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Profile Message Hider",
        description = "Removes the \"§aYou are playing on profile: §eFruit§r\" messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var profileHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Spook Message Hider",
        description = "§b[WIP] §rRemoves the messages from the Great Spooky Staff from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var spookyMessageHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Blessing Enchant Hider",
        description = "Removes blessing enchant message from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var blessingEnchantHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Blessed Bait Hider",
        description = "Removes blessed bait message from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var blessedBaitHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Sea Creature Catch Hider",
        description = "Removes regular sea creature catch messages from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var scCatchHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Legendary Sea Creature Catch Hider",
        description = "Removes legendary sea creature catch messages from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var legendaryScCatchHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Good Fishing Treasure Hider",
        description = "Removes good catch messages from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var goodTreasureHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Great Fishing Treasure Hider",
        description = "Removes great catch messages from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var greatTreasureHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Compact Hider",
        description = "Removes Compact messages from mining.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var compactHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Pristine Hider",
        description = "Removes Pristine messages from mining.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var pristineHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Wind Direction Hider",
        description = "Removes Gone With the Wind direction change messages.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"]
    )
    var windHider = 0

    init {
        addDependency("showEtherwarpTeleportPosColor", "showEtherwarpTeleportPos")

        addDependency("samScytheColor", "showSamScytheBlocks")

        addDependency("itemRarityOpacity", "showItemRarity")
        addDependency("itemRarityShape", "showItemRarity")

        listOf(
            "showLowestBINPrice",
            "betterAuctionPriceInput",
            "dungeonChestProfit",
            "showCoinsPerBit",
            "protectItemBINThreshold",
            "containerSellValue",
            "visitorOfferHelper",
            "showCoinsPerCopper"
        ).forEach { propertyName ->
            addDependency(propertyName, "fetchLowestBINPrices")
            registerListener(propertyName) { prop: Any ->
                if (prop is Boolean && prop) fetchLowestBINPrices = true
            }
        }

        addDependency("dungeonChestProfitIncludesEssence", "dungeonChestProfit")
        addDependency("kismetRerollThreshold", "dungeonChestProfit")

        addDependency("message270Score", "sendMessageOn270Score")
        addDependency("messageTitle270Score", "createTitleOn270Score")

        addDependency("message300Score", "sendMessageOn300Score")
        addDependency("messageTitle300Score", "createTitleOn300Score")

        addDependency("bloodHelperColor", "bloodHelper")

        addDependency("highlightDoorOpener", "spiritLeapNames")

        addDependency("showNextBlaze", "blazeSolver")
        addDependency("lowestBlazeColor", "blazeSolver")
        addDependency("highestBlazeColor", "blazeSolver")
        addDependency("nextBlazeColor", "showNextBlaze")
        addDependency("teleportMazeSolverColor", "teleportMazeSolver")
        addDependency("ticTacToeSolverColor", "ticTacToeSolver")
        addDependency("clickInOrderFirst", "clickInOrderTerminalSolver")
        addDependency("clickInOrderSecond", "clickInOrderTerminalSolver")
        addDependency("clickInOrderThird", "clickInOrderTerminalSolver")
        addDependency("changeToSameColorMode", "changeAllSameColorTerminalSolver")
        addDependency("lividFinderType", "findCorrectLivid")

        listOf(
            "emptyBurrowColor",
            "mobBurrowColor",
            "treasureBurrowColor"
        ).forEach { propertyName -> addDependency(propertyName, "showGriffinBurrows") }

        addDependency("activePetColor", "highlightActivePet")
        addDependency("favoritePetColor", "highlightFavoritePets")

        addDependency("showTankRadiusWall", "showTankRadius")
        addDependency("tankRadiusDisplayColor", "showTankRadius")
        addDependency("boxedTankColor", "boxedTanks")
        addDependency("boxedProtectedTeammatesColor", "boxedProtectedTeammates")

        addDependency("yangGlyphColor", "highlightYangGlyph")
        addDependency("nukekebiHeadColor", "highlightNukekebiHeads")

        listOf(
            "seraphBeaconPhaseColor",
            "seraphHitsPhaseColor",
            "seraphNormalPhaseColor"
        ).forEach { propertyName -> addDependency(propertyName, "recolorSeraphBoss") }

        addDependency("powerOrbDuration", "powerOrbLock")
        addDependency("dupeTrackerOverlayColor", "dupeTracker")

        addDependency("containerSellValueMaxItems", "containerSellValue")
        addDependency("includeModifiersInSellValue", "containerSellValue")

        addDependency("assumeWitherImpact", "witherShieldCooldown")

        registerListener("protectItemBINThreshold") { _: String ->
            TickTask(1) {
                val numeric = protectItemBINThreshold.replace(Regex("[^0-9]"), "")
                protectItemBINThreshold = numeric.ifEmpty { "0" }
                if (protectItemBINThreshold != "0") fetchLowestBINPrices = true
                markDirty()
            }
        }

        registerListener("darkModeMist") { _: Boolean -> mc.renderGlobal.loadRenderers() }
        registerListener("recolorCarpets") { _: Boolean -> mc.renderGlobal.loadRenderers() }

        registerListener("itemRarityShape") { i: Int ->
            if (i == 4 && Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION)) {
                val old = itemRarityShape
                runCatching {
                    val loc = ResourceLocation("skytils:gui/customrarity.png")
                    mc.resourceManager.getResource(loc)
                }.onFailure {
                    TickTask(1) {
                        if (itemRarityShape == 4) {
                            itemRarityShape = old
                            EssentialAPI.getNotifications()
                                .push("Invalid Value", "You cannot use the Custom rarity while the texture is missing!")
                        }
                    }
                }
            }
        }

        registerListener("overrideReparty") { state: Boolean ->
            if (state) {
                (ClientCommandHandler.instance as AccessorCommandHandler).commandMap["reparty"] =
                    RepartyCommand
                (ClientCommandHandler.instance as AccessorCommandHandler).commandMap["rp"] =
                    RepartyCommand
            }
        }
    }

    fun init() {
        initialize()
        if (Skytils.config.lastLaunchedVersion != Skytils.VERSION) {
            val ver = UpdateChecker.SkytilsVersion(Skytils.config.lastLaunchedVersion)
            when {
                !ver.isSafe || ver < UpdateChecker.SkytilsVersion("1.2-pre3") || Skytils.config.lastLaunchedVersion == "0" -> {
                    if (GuiManager.GUISCALES["Crystal Hollows Map"] == 0.1f) {
                        GuiManager.GUISCALES["Crystal Hollows Map"] = 1f
                        PersistentSave.markDirty<GuiManager>()
                    }
                }
            }
        }
        lastLaunchedVersion = Skytils.VERSION
        markDirty()
    }

    private object ConfigSorting : SortingBehavior() {
        override fun getCategoryComparator(): Comparator<in Category> = Comparator { o1, o2 ->
            if (o1.name == "General") return@Comparator -1
            if (o2.name == "General") return@Comparator 1
            else compareValuesBy(o1, o2) {
                it.name
            }
        }
    }
}
