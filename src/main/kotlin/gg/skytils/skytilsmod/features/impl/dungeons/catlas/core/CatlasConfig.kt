/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.features.impl.dungeons.catlas.core

import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.*
import java.awt.Color
import java.io.File

object CatlasConfig : Vigilant(
    File("./config/skytils/catlas/config.toml"),
    "Catlas",
    sortingBehavior = CategorySorting
) {

    @Property(
        name = "Map Enabled",
        type = PropertyType.SWITCH,
        description = "Render the map!",
        category = "Map",
        subcategory = "Toggle",
        i18nName = "catlas.config.map.toggle.map_enabled",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.toggle"
    )
    var mapEnabled = false

    @Property(
        name = "Rotate Map",
        type = PropertyType.SWITCH,
        description = "Rotates map to follow the player.",
        category = "Map",
        subcategory = "Toggle",
        i18nName = "catlas.config.map.toggle.rotate_map",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.toggle"
    )
    var mapRotate = false

    @Property(
        name = "Center Map",
        type = PropertyType.SWITCH,
        description = "Centers the map on the player if Rotate Map is enabled.",
        category = "Map",
        subcategory = "Toggle",
        i18nName = "catlas.config.map.toggle.center_map",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.toggle"
    )
    var mapCenter = false

    @Property(
        name = "Dynamic Rotate",
        type = PropertyType.SWITCH,
        description = "Keeps the entrance room at the bottom. Does not work with rotate map.",
        category = "Map",
        subcategory = "Toggle",
        i18nName = "catlas.config.map.toggle.dynamic_rotate",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.toggle"
    )
    var mapDynamicRotate = false

    @Property(
        name = "Hide In Boss",
        type = PropertyType.SWITCH,
        description = "Hides the map in boss.",
        category = "Map",
        subcategory = "Toggle",
        i18nName = "catlas.config.map.toggle.hide_in_boss",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.toggle"
    )
    var mapHideInBoss = false

    @Property(
        name = "Show Player Names",
        type = PropertyType.SELECTOR,
        description = "Show player name under player head",
        category = "Map",
        subcategory = "Toggle",
        options = ["Off", "Holding Leap", "Always"],
        i18nName = "catlas.config.map.toggle.show_player_names",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.toggle"
    )
    var playerHeads = 0

    @Property(
        name = "Vanilla Head Marker",
        type = PropertyType.SWITCH,
        description = "Uses the vanilla head marker for yourself.",
        category = "Map",
        subcategory = "Toggle",
        i18nName = "catlas.config.map.toggle.vanilla_head_marker",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.toggle"
    )
    var mapVanillaMarker = false

    @Property(
        name = "Map Text Scale",
        type = PropertyType.DECIMAL_SLIDER,
        description = "Scale of room names and secret counts relative to map size.",
        category = "Map",
        subcategory = "Size",
        maxF = 2f,
        decimalPlaces = 2,
        i18nName = "catlas.config.map.size.map_text_scale",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.size"
    )
    var textScale = 0.75f

    @Property(
        name = "Player Heads Scale",
        type = PropertyType.DECIMAL_SLIDER,
        description = "Scale of player heads relative to map size.",
        category = "Map",
        subcategory = "Size",
        maxF = 2f,
        decimalPlaces = 2,
        i18nName = "catlas.config.map.size.player_heads_scale",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.size"
    )
    var playerHeadScale = 1f

    @Property(
        name = "Player Name Scale",
        type = PropertyType.DECIMAL_SLIDER,
        description = "Scale of player names relative to head size.",
        category = "Map",
        subcategory = "Size",
        maxF = 2f,
        decimalPlaces = 2,
        i18nName = "catlas.config.map.size.player_name_scale",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.size"
    )
    var playerNameScale = .8f

    @Property(
        name = "Map Background Color",
        type = PropertyType.COLOR,
        category = "Map",
        subcategory = "Render",
        allowAlpha = true,
        i18nName = "catlas.config.map.render.map_background_color",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.render"
    )
    var mapBackground = Color(0, 0, 0, 100)

    @Property(
        name = "Map Border Color",
        type = PropertyType.COLOR,
        category = "Map",
        subcategory = "Render",
        allowAlpha = true,
        i18nName = "catlas.config.map.render.map_border_color",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.render"
    )
    var mapBorder = Color(0, 0, 0, 255)

    @Property(
        name = "Border Thickness",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Map",
        subcategory = "Render",
        maxF = 10f,
        i18nName = "catlas.config.map.render.border_thickness",
        i18nCategory = "catlas.config.map",
        i18nSubcategory = "catlas.config.map.render"
    )
    var mapBorderWidth = 3f

    @Property(
        name = "Room Names",
        type = PropertyType.SELECTOR,
        description = "Shows names of rooms on map.",
        category = "Rooms",
        options = ["None", "Puzzles / Trap", "All"],
        i18nName = "catlas.config.rooms..room_names",
        i18nCategory = "catlas.config.rooms"
    )
    var mapRoomNames = 1

    @Property(
        name = "Center Room Names",
        type = PropertyType.SWITCH,
        description = "Center room names.",
        subcategory = "Text",
        category = "Rooms",
        i18nName = "catlas.config.rooms.text.center_room_names",
        i18nCategory = "catlas.config.rooms",
        i18nSubcategory = "catlas.config.rooms.text"
    )
    var mapCenterRoomName = true

    @Property(
        name = "Room Secrets",
        type = PropertyType.SELECTOR,
        description = "Shows total secrets of rooms on map.",
        category = "Rooms",
        options = ["Off", "On", "Replace Checkmark"],
        i18nName = "catlas.config.rooms..room_secrets",
        i18nCategory = "catlas.config.rooms"
    )
    var mapRoomSecrets = 0

    // TODO: Add translation
    @Property(
        name = "Found Room Secrets",
        type = PropertyType.SELECTOR,
        description = "Shows found secrets of rooms on map.",
        category = "Rooms",
        options = ["Off", "On", "Replace Total"],
        i18nCategory = "catlas.config.rooms"
    )
    var foundRoomSecrets = 0

    @Property(
        name = "Color Text",
        type = PropertyType.SWITCH,
        description = "Colors name and secret count based on room state.",
        category = "Rooms", i18nName = "catlas.config.rooms..color_text", i18nCategory = "catlas.config.rooms"
    )
    var mapColorText = false

    @Property(
        name = "Room Checkmarks",
        type = PropertyType.SELECTOR,
        description = "Adds room checkmarks based on room state.",
        category = "Rooms",
        options = ["None", "Default", "NEU"],
        i18nName = "catlas.config.rooms..room_checkmarks",
        i18nCategory = "catlas.config.rooms"
    )
    var mapCheckmark = 1

    @Property(
        name = "Center Room Checkmarks",
        type = PropertyType.SWITCH,
        description = "Center room checkmarks.",
        subcategory = "Checkmarks",
        category = "Rooms",
        i18nName = "catlas.config.rooms.checkmarks.center_room_checkmarks",
        i18nCategory = "catlas.config.rooms",
        i18nSubcategory = "catlas.config.rooms.checkmarks"
    )
    var mapCenterCheckmark = true

    @Property(
        name = "Blood Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true,
        i18nName = "catlas.config.colors.doors.blood_door",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.doors"
    )
    var colorBloodDoor = Color(231, 0, 0)

    @Property(
        name = "Entrance Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true,
        i18nName = "catlas.config.colors.doors.entrance_door",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.doors"
    )
    var colorEntranceDoor = Color(20, 133, 0)

    @Property(
        name = "Normal Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true,
        i18nName = "catlas.config.colors.doors.normal_door",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.doors"
    )
    var colorRoomDoor = Color(92, 52, 14)

    @Property(
        name = "Wither Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true,
        i18nName = "catlas.config.colors.doors.wither_door",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.doors"
    )
    var colorWitherDoor = Color(0, 0, 0)

    @Property(
        name = "Opened Wither Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true,
        i18nName = "catlas.config.colors.doors.opened_wither_door",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.doors"
    )
    var colorOpenWitherDoor = Color(92, 52, 14)

    @Property(
        name = "Unopened Door",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Doors",
        allowAlpha = true,
        i18nName = "catlas.config.colors.doors.unopened_door",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.doors"
    )
    var colorUnopenedDoor = Color(65, 65, 65)

    @Property(
        name = "Blood Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true,
        i18nName = "catlas.config.colors.rooms.blood_room",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.rooms"
    )
    var colorBlood = Color(255, 0, 0)

    @Property(
        name = "Entrance Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true,
        i18nName = "catlas.config.colors.rooms.entrance_room",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.rooms"
    )
    var colorEntrance = Color(20, 133, 0)

    @Property(
        name = "Fairy Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true,
        i18nName = "catlas.config.colors.rooms.fairy_room",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.rooms"
    )
    var colorFairy = Color(224, 0, 255)

    @Property(
        name = "Miniboss Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true,
        i18nName = "catlas.config.colors.rooms.miniboss_room",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.rooms"
    )
    var colorMiniboss = Color(254, 223, 0)

    @Property(
        name = "Normal Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true,
        i18nName = "catlas.config.colors.rooms.normal_room",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.rooms"
    )
    var colorRoom = Color(107, 58, 17)

    @Property(
        name = "Puzzle Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true,
        i18nName = "catlas.config.colors.rooms.puzzle_room",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.rooms"
    )
    var colorPuzzle = Color(117, 0, 133)

    @Property(
        name = "Rare Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true,
        i18nName = "catlas.config.colors.rooms.rare_room",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.rooms"
    )
    var colorRare = Color(255, 203, 89)

    @Property(
        name = "Trap Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true,
        i18nName = "catlas.config.colors.rooms.trap_room",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.rooms"
    )
    var colorTrap = Color(216, 127, 51)

    @Property(
        name = "Unopened Room",
        type = PropertyType.COLOR,
        category = "Colors",
        subcategory = "Rooms",
        allowAlpha = true,
        i18nName = "catlas.config.colors.rooms.unopened_room",
        i18nCategory = "catlas.config.colors",
        i18nSubcategory = "catlas.config.colors.rooms"
    )
    var colorUnopened = Color(65, 65, 65)


    @Property(
        name = "Box Wither Doors",
        description = "Boxes unopened wither doors.",
        type = PropertyType.SWITCH,
        category = "Other Features",
        subcategory = "Wither Door",
        i18nName = "catlas.config.other_features.wither_door.box_wither_doors",
        i18nCategory = "catlas.config.other_features",
        i18nSubcategory = "catlas.config.other_features.wither_door"
    )
    var boxWitherDoors = false

    @Property(
        name = "No Key Color",
        type = PropertyType.COLOR,
        category = "Other Features",
        subcategory = "Wither Door",
        allowAlpha = true,
        i18nName = "catlas.config.other_features.wither_door.no_key_color",
        i18nCategory = "catlas.config.other_features",
        i18nSubcategory = "catlas.config.other_features.wither_door"
    )
    var witherDoorNoKeyColor = Color(255, 0, 0)

    @Property(
        name = "Has Key Color",
        type = PropertyType.COLOR,
        category = "Other Features",
        subcategory = "Wither Door",
        allowAlpha = true,
        i18nName = "catlas.config.other_features.wither_door.has_key_color",
        i18nCategory = "catlas.config.other_features",
        i18nSubcategory = "catlas.config.other_features.wither_door"
    )
    var witherDoorKeyColor = Color(0, 255, 0)

    @Property(
        name = "Door Outline Width",
        type = PropertyType.DECIMAL_SLIDER,
        category = "Other Features",
        subcategory = "Wither Door",
        minF = 1f,
        maxF = 10f,
        i18nName = "catlas.config.other_features.wither_door.door_outline_width",
        i18nCategory = "catlas.config.other_features",
        i18nSubcategory = "catlas.config.other_features.wither_door"
    )
    var witherDoorOutlineWidth = 3f

    @Property(
        name = "Door Outline Opacity",
        type = PropertyType.PERCENT_SLIDER,
        category = "Other Features",
        subcategory = "Wither Door",
        i18nName = "catlas.config.other_features.wither_door.door_outline_opacity",
        i18nCategory = "catlas.config.other_features",
        i18nSubcategory = "catlas.config.other_features.wither_door"
    )
    var witherDoorOutline = 1f

    @Property(
        name = "Door Fill Opacity",
        type = PropertyType.PERCENT_SLIDER,
        category = "Other Features",
        subcategory = "Wither Door",
        i18nName = "catlas.config.other_features.wither_door.door_fill_opacity",
        i18nCategory = "catlas.config.other_features",
        i18nSubcategory = "catlas.config.other_features.wither_door"
    )
    var witherDoorFill = 0.25f

    init {
        initialize()
        setCategoryDescription(
            "Map", "Catlas is a fork of works created by SkyblockAddons, UnclaimedBloom6, and Harry282"
        )
    }

    private object CategorySorting : SortingBehavior() {

        private val configCategories = listOf(
            "Map", "Rooms", "Colors", "Other Features"
        )

        private val configSubcategories = listOf(
            "Toggle", "Elements", "Scanning", "Size", "Render"
        )

        override fun getCategoryComparator(): Comparator<in Category> = compareBy { configCategories.indexOf(it.name) }

        override fun getSubcategoryComparator(): Comparator<in Map.Entry<String, List<PropertyData>>> =
            compareBy { configSubcategories.indexOf(it.key) }
    }
}
