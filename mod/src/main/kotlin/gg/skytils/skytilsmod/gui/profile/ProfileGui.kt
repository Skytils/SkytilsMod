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

package gg.skytils.skytilsmod.gui.profile

import com.mojang.authlib.GameProfile
import gg.essential.api.EssentialAPI
import gg.essential.api.gui.buildEmulatedPlayer
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UMinecraft
import gg.essential.vigilance.gui.VigilancePalette
import gg.skytils.hypixel.types.player.Player
import gg.skytils.hypixel.types.skyblock.Member
import gg.skytils.hypixel.types.skyblock.Profile
import gg.skytils.skytilsmod.Skytils
import gg.skytils.skytilsmod.core.API
import gg.skytils.skytilsmod.gui.constraints.FixedChildBasedRangeConstraint
import gg.skytils.skytilsmod.gui.profile.components.*
import gg.skytils.skytilsmod.gui.profile.states.alwaysMap
import gg.skytils.skytilsmod.gui.profile.states.alwaysUpdateState
import gg.skytils.skytilsmod.utils.MojangUtil
import gg.skytils.skytilsmod.utils.Utils
import gg.skytils.skytilsmod.utils.nonDashedString
import gg.skytils.skytilsmod.utils.toMCItems
import kotlinx.coroutines.launch
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import java.awt.Color
import java.util.*

class ProfileGui(uuid: UUID, name: String) : WindowScreen(ElementaVersion.V4, drawDefaultBackground = false) {
    private val uuidState: State<UUID> = BasicState(uuid).also {
        it.onSetValue { uuid ->
            loadProfile(uuid)
        }
    }
    private val hypixelPlayer: State<Player?> = BasicState(null)
    private val profiles: State<List<Profile>?> = alwaysUpdateState(null)
    private val selection: State<Int> = BasicState(0)
    private val profileList: State<List<String>> =
        profiles.alwaysMap { selection.set(0); it?.map { profile -> profile.name } ?: listOf("None") }
    private val profileState: State<Member?> = selection.zip(profiles).alwaysMap { (selection, profiles) ->
        val a = profiles?.get(selection)?.members?.get(uuidState.get().nonDashedString())
        a
    }
    private val gameProfileState: State<GameProfile?> = BasicState(null)
    override fun afterInitialization() {
        loadProfile(uuidState.get())
    }

    // Navbar Section

    private val navBar by UIBlock(Color(0, 0, 0, 160))
        .constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = RelativeConstraint()
            height = 25.pixels()
        } childOf window

    private val logo by UIImage.ofResourceCached("/assets/skytils/logo.png").constrain {
        x = 2.5.pixels
        y = CenterConstraint()
        width = 20.pixels
        height = AspectConstraint()
    } childOf navBar

    private val navText by UIText("${if (Utils.isBSMod) "BSMod" else "Skytils"} Profile Viewer").constrain {
        x = SiblingConstraint(5f)
        y = CenterConstraint()
    } childOf navBar

    private val nameState: State<String> = BasicState(name).also { state ->
        state.onSetValue { name ->
            println(name)
            Skytils.launch {
                MojangUtil.getUUIDFromUsername(name)?.let { uuidState.set(it) }
            }
        }
    }

    private val searchBar by SearchComponent().bindValue(nameState).constrain {
        x = CenterConstraint()
        y = 2.5.pixels
        height = 20.pixels
        width = 50.percentOfWindow
        color = VigilancePalette.getSearchBarBackground().withAlpha(120).constraint
    } childOf navBar

    private val profilesDropdown by DropdownComponent(0, profileList.get(), optionPadding = 2.5f)
        .bindOptions(profileList)
        .bindSelection(selection)
        .constrain {
            x = 10.pixels(true)
            y = 2.5.pixels
        } childOf navBar

    // Player Section

    private val playerContainer by UIBlock(Color(0, 0, 0, 40))
        .constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = 35.percent()
            height = 100.percentOfWindow - 25.pixels
        } childOf window

    private val playerComponent = EssentialAPI.getEssentialComponentFactory().buildEmulatedPlayer {
        profileState = gameProfileState

    }
        .constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = RelativeConstraint(0.5f)
            height = RelativeConstraint(0.5f)
        } childOf playerContainer

    // Main Section
    private val contentBlock by UIBlock(Color(0, 0, 0, 100))
        .constrain {
            x = CramSiblingConstraint()
            y = CramSiblingConstraint()
            width = 65.percent()
            height = basicHeightConstraint { window.getHeight() - 25f }
        } childOf window

    private val contentContainer by ScrollComponent()
        .constrain {
            x = CopyConstraintFloat() boundTo contentBlock
            y = CopyConstraintFloat() boundTo contentBlock
            width = CopyConstraintFloat() boundTo contentBlock
            height = CopyConstraintFloat() boundTo contentBlock
            color = Color(0, 0, 0, 100).toConstraint()
        } childOf window

    private val scrollBar by UIBlock().constrain {
        x = 6.pixels(true)
        y = 10.pixels
        width = 3.5.pixels
        color = Color(0xadadad).constraint
    }.apply {
        this childOf window
        contentContainer.setVerticalScrollBarComponent(this, true)
    }

    // Skills
    private val skillContainer by UIContainer()
        .constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = RelativeConstraint()
            height = FixedChildBasedRangeConstraint() + 5.pixels
        } childOf contentContainer

    private val taming by SkillComponent(
        ItemComponent(Items.spawn_egg),
        Color(65, 102, 245).toConstraint(),
        "SKILL_TAMING",
        profileState
    )
        .constrain {
            x = 5.percent()
            y = 5.pixels()
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val farming by SkillComponent(
        ItemComponent(Items.golden_hoe),
        Color(65, 102, 245).toConstraint(),
        "SKILL_FARMING",
        profileState
    )
        .constrain {
            x = 52.5.percent()
            y = 5.pixels()
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val mining by SkillComponent(
        ItemComponent(Items.stone_pickaxe),
        Color(65, 102, 245).toConstraint(),
        "SKILL_MINING",
        profileState
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val combat by SkillComponent(
        ItemComponent(Items.stone_sword),
        Color(65, 102, 245).toConstraint(),
        "SKILL_COMBAT",
        profileState
    )
        .constrain {
            x = 52.5.percent()
            y = CopyConstraintFloat() boundTo mining
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val foraging by SkillComponent(
        ItemComponent(Item.getItemFromBlock(Blocks.sapling), 3),
        Color(65, 102, 245).toConstraint(),
        "SKILL_FORAGING",
        profileState
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val fishing by SkillComponent(
        ItemComponent(Items.fishing_rod),
        Color(65, 102, 245).toConstraint(),
        "SKILL_FISHING",
        profileState
    )
        .constrain {
            x = 52.5.percent()
            y = CopyConstraintFloat() boundTo foraging
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val enchanting by SkillComponent(
        ItemComponent(Item.getItemFromBlock(Blocks.enchanting_table)),
        Color(65, 102, 245).toConstraint(),
        "SKILL_ENCHANTING",
        profileState
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val alchemy by SkillComponent(
        ItemComponent(Items.brewing_stand),
        Color(65, 102, 245).toConstraint(),
        "SKILL_ALCHEMY",
        profileState
    )
        .constrain {
            x = 52.5.percent()
            y = CopyConstraintFloat() boundTo enchanting
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val carpentry by SkillComponent(
        ItemComponent(Item.getItemFromBlock(Blocks.crafting_table)),
        Color(65, 102, 245).toConstraint(),
        "SKILL_CARPENTRY",
        profileState
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val runecrafting by SkillComponent(
        ItemComponent(Items.magma_cream),
        Color(65, 102, 245).toConstraint(),
        "SKILL_RUNECRAFTING",
        profileState
    )
        .constrain {
            x = 52.5.percent()
            y = basicYConstraint { carpentry.getTop() }
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val wardrobe by WardrobeComponent(profileState).constrain {
        x = 5.percent
        y = SiblingConstraint(5f)
    } childOf contentContainer

    private val inventory = InventoryComponent(profileState.alwaysMap { it?.inventory?.inventory?.toMCItems() }).constrain {
        x = 5.percent()
        y = SiblingConstraint(5f)
        width = (9 * (16 + 2)).pixels
        height = (4 * (16 + 2)).pixels
    } childOf contentContainer

    //TODO: Fix HOTM
/*    private val hotm = HOTMComponent(profileState).constrain {
        x = CramSiblingConstraint(5f) + 5.percent
        y = CramSiblingConstraint(5f)
        width = (9 * (16 + 2)).pixels
        height = (7 * (16 + 2)).pixels
    } childOf contentContainer*/

    private val dungeons = DungeonsComponent(hypixelPlayer, profileState).constrain {
        x = 0.pixels
        y = SiblingConstraint(5f)
        width = RelativeConstraint()
        height = FixedChildBasedRangeConstraint()
    } childOf contentContainer

    private val slayer = SlayerComponent(profileState).constrain {
        x = 5.percent()
        y = SiblingConstraint(5f)
        width = 90.percent
        height = FixedChildBasedRangeConstraint() + 5.pixels
    } childOf contentContainer

    init {
        if (EssentialAPI.getMinecraftUtil().isDevelopment()) {
            Inspector(window).constrain {
                x = 10.pixels
                y = 10.pixels
            } childOf window
        }
    }

    fun loadProfile(uuid: UUID) {
        val profile = GameProfile(uuid, "")
        Skytils.launch {
            launch {
                hypixelPlayer.set(API.getPlayer(uuid))

            }
            launch {
                API.getSkyblockProfiles(uuid).let {
                    profiles.set(it)
                    selection.set(it?.indexOfFirst { it.selected } ?: 0)
                    println(it?.joinToString("\n") { it.name })
                }
            }
            launch {
                UMinecraft.getMinecraft().sessionService.fillProfileProperties(profile, true)
            }.invokeOnCompletion {
                gameProfileState.set(profile)
            }
        }
    }

}