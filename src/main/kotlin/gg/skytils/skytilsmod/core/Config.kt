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
        type = PropertyType.SWITCH, name = "Show Rarity Background",
        description = "Shows the Rarity of items as their background.",
        category = "Miscellaneous", subcategory = "Quality of Life"
    )
    var showRarityBackground = false

    fun init() {
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