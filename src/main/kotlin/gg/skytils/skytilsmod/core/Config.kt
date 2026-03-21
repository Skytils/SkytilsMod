package gg.skytils.skytilsmod.core

import gg.essential.universal.UDesktop
import gg.essential.vigilance.Vigilant
import gg.essential.vigilance.data.Category
import gg.essential.vigilance.data.Property
import gg.essential.vigilance.data.PropertyType
import gg.essential.vigilance.data.SortingBehavior
import gg.skytils.skytilsmod.Reference
import gg.skytils.skytilsmod.Skytils
import java.net.URI

object Config : Vigilant(
    Skytils.modDir.resolve("config.toml").toFile(),
    "Skytils (${Reference.VERSION})",
    sortingBehavior = ConfigSorting
) {
    @Property(
        type = PropertyType.BUTTON, name = "Join the Skytils Discord",
        description = "Join the Skytils Discord server for help using any of the features.",
        category = "General", subcategory = "Other",
        placeholder = "Join",
    )
    @Suppress("unused")
    fun openDiscordLink() {
        UDesktop.browse(URI.create("https://discord.gg/skytils"))
    }

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
        type = PropertyType.SWITCH, name = "Hide Potion Effects in Inventory",
        description = "Prevents the game from rendering the potion effects in inventories while in Skyblock.",
        category = "Miscellaneous", subcategory = "Quality of Life",
    )
    var hidePotionEffects = false

    @Property(
        type = PropertyType.SWITCH, name = "Press Enter to confirm Sign Popups",
        description = "Allows pressing enter to confirm a sign popup, such as the bazaar or auction house prices.",
        category = "Miscellaneous", subcategory = "Quality of Life",
    )
    var pressEnterToConfirmSignQuestion = false
  
    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Dropped Item Scale",
        description = "Changes the scale of dropped items.",
        category = "Miscellaneous", subcategory = "Quality of Life",
        minF = 0.10f, maxF = 2f, decimalPlaces = 2
    )
    var droppedItemScale = 1f

    @Property(
        type = PropertyType.SWITCH, name = "Prevent Cursor Reset",
        description = "Prevents the cursor from resetting to the center of the screen when you open a GUI.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var preventCursorReset = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Item Rarity",
        description = "Shows the Rarity of items as their background.",
        category = "Miscellaneous", subcategory = "Item Rarity"
    )
    var showItemRarity = false

    @Property(
        type = PropertyType.SWITCH, name = "Show Pet Rarity",
        description = "Shows the Rarity of pets as their background.",
        category = "Miscellaneous", subcategory = "Item Rarity"
    )
    var showPetRarity = false

    @Property(
        type = PropertyType.SELECTOR, name = "Item Rarity Shape",
        description = "Select the shape of the rarity background.",
        category = "Miscellaneous", subcategory = "Item Rarity",
        options = ["Square", "Square Outline", "Outline"],
    )
    var itemRarityShape = 0

    @Property(
        type = PropertyType.PERCENT_SLIDER, name = "Item Rarity Opacity",
        description = "Changes how visible the rarity background is. Lower values are less visible.",
        category = "Miscellaneous", subcategory = "Item Rarity"
    )
    var itemRarityOpacity = 1f

    fun init() {
        addDependency("showPetRarity", "showItemRarity")
        addDependency("itemRarityShape", "showItemRarity")
        addDependency("itemRarityOpacity", "showItemRarity")

        initialize()
        lastLaunchedVersion = Reference.VERSION
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