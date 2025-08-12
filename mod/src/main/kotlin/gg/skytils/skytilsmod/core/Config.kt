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
import gg.essential.elementa.state.v2.ReferenceHolder
import gg.essential.elementa.unstable.state.v2.effect
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UChat
import gg.essential.universal.UDesktop
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.*
import gg.skytils.skytilsmod.Reference
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.Skytils.mc
import gg.skytils.skytilsmod.features.impl.dungeons.catlas.core.CatlasConfig
import gg.skytils.skytilsmod.features.impl.handlers.CommandAliases
import gg.skytils.skytilsmod.features.impl.trackers.Tracker
import gg.skytils.skytilsmod.gui.features.PotionNotificationsGui
import gg.skytils.skytilsmod.gui.features.ProtectItemGui
import gg.skytils.skytilsmod.gui.features.SpiritLeapNamesGui
import gg.skytils.skytilsmod.utils.ModChecker
import gg.skytils.skytilsmod.utils.SuperSecretSettings
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.startsWithAny
import gg.skytils.skytilsws.client.WSClient
import gg.skytils.vigilance.property
import net.minecraft.util.Identifier
import java.awt.Color
import java.io.File
import java.net.URI

//#if FORGE
//$$ import net.minecraftforge.client.ClientCommandHandler
//$$ import net.minecraftforge.fml.common.Loader
//$$ import net.minecraftforge.fml.common.LoaderState
//#endif

object Config : Vigilant(
    File("./config/skytils/config.toml"),
    (if (Utils.isBSMod) "BSMod" else "Skytils") + " (${Reference.VERSION})",
    sortingBehavior = ConfigSorting
) {
    @Property(
        type = PropertyType.SWITCH, name = "Connect to Skytils WS Server",
        description = "Skytils now can relay information from other Skytils users through our first-party websocket server!\nSome features will be hidden and will not work if this switch isn't on.",
        category = "General", subcategory = "API",
        i18nName = "skytils.config.general.api.connect_to_skytils_ws_server",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.api"
    )
    var connectToWS = true

    @Property(
        type = PropertyType.SWITCH, name = "Fetch Kuudra Prices",
        description = "Fetches the Kuudra prices for Skytils to use.\nSkytils currently uses a third-party to retrieve this information.\nSome features will be hidden and will not work if this switch isn't on.",
        category = "General", subcategory = "API",
        searchTags = ["Kuudra Chest Profit"],
        i18nName = "skytils.config.general.api.fetch_kuudra_prices",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.api"
    )
    var fetchKuudraPrices = false

    @Property(
        type = PropertyType.SWITCH, name = "Fetch Lowest BIN Prices",
        description = "Fetches the lowest BIN features for Skytils to use.\nSome features will be hidden and will not work if this switch isn't on.",
        category = "General", subcategory = "API",
        searchTags = ["BIN", "Bits", "Price Input", "Protect Items Above Value", "Chest Profit", "Dungeon Profit", "Container Sell Value", "Visitor Offer Helper", "Copper", "Kuudra"],
        i18nName = "skytils.config.general.api.fetch_lowest_bin_prices",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.api"
    )
    var fetchLowestBINPrices = false

    @Property(
        type = PropertyType.SELECTOR, name = "Command Alias Mode",
        description = "Choose which mode to use for Command Aliases.",
        category = "General", subcategory = "Command Aliases",
        options = ["Simple", "Advanced"],
        i18nName = "skytils.config.general.command_aliases.command_alias_mode",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.command_aliases"
    )
    var commandAliasMode = 0

    @Property(
        type = PropertyType.SWITCH, name = "Command Alias Subcommands",
        description = "§b[BETA] Allows the use of spaces in command aliases to target subcommands.\nThis should only be turned on if used.",
        category = "General", subcategory = "Command Aliases",
        i18nName = "skytils.config.general.command_aliases.command_alias_subcommands",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.command_aliases"
    )
    var commandAliasesSpaces = false

    @Property(
        type = PropertyType.SWITCH, name = "Auto Start Local API",
        description = "Automatically launches the Local API on game startup.",
        category = "General", subcategory = "Local API",
        i18nName = "skytils.config.general.local_api.auto_start_local_api",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.local_api"
    )
    var localAPIAutoStart = false

    @Property(
        type = PropertyType.TEXT, name = "Local API Password",
        description = "Sets the password for the local API. No password will cause all requests to be rejected.",
        category = "General", subcategory = "Local API",
        protectedText = true,
        i18nName = "skytils.config.general.local_api.local_api_password",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.local_api"
    )
    var localAPIPassword = ""

    @Property(
        type = PropertyType.BUTTON, name = "Join the Skytils Discord",
        description = "Join the Skytils Discord server for help using any of the features.",
        category = "General", subcategory = "Other",
        placeholder = "Join",
        i18nName = "skytils.config.general.other.join_the_skytils_discord",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.other"
    )
    @Suppress("unused")
    fun openDiscordLink() {
        UDesktop.browse(URI.create("https://discord.gg/skytils"))
    }

    @Property(
        type = PropertyType.SWITCH, name = "First Launch",
        description = "Used to see if the user is a new user of Skytils.",
        category = "General", subcategory = "Other",
        hidden = true,
        i18nName = "skytils.config.general.other.first_launch",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.other"
    )
    var firstLaunch = true

    @Property(
        type = PropertyType.TEXT, name = "Last Launched Skytils Version",
        category = "General", subcategory = "Other",
        hidden = true,
        i18nName = "skytils.config.general.other.last_launched_skytils_version",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.other"
    )
    var lastLaunchedVersion = "0"

    @Property(
        type = PropertyType.SWITCH, name = "Always Sprint in Skyblock",
        description = "Makes you always Sprint in Skyblock when you are eligible to.",
        category = "General", subcategory = "Other",
        i18nName = "skytils.config.general.other.always_sprint_in_skyblock",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.other"
    )
    var alwaysSprint = false

    @Property(
        type = PropertyType.SWITCH, name = "Config Button on Pause",
        description = "Adds a button to configure Skytils to the pause menu.",
        category = "General", subcategory = "Other",
        i18nName = "skytils.config.general.other.config_button_on_pause",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.other"
    )
    var configButtonOnPause = true

    @Property(
        type = PropertyType.SWITCH, name = "Disable Volume Overrides",
        description = "Disables overriding your volume to play sounds at max category volume.",
        category = "General", subcategory = "Other",
        i18nName = "skytils.config.general.other.disable_volume_overrides",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.other"
    )
    var disableVolumeOverrides = false

    @Property(
        type = PropertyType.SWITCH, name = "Reopen Options Menu",
        description = "Sets the menu to the Skytils options menu instead of exiting when on a Skytils config menu.",
        category = "General", subcategory = "Other",
        i18nName = "skytils.config.general.other.reopen_options_menu",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.other"
    )
    var reopenOptionsMenu = true

    @Property(
        type = PropertyType.SWITCH, name = "Coop Add Confirmation",
        description = "Requires you to run the /coopadd command twice to add a member.",
        category = "General", subcategory = "Hypixel",
        i18nName = "skytils.config.general.hypixel.coop_add_confirmation",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.hypixel"
    )
    var coopAddConfirmation = true

    @Property(
        type = PropertyType.SWITCH, name = "Guild Leave Confirmation",
        description = "Requires you to run the /g leave command twice to leave your guild.",
        category = "General", subcategory = "Hypixel",
        i18nName = "skytils.config.general.hypixel.guild_leave_confirmation",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.hypixel"
    )
    var guildLeaveConfirmation = true

    @Property(
        type = PropertyType.SWITCH, name = "Multiple Party Invites Fix",
        description = "§b[WIP] Tries to fix the ghost party issue when inviting multiple in one command.",
        category = "General", subcategory = "Hypixel",
        i18nName = "skytils.config.general.hypixel.multiple_party_invites_fix",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.hypixel"
    )
    var multiplePartyInviteFix = false

    @Property(
        type = PropertyType.SWITCH, name = "Auto-Accept Reparty",
        description = "Automatically accepts reparty invites",
        category = "General", subcategory = "Reparty",
        i18nName = "skytils.config.general.reparty.autoaccept_reparty",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.reparty"
    )
    var autoReparty = false

    @Property(
        type = PropertyType.SLIDER, name = "Auto-Accept Reparty Timeout",
        description = "Timeout in seconds for accepting a reparty invite",
        category = "General", subcategory = "Reparty",
        max = 120,
        i18nName = "skytils.config.general.reparty.autoaccept_reparty_timeout",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.reparty"
    )
    var autoRepartyTimeout = 60

    @Property(
        type = PropertyType.SELECTOR, name = "Update Channel",
        description = "Choose what type of updates you get notified for.",
        category = "General", subcategory = "Updates",
        options = ["None", "Beta (Pre-Release)", "Release", "Alpha (Experimental)"],
        i18nName = "skytils.config.general.updates.update_channel",
        i18nCategory = "skytils.config.general",
        i18nSubcategory = "skytils.config.general.updates"
    )
    var updateChannel = 2

    @Property(
        type = PropertyType.SWITCH, name = "Inject Fake Dungeon Map",
        description = "Injects a fake Magical Map into your hotbar to make old mods work again!\nP.S.: Use Catlas!",
        category = "Dungeons", subcategory = "Fixes",
        i18nName = "skytils.config.dungeons.fixes.inject_fake_dungeon_map",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.fixes"
    )
    var injectFakeDungeonMap = false

    val bigCryptsCounter = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Dungeon Crypts Counter",
            description = "Shows the amount of crypts destroyed on your HUD.",
            category = "Dungeons", subcategory = "HUD",
            i18nName = "skytils.config.dungeons.hud.dungeon_crypts_counter",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.hud"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Auto Copy Fails to Clipboard",
        description = "Copies deaths and fails in dungeons to your clipboard.",
        category = "Dungeons", subcategory = "Miscellaneous",
        i18nName = "skytils.config.dungeons.miscellaneous.auto_copy_fails_to_clipboard",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.miscellaneous"
    )
    var autoCopyFailToClipboard = false

    @Property(
        type = PropertyType.SWITCH, name = "Auto-Reparty on Dungeon Ending",
        description = "Runs the reparty command when your dungeon ends.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.autoreparty_on_dungeon_ending",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var autoRepartyOnDungeonEnd = false

    @Property(
        type = PropertyType.SWITCH, name = "Death Counter",
        description = "Counts the amount of times each member of your team has died in a dungeon.",
        category = "Dungeons", subcategory = "Miscellaneous",
        i18nName = "skytils.config.dungeons.miscellaneous.death_counter",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.miscellaneous"
    )
    var dungeonDeathCounter = false

    @Property(
        type = PropertyType.SWITCH, name = "Party Finder Stats",
        description = "§b[WIP] Displays Stats about a Player who joined.",
        category = "Dungeons", subcategory = "Party Finder",
        i18nName = "skytils.config.dungeons.party_finder.party_finder_stats",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.party_finder"
    )
    var partyFinderStats = false

    val dungeonChestProfit = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Dungeon Chest Profit",
            description = "Shows the estimated profit for items from chests in dungeons.",
            category = "Dungeons", subcategory = "Miscellaneous",
            i18nName = "skytils.config.dungeons.miscellaneous.dungeon_chest_profit",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.miscellaneous"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Dungeon Chest Profit Includes Essence",
        description = "Include essence when calculating Dungeon Chest Profit.",
        category = "Dungeons", subcategory = "Miscellaneous",
        i18nName = "skytils.config.dungeons.miscellaneous.dungeon_chest_profit_includes_essence",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.miscellaneous"
    )
    var dungeonChestProfitIncludesEssence = true

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Unopened Croesus Chests",
        description = "Highlight runs in Croesus based on how many more chests can be opened.",
        category = "Dungeons", subcategory = "Miscellaneous",
        i18nName = "skytils.config.dungeons.miscellaneous.highlight_unopened_croesus_chests",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.miscellaneous"
    )
    var croesusChestHighlight = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Opened Croesus Chests",
        description = "Hide runs in Croesus if no more chests can be opened.",
        category = "Dungeons", subcategory = "Miscellaneous",
        i18nName = "skytils.config.dungeons.miscellaneous.hide_opened_croesus_chests",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.miscellaneous"
    )
    var croesusHideOpened = false

    @Property(
        type = PropertyType.BUTTON, name = "Catlas",
        description = "Click to configure the dungeon map settings.",
        category = "Dungeons", subcategory = "Miscellaneous",
        searchTags = ["Highlight Box Wither Doors", "Cataclysmic Map", "Dungeon Map"],
        i18nName = "skytils.config.dungeons.miscellaneous.catlas",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.miscellaneous"
    )
    fun openCatlasConfig() {
        Skytils.displayScreen = CatlasConfig.gui()
    }

    @Property(
        type = PropertyType.SWITCH, name = "Dungeon Start Confirmation",
        description = "Requires a confirmation to start the dungeon when not in a full party.",
        category = "Dungeons", subcategory = "Miscellaneous",
        i18nName = "skytils.config.dungeons.miscellaneous.dungeon_start_confirmation",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.miscellaneous"
    )
    var noChildLeftBehind = false

    @Property(
        type = PropertyType.BUTTON, name = "Dungeon Sweat",
        description = "Click if dungeon sweat???\n§1In memory of AzuredBlue, 2019-2024. §cRIP\n§7Yes this actually does something...",
        category = "Dungeons", subcategory = "Miscellaneous",
        searchTags = ["predev", "pre-dev", "arrow", "tic tac toe", "solver", "device"],
        i18nName = "skytils.config.dungeons.miscellaneous.dungeon_sweat",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.miscellaneous"
    )
    fun openDungeonSweat() {
        if (SuperSecretSettings.azooPuzzoo)
            SuperSecretSettings.remove("azoopuzzoo")
        else
            SuperSecretSettings.add("azoopuzzoo")
        SuperSecretSettings.save()
        if (ModChecker.canShowNotifications) {
            Notifications.push("Dungeon Sweat", "Dungeon Sweat mode ${SuperSecretSettings.azooPuzzoo}")
        } else {
            UChat.chat("${Skytils.prefix} §bDungeon Sweat mode ${SuperSecretSettings.azooPuzzoo}")
        }
        UDesktop.browse(URI.create("https://l.skytils.gg/dungeonsweatsonly"))
    }

    val dungeonTimer
        get() = dungeonTimerState.getUntracked()

    val dungeonTimerState = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Dungeon Timer",
            description = "Shows the time taken for certain actions in dungeons.",
            category = "Dungeons", subcategory = "Miscellaneous",
            i18nName = "skytils.config.dungeons.miscellaneous.dungeon_timer",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.miscellaneous"
        ), false
    )

    val necronPhaseTimer
        get() = necronPhaseTimerState.getUntracked()

    val necronPhaseTimerState = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Necron Phase Timer",
            description = "Shows the time taken for each phase in the Necron boss fight.",
            category = "Dungeons", subcategory = "Miscellaneous",
            i18nName = "skytils.config.dungeons.miscellaneous.necron_phase_timer",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.miscellaneous"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Red Screen Fix",
        description = "Fixes an issue in The Catacombs Floors 2 and 3 where the screen turns red on fancy graphics.",
        category = "Dungeons", subcategory = "Miscellaneous",
        i18nName = "skytils.config.dungeons.miscellaneous.red_screen_fix",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.miscellaneous"
    )
    var worldborderFix = true

    @Property(
        type = PropertyType.SWITCH, name = "Show Decimal Seconds on Timers",
        description = "Adds decimals to the amount of seconds on the dungeon timers.",
        category = "Dungeons", subcategory = "Miscellaneous",
        i18nName = "skytils.config.dungeons.miscellaneous.show_decimal_seconds_on_timers",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.miscellaneous"
    )
    var showMillisOnDungeonTimer = false

    val sadanPhaseTimer
        get() = sadanPhaseTimerState.getUntracked()

    val sadanPhaseTimerState = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Sadan Phase Timer",
            description = "Shows the time taken for each phase in the Sadan boss fight.",
            category = "Dungeons", subcategory = "Miscellaneous",
            i18nName = "skytils.config.dungeons.miscellaneous.sadan_phase_timer",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.miscellaneous"
        ), false
    )

    val showScoreCalculation = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Show Dungeon Score Estimate",
            description = "Shows an estimate of the current dungeon score.",
            category = "Dungeons", subcategory = "Score Calculation",
            i18nName = "skytils.config.dungeons.score_calculation.show_dungeon_score_estimate",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.score_calculation"
        ), false
    )

    val minimizedScoreCalculationState = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Minimized Dungeon Score Estimate",
            description = "Only shows the dungeon score.",
            category = "Dungeons", subcategory = "Score Calculation",
            i18nName = "skytils.config.dungeons.score_calculation.minimized_dungeon_score_estimate",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.score_calculation"
        ),
        false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Score Calculation Party Assist",
        description = "Helps your party determine the state of the mimic in your dungeon by sending in party chat.\n§cThis feature is use at your own risk.",
        category = "Dungeons", subcategory = "Score Calculation",
        i18nName = "skytils.config.dungeons.score_calculation.score_calculation_party_assist",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var scoreCalculationAssist = false

    @Property(
        type = PropertyType.SWITCH, name = "Receive Score Calculation Party Assist",
        description = "Receive help from your party in order to determine the state of the mimic in the dungeon.",
        category = "Dungeons", subcategory = "Score Calculation",
        i18nName = "skytils.config.dungeons.score_calculation.receive_score_calculation_party_assist",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var scoreCalculationReceiveAssist = false

    @Property(
        type = PropertyType.SWITCH, name = "Allow Mimic Dead! from other Mods",
        description = "Uses the Mimic dead! in order to determine the state of the mimic in the dungeon.",
        category = "Dungeons", subcategory = "Score Calculation",
        i18nName = "skytils.config.dungeons.score_calculation.allow_mimic_dead_from_other_mods",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var receiveHelpFromOtherModMimicDead = false

    @Property(
        type = PropertyType.SWITCH, name = "Send message on 270 score",
        description = "Send message on 270 score.",
        category = "Dungeons", subcategory = "Score Calculation",
        i18nName = "skytils.config.dungeons.score_calculation.send_message_on_270_score",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var sendMessageOn270Score = false

    @Property(
        type = PropertyType.PARAGRAPH, name = "Message for 270 score",
        description = "Customize the message sent on hitting 270 score.",
        category = "Dungeons", subcategory = "Score Calculation",
        placeholder = "Skytils > 270 score",
        i18nName = "skytils.config.dungeons.score_calculation.message_for_270_score",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var message270Score = ""

    @Property(
        type = PropertyType.SWITCH, name = "Create Title on 270 score",
        description = "Create title on 270 score.",
        category = "Dungeons", subcategory = "Score Calculation",
        i18nName = "skytils.config.dungeons.score_calculation.create_title_on_270_score",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var createTitleOn270Score = false

    @Property(
        type = PropertyType.PARAGRAPH, name = "270 Title Message",
        description = "Customize the message that will be sent when the score reaches 270.",
        category = "Dungeons", subcategory = "Score Calculation",
        placeholder = "270",
        i18nName = "skytils.config.dungeons.score_calculation.270_title_message",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var messageTitle270Score = ""

    @Property(
        type = PropertyType.SWITCH, name = "Send message on 300 score",
        description = "Send message on 300 score.",
        category = "Dungeons", subcategory = "Score Calculation",
        i18nName = "skytils.config.dungeons.score_calculation.send_message_on_300_score",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var sendMessageOn300Score = false

    @Property(
        type = PropertyType.PARAGRAPH, name = "Message for 300 score",
        description = "Customize the message sent on hitting 300 score.",
        category = "Dungeons", subcategory = "Score Calculation",
        placeholder = "Skytils > 300 score",
        i18nName = "skytils.config.dungeons.score_calculation.message_for_300_score",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var message300Score = ""

    @Property(
        type = PropertyType.SWITCH, name = "Create Title on 300 score",
        description = "Create title on 300 score.",
        category = "Dungeons", subcategory = "Score Calculation",
        i18nName = "skytils.config.dungeons.score_calculation.create_title_on_300_score",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var createTitleOn300Score = false

    @Property(
        type = PropertyType.PARAGRAPH, name = "300 Title Message",
        description = "Customize the message that will be sent when the score reaches 300.",
        category = "Dungeons", subcategory = "Score Calculation",
        placeholder = "300",
        i18nName = "skytils.config.dungeons.score_calculation.300_title_message",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.score_calculation"
    )
    var messageTitle300Score = ""

    @Property(
        type = PropertyType.SWITCH, name = "Blood Camp Helper",
        description = "Draws an outline where blood mobs spawn in after spinning as armor stands.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.blood_camp_helper",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var bloodHelper = false

    @Property(
        type = PropertyType.COLOR, name = "Blood Camp Helper Color",
        description = "Changes the color of the outline.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.blood_camp_helper_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var bloodHelperColor: Color = Color.RED

    @Property(
        type = PropertyType.SWITCH, name = "Box Starred Mobs",
        description = "Draws the bounding box for Starred Mobs.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.box_starred_mobs",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var boxStarredMobs = false

    @Property(
        type = PropertyType.COLOR, name = "Box Starred Mobs Color",
        description = "Color of the bounding box for Starred Mobs.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.box_starred_mobs_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var boxStarredMobsColor = Color(0, 255, 255, 255)

    @Property(
        type = PropertyType.SWITCH, name = "Box Skeleton Masters",
        description = "Draws the bounding box for Skeleton Masters.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.box_skeleton_masters",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var boxSkeletonMasters = false

    @Property(
        type = PropertyType.SWITCH, name = "Box Spirit Bear",
        description = "Draws the bounding box for Spirit Bears.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.box_spirit_bear",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var boxSpiritBears = false

    @Property(
        type = PropertyType.SWITCH, name = "Box Spirit Bow",
        description = "Draws a box around the Spirit Bow.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.box_spirit_bow",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var boxSpiritBow = false

    @Property(
        type = PropertyType.NUMBER, name = "Dungeon Chest Reroll Confirmation",
        description = "Requires you to click multiple times in order to reroll a chest.",
        category = "Dungeons", subcategory = "Quality of Life",
        max = 5,
        i18nName = "skytils.config.dungeons.quality_of_life.dungeon_chest_reroll_confirmation",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var kismetRerollConfirm = 0

    @Property(
        type = PropertyType.NUMBER, name = "Dungeon Chest Reroll Protection Threshold",
        description = "Prevents rerolling if the value of the items is higher than this value in millions.",
        category = "Dungeons", subcategory = "Quality of Life",
        max = 1000,
        i18nName = "skytils.config.dungeons.quality_of_life.dungeon_chest_reroll_protection_threshold",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var kismetRerollThreshold = 0

    val dungeonSecretDisplay = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Dungeon Secret Display",
            description = "Shows the amount of dungeon secrets in the current room.",
            category = "Dungeons", subcategory = "Quality of Life",
            i18nName = "skytils.config.dungeons.quality_of_life.dungeon_secret_display",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.quality_of_life"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Ghost Leap Names",
        description = "Shows names next to the heads on the Ghost Leap menu.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.ghost_leap_names",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var ghostTeleportMenuNames = false


    @Property(
        type = PropertyType.SWITCH, name = "Hide Archer Bone Passive",
        description = "Hides the archer bone shield passive.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_archer_bone_passive",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideArcherBonePassive = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Wither King Dragon Death",
        description = "Removes the dragon death animation from the Master Mode 7 boss fight.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_wither_king_dragon_death",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideWitherKingDragonDeath = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Fairies",
        description = "Hides the fairies in dungeons.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_fairies",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideFairies = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Floor 4 Crowd Messages",
        description = "Hides the messages from the Crowd on Floor 4.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_floor_4_crowd_messages",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideF4Spam = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Oruo Messages",
        description = "Hides the messages from Oruo during the Trivia.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_oruo_messages",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideOruoMessages = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Spirit Animal Nametags",
        description = "Removes the nametags above spirit animals on Floor 4.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_spirit_animal_nametags",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideF4Nametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Terminal Completion Titles",
        description = "Removes the title that shows up when a terminal is completed.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_terminal_completion_titles",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideTerminalCompletionTitles = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Wither Miner Nametags",
        description = "Removes the nametags above Wither Miners on Floor 7.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_wither_miner_nametags",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideWitherMinerNametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Terracotta Nametags",
        description = "Hides the nametags of the Terracotta while in Dungeons",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_terracotta_nametags",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideTerracotaNametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Non-Starred Mobs Nametags",
        description = "Hides the nametags of non-starred mobs while in Dungeons",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_nonstarred_mobs_nametags",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideNonStarredNametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Larger Bat Models",
        description = "Increases the size of bat models.\nThe hitbox of the bat may be offset from what is shown.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.larger_bat_models",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var biggerBatModels = false

    @Property(
        type = PropertyType.SWITCH, name = "Change Hurt Color on the Wither King's Dragons",
        description = "Reduces the tinting on hurting the wither king's dragons.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.change_hurt_color_on_the_wither_kings_dragons",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var changeHurtColorOnWitherKingsDragons = false

    @Property(
        type = PropertyType.SWITCH, name = "Retexture Wither King's Dragons",
        description = "Retextures the dragons in Master Mode 7 to their respective colors.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.retexture_wither_kings_dragons",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var retextureWitherKingsDragons = false

    @Property(
        type = PropertyType.SWITCH, name = "Revive Stone Names",
        description = "Shows names next to the heads on the Revive Stone menu.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.revive_stone_names",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var reviveStoneNames = false

    @Property(
        type = PropertyType.SWITCH, name = "Say Blaze Done",
        description = "Says in chat when blaze is done.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.say_blaze_done",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var sayBlazeDone = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Bat Hitboxes",
        description = "Draws the outline of a bat's bounding box.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.show_bat_hitboxes",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var showBatHitboxes = false

    @Property(
        type = PropertyType.SWITCH, name = "Color Brewing Stands",
        description = "Color brewing stands if they are done or not.",
        category = "Miscellaneous", subcategory = "Brewing",
        i18nName = "skytils.config.miscellaneous.brewing.color_brewing_stands",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.brewing"
    )
    var colorBrewingStands = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Dungeon Floor as Stack Size",
        description = "Shows the dungeon floor as the stack size.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_dungeon_floor_as_stack_size",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showDungeonFloorAsStackSize = false

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Held Item Scale",
        description = "Changes the size of your held item.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.held_item_scale",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var itemScale = 1f

    val showGiantHP = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Show Giant HP",
            description = "Shows the HP of Giants in your HUD.",
            category = "Dungeons", subcategory = "Quality of Life",
            i18nName = "skytils.config.dungeons.quality_of_life.show_giant_hp",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.quality_of_life"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Show Giant HP at Feet",
        description = "Shows the HP of giants' at the giant's feet.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.show_giant_hp_at_feet",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var showGiantHPAtFeet = false

    val showGuardianRespawnTimer = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Show Guardian Respawn Timer",
            description = "Shows the respawn timer for the Guardians in Floor 3.",
            category = "Dungeons", subcategory = "Quality of Life",
            i18nName = "skytils.config.dungeons.quality_of_life.show_guardian_respawn_timer",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.quality_of_life"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Show Wither King Statue Box",
        description = "Draws a box around the Wither King Statues.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.show_wither_king_statue_box",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var showWitherKingStatueBox = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Sadan's Interest",
        description = "Replace Sadan's interest display with Skytils' own.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.show_sadans_interest",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var showSadanInterest = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Terracotta Respawn Time",
        description = "Displays a timer until Terracotta respawn",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.show_terracotta_respawn_time",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var terracottaRespawnTimer = false

    @Property(
        type = PropertyType.SELECTOR, name = "Show Necron's HP",
        description = "Shows additional info about Necron's health.",
        category = "Dungeons", subcategory = "Quality of Life",
        options = ["None", "HP", "Percentage Health"],
        i18nName = "skytils.config.dungeons.quality_of_life.show_necrons_hp",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var necronHealth = 0

    @Property(
        type = PropertyType.SWITCH, name = "Show Wither King's Dragons' Color as Text",
        description = "Displays a more clear indicator of the dragons' text to make the game more accessible.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.show_wither_kings_dragons_color_as_text",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var showWitherDragonsColorBlind = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Wither King's Dragons' HP",
        description = "Displays a more clear indicator of the dragons' HP.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.show_wither_kings_dragons_hp",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var showWitherKingDragonsHP = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Wither King's Dragons' Spawn Timer",
        description = "Displays a timer for when the dragons are about to spawn in.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.show_wither_kings_dragons_spawn_timer",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var showWitherKingDragonsSpawnTimer = false

    val spiritBearTimer = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Spirit Bear Timer",
            description = "Shows the time it takes for the Spirit Bears to spawn.",
            category = "Dungeons", subcategory = "Quality of Life",
            i18nName = "skytils.config.dungeons.quality_of_life.spirit_bear_timer",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.quality_of_life"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Spirit Leap Names",
        description = "Shows names next to the heads on the Spirit Leap menu.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.spirit_leap_names",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var spiritLeapNames = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Door Opener",
        description = "Highlight the player that most recently opened a Wither Door on the spirit leap menu.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.highlight_door_opener",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var highlightDoorOpener = false

    @Property(
        type = PropertyType.BUTTON, name = "Spirit Leap Highlights",
        description = "Highlights names and classes on the Spirit Leap menu.",
        category = "Dungeons", subcategory = "Quality of Life",
        placeholder = "Open GUI",
        i18nName = "skytils.config.dungeons.quality_of_life.spirit_leap_highlights",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    @Suppress("unused")
    fun spiritLeapNameButton() {
        Skytils.displayScreen = SpiritLeapNamesGui()
    }

    @Property(
        type = PropertyType.SWITCH, name = "Spirit Pet Warning",
        description = "Lets you know if someone in your party has a Spirit pet equipped before the dungeon starts.\n§cYou must have pet visibility on in Skyblock.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.spirit_pet_warning",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var spiritPetWarning = false

    @Property(
        type = PropertyType.SWITCH, name = "Wither King Dragon Dimensional Slash Alert",
        description = "Creates a title when you are in range of dimensional slash.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.wither_king_dragon_dimensional_slash_alert",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var witherKingDragonSlashAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Wither King Dragon Spawn Alert",
        description = "Shows a message when the Wither King spawns a dragon.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.wither_king_dragon_spawn_alert",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var witherKingDragonSpawnAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Blaze Solver",
        description = "Changes the color of the blaze to shoot on Higher or Lower.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.blaze_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var blazeSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Next Blaze",
        description = "Colors the next blaze to shoot in Higher or Lower yellow.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.show_next_blaze",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var showNextBlaze = false

    @Property(
        type = PropertyType.SWITCH, name = "Line to Next Blaze",
        description = "Draws line to next blaze to shoot in Higher or Lower.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.line_to_next_blaze",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var lineToNextBlaze = false

    @Property(
        type = PropertyType.COLOR, name = "Lowest Blaze Color",
        description = "Color used to highlight the lowest blaze in.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.lowest_blaze_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var lowestBlazeColor = Color(255, 0, 0, 200)

    @Property(
        type = PropertyType.COLOR, name = "Highest Blaze Color",
        description = "Color used to highlight the highest blaze in.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.highest_blaze_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var highestBlazeColor = Color(0, 255, 0, 200)

    @Property(
        type = PropertyType.COLOR, name = "Next Blaze Color",
        description = "Color used to highlight the next blaze in.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.next_blaze_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var nextBlazeColor = Color(255, 255, 0, 200)

    @Property(
        type = PropertyType.COLOR, name = "Line to Next Blaze Color",
        description = "Color used to draw line to the next blaze in.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.line_to_next_blaze_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var lineToNextBlazeColor = Color(255, 255, 0, 200)

    @Property(
        type = PropertyType.SWITCH, name = "Boulder Solver",
        description = "Show which boxes to move on the Boulder puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.boulder_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var boulderSolver = false

    @Property(
        type = PropertyType.COLOR, name = "Boulder Solver Color",
        description = "Color of the box that shows which button to click in the Boulder puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.boulder_solver_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var boulderSolverColor = Color(255, 0, 0, 255)

    @Property(
        type = PropertyType.SWITCH, name = "Creeper Beams Solver",
        description = "Shows pairs on the Creeper Beams puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.creeper_beams_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var creeperBeamsSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Ice Fill Solver",
        description = "§b[WIP] §rShows the path to take on the Ice Fill puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.ice_fill_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var iceFillSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Ice Path Solver",
        description = "Show the path for the silverfish to follow on the Ice Path puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.ice_path_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var icePathSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Teleport Maze Solver",
        description = "Shows which pads you've stepped on, and which pads to step on in the Teleport Maze puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.teleport_maze_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var teleportMazeSolver = false

    @Property(
        type = PropertyType.COLOR, name = "Teleport Maze Solver Color",
        description = "Color of the box that shows which pads you've stepped on in the Teleport Maze puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.teleport_maze_solver_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var teleportMazeSolverColor = Color(255, 0, 0, 200)

    @Property(
        type = PropertyType.SWITCH, name = "Three Weirdos Solver",
        description = "Shows which chest to click in the Three Weirdos puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.three_weirdos_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var threeWeirdosSolver = false

    @Property(
        type = PropertyType.COLOR, name = "Three Weirdos Solver Color",
        description = "Color of the chest to click on the Three Weirdos puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.three_weirdos_solver_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var threeWeirdosSolverColor = Color(255, 0, 0, 255)

    @Property(
        type = PropertyType.SWITCH, name = "Tic Tac Toe Solver",
        description = "Displays the best move on the Tic Tac Toe puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.tic_tac_toe_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var ticTacToeSolver = false

    @Property(
        type = PropertyType.COLOR, name = "Tic Tac Toe Solver Color",
        description = "Color of the outline that displays the best move on the Tic Tac Toe puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.tic_tac_toe_solver_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var ticTacToeSolverColor = Color(23, 234, 99, 204)

    @Property(
        type = PropertyType.SWITCH, name = "Trivia Solver",
        description = "Shows the correct answer for the questions on the Trivia puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.trivia_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var triviaSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Water Board Solver",
        description = "§b[WIP] §rDisplays which levers to flip for the Water Board puzzle.",
        category = "Dungeons", subcategory = "Solvers",
        i18nName = "skytils.config.dungeons.solvers.water_board_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.solvers"
    )
    var waterBoardSolver = false

    val findCorrectLivid
        get() = findCorrectLividState.getUntracked()

    val findCorrectLividState = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Find correct Livid",
            description = "Shows the hp of the correct livid on F5 and M5",
            category = "Dungeons", subcategory = "Solvers",
            i18nName = "skytils.config.dungeons.solvers.find_correct_livid",
            i18nCategory = "skytils.config.dungeons",
            i18nSubcategory = "skytils.config.dungeons.solvers"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Boxed Tanks",
        description = "Shows the bounding box of all tanks through walls.",
        category = "Dungeons", subcategory = "Tank Helper Tools",
        i18nName = "skytils.config.dungeons.tank_helper_tools.boxed_tanks",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.tank_helper_tools"
    )
    var boxedTanks = false

    @Property(
        type = PropertyType.COLOR, name = "Boxed Tank Color",
        description = "Choose the color of the tanks in the bounding box",
        category = "Dungeons", subcategory = "Tank Helper Tools",
        i18nName = "skytils.config.dungeons.tank_helper_tools.boxed_tank_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.tank_helper_tools"
    )
    var boxedTankColor = Color(0, 255, 0)

    @Property(
        type = PropertyType.SWITCH, name = "Box Protected Teammates",
        description = "Shows the bounding box of protected teammates through walls.",
        category = "Dungeons", subcategory = "Tank Helper Tools",
        i18nName = "skytils.config.dungeons.tank_helper_tools.box_protected_teammates",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.tank_helper_tools"
    )
    var boxedProtectedTeammates = false

    @Property(
        type = PropertyType.COLOR, name = "Protected Teammate Box Color",
        description = "Choose the color of the teammates in the bounding box",
        category = "Dungeons", subcategory = "Tank Helper Tools",
        i18nName = "skytils.config.dungeons.tank_helper_tools.protected_teammate_box_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.tank_helper_tools"
    )
    var boxedProtectedTeammatesColor = Color(255, 0, 0)

    @Property(
        type = PropertyType.SWITCH, name = "Tank Protection Range Display",
        description = "Shows the range in which players will be protected by a tank.",
        category = "Dungeons", subcategory = "Tank Helper Tools",
        i18nName = "skytils.config.dungeons.tank_helper_tools.tank_protection_range_display",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.tank_helper_tools"
    )
    var showTankRadius = false

    @Property(
        type = PropertyType.SWITCH, name = "Tank Range Wall",
        description = "Shows the range as a wall instead of a circle.",
        category = "Dungeons", subcategory = "Tank Helper Tools",
        i18nName = "skytils.config.dungeons.tank_helper_tools.tank_range_wall",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.tank_helper_tools"
    )
    var showTankRadiusWall = true

    @Property(
        type = PropertyType.COLOR, name = "Tank Range Wall Color",
        description = "The color to display the Tank Range as.",
        category = "Dungeons", subcategory = "Tank Helper Tools",
        i18nName = "skytils.config.dungeons.tank_helper_tools.tank_range_wall_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.tank_helper_tools"
    )
    var tankRadiusDisplayColor = Color(100, 255, 0, 50)

    @Property(
        type = PropertyType.SWITCH, name = "Block Incorrect Terminal Clicks",
        description = "Blocks incorrect clicks on terminals.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.block_incorrect_terminal_clicks",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var blockIncorrectTerminalClicks = false

    @Property(
        type = PropertyType.SWITCH, name = "Middle Click on Terminals",
        description = "Replaces left clicks while on terminals with middle clicks.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.middle_click_on_terminals",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var middleClickTerminals = true

    @Property(
        type = PropertyType.SWITCH, name = "Change All to Same Color Solver",
        description = "Shows the best path of clicks for the Change All to Same Color Terminal.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.change_all_to_same_color_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var changeAllSameColorTerminalSolver = false

    @Property(
        type = PropertyType.SELECTOR, name = "Change All to Same Color Solver Mode",
        description = "Changes the display mode of the solver.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        options = ["Normal", "LMB only"],
        i18nName = "skytils.config.dungeons.terminal_solvers.change_all_to_same_color_solver_mode",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var changeToSameColorMode = 0

    @Property(
        type = PropertyType.SWITCH, name = "Change All to Same Color Solver Lock",
        description = "Locks the first selected target color in place.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.change_all_to_same_color_solver_lock",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var changeToSameColorLock = false

    @Property(
        type = PropertyType.SWITCH, name = "Click in Order Solver",
        description = "Shows the items to click on the Click in Order terminal.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.click_in_order_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var clickInOrderTerminalSolver = false

    @Property(
        type = PropertyType.COLOR, name = "Click in Order First Color",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.click_in_order_first_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var clickInOrderFirst = Color(2, 62, 138, 255)

    @Property(
        type = PropertyType.COLOR, name = "Click in Order Second Color",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.click_in_order_second_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var clickInOrderSecond = Color(65, 102, 245, 255)

    @Property(
        type = PropertyType.COLOR, name = "Click in Order Third Color",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.click_in_order_third_color",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var clickInOrderThird = Color(144, 224, 239, 255)

    @Property(
        type = PropertyType.SWITCH, name = "Select All Colors Solver",
        description = "Shows the items to click on the Select All Color terminal.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.select_all_colors_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var selectAllColorTerminalSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Starts With Sequence Solver",
        description = "Shows the items to click on the What starts with? terminal.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.starts_with_sequence_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var startsWithSequenceTerminalSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Item Frame Alignment Solver",
        description = "Shows the amount of clicks needed on the device in Floor 7.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.item_frame_alignment_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var alignmentTerminalSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Predict Clicks for Alignment Solver",
        description = "Predict the amount of clicks needed on the alignment device in Floor 7.\nHighly recommended for high latency.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.predict_clicks_for_alignment_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var predictAlignmentClicks = true

    @Property(
        type = PropertyType.SWITCH, name = "Shoot the Target Solver",
        description = "Shows all the shot blocks on the device in Floor 7.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.shoot_the_target_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var shootTheTargetSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Simon Says Solver",
        description = "Show which buttons to press on the Simon Says device in Floor 7.\n§cIf a teammate clicks a button it will not register.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.simon_says_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var simonSaysSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Predict Clicks for Simon Says Solver",
        description = "Attempts to register teammate clicks on Simon Says Solver.",
        category = "Dungeons", subcategory = "Terminal Solvers",
        i18nName = "skytils.config.dungeons.terminal_solvers.predict_clicks_for_simon_says_solver",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.terminal_solvers"
    )
    var predictSimonClicks = false

    val displayJerryPerks = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Display Jerry Perks",
            description = "Displays the perks for Jerry.\nYou must visit Jerry in order for the display to function correctly.",
            category = "Events", subcategory = "Mayor Jerry",
            i18nName = "skytils.config.events.mayor_jerry.display_jerry_perks",
            i18nCategory = "skytils.config.events",
            i18nSubcategory = "skytils.config.events.mayor_jerry"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Hidden Jerry Alert",
        description = "Displays an alert when you find a hidden Jerry.",
        category = "Events", subcategory = "Mayor Jerry",
        i18nName = "skytils.config.events.mayor_jerry.hidden_jerry_alert",
        i18nCategory = "skytils.config.events",
        i18nSubcategory = "skytils.config.events.mayor_jerry"
    )
    var hiddenJerryAlert = false

    val hiddenJerryTimer = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Hidden Jerry Timer",
            description = "Displays a timer from when you last discovered a Hidden Jerry.",
            category = "Events", subcategory = "Mayor Jerry",
            i18nName = "skytils.config.events.mayor_jerry.hidden_jerry_timer",
            i18nCategory = "skytils.config.events",
            i18nSubcategory = "skytils.config.events.mayor_jerry"
        ), false
    )

    val trackHiddenJerry = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Track Mayor Jerry Items",
            description = "Tracks the amount of each type of Jerry that you've found, as well as drops obtained from Jerry Boxes.",
            category = "Events", subcategory = "Mayor Jerry",
            i18nName = "skytils.config.events.mayor_jerry.track_mayor_jerry_items",
            i18nCategory = "skytils.config.events",
            i18nSubcategory = "skytils.config.events.mayor_jerry"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Show Griffin Burrows",
        description = "Uses particles to locate nearby burrows.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Burrow", "Borrow", "Waypoint", "ESP"],
        i18nName = "skytils.config.events.mythological.show_griffin_burrows",
        i18nCategory = "skytils.config.events",
        i18nSubcategory = "skytils.config.events.mythological"
    )
    var showGriffinBurrows = false

    @Property(
        type = PropertyType.COLOR, name = "Empty/Start Burrow Color",
        description = "Color used to highlight the Griffin Burrows in.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Burrow", "Borrow"],
        i18nName = "skytils.config.events.mythological.emptystart_burrow_color",
        i18nCategory = "skytils.config.events",
        i18nSubcategory = "skytils.config.events.mythological"
    )
    var emptyBurrowColor = Color(173, 216, 230)

    @Property(
        type = PropertyType.COLOR, name = "Mob Burrow Color",
        description = "Color used to highlight the Griffin Burrows in.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Burrow", "Borrow"],
        i18nName = "skytils.config.events.mythological.mob_burrow_color",
        i18nCategory = "skytils.config.events",
        i18nSubcategory = "skytils.config.events.mythological"
    )
    var mobBurrowColor = Color(173, 216, 230)

    @Property(
        type = PropertyType.COLOR, name = "Treasure Burrow Color",
        description = "Color used to highlight the Griffin Burrows in.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Burrow", "Borrow"],
        i18nName = "skytils.config.events.mythological.treasure_burrow_color",
        i18nCategory = "skytils.config.events",
        i18nSubcategory = "skytils.config.events.mythological"
    )
    var treasureBurrowColor = Color(173, 216, 230)

    // TODO: Add translations
    @Property(
        type = PropertyType.SWITCH, name = "Ping when Burrow is Nearby",
        description = "Pings when a burrow is nearby.",
        category = "Events", subcategory = "Mythological"
    )
    var pingNearbyBurrow = false

    @Property(
        type = PropertyType.SWITCH, name = "Griffin Burrow Estimation",
        description = "Estimates griffin burrow position after using spade near the previous burrow.",
        category = "Events", subcategory = "Mythological"
    )
    var burrowEstimation = false

    @Property(
        type = PropertyType.SWITCH, name = "Griffin Burrow Estimation (MEOW)",
        description = "Estimates griffin burrow position after using spade ANYWHERE. Use of this mode will disable the other mode.",
        category = "Events", subcategory = "Mythological"
    )
    var experimentBurrowEstimation = false

    @Property(
        type = PropertyType.SWITCH, name = "Broadcast Rare Drop Notifications",
        description = "Sends rare drop notification when you obtain a rare drop from a Mythological Creature.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth", "Tracker"],
        i18nName = "skytils.config.events.mythological.broadcast_rare_drop_notifications",
        i18nCategory = "skytils.config.events",
        i18nSubcategory = "skytils.config.events.mythological"
    )
    var broadcastMythCreatureDrop = false

    @Property(
        type = PropertyType.SWITCH, name = "Display Gaia Construct Hits",
        description = "Tracks the amount of times a Gaia Construct has been hit.",
        category = "Events", subcategory = "Mythological",
        searchTags = ["Griffin", "Diana", "Myth"],
        i18nName = "skytils.config.events.mythological.display_gaia_construct_hits",
        i18nCategory = "skytils.config.events",
        i18nSubcategory = "skytils.config.events.mythological"
    )
    var trackGaiaHits = false

    val trackMythEvent
        get() = trackMythEventState.getUntracked()

    val trackMythEventState = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Track Mythological Creatures",
            description = "Tracks and saves drops from Mythological Creatures.",
            category = "Events", subcategory = "Mythological",
            searchTags = listOf("Griffin", "Diana", "Myth", "Tracker"),
            i18nName = "skytils.config.events.mythological.track_mythological_creatures",
            i18nCategory = "skytils.config.events",
            i18nSubcategory = "skytils.config.events.mythological"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Trick or Treat Chest Alert",
        description = "Displays a title when any trick or treat chest spawns near you.",
        category = "Events", subcategory = "Spooky",
        searchTags = ["Spooky Chest", "Spooky"],
        i18nName = "skytils.config.events.spooky.trick_or_treat_chest_alert",
        i18nCategory = "skytils.config.events",
        i18nSubcategory = "skytils.config.events.spooky"
    )
    var trickOrTreatChestAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Shiny Orb Waypoints",
        description = "Creates a waypoint of where your shiny orbs are",
        category = "Events", subcategory = "Technoblade",
        searchTags = ["Pig"],
        i18nName = "skytils.config.events.technoblade.show_shiny_orb_waypoints",
        i18nCategory = "skytils.config.events",
        i18nSubcategory = "skytils.config.events.technoblade"
    )
    var shinyOrbWaypoints = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Shiny Pig Locations",
        description = "Shows the location of the pig and draws a line to its orb.",
        category = "Events", subcategory = "Technoblade",
        searchTags = ["Pig"],
        i18nName = "skytils.config.events.technoblade.show_shiny_pig_locations",
        i18nCategory = "skytils.config.events",
        i18nSubcategory = "skytils.config.events.technoblade"
    )
    var shinyPigLocations = false

    @Property(
        type = PropertyType.SWITCH, name = "Plot Cleanup Helper",
        description = "Makes flowers and grass more visible by rendering them as sponges.",
        category = "Farming", subcategory = "Garden",
        i18nName = "skytils.config.farming.garden.plot_cleanup_helper",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.garden"
    )
    var gardenPlotCleanupHelper = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Sam's Scythe Blocks",
        description = "Shows the blocks which will be broken when using Sam's Scythe or Garden Scythe.",
        category = "Farming", subcategory = "Garden",
        i18nName = "skytils.config.farming.garden.show_sams_scythe_blocks",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.garden"
    )
    var showSamScytheBlocks = false

    @Property(
        type = PropertyType.COLOR, name = "Color of Sam's Scythe Marked Blocks",
        description = "Sets the color of the highlighted blocks.",
        category = "Farming", subcategory = "Garden",
        i18nName = "skytils.config.farming.garden.color_of_sams_scythe_marked_blocks",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.garden"
    )
    var samScytheColor = Color(255, 0, 0, 50)

    @Property(
        type = PropertyType.SWITCH, name = "Visitor Offer Helper",
        description = "Displays information about visitor offers.",
        category = "Farming", subcategory = "Garden",
        i18nName = "skytils.config.farming.garden.visitor_offer_helper",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.garden"
    )
    var visitorOfferHelper = false

    @Property(
        type = PropertyType.SWITCH, name = "Visitor Notifications",
        description = "Sends a message in chat when a visitor is at your garden.",
        category = "Farming", subcategory = "Garden",
        i18nName = "skytils.config.farming.garden.visitor_notifications",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.garden"
    )
    var visitorNotifications = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Farming RNG Titles",
        description = "Removes the titles that show up after getting a drop with Pumpkin Dicer / Melon Dicer",
        category = "Farming", subcategory = "Quality of Life",
        i18nName = "skytils.config.farming.quality_of_life.hide_farming_rng_titles",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.quality_of_life"
    )
    var hideFarmingRNGTitles = false

    @Property(
        type = PropertyType.SWITCH, name = "Hungry Hiker Solver",
        description = "Tells you what item the Hungry Hiker wants.",
        category = "Farming", subcategory = "Solvers",
        i18nName = "skytils.config.farming.solvers.hungry_hiker_solver",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.solvers"
    )
    var hungryHikerSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Treasure Hunter Solver",
        description = "Tells you where the Treasure Hunter's treasure is.",
        category = "Farming", subcategory = "Solvers",
        i18nName = "skytils.config.farming.solvers.treasure_hunter_solver",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.solvers"
    )
    var treasureHunterSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Click to Accept Trapper Task",
        description = "Open chat, then click anywhere on screen to accept Trapper Task.",
        category = "Farming", subcategory = "Quality of Life",
        i18nName = "skytils.config.farming.quality_of_life.click_to_accept_trapper_task",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.quality_of_life"
    )
    var acceptTrapperTask = true

    @Property(
        type = PropertyType.SWITCH, name = "Trapper Cooldown Alarm",
        description = "Quickly plays five notes once the Trapper is off cooldown.",
        category = "Farming", subcategory = "Quality of Life",
        i18nName = "skytils.config.farming.quality_of_life.trapper_cooldown_alarm",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.quality_of_life"
    )
    var trapperPing = false

    @Property(
        type = PropertyType.SWITCH, name = "Talbot's Theodolite Helper",
        description = "Shows Y coordinate bounds based on Talbot's Theodolite output",
        category = "Farming", subcategory = "Quality of Life",
        i18nName = "skytils.config.farming.quality_of_life.talbots_theodolite_helper",
        i18nCategory = "skytils.config.farming",
        i18nSubcategory = "skytils.config.farming.quality_of_life"
    )
    var talbotsTheodoliteHelper = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Non-Nametag Armor Stands on Kuudra",
        description = "Hides non nametag armor stands on Kuudra Island.",
        category = "Kuudra", subcategory = "Performance",
        i18nName = "skytils.config.kuudra.performance.hide_nonnametag_armor_stands_on_kuudra",
        i18nCategory = "skytils.config.kuudra",
        i18nSubcategory = "skytils.config.kuudra.performance"
    )
    var kuudraHideNonNametags = false

    val kuudraChestProfit = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Kuudra Chest Profit",
            description = "Shows the estimated profit for items from chests in Kuudra.",
            category = "Kuudra", subcategory = "Price Checking",
            i18nName = "skytils.config.kuudra.price_checking.kuudra_chest_profit",
            i18nCategory = "skytils.config.kuudra",
            i18nSubcategory = "skytils.config.kuudra.price_checking"
        ),
        false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Kuudra Chest Profit Includes Essence",
        description = "Include essence when calculating Kuudra Chest Profit.",
        category = "Kuudra", subcategory = "Price Checking",
        i18nName = "skytils.config.kuudra.price_checking.kuudra_chest_profit_includes_essence",
        i18nCategory = "skytils.config.kuudra",
        i18nSubcategory = "skytils.config.kuudra.price_checking"
    )
    var kuudraChestProfitIncludesEssence = true

    @Property(
        type = PropertyType.SWITCH, name = "Kuudra Chest Profit Subtracts Key",
        description = "Deduct the estimated cost of the Kuudra key used to open the Paid Chest.\nNPC discounts and soulbound items are not accounted for.",
        category = "Kuudra", subcategory = "Price Checking",
        i18nName = "skytils.config.kuudra.price_checking.kuudra_chest_profit_subtracts_key",
        i18nCategory = "skytils.config.kuudra",
        i18nSubcategory = "skytils.config.kuudra.price_checking"
    )
    var kuudraChestProfitCountsKey = true

    @Property(
        type = PropertyType.SWITCH, name = "Show Kuudra Lowest BIN Price",
        description = "Shows the lowest Buy It Now price (including attributes) for various items in Skyblock.",
        category = "Kuudra", subcategory = "Price Checking",
        i18nName = "skytils.config.kuudra.price_checking.show_kuudra_lowest_bin_price",
        i18nCategory = "skytils.config.kuudra",
        i18nSubcategory = "skytils.config.kuudra.price_checking"
    )
    var showKuudraLowestBinPrice = false

    //# if MC>12000
    @Property(
        type = PropertyType.SWITCH, name = "Glass Pane Desync Fix",
        description = "Fixes glass pane shape desync in modern versions.",
        category = "Mining", subcategory = "Quality of Life",
        i18nName = "skytils.config.mining.quality_of_life.glass_pane_desync_fix",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.quality_of_life"
    )
    var glassPaneDesync = false
    //# endif

    @Property(
        type = PropertyType.SWITCH, name = "Dark Mode Mist",
        description = "Replaces colors in The Mist with darker variants.",
        category = "Mining", subcategory = "Quality of Life",
        i18nName = "skytils.config.mining.quality_of_life.dark_mode_mist",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.quality_of_life"
    )
    var darkModeMist = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Completed Commissions",
        description = "Marks completed commissions in the menu with a red background.",
        category = "Mining", subcategory = "Quality of Life",
        i18nName = "skytils.config.mining.quality_of_life.highlight_completed_commissions",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.quality_of_life"
    )
    var highlightCompletedCommissions = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Disabled HOTM Perks",
        description = "Marks disabled perks in the menu with a red background.",
        category = "Mining", subcategory = "Quality of Life",
        i18nName = "skytils.config.mining.quality_of_life.highlight_disabled_hotm_perks",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.quality_of_life"
    )
    var highlightDisabledHOTMPerks = false

    @Property(
        type = PropertyType.SWITCH, name = "More Visible Ghosts",
        description = "Makes ghosts more visible in the Dwarven Mines.\nThis is allowed on the Hypixel network and can be done in Vanilla.",
        category = "Mining", subcategory = "Quality of Life",
        i18nName = "skytils.config.mining.quality_of_life.more_visible_ghosts",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.quality_of_life"
    )
    var moreVisibleGhosts = false

    @Property(
        type = PropertyType.SWITCH, name = "Powder Ghast Ping",
        description = "Displays a title on your screen when a Powder Ghast spawns.",
        category = "Mining", subcategory = "Quality of Life",
        i18nName = "skytils.config.mining.quality_of_life.powder_ghast_ping",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.quality_of_life"
    )
    var powerGhastPing = false

    @Property(
        type = PropertyType.SWITCH, name = "Raffle Warning",
        description = "Displays a title on your screen 15 seconds from the ending of the raffle.",
        category = "Mining", subcategory = "Quality of Life",
        i18nName = "skytils.config.mining.quality_of_life.raffle_warning",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.quality_of_life"
    )
    var raffleWarning = false

    @Property(
        type = PropertyType.SWITCH, name = "Raffle Waypoint",
        description = "Displays a waypoint on your screen to the raffle box after you deposit a ticket.",
        category = "Mining", subcategory = "Quality of Life",
        i18nName = "skytils.config.mining.quality_of_life.raffle_waypoint",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.quality_of_life"
    )
    var raffleWaypoint = false

    @Property(
        type = PropertyType.SWITCH, name = "Recolor Carpets",
        description = "Changes the color of carpets in the Dwarven Mines to red.",
        category = "Mining", subcategory = "Quality of Life",
        i18nName = "skytils.config.mining.quality_of_life.recolor_carpets",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.quality_of_life"
    )
    var recolorCarpets = false

    @Property(
        type = PropertyType.SWITCH, name = "Skymall Reminder",
        description = "Reminds you every Skyblock day to check your Skymall perk while in the Dwarven Mines.",
        category = "Mining", subcategory = "Quality of Life",
        i18nName = "skytils.config.mining.quality_of_life.skymall_reminder",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.quality_of_life"
    )
    var skymallReminder = false

    @Property(
        type = PropertyType.SWITCH, name = "Fetchur Solver",
        description = "Tells you what item Fetchur wants.",
        category = "Mining", subcategory = "Solvers",
        i18nName = "skytils.config.mining.solvers.fetchur_solver",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.solvers"
    )
    var fetchurSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Puzzler Solver",
        description = "Shows which block to mine for Puzzler.",
        category = "Mining", subcategory = "Solvers",
        i18nName = "skytils.config.mining.solvers.puzzler_solver",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.solvers"
    )
    var puzzlerSolver = false

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows Death Waypoints",
        description = "Drops a waypoint to where you last died in the Crystal Hollows.",
        category = "Mining", subcategory = "Crystal Hollows",
        i18nName = "skytils.config.mining.crystal_hollows.crystal_hollows_death_waypoints",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.crystal_hollows"
    )
    var crystalHollowDeathWaypoint = false

    val crystalHollowMap = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Crystal Hollows map",
            description = "Shows a map to see in which part of the crystal hollows you are and saves locations of special places.",
            category = "Mining", subcategory = "Crystal Hollows",
            i18nName = "skytils.config.mining.crystal_hollows.crystal_hollows_map",
            i18nCategory = "skytils.config.mining",
            i18nSubcategory = "skytils.config.mining.crystal_hollows"
        ), false
    )

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Crystal Hollows map player arrow scale",
        description = "Scale the arrow indicating the player on the crystal hollows map",
        category = "Mining", subcategory  = "Crystal Hollows",
        minF = 0.5f, maxF = 10f, decimalPlaces = 2,
        i18nName = "skytils.config.mining.crystal_hollows.player_arrow_scaling",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.crystal_hollows"
    )
    var crystalHollowsMapPlayerScale = 2.25F

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows map special places",
        description = "Show special places on the map (like Lost Precusor City).",
        category = "Mining", subcategory = "Crystal Hollows",
        i18nName = "skytils.config.mining.crystal_hollows.crystal_hollows_map_special_places",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.crystal_hollows"
    )
    var crystalHollowMapPlaces = false

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows waypoints",
        description = "Shows waypoints to special places inside the Crystal Hollows.",
        category = "Mining", subcategory = "Crystal Hollows",
        i18nName = "skytils.config.mining.crystal_hollows.crystal_hollows_waypoints",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.crystal_hollows"
    )
    var crystalHollowWaypoints = false

    @Property(
        type = PropertyType.SWITCH, name = "King Yolkar waypoint",
        description = "Adds a waypoint for King Yolkar upon interacting with him",
        category = "Mining", subcategory = "Crystal Hollows",
        i18nName = "skytils.config.mining.crystal_hollows.king_yolkar_waypoint",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.crystal_hollows"
    )
    var kingYolkarWaypoint = false

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows chat coordinates grabber",
        description = "When coordinates are shared in chat asks which one it is and displays a waypoint there and shows it on the map.",
        category = "Mining", subcategory = "Crystal Hollows",
        i18nName = "skytils.config.mining.crystal_hollows.crystal_hollows_chat_coordinates_grabber",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.crystal_hollows"
    )
    var hollowChatCoords = false

    @Property(
        type = PropertyType.SWITCH, name = "Crystal Hollows Treasure Helper",
        description = "Helps you open treasure chests in the Crystal Hollows.",
        category = "Mining", subcategory = "Crystal Hollows",
        i18nName = "skytils.config.mining.crystal_hollows.crystal_hollows_treasure_helper",
        i18nCategory = "skytils.config.mining",
        i18nSubcategory = "skytils.config.mining.crystal_hollows"
    )
    var chTreasureHelper = false

    @Property(
        type = PropertyType.SWITCH, name = "Chat Tabs",
        description = "Creates various tabs to organize chat.",
        category = "Miscellaneous", subcategory = "Chat Tabs",
        i18nName = "skytils.config.miscellaneous.chat_tabs.chat_tabs",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.chat_tabs"
    )
    var chatTabs = false

    @Property(
        type = PropertyType.SWITCH, name = "Pre-fill Chat Commands",
        description = "Auto fills the respective command for each tab.",
        category = "Miscellaneous", subcategory = "Chat Tabs",
        i18nName = "skytils.config.miscellaneous.chat_tabs.prefill_chat_commands",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.chat_tabs"
    )
    var preFillChatTabCommands = false

    @Property(
        type = PropertyType.SWITCH, name = "Auto Switch Chat Channel",
        description = "Automatically types the command to switch to a certain channel.",
        category = "Miscellaneous", subcategory = "Chat Tabs",
        i18nName = "skytils.config.miscellaneous.chat_tabs.auto_switch_chat_channel",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.chat_tabs"
    )
    var autoSwitchChatChannel = false

    @Property(
        type = PropertyType.SWITCH, name = "Copy Chat Messages",
        description = "Copy chat messages with control + click.",
        category = "Miscellaneous", subcategory = "Chat Tabs",
        i18nName = "skytils.config.miscellaneous.chat_tabs.copy_chat_messages",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.chat_tabs"
    )
    var copyChat = false

    @Property(
        type = PropertyType.SWITCH, name = "Boss Bar Fix",
        description = "Attempts to stop boss bars from disappearing.",
        category = "Miscellaneous", subcategory = "Fixes",
        i18nName = "skytils.config.miscellaneous.fixes.boss_bar_fix",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.fixes"
    )
    var bossBarFix = false

    @Property(
        type = PropertyType.SWITCH, name = "Fix Falling Sand Rendering",
        description = "Adds a check to rendering in order to prevent crashes.",
        category = "Miscellaneous", subcategory = "Fixes",
        i18nName = "skytils.config.miscellaneous.fixes.fix_falling_sand_rendering",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.fixes"
    )
    var fixFallingSandRendering = false

    @Property(
        type = PropertyType.SWITCH, name = "Fix World Time",
        description = "Fixes world time on other mods being messed up due to certain mods.",
        category = "Miscellaneous", subcategory = "Fixes",
        i18nName = "skytils.config.miscellaneous.fixes.fix_world_time",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.fixes"
    )
    var fixWorldTime = false

    @Property(
        type = PropertyType.SWITCH, name = "Prevent Log Spam",
        description = "Prevents your logs from being spammed with exceptions while on Hypixel.",
        category = "Miscellaneous", subcategory = "Fixes",
        i18nName = "skytils.config.miscellaneous.fixes.prevent_log_spam",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.fixes"
    )
    var preventLogSpam = true

    @Property(
        type = PropertyType.SWITCH, name = "Twitch Fix",
        description = "Fix twitch stuff.",
        category = "Miscellaneous", subcategory = "Fixes",
        i18nName = "skytils.config.miscellaneous.fixes.twitch_fix",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.fixes"
    )
    var twitchFix = true

    @Property(
        type = PropertyType.SWITCH, name = "Price Paid",
        description = "Records and shows the price you paid for certain items.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.price_paid",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var pricePaid = false

    @Property(
        type = PropertyType.SWITCH, name = "Block Zapper: Left Click to Undo",
        description = "Left clicking the block zapper will automatically run /undozap",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.block_zapper_left_click_to_undo",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var blockZapperLeftClickUndo = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Block Animation",
        description = "Removes the block animation on swords.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.disable_block_animation",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var disableBlockAnimation = false

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Dropped Item Size",
        description = "Change the size of dropped items.",
        category = "Miscellaneous", subcategory = "Items",
        maxF = 5f,
        decimalPlaces = 2,
        i18nName = "skytils.config.miscellaneous.items.dropped_item_size",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var itemDropScale = 1f

    @Property(
        type = PropertyType.SWITCH, name = "Hide Implosion Particles",
        description = "Removes the explosion created by the Implosion ability.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.hide_implosion_particles",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var hideImplosionParticles = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Midas Staff Gold",
        description = "Prevents the gold blocks from Molten Wave from rendering, leaving only the particles.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.hide_midas_staff_gold",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var hideMidasStaffGoldBlocks = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Filled Bazaar Orders",
        description = "Highlights 100%% filled orders in the bazaar.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.highlight_filled_bazaar_orders",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var highlightFilledBazaarOrders = false

    var itemCooldownDisplay = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Item Cooldown Display",
            description = "Displays the cooldowns for your items. Items must be whitelisted with the /trackcooldown command.",
            category = "Miscellaneous", subcategory = "Items",
            searchTags = listOf("Wither Impact", "Hyperion", "Wither Shield"),
            i18nName = "skytils.config.miscellaneous.items.item_cooldown_display",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.items"
        ), false
    )

    @Property(
        type = PropertyType.SELECTOR, name = "Item Stars Display",
        description = "Changes the way Item Stars are displayed on Items.",
        category = "Miscellaneous", subcategory = "Items",
        options = ["Normal", "Old", "Compact"],
        searchTags = ["1.3.0-pre4"],
        i18nName = "skytils.config.miscellaneous.items.item_stars_display",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var starDisplayType = 0

    @Property(
        type = PropertyType.SWITCH, name = "Show Item Quality ",
        description = "Shows the base stat boost and item tier.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_item_quality",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showItemQuality = false

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Head Display Size",
        description = "Change the size of heads in your inventory.",
        category = "Miscellaneous", subcategory = "Items",
        maxF = 2f,
        decimalPlaces = 2,
        i18nName = "skytils.config.miscellaneous.items.head_display_size",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var largerHeadScale = 1f

    @Property(
        type = PropertyType.SWITCH, name = "Prevent Placing Weapons",
        description = "Stops the game from trying to place the Flower of Truth, Moody Grappleshot, Spirit Sceptre, Pumpkin Launcher, Weird Tuba, and Gemstone Gauntlet items.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.prevent_placing_weapons",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var preventPlacingWeapons = false

    val witherShieldCooldown = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Wither Shield Cooldown Tracker",
            description = "Displays the cooldowns for your wither shield (because apparently people can't follow directions)",
            category = "Miscellaneous", subcategory = "Items",
            searchTags = listOf("Wither Impact", "Hyperion", "Wither Shield", "Scylla", "Astraea", "Valkyrie"),
            i18nName = "skytils.config.miscellaneous.items.wither_shield_cooldown_tracker",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.items"
        ), false
    )

    val assumeWitherImpactState = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Wither Shield has Wither Impact",
            description = "Get better tbh imagine only 1 scroll",
            category = "Miscellaneous", subcategory = "Items",
            searchTags = listOf("Wither Impact", "Hyperion", "Wither Shield", "Scylla", "Astraea", "Valkyrie"),
            i18nName = "skytils.config.miscellaneous.items.wither_shield_has_wither_impact",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.items"
        ),
        true
    )

    @Property(
        type = PropertyType.SWITCH, name = "Show Enchanted Book Abbreviation",
        description = "Shows the abbreviated name of books with only 1 enchantment.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_enchanted_book_abbreviation",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showEnchantedBookAbbreviation = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Attribute Shard Abbreviation",
        description = "Shows the abbreviated name of shards with only 1 enchantment.",
        category = "Miscellaneous", subcategory = "Items",
        searchTags = ["1.3.0-pre1"],
        i18nName = "skytils.config.miscellaneous.items.show_attribute_shard_abbreviation",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showAttributeShardAbbreviation = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Attribute Shard Level",
        description = "Shows the level of Attribute Shards as the stack size.",
        category = "Miscellaneous", subcategory = "Items",
        searchTags = ["1.3.0-pre1"],
        i18nName = "skytils.config.miscellaneous.items.show_attribute_shard_level",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showAttributeShardLevel = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Enchanted Book Tier",
        description = "Shows the tier of books with only 1 enchantment.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_enchanted_book_tier",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showEnchantedBookTier = false

    @Property(
        type = PropertyType.SWITCH, name = "Combine Helper",
        description = "Shows if you're combining incompatible books or attribute shards",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.combine_helper",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var combineHelper = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Etherwarp Teleport Position",
        description = "Shows the block you will teleport to with the Etherwarp Transmission ability.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_etherwarp_teleport_position",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showEtherwarpTeleportPos = false

    @Property(
        type = PropertyType.COLOR, name = "Etherwarp Teleport Position Color",
        description = "Color the thing that shows the block you will teleport to with the Etherwarp Transmission ability.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.etherwarp_teleport_position_color",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showEtherwarpTeleportPosColor = Color(0, 0, 255, 69)

    @Property(
        type = PropertyType.SWITCH, name = "Show Gemstones",
        description = "Shows the added gemstones on items.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_gemstones",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showGemstones = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Head Floor Number",
        description = "Shows the corresponding floor number for Diamond/Gold Catacombs Heads.",
        category = "Miscellaneous", subcategory = "Items",
        searchTags = ["Dungeons"],
        i18nName = "skytils.config.miscellaneous.items.show_head_floor_number",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showHeadFloorNumber = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Item Origin",
        description = "Shows the origin on items.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_item_origin",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showOrigin = false

    @Property(
        type = PropertyType.SWITCH, name = "Show New Year Cake Year",
        description = "Shows the year of a New Year Cake as the stack size.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_new_year_cake_year",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showNYCakeYear = false

    @Property(
        type = PropertyType.SWITCH, name = "Show NPC Sell Price",
        description = "Shows the NPC Sell Price on certain items.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_npc_sell_price",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showNPCSellPrice = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Potion Tier",
        description = "Shows the tier of potions as the stack size.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_potion_tier",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showPotionTier = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Pet Candies",
        description = "Shows the number of candies used as the stack size",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_pet_candies",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showPetCandies = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Item Star Count",
        description = "Shows the amount of stars on items as the stack size.",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.show_item_star_count",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showStarCount = false

    var stackingEnchantProgressDisplay = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Stacking Enchant Progress Display",
            description = "Displays the progress for the held item's stacking enchant.",
            category = "Miscellaneous", subcategory = "Items",
            i18nName = "skytils.config.miscellaneous.items.stacking_enchant_progress_display",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.items"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Radioactive Bonus",
        description = "Shows the current Critical Damage bonus from Tarantula helmet",
        category = "Miscellaneous", subcategory = "Items",
        i18nName = "skytils.config.miscellaneous.items.radioactive_bonus",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.items"
    )
    var showRadioactiveBonus = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Item Rarity",
        description = "Shows the rarity of an item in the color",
        category = "Miscellaneous", subcategory = "Item Rarity",
        i18nName = "skytils.config.miscellaneous.item_rarity.show_item_rarity",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.item_rarity"
    )
    var showItemRarity = false

    @Property(
        type = PropertyType.SELECTOR, name = "Item Rarity Shape",
        description = "Select the shape of the item rarity's background.\n" +
                "§cCustom is made for Texture Pack makers, the png must be named customrarity.png.\n" +
                "§cDon't use it if you don't know what you are doing",
        category = "Miscellaneous", subcategory = "Item Rarity",
        options = ["Circle", "Square", "Square Outline", "Outline", "Custom", "Item Outline"],
        i18nName = "skytils.config.miscellaneous.item_rarity.item_rarity_shape",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.item_rarity"
    )
    var itemRarityShape = 0

    @Property(
        type = PropertyType.SWITCH, name = "Show Pet Rarity",
        description = "Shows the rarity of a pet in the color",
        category = "Miscellaneous", subcategory = "Item Rarity",
        i18nName = "skytils.config.miscellaneous.item_rarity.show_pet_rarity",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.item_rarity"
    )
    var showPetRarity = false

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Item Rarity Transparency",
        description = "How opaque the rarity color will be",
        category = "Miscellaneous", subcategory = "Item Rarity",
        i18nName = "skytils.config.miscellaneous.item_rarity.item_rarity_transparency",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.item_rarity"
    )
    var itemRarityOpacity = 0.75f

    @Property(
        type = PropertyType.SWITCH, name = "Only Collect Enchanted Items",
        description = "Prevents you from collecting unenchanted items from minions if there is a Super Compactor.",
        category = "Miscellaneous", subcategory = "Minions",
        i18nName = "skytils.config.miscellaneous.minions.only_collect_enchanted_items",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.minions"
    )
    var onlyCollectEnchantedItems = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Minion Tier",
        description = "Shows the tier of minions as the stack size.",
        category = "Miscellaneous", subcategory = "Minions",
        i18nName = "skytils.config.miscellaneous.minions.show_minion_tier",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.minions"
    )
    var showMinionTier = false

    @Property(
        type = PropertyType.SWITCH, name = "Always Show Item Name Highlight",
        description = "Always shows the item name highlight.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.always_show_item_name_highlight",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var alwaysShowItemHighlight = false

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Low Health Vignette Threshold",
        description = "Render a red vignette on the edge of the screen when your health drops below this threshold. Set to 0.0%% to disable.\n§cThis feature will temporarily disable HUD Caching while active.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.low_health_vignette_threshold",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var lowHealthVignetteThreshold = 0.0f

    @Property(
        type = PropertyType.COLOR, name = "Low Health Vignette Color",
        description = "The color of the vignette that is shown when your health is below the threshold.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.low_health_vignette_color",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var lowHealthVignetteColor: Color = Color.RED

    @Property(
        type = PropertyType.SWITCH, name = "Hide Tooltips while on Storage",
        description = "Hides the tooltips of backpacks and ender chest while on the Storage GUI",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.hide_tooltips_while_on_storage",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var hideTooltipsOnStorage = false

    @Property(
        type = PropertyType.SWITCH, name = "Copy Deaths to Clipboard",
        description = "Copies the deaths outside dungeons to your clipboard after clicking them in the chat.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.copy_deaths_to_clipboard",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var copyDeathToClipboard = false

    @Property(
        type = PropertyType.SWITCH, name = "Auto Copy RNG Drops to Clipboard",
        description = "Automatically copies RNG drops to your clipboard.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.auto_copy_rng_drops_to_clipboard",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var autoCopyRNGDrops = false

    @Property(
        type = PropertyType.SWITCH, name = "Also Copy Very Rare Drops to Clipboard",
        description = "Automatically copies very rare drops to your clipboard.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.also_copy_very_rare_drops_to_clipboard",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var autoCopyVeryRareDrops = false

    @Property(
        type = PropertyType.SWITCH, name = "Dupe Tracker",
        description = "Tries to track duplicated items on the auction house.\nThis will not catch every single duped item.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.dupe_tracker",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var dupeTracker = false

    @Property(
        type = PropertyType.COLOR, name = "Dupe Tracker Overlay Color",
        description = "Changes the color of the Dupe Tracker Overlay.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.dupe_tracker_overlay_color",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var dupeTrackerOverlayColor = Color.BLACK.withAlpha(169)

    val golemSpawnTimer = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Endstone Protector Spawn Timer",
            description = "Counts down the time until the Endstone Protector spawns.",
            category = "Miscellaneous", subcategory = "Other",
            i18nName = "skytils.config.miscellaneous.other.endstone_protector_spawn_timer",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.other"
        ), false
    )

    val playersInRangeDisplay = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Players in Range Display",
            description = "Shows the amount of players within a 30 block radius.",
            category = "Miscellaneous", subcategory = "Other",
            searchTags = listOf("Dolphin", "Legion", "Bobbin' Time"),
            i18nName = "skytils.config.miscellaneous.other.players_in_range_display",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.other"
        ), false
    )

    val summoningEyeDisplay = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Placed Summoning Eye Display",
            description = "Shows the amount of summoning eyes placed in the Dragon's Nest.",
            category = "Miscellaneous", subcategory = "Other",
            i18nName = "skytils.config.miscellaneous.other.placed_summoning_eye_display",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.other"
        ), false
    )

    var pingDisplay = property(
        PropertyAttributesExt(
            type = PropertyType.SELECTOR, name = "Ping Display",
            description = "Shows your ping to the current server, similar to the /skytils ping command.\nYou must be in a GUI or not moving in order to queue a ping.\nThere is a tiny chance that this will cause you to be punished.",
            category = "Miscellaneous", subcategory = "Other",
            options = listOf("Off", "Server List", "Packet"),
            i18nName = "skytils.config.miscellaneous.other.ping_display",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.other"
        ), 0
    )

    @Property(
        type = PropertyType.SWITCH, name = "Random Stuff",
        description = "Random stuff that may or may not increase your FPS.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.random_stuff",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var randomStuff = false

    @Property(
        type = PropertyType.SWITCH, name = "Scam Check",
        description = "Check if the other party is a known scammer when trading.\nThis relies on databases not controlled by Skytils and may not contain all scammers.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.scam_check",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var scamCheck = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Bestiary Level",
        description = "Shows the bestiary level as the stack size.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.show_bestiary_level",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var showBestiaryLevel = false

    val showSelectedArrowDisplay = property(
        PropertyAttributesExt(
            PropertyType.SWITCH, name = "Show Selected Arrow",
            description = "Shows your current selected arrow.",
            category = "Miscellaneous", subcategory = "Other",
            i18nName = "skytils.config.miscellaneous.other.show_selected_arrow",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.other"
        ), false
    )

    val showWorldAgeState = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Show World Age",
            description = "Displays the day count of the current server.",
            category = "Miscellaneous", subcategory = "Other",
            i18nName = "skytils.config.miscellaneous.other.show_world_age",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.other"
        ),
        false
    )
    val showWorldAge: Boolean
        get() = showWorldAgeState.getUntracked()

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Transparent Armor Layer",
        description = "Changes the transparency of your armor layer.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.transparent_armor_layer",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var transparentArmorLayer = 1f

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Head Layer Transparency",
        description = "Changes the transparency of your head layer.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.head_layer_transparency",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var transparentHeadLayer = 1f

    @Property(
        type = PropertyType.SWITCH, name = "Fix Summon Skin",
        description = "§c[WIP] §rChanges the summon's skin to the correct one.\n§cThis is very broken and may crash your game.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.fix_summon_skin",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var fixSummonSkin = false

    @Property(
        type = PropertyType.SWITCH, name = "Use Player Skin",
        description = "Uses the player's skin for necromancy mobs.",
        category = "Miscellaneous", subcategory = "Other",
        i18nName = "skytils.config.miscellaneous.other.use_player_skin",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.other"
    )
    var usePlayerSkin = false

    @Property(
        type = PropertyType.SWITCH, name = "Custom Auction Price Input",
        description = "Displays Skytils' own auction input GUI instead of a sign.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.custom_auction_price_input",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var betterAuctionPriceInput = false

    val containerSellValue
        get() = containerSellValueState.getUntracked()

    val containerSellValueState = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Container Sell Value",
            description = "Display the lowest BIN prices for the most valuable items in backpacks, ender chest pages, minions, and island chests.",
            category = "Miscellaneous", subcategory = "Quality of Life",
            i18nName = "skytils.config.miscellaneous.quality_of_life.container_sell_value",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Include Item Modifiers",
        description = "Includes potato books, recombobulators, enchantments, and master stars in the item price calculations.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.include_item_modifiers",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var includeModifiersInSellValue = true

    @Property(
        type = PropertyType.NUMBER, name = "Max Displayed Items",
        description = "The maximum amount of items to display in the Container Sell Value GUI.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        min = 5, max = 30, increment = 1,
        i18nName = "skytils.config.miscellaneous.quality_of_life.max_displayed_items",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var containerSellValueMaxItems = 20

    @Property(
        type = PropertyType.SWITCH, name = "Disable Enderman Teleportation",
        description = "Removes the enderman teleport effect.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.disable_enderman_teleportation",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var disableEndermanTeleport = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Night Vision",
        description = "Removes the vanilla effects of Night Vision.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.disable_night_vision",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var disableNightVision = false

    @Property(
        type = PropertyType.SLIDER, name = "Dungeon Pot Lock",
        description = "Only allows you to purchase this dungeon pot from Ophelia, no other items.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        max = 7,
        i18nName = "skytils.config.miscellaneous.quality_of_life.dungeon_pot_lock",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var dungeonPotLock = 0

    @Property(
        type = PropertyType.SWITCH, name = "Enchant Glint Fix",
        description = "Fixes some items not having the enchantment glint.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.enchant_glint_fix",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var enchantGlintFix = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Absorption Hearts",
        description = "Prevents the game from rendering absorption hearts.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_absorption_hearts",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideAbsorption = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Air Display",
        description = "Prevents the game from rendering the air bubbles while underwater.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_air_display",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideAirDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Armor Display",
        description = "Prevents the game from rendering the vanilla armor points.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_armor_display",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideArmorDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Cheap Coins",
        description = "Prevents the game from rendering cheap coins.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_cheap_coins",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideCheapCoins = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Dying Mobs",
        description = "Removes dead/dying mobs from your screen.",
        category = "Dungeons", subcategory = "Quality of Life",
        i18nName = "skytils.config.dungeons.quality_of_life.hide_dying_mobs",
        i18nCategory = "skytils.config.dungeons",
        i18nSubcategory = "skytils.config.dungeons.quality_of_life"
    )
    var hideDyingMobs = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Fire on Entities",
        description = "Prevents the game from rendering fire on burning entities.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_fire_on_entities",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideEntityFire = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Fishing Hooks",
        description = "Hides fishing hooks from other players",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_fishing_hooks",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideFishingHooks = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Hunger Display",
        description = "Prevents the game from rendering the vanilla hunger points.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_hunger_display",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideHungerDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Jerry Rune",
        description = "Prevents the game from rendering the items spawned by the Jerry rune.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_jerry_rune",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideJerryRune = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Lightning",
        description = "Prevents all lightning from rendering.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_lightning",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideLightning = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Mob Death Particles",
        description = "Hides the smoke particles created when mobs die.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_mob_death_particles",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideDeathParticles = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Pet Health Display",
        description = "Hides the Vanilla pet hearts.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_pet_health_display",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hidePetHealth = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Players in Spawn",
        description = "Hides players in the spawn area at the Hub.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_players_in_spawn",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hidePlayersInSpawn = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Potion Effects in Inventory",
        description = "Prevents the game from rendering the potion effects in inventories while in Skyblock.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_potion_effects_in_inventory",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hidePotionEffects = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Scoreboard Score",
        description = "Removes the red score numbers on the scoreboard.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_scoreboard_score",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideScoreboardScore = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Vanilla Health Display",
        description = "Prevents the game from rendering the vanilla heart points.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.hide_vanilla_health_display",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var hideHealthDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Disabled Potion Effects",
        description = "Marks disabled potion effects in the toggle menu.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.highlight_disabled_potion_effects",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var highlightDisabledPotionEffects = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Salvageable Items",
        description = "Highlights items that can be salvaged.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.highlight_salvageable_items",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var highlightSalvageableItems = false

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Dungeon-Sellable Items",
        description = "Highlights dungeon-sellable items such as training weights in Ophelia NPC or Trades menu.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.highlight_dungeonsellable_items",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var highlightDungeonSellableItems = false

    @Property(
        type = PropertyType.SWITCH, name = "Lower Enderman Nametags",
        description = "Lowers the health and nametag of endermen so it's easier to see.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.lower_enderman_nametags",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var lowerEndermanNametags = false

    @Property(
        type = PropertyType.SWITCH, name = "Middle Click GUI Items",
        description = "Replaces left clicks on items with no Skyblock ID with middle clicks.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.middle_click_gui_items",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var middleClickGUIItems = false

    val moveableActionBar = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Moveable Action Bar",
            description = "Allows you to move the action bar as if it were a Skytils HUD element.",
            category = "Miscellaneous", subcategory = "Quality of Life",
            i18nName = "skytils.config.miscellaneous.quality_of_life.moveable_action_bar",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
        ), false
    )

    val moveableItemNameHighlight = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Moveable Item Name Highlight",
            description = "Allows you to move the item name highlight as if it were a Skytils HUD element.",
            category = "Miscellaneous", subcategory = "Quality of Life",
            i18nName = "skytils.config.miscellaneous.quality_of_life.moveable_item_name_highlight",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "No Fire",
        description = "Removes first-person fire overlay when you are burning.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.no_fire",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var noFire = false

    @Property(
        type = PropertyType.SWITCH, name = "No Hurtcam",
        description = "Removes the screen shake when you are hurt.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.no_hurtcam",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var noHurtcam = false

    @Property(
        type = PropertyType.SWITCH, name = "Party Addons",
        description = "Adds a few features to the party list.\n§eNote: Requires Hypixel Language to be set to English. §7(/lang en)",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.party_addons",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var partyAddons = false

    @Property(
        type = PropertyType.SWITCH, name = "Prevent Cursor Reset",
        description = "Prevents the cursor from resetting to the center of the screen when you open a GUI.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.prevent_cursor_reset",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var preventCursorReset = false

    @Property(
        type = PropertyType.SWITCH, name = "Prevent Moving on Death",
        description = "Unpresses all keys on death to prevent you from moving.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.prevent_moving_on_death",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var preventMovingOnDeath = false

    @Property(
        type = PropertyType.SWITCH, name = "Power Orb Lock",
        description = "Prevents placing the power orb if the same or better power orb is within range.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.power_orb_lock",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var powerOrbLock = false

    @Property(
        type = PropertyType.NUMBER, name = "Power Orb Lock Duration",
        description = "Allows overwriting a power orb, if it has less time left than this option.",
        min = 1, max = 120,
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.power_orb_lock_duration",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var powerOrbDuration = 10

    @Property(
        type = PropertyType.SWITCH, name = "Press Enter to confirm Sign Popups",
        description = "Allows pressing enter to confirm a sign popup, such as the bazaar or auction house prices.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.press_enter_to_confirm_sign_popups",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var pressEnterToConfirmSignQuestion = false

    @Property(
        type = PropertyType.BUTTON, name = "Protect Items",
        description = "Prevents you from dropping, salvaging, or selling items that you have selected.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        searchTags = ["Lock", "Slot"],
        i18nName = "skytils.config.miscellaneous.quality_of_life.protect_items",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    fun protectItems() {
        if (ModChecker.canShowNotifications) {
            Notifications.push("Protect Items Help", "Hold the item you'd like to protect, and then run /protectitem.", 5f)
        } else UChat.chat("${Skytils.prefix} §bHold the item you'd like to protect, and then run /protectitem.")
        Skytils.displayScreen = ProtectItemGui()
    }

    @Property(
        type = PropertyType.TEXT, name = "Protect Items Above Value",
        description = "Prevents you from dropping, salvaging, or selling items worth more than this value. Based on Lowest BIN price.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        searchTags = ["Lock"],
        i18nName = "skytils.config.miscellaneous.quality_of_life.protect_items_above_value",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var protectItemBINThreshold = "0"

    @Property(
        type = PropertyType.SWITCH, name = "Protect Starred Items",
        description = "Prevents you from dropping, salvaging, or selling starred dungeon items.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        searchTags = ["Lock"],
        i18nName = "skytils.config.miscellaneous.quality_of_life.protect_starred_items",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var protectStarredItems = false

    val quiverDisplay = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Quiver Display",
            description = "Displays the amount of arrows in your quiver.",
            category = "Miscellaneous", subcategory = "Quality of Life",
            i18nName = "skytils.config.miscellaneous.quality_of_life.quiver_display",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
        ), false
    )

    @Property(
        type = PropertyType.NUMBER, name = "Restock Arrows Warning",
        description = "Shows a warning when your quiver is low on arrows. Set to 0 to disable.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        min = 0, max = 1200, increment = 100,
        i18nName = "skytils.config.miscellaneous.quality_of_life.restock_arrows_warning",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var restockArrowsWarning = 0

    @Property(
        type = PropertyType.SWITCH, name = "Spider's Den Rain Timer",
        description = "Shows the duration of rain in the Spider's Den.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.spiders_den_rain_timer",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var rainTimer = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Arachne Spawn",
        description = "Shows the location of the Arachne Altar when a fragment is placed.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.show_arachne_spawn",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var showArachneSpawn = false

    val showArachneHP = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Show Arachne HP",
            description = "Shows the HP of Arachne on your HUD.",
            category = "Miscellaneous", subcategory = "Quality of Life",
            i18nName = "skytils.config.miscellaneous.quality_of_life.show_arachne_hp",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Show Coins per Bit",
        description = "Shows how many coins you will get per bit spent at the Community Shop.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.show_coins_per_bit",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var showCoinsPerBit = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Coins per Copper",
        description = "Shows how many coins you will get per copper spent at the SkyMart.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.show_coins_per_copper",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var showCoinsPerCopper = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Lowest BIN Price",
        description = "Shows the lowest Buy It Now price for various items in Skyblock.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.show_lowest_bin_price",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var showLowestBINPrice = false

    @Property(
        type = PropertyType.SWITCH, name = "Stop Clicking Non-Salvageable Items",
        description = "Stops you from clicking Non-Salvageable items while in the Salvage menu",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.stop_clicking_nonsalvageable_items",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var stopClickingNonSalvageable = false

    @Property(
        type = PropertyType.SWITCH, name = "View Relic Waypoints",
        description = "Shows the location of all the relics at the Spider's Den.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.view_relic_waypoints",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var relicWaypoints = false

    @Property(
        type = PropertyType.SWITCH, name = "Find Rare Relics",
        description = "Finds rare relics at the Spider's Den as you walk near them.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.find_rare_relics",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var rareRelicFinder = false

    @Property(
        type = PropertyType.BUTTON, name = "Reset Found Relic Waypoints",
        description = "Resets the state of all the relics at the Spider's Den.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.reset_found_relic_waypoints",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    @Suppress("unused")
    fun resetRelicWaypoints() {
        Tracker.getTrackerById("found_spiders_den_relics")!!.doReset()
    }

    @Property(
        type = PropertyType.BUTTON, name = "Potion Duration Notifications",
        description = "Displays a notification when a Potion is about to expire.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.potion_duration_notifications",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    @Suppress("unused")
    fun openPotionEffectTimersGUI() {
        Skytils.displayScreen = PotionNotificationsGui()
    }


    @Property(
        type = PropertyType.SWITCH, name = "Stop Hook Sinking in Lava",
        description = "Stops your fishing hook from sinking in lava.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.stop_hook_sinking_in_lava",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var lavaBobber = false

    @Property(
        type = PropertyType.SWITCH, name = "Fishing Hook Age",
        description = "Shows how long your fishing hook has been cast",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.fishing_hook_age",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var fishingHookAge = false

    val trophyFishTracker = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Trophy Fish Tracker",
            description = "Tracks trophy fish caught.",
            category = "Miscellaneous", subcategory = "Quality of Life",
            i18nName = "skytils.config.miscellaneous.quality_of_life.trophy_fish_tracker",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
        ), false
    )

    @Property(
        type = PropertyType.SWITCH, name = "Show Trophy Fish Totals",
        description = "Shows totals of each trophy fish.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        i18nName = "skytils.config.miscellaneous.quality_of_life.show_trophy_fish_totals",
        i18nCategory = "skytils.config.miscellaneous",
        i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
    )
    var showTrophyFishTotals = false

    val showTotalTrophyFishState = property(
        PropertyAttributesExt(
            type = PropertyType.SWITCH, name = "Show Total Trophy Fish",
            description = "Shows the total of all trophy fish caught.",
            category = "Miscellaneous", subcategory = "Quality of Life",
            i18nName = "skytils.config.miscellaneous.quality_of_life.show_total_trophy_fish",
            i18nCategory = "skytils.config.miscellaneous",
            i18nSubcategory = "skytils.config.miscellaneous.quality_of_life"
        ),
        false
    )

    @Property(
        type = PropertyType.SELECTOR, name = "Autopet Message Hider",
        description = "Removes autopet messages from your chat.",
        category = "Pets", subcategory = "Quality of Life",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.pets.quality_of_life.autopet_message_hider",
        i18nCategory = "skytils.config.pets",
        i18nSubcategory = "skytils.config.pets.quality_of_life"
    )
    var hideAutopetMessages = 0

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Active Pet",
        description = "Highlights the current active pet.",
        category = "Pets", subcategory = "Quality of Life",
        i18nName = "skytils.config.pets.quality_of_life.highlight_active_pet",
        i18nCategory = "skytils.config.pets",
        i18nSubcategory = "skytils.config.pets.quality_of_life"
    )
    var highlightActivePet = false

    @Property(
        type = PropertyType.COLOR, name = "Active Pet Highlight Color",
        description = "Color used to highlight the active pet in.",
        category = "Pets", subcategory = "Quality of Life",
        i18nName = "skytils.config.pets.quality_of_life.active_pet_highlight_color",
        i18nCategory = "skytils.config.pets",
        i18nSubcategory = "skytils.config.pets.quality_of_life"
    )
    var activePetColor = Color(0, 255, 0)

    @Property(
        type = PropertyType.SWITCH, name = "Highlight Favorite Pets",
        description = "Highlight pets marked as favorite.",
        category = "Pets", subcategory = "Quality of Life",
        i18nName = "skytils.config.pets.quality_of_life.highlight_favorite_pets",
        i18nCategory = "skytils.config.pets",
        i18nSubcategory = "skytils.config.pets.quality_of_life"
    )
    var highlightFavoritePets = false

    @Property(
        type = PropertyType.COLOR, name = "Favorite Pet Highlight Color",
        description = "Color used to highlight the favorite pets in.",
        category = "Pets", subcategory = "Quality of Life",
        i18nName = "skytils.config.pets.quality_of_life.favorite_pet_highlight_color",
        i18nCategory = "skytils.config.pets",
        i18nSubcategory = "skytils.config.pets.quality_of_life"
    )
    var favoritePetColor = Color(0, 255, 255)

    @Property(
        type = PropertyType.SWITCH, name = "Pet Item Confirmation",
        description = "Requires a confirmation before using a pet item.",
        category = "Pets", subcategory = "Quality of Life",
        i18nName = "skytils.config.pets.quality_of_life.pet_item_confirmation",
        i18nCategory = "skytils.config.pets",
        i18nSubcategory = "skytils.config.pets.quality_of_life"
    )
    var petItemConfirmation = false

    @Property(
        type = PropertyType.SWITCH, name = "Quad Link Legacy Solver",
        description = "§b[WIP]§r Solves the Quad Link Legacy (CONNECT4) puzzle.\nThis currently does not work.",
        category = "Rift", subcategory = "Solvers",
        i18nName = "skytils.config.rift.solvers.quad_link_legacy_solver",
        i18nCategory = "skytils.config.rift",
        i18nSubcategory = "skytils.config.rift.solvers",
        searchTags = ["Wizardman", "Connect4", "ConnectFOUR"],
    )
    var quadLinkLegacySolver = false

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Revenant RNG Meter",
        description = "Internal value to store current Revenant RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true,
        i18nName = "skytils.config.slayer..current_revenant_rng_meter",
        i18nCategory = "skytils.config.slayer"
    )
    var revRNG = 0f

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Tarantula RNG Meter",
        description = "Internal value to store current Tarantula RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true,
        i18nName = "skytils.config.slayer..current_tarantula_rng_meter",
        i18nCategory = "skytils.config.slayer"
    )
    var taraRNG = 0f

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Sven RNG Meter",
        description = "Internal value to store current Sven RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true,
        i18nName = "skytils.config.slayer..current_sven_rng_meter",
        i18nCategory = "skytils.config.slayer"
    )
    var svenRNG = 0f

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Voidgloom RNG Meter",
        description = "Internal value to store current Voidgloom Seraph RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true,
        i18nName = "skytils.config.slayer..current_voidgloom_rng_meter",
        i18nCategory = "skytils.config.slayer"
    )
    var voidRNG = 0f

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Inferno RNG Meter",
        description = "Internal value to store current Inferno Demonlord RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true,
        i18nName = "skytils.config.slayer..current_inferno_rng_meter",
        i18nCategory = "skytils.config.slayer"
    )
    var blazeRNG = 0f

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Current Bloodfiend RNG Meter",
        description = "Internal value to store current Riftstalker Bloodfiend RNG meter",
        category = "Slayer",
        decimalPlaces = 1,
        maxF = 100f,
        hidden = true,
        i18nName = "skytils.config.slayer..current_bloodfiend_rng_meter",
        i18nCategory = "skytils.config.slayer"
    )
    var vampRNG = 0f

    @Property(
        type = PropertyType.SELECTOR, name = "Carry Mode",
        description = "Allow middle clicking to set your slayer boss.\nDisable this if you are doing your own boss.",
        category = "Slayer", subcategory = "General",
        options = ["Off", "T1", "T2", "T3", "T4", "T5"],
        i18nName = "skytils.config.slayer.general.carry_mode",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.general"
    )
    var slayerCarryMode = 0

    @Property(
        type = PropertyType.SWITCH, name = "Use Hits to Detect Slayer",
        description = "Finds your slayer based on the one you hit the most.",
        category = "Slayer", subcategory = "General",
        i18nName = "skytils.config.slayer.general.use_hits_to_detect_slayer",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.general"
    )
    var useSlayerHitMethod = true

    @Property(
        type = PropertyType.SWITCH, name = "Use Nametag to Detect Slayer",
        description = "Finds your slayer based on the nametag.",
        category = "Slayer", subcategory = "General",
        i18nName = "skytils.config.slayer.general.use_nametag_to_detect_slayer",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.general"
    )
    var useNametagHitMethod = false

    @Property(
        type = PropertyType.SWITCH, name = "Ping when in Atoned Horror Danger Zone",
        description = "Pings when you are standing on the Atoned Horror's TNT target.",
        category = "Slayer", subcategory = "Quality of Life",
        i18nName = "skytils.config.slayer.quality_of_life.ping_when_in_atoned_horror_danger_zone",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.quality_of_life"
    )
    var rev5TNTPing = false

    @Property(
        type = PropertyType.SWITCH, name = "Slayer Boss Hitbox",
        description = "Draws a box around slayer mini-bosses.",
        category = "Slayer", subcategory = "Quality of Life",
        i18nName = "skytils.config.slayer.quality_of_life.slayer_boss_hitbox",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.quality_of_life"
    )
    var slayerBossHitbox = false

    @Property(
        type = PropertyType.SWITCH, name = "Slayer Miniboss Spawn Alert",
        description = "Displays a title when a slayer miniboss spawns.",
        category = "Slayer", subcategory = "Quality of Life",
        i18nName = "skytils.config.slayer.quality_of_life.slayer_miniboss_spawn_alert",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.quality_of_life"
    )
    var slayerMinibossSpawnAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Show RNGesus Meter",
        description = "Shows your current RNGesus meter as the boss bar.",
        category = "Slayer", subcategory = "Quality of Life",
        i18nName = "skytils.config.slayer.quality_of_life.show_rngesus_meter",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.quality_of_life"
    )
    var showRNGMeter = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Slayer Armor Kills",
        description = "Displays the kills on your Final Destination Armor.",
        category = "Slayer", subcategory = "Quality of Life",
        i18nName = "skytils.config.slayer.quality_of_life.show_slayer_armor_kills",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.quality_of_life"
    )
    var showSlayerArmorKills = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Slayer Display",
        description = "Shows your current slayer's health and the time left",
        category = "Slayer", subcategory = "Quality of Life",
        i18nName = "skytils.config.slayer.quality_of_life.show_slayer_display",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.quality_of_life"
    )
    var showSlayerDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Slayer Time to Kill",
        description = "Shows the amount of time used to kill the slayer",
        category = "Slayer", subcategory = "Quality of Life",
        i18nName = "skytils.config.slayer.quality_of_life.show_slayer_time_to_kill",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.quality_of_life"
    )
    var slayerTimeToKill = false

    @Property(
        type = PropertyType.SWITCH, name = "Hide Others' Broken Heart Radiation",
        description = "Removes Broken Heart Radiation from other slayer's while yours is spawned",
        category = "Slayer",
        subcategory = "Voidgloom Seraph",
        i18nName = "skytils.config.slayer.voidgloom_seraph.hide_others_broken_heart_radiation",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var hideOthersBrokenHeartRadiation = false

    @Property(
        PropertyType.SWITCH, name = "Recolor Seraph Boss",
        description = "Changes the color of your Seraph boss based on the phase it is in.\nBeacon takes priority over the other colors.",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        i18nName = "skytils.config.slayer.voidgloom_seraph.recolor_seraph_boss",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var recolorSeraphBoss = false

    @Property(
        PropertyType.COLOR, name = "Seraph Beacon Phase Color",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        allowAlpha = false,
        i18nName = "skytils.config.slayer.voidgloom_seraph.seraph_beacon_phase_color",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var seraphBeaconPhaseColor = Color(255, 255, 255)

    @Property(
        PropertyType.COLOR, name = "Seraph Hits Phase Color",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        allowAlpha = false,
        i18nName = "skytils.config.slayer.voidgloom_seraph.seraph_hits_phase_color",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var seraphHitsPhaseColor = Color(255, 255, 255)

    @Property(
        PropertyType.COLOR, name = "Seraph Normal Phase Color",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        allowAlpha = false,
        i18nName = "skytils.config.slayer.voidgloom_seraph.seraph_normal_phase_color",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var seraphNormalPhaseColor = Color(255, 255, 255)

    val showSeraphDisplay = property(
        PropertyAttributesExt(
            PropertyType.SWITCH, name = "Show Seraph Display",
            description = "§b[WIP] §rShows info about your current Voidgloom Seraph boss.",
            category = "Slayer", subcategory = "Voidgloom Seraph",
            i18nName = "skytils.config.slayer.voidgloom_seraph.show_seraph_display",
            i18nCategory = "skytils.config.slayer",
            i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
        ), false
    )


    /*    @Property(
            PropertyType.SWITCH, name = "Experimental Yang Glyph Detection",
            description = "Testing new detection for Yang Glyphs. Give us feedback on Discord!",
            category = "Slayer", subcategory = "Voidgloom Seraph"

, i18nName = "skytils.config.slayer.voidgloom_seraph.experimental_yang_glyph_detection", i18nCategory = "skytils.config.slayer", i18nSubcategory = "skytils.config.slayer.voidgloom_seraph")*/
    var experimentalYangGlyphDetection = true

    @Property(
        PropertyType.SWITCH, name = "Yang Glyph Ping",
        description = "Alerts you when the Voidgloom Seraph throws down a Yang Glyph(beacon).",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        i18nName = "skytils.config.slayer.voidgloom_seraph.yang_glyph_ping",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var yangGlyphPing = false

    @Property(
        PropertyType.SWITCH, name = "Yang Glyph Ping on Land",
        description = "Changes the Yang Glyph ping to ping on land rather than on throw.",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        i18nName = "skytils.config.slayer.voidgloom_seraph.yang_glyph_ping_on_land",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var yangGlyphPingOnLand = false

    @Property(
        PropertyType.SWITCH, name = "Highlight Yang Glyph",
        description = "Highlights the Yang Glyph block.",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        i18nName = "skytils.config.slayer.voidgloom_seraph.highlight_yang_glyph",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var highlightYangGlyph = false

    @Property(
        PropertyType.SWITCH, name = "Point to Yang Glyph",
        description = "Draws an arrow in the direction of the Yang Glyph Block.",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        i18nName = "skytils.config.slayer.voidgloom_seraph.point_to_yang_glyph",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var pointYangGlyph = false

    @Property(
        PropertyType.COLOR, name = "Yang Glyph Highlight Color",
        description = "Changes the color for the Yang Glyph block",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        i18nName = "skytils.config.slayer.voidgloom_seraph.yang_glyph_highlight_color",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var yangGlyphColor = Color(65, 102, 245, 128)

    @Property(
        PropertyType.SWITCH, name = "Highlight Nukekebi Fixation Heads",
        description = "Draws the hitbox of Nukekebi Fixation heads",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        i18nName = "skytils.config.slayer.voidgloom_seraph.highlight_nukekebi_fixation_heads",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var highlightNukekebiHeads = false

    @Property(
        PropertyType.COLOR, name = "Nukekebi Fixation Head Color",
        description = "Changes the color for the Nukekebi Fixation Head Highlight",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        i18nName = "skytils.config.slayer.voidgloom_seraph.nukekebi_fixation_head_color",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var nukekebiHeadColor = Color(65, 102, 245, 128)

    val showSoulflowDisplay = property(
        PropertyAttributesExt(
            PropertyType.SWITCH, name = "Show Soulflow Display",
            description = "Shows your current internalized soulflow.\n" +
                    "§cRequires your Soulflow battery to be in your inventory.",
            category = "Slayer", subcategory = "Voidgloom Seraph",
            i18nName = "skytils.config.slayer.voidgloom_seraph.show_soulflow_display",
            i18nCategory = "skytils.config.slayer",
            i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
        ), false
    )

    @Property(
        PropertyType.NUMBER, name = "Low Soulflow Ping",
        description = "Alerts you when your soulflow is low.\n" +
                "§cRequires your Soulflow battery to be in your inventory.",
        category = "Slayer", subcategory = "Voidgloom Seraph",
        min = 0,
        max = 500,
        i18nName = "skytils.config.slayer.voidgloom_seraph.low_soulflow_ping",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.voidgloom_seraph"
    )
    var lowSoulflowPing = 0

    @Property(
        PropertyType.SWITCH, name = "Show Totem Display",
        description = "Shows the current totem's timer and hits.",
        category = "Slayer", subcategory = "Inferno Demonlord",
        i18nName = "skytils.config.slayer.inferno_demonlord.show_totem_display",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.inferno_demonlord"
    )
    var showTotemDisplay = false

    @Property(
        PropertyType.NUMBER, name = "Totem Ping",
        description = "Alerts you of a specific time (seconds) on the Inferno Demonlord's Totem.",
        category = "Slayer", subcategory = "Inferno Demonlord",
        searchTags = ["1.3.0-pre1"],
        max = 8,
        i18nName = "skytils.config.slayer.inferno_demonlord.totem_ping",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.inferno_demonlord"
    )
    var totemPing = 0

    @Property(
        PropertyType.SWITCH, name = "Hide Pacified Blazes",
        description = "Stops rendering faraway blazes when fighting the Inferno Demonlord if Smoldering Polarization is active.\n" +
                "Do note that you will still be able to interact with them! /skytilsupdatepotioneffects",
        category = "Slayer", subcategory = "Inferno Demonlord",
        i18nName = "skytils.config.slayer.inferno_demonlord.hide_pacified_blazes",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.inferno_demonlord"
    )
    var ignorePacifiedBlazes = false

    @Property(
        type = PropertyType.SWITCH, name = "Ping when in Inferno Demonlord Fire",
        description = "Shows a warning when you are standing on Inferno Demonlord's fire.",
        category = "Slayer", subcategory = "Inferno Demonlord",
        i18nName = "skytils.config.slayer.inferno_demonlord.ping_when_in_inferno_demonlord_fire",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.inferno_demonlord"
    )
    var blazeFireWarning = false

    @Property(
        PropertyType.SWITCH, name = "Recolor Demonlord Boss by Attunement",
        description = "Recolors the Inferno boss and demons depending on the correct dagger attunement.",
        category = "Slayer", subcategory = "Inferno Demonlord",
        i18nName = "skytils.config.slayer.inferno_demonlord.recolor_demonlord_boss_by_attunement",
        i18nCategory = "skytils.config.slayer",
        i18nSubcategory = "skytils.config.slayer.inferno_demonlord"
    )
    var attunementDisplay = false

    @Property(
        type = PropertyType.SWITCH, name = "Vampire Slayer One Shot Alert",
        description = "Shows a title when you can one-shot the Vampire Slayer with Steak Stake",
        category = "Slayer",
        i18nName = "skytils.config.slayer..vampire_slayer_one_shot_alert",
        i18nCategory = "skytils.config.slayer"
    )
    var oneShotAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Twinclaw Alert",
        description = "Shows a title when the Vampire Slayer is about to do a Twinclaw attack",
        category = "Slayer",
        i18nName = "skytils.config.slayer..twinclaw_alert",
        i18nCategory = "skytils.config.slayer"
    )
    var twinclawAlert = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Cooldown Sounds",
        description = "Blocks the sound effect played while an item is on cooldown.",
        category = "Sounds", subcategory = "Abilities",
        i18nName = "skytils.config.sounds.abilities.disable_cooldown_sounds",
        i18nCategory = "skytils.config.sounds",
        i18nSubcategory = "skytils.config.sounds.abilities"
    )
    var disableCooldownSounds = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Jerry-chine Gun Sounds",
        description = "Blocks the villager hrmm noises that the Jerry-chine gun projectiles play.",
        category = "Sounds", subcategory = "Abilities",
        i18nName = "skytils.config.sounds.abilities.disable_jerrychine_gun_sounds",
        i18nCategory = "skytils.config.sounds",
        i18nSubcategory = "skytils.config.sounds.abilities"
    )
    var disableJerrygunSounds = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Flower of Truth Sounds",
        description = "Blocks the eating noises that the Flower of Truth plays.",
        category = "Sounds", subcategory = "Abilities",
        i18nName = "skytils.config.sounds.abilities.disable_flower_of_truth_sounds",
        i18nCategory = "skytils.config.sounds",
        i18nSubcategory = "skytils.config.sounds.abilities"
    )
    var disableTruthFlowerSounds = false

    @Property(
        type = PropertyType.SWITCH, name = "Disable Terracotta Sounds",
        description = "Prevent the game from playing the loud sounds created by the Terracotta.",
        category = "Sounds", subcategory = "Dungeons",
        i18nName = "skytils.config.sounds.dungeons.disable_terracotta_sounds",
        i18nCategory = "skytils.config.sounds",
        i18nSubcategory = "skytils.config.sounds.dungeons"
    )
    var disableTerracottaSounds = false

    @Property(
        type = PropertyType.SELECTOR, name = "Text Shadow",
        description = "Changes the shadow type for the text displayed in the spam hider element.",
        category = "Spam", subcategory = "Display",
        options = ["Normal", "None", "Outline"],
        i18nName = "skytils.config.spam.display.text_shadow",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.display"
    )
    var spamShadow = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Implosion Hider",
        description = "Removes Implosion messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.abilities.implosion_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.abilities"
    )
    var implosionHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Midas Staff Hider",
        description = "Removes Midas Staff messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.abilities.midas_staff_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.abilities"
    )
    var midasStaffHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Spirit Sceptre Hider",
        description = "Removes Spirit Sceptre messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.abilities.spirit_sceptre_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.abilities"
    )
    var spiritSceptreHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Giant Sword Hider",
        description = "Removes Giant Sword messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.abilities.giant_sword_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.abilities"
    )
    var giantSwordHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Livid Dagger Hider",
        description = "Removes Livid Dagger messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.abilities.livid_dagger_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.abilities"
    )
    var lividHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Ray of Hope Hider",
        description = "Removes Ray of Hope messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.abilities.ray_of_hope_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.abilities"
    )
    var hopeHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Mining ability hider",
        description = "Removes Mining ability messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.abilities.mining_ability_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.abilities"
    )
    var miningAbilityHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Mana Use Hider",
        description = "Removes mana usage updates from the action bar.\nWorks best with SkyblockAddons.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.abilities.mana_use_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.abilities"
    )
    var manaUseHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Healing Message Hider",
        description = "Removes Zombie Sword and Werewolf healing messages from your chat.",
        category = "Spam", subcategory = "Abilities",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.abilities.healing_message_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.abilities"
    )
    var healingHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Blessing Hider",
        description = "Removes blessing messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Toasts"],
        i18nName = "skytils.config.spam.dungeons.blessing_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var blessingHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Blood Key Hider",
        description = "Removes Blood Key messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"],
        i18nName = "skytils.config.spam.dungeons.blood_key_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var bloodKeyHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Boss Messages Hider",
        description = "Hides Boss Messages.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.dungeons.boss_messages_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var hideBossMessages = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Wither Essence Hider",
        description = "Removes Wither Essence unlock messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.dungeons.wither_essence_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var witherEssenceHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Undead Essence Hider",
        description = "Removes Undead Essence unlock messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.dungeons.undead_essence_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var undeadEssenceHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Countdown and Ready Messages Hider",
        description = "Hides the Dungeon countdown and ready messages",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.dungeons.countdown_and_ready_messages_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var hideDungeonCountdownAndReady = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Dungeon Abilities Messages Hider",
        description = "Hides dungeon abilities messages and ultimates messages in chat",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.dungeons.dungeon_abilities_messages_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var hideDungeonAbilities = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Mort Messages Hider",
        description = "Hides Mort's messages while in dungeons",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.dungeons.mort_messages_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var hideMortMessages = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Superboom Pickup Hider",
        description = "Removes Superboom pickup messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"],
        i18nName = "skytils.config.spam.dungeons.superboom_pickup_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var superboomHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Revive Stone Pickup Hider",
        description = "Removes Revive Stone pickup messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"],
        i18nName = "skytils.config.spam.dungeons.revive_stone_pickup_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var reviveStoneHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Wither Key Hider",
        description = "Removes Wither Key messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui", "Toasts"],
        i18nName = "skytils.config.spam.dungeons.wither_key_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var witherKeyHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Tether Hider",
        description = "Removes Healer Tether messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"],
        i18nName = "skytils.config.spam.dungeons.tether_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var tetherHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Self Orb Pickup Hider",
        description = "Removes Healer Orb messages that you pick up from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"],
        i18nName = "skytils.config.spam.dungeons.self_orb_pickup_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var selfOrbHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Other Orb Pickup Hider",
        description = "Removes Healer Orb messages that others pick up from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"],
        i18nName = "skytils.config.spam.dungeons.other_orb_pickup_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var otherOrbHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Trap Damage Hider",
        description = "Removes Trap Damage messages from your chat.",
        category = "Spam", subcategory = "Dungeons",
        options = ["Normal", "Hidden", "Separate Gui"],
        i18nName = "skytils.config.spam.dungeons.trap_damage_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var trapDamageHider = 0

    @Property(
        type = PropertyType.SLIDER, name = "Toast Time",
        description = "Number of milliseconds that toasts are displayed for.",
        category = "Spam", subcategory = "Dungeons",
        max = 10000,
        i18nName = "skytils.config.spam.dungeons.toast_time",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.dungeons"
    )
    var toastTime = 2500

    @Property(
        type = PropertyType.SELECTOR, name = "Blocks in the way Hider",
        description = "Removes blocks in the way messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.miscellaneous.blocks_in_the_way_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var inTheWayHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Can't use Ability Hider",
        description = "Hides the you can't use abilities in this room message ",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.miscellaneous.cant_use_ability_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var hideCantUseAbility = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Combo Hider",
        description = "Removes combo messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI", "Toasts"],
        i18nName = "skytils.config.spam.miscellaneous.combo_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var comboHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Auto-Recombobulator Hider",
        description = "Removes Auto-Recombobulator messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI", "Toasts"],
        i18nName = "skytils.config.spam.miscellaneous.autorecombobulator_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var autoRecombHider = 0

    @Property(
        type = PropertyType.SWITCH, name = "Compact Building Tools",
        description = "Compacts messages from the Block Zapper and the Builder's Wand.",
        category = "Spam", subcategory = "Miscellaneous",
        i18nName = "skytils.config.spam.miscellaneous.compact_building_tools",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var compactBuildingTools = false

    @Property(
        type = PropertyType.SWITCH, name = "Compact Mining Powder Gain",
        description = "Compacts messages from the chests when gaining powder",
        category = "Spam", subcategory = "Miscellaneous",
        i18nName = "skytils.config.spam.miscellaneous.compact_mining_powder_gain",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var compactPowderMessages = false

    @Property(
        type = PropertyType.SELECTOR, name = "Cooldown Hider",
        description = "Removes ability still on cooldown messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.miscellaneous.cooldown_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var cooldownHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "No Enemies Nearby Hider",
        description = "Hides the 'There are no enemies nearby!' message",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.miscellaneous.no_enemies_nearby_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var hideNoEnemiesNearby = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Out of mana Hider",
        description = "Removes out of mana messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.miscellaneous.out_of_mana_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var manaMessages = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Profile Message Hider",
        description = "Removes the \"§aYou are playing on profile: §eFruit§r\" messages from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.miscellaneous.profile_message_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var profileHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Spook Message Hider",
        description = "§b[WIP] §rRemoves the messages from the Great Spooky Staff from your chat.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.miscellaneous.spook_message_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var spookyMessageHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Blessing Enchant Hider",
        description = "Removes blessing enchant message from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.fishing.blessing_enchant_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.fishing"
    )
    var blessingEnchantHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Blessed Bait Hider",
        description = "Removes blessed bait message from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.fishing.blessed_bait_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.fishing"
    )
    var blessedBaitHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Sea Creature Catch Hider",
        description = "Removes regular sea creature catch messages from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.fishing.sea_creature_catch_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.fishing"
    )
    var scCatchHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Legendary Sea Creature Catch Hider",
        description = "Removes legendary sea creature catch messages from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.fishing.legendary_sea_creature_catch_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.fishing"
    )
    var legendaryScCatchHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Good Fishing Treasure Hider",
        description = "Removes good catch messages from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.fishing.good_fishing_treasure_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.fishing"
    )
    var goodTreasureHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Great Fishing Treasure Hider",
        description = "Removes great catch messages from fishing.",
        category = "Spam", subcategory = "Fishing",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.fishing.great_fishing_treasure_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.fishing"
    )
    var greatTreasureHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Compact Hider",
        description = "Removes Compact messages from mining.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.miscellaneous.compact_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var compactHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Pristine Hider",
        description = "Removes Pristine messages from mining.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.miscellaneous.pristine_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var pristineHider = 0

    @Property(
        type = PropertyType.SELECTOR, name = "Wind Direction Hider",
        description = "Removes Gone With the Wind direction change messages.",
        category = "Spam", subcategory = "Miscellaneous",
        options = ["Normal", "Hidden", "Separate GUI"],
        i18nName = "skytils.config.spam.miscellaneous.wind_direction_hider",
        i18nCategory = "skytils.config.spam",
        i18nSubcategory = "skytils.config.spam.miscellaneous"
    )
    var windHider = 0

    init {
        registerListener("commandAliasesSpaces") { prop: Boolean ->
            CommandAliases.recreateMap(prop)
        }

        addDependency("showEtherwarpTeleportPosColor", "showEtherwarpTeleportPos")

        addDependency("samScytheColor", "showSamScytheBlocks")

        addDependency("itemRarityOpacity", "showItemRarity")
        addDependency("itemRarityShape", "showItemRarity")
        addDependency("showPetRarity", "showItemRarity")

        arrayOf(
            "showLowestBINPrice",
            "betterAuctionPriceInput",
//            "dungeonChestProfit",
            "showCoinsPerBit",
            "protectItemBINThreshold",
//            "containerSellValue",
            "visitorOfferHelper",
            "showCoinsPerCopper",
//            "kuudraChestProfit",
            "fetchKuudraPrices"
        ).forEach { propertyName ->
            addDependency(propertyName, "fetchLowestBINPrices")
            registerListener(propertyName) { prop: Any ->
                if (prop is Boolean && prop) fetchLowestBINPrices = true
            }
        }

//        arrayOf("kuudraChestProfit").forEach { propertyName ->
//            addDependency(propertyName, "fetchKuudraPrices")
//            registerListener(propertyName) { prop: Any ->
//                if (prop is Boolean && prop) fetchKuudraPrices = true
//            }
//        }
        effect(ReferenceHolder.Weak) {
            if (kuudraChestProfit()) {
                fetchKuudraPrices = true
            }
        }

//        addDependency("dungeonChestProfitIncludesEssence", "dungeonChestProfit")
        addDependency("croesusHideOpened", "croesusChestHighlight")
//        addDependency("kismetRerollThreshold", "dungeonChestProfit")

//        addDependency("kuudraChestProfitIncludesEssence", "kuudraChestProfit")
//        addDependency("kuudraChestProfitCountsKey", "kuudraChestProfit")

        addDependency("message270Score", "sendMessageOn270Score")
        addDependency("messageTitle270Score", "createTitleOn270Score")

        addDependency("message300Score", "sendMessageOn300Score")
        addDependency("messageTitle300Score", "createTitleOn300Score")

        addDependency("bloodHelperColor", "bloodHelper")
        addDependency("boxStarredMobsColor", "boxStarredMobs")

        addDependency("highlightDoorOpener", "spiritLeapNames")

        addDependency("showNextBlaze", "blazeSolver")
        addDependency("lineToNextBlaze", "showNextBlaze")
        addDependency("lowestBlazeColor", "blazeSolver")
        addDependency("highestBlazeColor", "blazeSolver")
        addDependency("nextBlazeColor", "showNextBlaze")
        addDependency("lineToNextBlazeColor", "lineToNextBlaze")
        addDependency("teleportMazeSolverColor", "teleportMazeSolver")
        addDependency("ticTacToeSolverColor", "ticTacToeSolver")
        addDependency("clickInOrderFirst", "clickInOrderTerminalSolver")
        addDependency("clickInOrderSecond", "clickInOrderTerminalSolver")
        addDependency("clickInOrderThird", "clickInOrderTerminalSolver")
        addDependency("changeToSameColorMode", "changeAllSameColorTerminalSolver")
        addDependency("changeToSameColorLock", "changeAllSameColorTerminalSolver")
        addDependency("predictAlignmentClicks", "alignmentTerminalSolver")
        addDependency("predictSimonClicks", "simonSaysSolver")

        arrayOf(
            "emptyBurrowColor",
            "mobBurrowColor",
            "treasureBurrowColor",
            "burrowEstimation",
            "pingNearbyBurrow",
            "experimentBurrowEstimation"
        ).forEach { propertyName -> addDependency(propertyName, "showGriffinBurrows") }

        addDependency("activePetColor", "highlightActivePet")
        addDependency("favoritePetColor", "highlightFavoritePets")

        addDependency("showTankRadiusWall", "showTankRadius")
        addDependency("tankRadiusDisplayColor", "showTankRadius")
        addDependency("boxedTankColor", "boxedTanks")
        addDependency("boxedProtectedTeammatesColor", "boxedProtectedTeammates")

        addDependency("yangGlyphColor", "highlightYangGlyph")
        addDependency("nukekebiHeadColor", "highlightNukekebiHeads")

        arrayOf(
            "seraphBeaconPhaseColor",
            "seraphHitsPhaseColor",
            "seraphNormalPhaseColor"
        ).forEach { propertyName -> addDependency(propertyName, "recolorSeraphBoss") }

        addDependency("powerOrbDuration", "powerOrbLock")
        addDependency("dupeTrackerOverlayColor", "dupeTracker")

//        addDependency("containerSellValueMaxItems", "containerSellValue")
//        addDependency("includeModifiersInSellValue", "containerSellValue")

//        addDependency("assumeWitherImpact", "witherShieldCooldown")

//        addDependency("showTrophyFishTotals", "trophyFishTracker")
//        addDependency("showTotalTrophyFish", "trophyFishTracker")

        addDependency("shinyPigLocations", "shinyOrbWaypoints")

//        addDependency("crystalHollowsMapPlayerScale", "crystalHollowMap")

        registerListener("protectItemBINThreshold") { _: String ->
            tickTimer(1) {
                val numeric = protectItemBINThreshold.replace(Regex("[^0-9]"), "")
                protectItemBINThreshold = numeric.ifEmpty { "0" }
                if (protectItemBINThreshold != "0") fetchLowestBINPrices = true
                markDirty()
            }
        }

        arrayOf(
            "darkModeMist",
            "gardenPlotCleanupHelper",
            "recolorCarpets"
        ).forEach { propertyName ->
            registerListener(propertyName) { _: Boolean -> mc.worldRenderer.reload() }
        }

        registerListener("itemRarityShape") { i: Int ->
            //#if FORGE
            //$$ if (i == 4 && Loader.instance().hasReachedState(LoaderState.POSTINITIALIZATION)) {
            //#else
            if (i == 4 && mc.isFinishedLoading) {
            //#endif
                val old = itemRarityShape
                runCatching {
                    val loc = Identifier.of("skytils", "gui/customrarity.png")
                    //#if MC==10809
                    //$$ mc.resourceManager.method_14486(loc)
                    //#else
                    mc.resourceManager.getResourceOrThrow(loc)
                    //#endif
                }.onFailure {
                    tickTimer(1) {
                        if (itemRarityShape == 4) {
                            itemRarityShape = old
                            Notifications
                                .push("Invalid Value", "You cannot use the Custom rarity while the texture is missing!")
                        }
                    }
                }
            }
        }

        registerListener("connectToWS") { state: Boolean ->
            if (state) {
                if (mc.world != null && !WSClient.connected) {
                    WSClient.openConnection()
                }
            } else {
                WSClient.closeConnection()
            }
        }
    }

    fun init() {
        initialize()
        if (Skytils.config.lastLaunchedVersion != Skytils.VERSION) {
            val ver = UpdateChecker.SkytilsVersion(Skytils.config.lastLaunchedVersion)
            when {
                (!ver.isSafe && lastLaunchedVersion.startsWithAny("0.", "1.")) || ver < UpdateChecker.SkytilsVersion("1.2-pre3") || Skytils.config.lastLaunchedVersion == "0" -> {
                    if (GuiManager.elementMetadata["Crystal Hollows Map"]?.scale == 0.1f) {
                        GuiManager.elementMetadata["Crystal Hollows Map"] = GuiManager.elementMetadata["Crystal Hollows Map"]!!.copy(
                            scale = 1f
                        )
                        PersistentSave.markDirty<GuiManager>()
                    }
                }
                ver < UpdateChecker.SkytilsVersion("1.9.0-pre4") -> {
                    val cataclysmicMapDir = File("./config/skytils/cataclysmicmap")
                    val catlasDir = File("./config/skytils/catlas")
                    catlasDir.mkdirs()
                    cataclysmicMapDir.copyRecursively(catlasDir, true) { _, _ -> OnErrorAction.SKIP }
                    cataclysmicMapDir.deleteRecursively()
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
