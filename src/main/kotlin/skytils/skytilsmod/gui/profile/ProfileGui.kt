/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2022 Skytils
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

package skytils.skytilsmod.gui.profile

import com.mojang.authlib.GameProfile
import gg.essential.api.EssentialAPI
import gg.essential.api.gui.buildEmulatedPlayer
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import gg.essential.universal.UMinecraft
import kotlinx.coroutines.launch
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import skytils.hylin.extension.getLatestSkyblockProfile
import skytils.hylin.extension.nonDashedString
import skytils.hylin.player.Player
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.Profile
import skytils.hylin.skyblock.Skills
import skytils.skytilsmod.Skytils
import skytils.skytilsmod.Skytils.Companion.hylinAPI
import skytils.skytilsmod.gui.profile.components.*
import skytils.skytilsmod.gui.profile.states.alwaysMap
import java.awt.Color
import java.util.*

class ProfileGui(uuid: UUID) : WindowScreen(ElementaVersion.V1, drawDefaultBackground = false) {
    private val uuidState: State<UUID> = BasicState(uuid).also {
        it.onSetValue { it ->
            val profile = GameProfile(it, "")
            Skytils.launch {
                launch {
                    hylinAPI.getPlayer(uuid).whenComplete {
                        hypixelPlayer.set(it)
                    }
                }
                launch {
                    hylinAPI.getSkyblockProfiles(uuid).whenComplete { list ->
                        profiles.set(list)
                        selection.set(list.indexOf(list.getLatestSkyblockProfile(uuid)))
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
    private val hypixelPlayer: State<Player?> = BasicState(null)
    private val profiles: State<List<Profile>?> = BasicState(null)
    private val selection: State<Int> = BasicState(0)
    private val profileList: State<List<String>> =
        profiles.map { selection.set(0); it?.map { profile -> profile.cuteName } ?: listOf("None") }
    private val profileState: State<Member?> = selection.zip(profiles).alwaysMap { (selection, profiles) ->
        profiles?.get(selection)?.members?.get(uuidState.get().nonDashedString())
    }
    private val gameProfileState: State<GameProfile?> = BasicState(null)

    override fun afterInitialization() {
        Skytils.launch {
            var b: List<Profile>? = null
            var c = 0
            val profile = GameProfile(uuidState.get(), "")
            launch {
                b = hylinAPI.getSkyblockProfiles(uuidState.get()).await()
                val e = b?.getLatestSkyblockProfile(uuidState.get())
                c = b?.indexOf(e) ?: 0
            }.invokeOnCompletion {
                profiles.set(b)
                selection.set(c)
            }
            launch {
                UMinecraft.getMinecraft().sessionService.fillProfileProperties(profile, true)
            }.invokeOnCompletion {
                gameProfileState.set(profile)
            }
        }
    }

    // Navbar Section

    private val navBar by UIBlock(Color(0, 0, 0, 160))
        .constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = RelativeConstraint()
            height = 25.pixels()
        } childOf window

    private val profilesDropdown by DropdownComponent(0, profileList.get(), optionPadding = 2.5f).bindOptions(
        profileList
    )
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

    private val contentContainer by UIBlock(Color(0, 0, 0, 100))
        .constrain {
            x = CramSiblingConstraint()
            y = CramSiblingConstraint()
            width = 65.percent()
            height = basicHeightConstraint { window.getHeight() - 25f }

        } childOf window

    // Skills
    private val skillContainer by UIContainer()
        .constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = RelativeConstraint()
            height = ChildBasedRangeConstraint() + 5.pixels
        } childOf contentContainer

    private val taming = SkillComponent(
        ItemComponent(Items.spawn_egg),
        Color(65, 102, 245).toConstraint(),
        Skills::tamingXP,
        profileState
    )
        .constrain {
            x = 5.percent()
            y = 5.pixels()
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val farming = SkillComponent(
        ItemComponent(Items.golden_hoe),
        Color(65, 102, 245).toConstraint(),
        Skills::farmingXP,
        profileState
    )
        .constrain {
            x = 52.5.percent()
            y = 5.pixels()
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val mining = SkillComponent(
        ItemComponent(Items.stone_pickaxe),
        Color(65, 102, 245).toConstraint(),
        Skills::miningXP,
        profileState
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val combat = SkillComponent(
        ItemComponent(Items.stone_sword),
        Color(65, 102, 245).toConstraint(),
        Skills::combatXP,
        profileState
    )
        .constrain {
            x = 52.5.percent()
            y = CopyConstraintFloat() boundTo mining
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val foraging = SkillComponent(
        ItemComponent(Item.getItemFromBlock(Blocks.sapling), 3),
        Color(65, 102, 245).toConstraint(),
        Skills::foragingXP,
        profileState
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val fishing = SkillComponent(
        ItemComponent(Items.fishing_rod),
        Color(65, 102, 245).toConstraint(),
        Skills::fishingXP,
        profileState
    )
        .constrain {
            x = 52.5.percent()
            y = CopyConstraintFloat() boundTo foraging
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val enchanting = SkillComponent(
        ItemComponent(Item.getItemFromBlock(Blocks.enchanting_table)),
        Color(65, 102, 245).toConstraint(),
        Skills::enchantingXP,
        profileState
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val alchemy = SkillComponent(
        ItemComponent(Items.brewing_stand),
        Color(65, 102, 245).toConstraint(),
        Skills::alchemyXP,
        profileState
    )
        .constrain {
            x = 52.5.percent()
            y = CopyConstraintFloat() boundTo enchanting
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val carpentry = SkillComponent(
        ItemComponent(Item.getItemFromBlock(Blocks.crafting_table)),
        Color(65, 102, 245).toConstraint(),
        Skills::carpentryXP,
        profileState
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val runecrafting = SkillComponent(
        ItemComponent(Items.magma_cream),
        Color(65, 102, 245).toConstraint(),
        Skills::runecraftingXP,
        profileState
    )
        .constrain {
            x = 52.5.percent()
            y = basicYConstraint { carpentry.getTop() }
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val wardrobe = WardrobeComponent(profileState).constrain {
        x = 5.percent
        y = SiblingConstraint(5f)
    } childOf contentContainer

    private val inventory = InventoryComponent(profileState.alwaysMap { it?.inventory }).constrain {
        x = 5.percent()
        y = SiblingConstraint(5f)
        width = (9 * (16 + 2)).pixels
        height = (4 * (16 + 2)).pixels
    } childOf contentContainer

    private val hotm = HOTMComponent(profileState).constrain {
        x = 5.percent()
        y = SiblingConstraint(5f)
        width = (9 * (16 + 2)).pixels
        height = (7 * (16 + 2)).pixels
    } childOf contentContainer

    private val dungeons = DungeonsComponent(hypixelPlayer, profileState).constrain {
        x = 5.percent()
        y = SiblingConstraint(5f)
        width = RelativeConstraint()
        height = ChildBasedRangeConstraint() + 5.pixels
    } childOf contentContainer

    init {
        if (EssentialAPI.getMinecraftUtil().isDevelopment()) {
            Inspector(window).constrain {
                x = 10.pixels
                y = 10.pixels
            } childOf window
        }
    }

}