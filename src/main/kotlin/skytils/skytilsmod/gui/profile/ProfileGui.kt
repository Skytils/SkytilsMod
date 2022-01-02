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
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State
import gg.essential.universal.UMinecraft
import kotlinx.coroutines.runBlocking
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import skytils.hylin.extension.getLatestSkyblockProfile
import skytils.hylin.extension.nonDashedString
import skytils.hylin.skyblock.Member
import skytils.hylin.skyblock.Skills
import skytils.skytilsmod.Skytils.Companion.hylinAPI
import skytils.skytilsmod.gui.profile.components.ItemComponent
import skytils.skytilsmod.gui.profile.components.XPComponent
import java.awt.Color
import java.util.*

class ProfileGui(uuid: UUID) : WindowScreen(drawDefaultBackground = false) {
    private val uuidState: State<UUID> = BasicState(uuid)
    private val profileState: State<Member> = uuidState.map {
        runBlocking {
            hylinAPI.getSkyblockProfiles(it).await().getLatestSkyblockProfile(it)!!.members[uuid.nonDashedString()]!!
        }
    }
    private val gameProfileState: State<GameProfile?> = uuidState.map {
        val profile = GameProfile(it, "")
        UMinecraft.getMinecraft().sessionService.fillProfileProperties(profile, true)
        return@map profile
    }
    private val profileData =
        hylinAPI.getSkyblockProfilesSync(uuid).getLatestSkyblockProfile(uuid)!!.members[uuid.nonDashedString()]!!

    private val navBar by UIBlock(Color(0, 0, 0, 160))
        .constrain {
            x = 0.pixels()
            y = 0.pixels()
            width = RelativeConstraint()
            height = 25.pixels()
        } childOf window

    // Player Section

    private val playerContainer by UIBlock(Color(0, 0, 0, 40))
        .constrain {
            x = 0.pixels()
            y = SiblingConstraint()
            width = 35.percent()
            height = basicHeightConstraint { window.getHeight() - 25f }
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

    private val taming = XPComponent(
        ItemComponent(Items.spawn_egg),
        Color(65, 102, 245).toConstraint(),
        Skills::tamingXP,
        profileData
    )
        .constrain {
            x = 5.percent()
            y = 5.pixels()
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val farming = XPComponent(
        ItemComponent(Items.golden_hoe),
        Color(65, 102, 245).toConstraint(),
        Skills::farmingXP,
        profileData
    )
        .constrain {
            x = 52.5.percent()
            y = 5.pixels()
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val mining = XPComponent(
        ItemComponent(Items.stone_pickaxe),
        Color(65, 102, 245).toConstraint(),
        Skills::miningXP,
        profileData
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val combat = XPComponent(
        ItemComponent(Items.stone_sword),
        Color(65, 102, 245).toConstraint(),
        Skills::combatXP,
        profileData
    )
        .constrain {
            x = 52.5.percent()
            y = basicYConstraint { mining.getTop() }
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val foraging = XPComponent(
        ItemComponent(Item.getItemFromBlock(Blocks.sapling), 3),
        Color(65, 102, 245).toConstraint(),
        Skills::foragingXP,
        profileData
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val fishing = XPComponent(
        ItemComponent(Items.fishing_rod),
        Color(65, 102, 245).toConstraint(),
        Skills::fishingXP,
        profileData
    )
        .constrain {
            x = 52.5.percent()
            y = basicYConstraint { foraging.getTop() }
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val enchanting = XPComponent(
        ItemComponent(Item.getItemFromBlock(Blocks.enchanting_table)),
        Color(65, 102, 245).toConstraint(),
        Skills::enchantingXP,
        profileData
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val alchemy = XPComponent(
        ItemComponent(Items.brewing_stand),
        Color(65, 102, 245).toConstraint(),
        Skills::alchemyXP,
        profileData
    )
        .constrain {
            x = 52.5.percent()
            y = basicYConstraint { enchanting.getTop() }
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val carpentry = XPComponent(
        ItemComponent(Item.getItemFromBlock(Blocks.crafting_table)),
        Color(65, 102, 245).toConstraint(),
        Skills::carpentryXP,
        profileData
    )
        .constrain {
            x = 5.percent()
            y = SiblingConstraint(5f)
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

    private val runecrafting = XPComponent(
        ItemComponent(Items.magma_cream),
        Color(65, 102, 245).toConstraint(),
        Skills::runecraftingXP,
        profileData
    )
        .constrain {
            x = 52.5.percent()
            y = basicYConstraint { carpentry.getTop() }
            width = 42.5.percent()
            height = 20.pixels()
        } childOf skillContainer

}