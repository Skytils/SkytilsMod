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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod

import gg.essential.vigilance.gui.SettingsGui
import com.google.common.collect.Lists
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import skytils.skytilsmod.commands.ArmorColorCommand
import skytils.skytilsmod.commands.GlintCustomizeCommand
import skytils.skytilsmod.commands.RepartyCommand
import skytils.skytilsmod.commands.SkytilsCommand
import skytils.skytilsmod.commands.stats.impl.CataCommand
import skytils.skytilsmod.core.*
import skytils.skytilsmod.events.PacketEvent
import skytils.skytilsmod.features.impl.dungeons.*
import skytils.skytilsmod.features.impl.dungeons.solvers.*
import skytils.skytilsmod.features.impl.dungeons.solvers.terminals.*
import skytils.skytilsmod.features.impl.events.GriffinBurrows
import skytils.skytilsmod.features.impl.events.MayorDiana
import skytils.skytilsmod.features.impl.events.MayorJerry
import skytils.skytilsmod.features.impl.events.TechnoMayor
import skytils.skytilsmod.features.impl.farming.FarmingFeatures
import skytils.skytilsmod.features.impl.farming.TreasureHunter
import skytils.skytilsmod.features.impl.handlers.*
import skytils.skytilsmod.features.impl.mining.DarkModeMist
import skytils.skytilsmod.features.impl.mining.MiningFeatures
import skytils.skytilsmod.features.impl.misc.*
import skytils.skytilsmod.features.impl.overlays.AuctionPriceOverlay
import skytils.skytilsmod.features.impl.protectitems.ProtectItems
import skytils.skytilsmod.features.impl.spidersden.RainTimer
import skytils.skytilsmod.features.impl.spidersden.RelicWaypoints
import skytils.skytilsmod.features.impl.spidersden.SpidersDenFeatures
import skytils.skytilsmod.features.impl.trackers.MayorJerryTracker
import skytils.skytilsmod.features.impl.trackers.MythologicalTracker
import skytils.skytilsmod.gui.LocationEditGui
import skytils.skytilsmod.gui.OptionsGui
import skytils.skytilsmod.gui.commandaliases.CommandAliasesGui
import skytils.skytilsmod.gui.keyshortcuts.KeyShortcutsGui
import skytils.skytilsmod.listeners.ChatListener
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.mixins.accessors.AccessorCommandHandler
import skytils.skytilsmod.mixins.accessors.AccessorSettingsGui
import skytils.skytilsmod.utils.SBInfo
import skytils.skytilsmod.utils.Utils
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import java.awt.Desktop
import java.io.File
import java.net.URL
import java.util.concurrent.Executors


@Mod(
    modid = Skytils.MODID,
    name = Skytils.MOD_NAME,
    version = Skytils.VERSION,
    acceptedMinecraftVersions = "[1.8.9]",
    clientSideOnly = true
)
class Skytils {

    companion object {
        const val MODID = "skytils"
        const val MOD_NAME = "Skytils"
        const val VERSION = "1.0-pre11"

        @JvmField
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        @JvmField
        val mc: Minecraft = Minecraft.getMinecraft()

        lateinit var config: Config

        @JvmField
        val modDir = File(File(mc.mcDataDir, "config"), "skytils")

        @JvmStatic
        lateinit var guiManager: GuiManager
        var ticks = 0

        @JvmField
        val sendMessageQueue = ArrayDeque<String>()

        @JvmField
        var usingDungeonRooms = false

        @JvmField
        var usingLabymod = false

        @JvmField
        var usingNEU = false

        @JvmField
        var jarFile: File? = null
        private var lastChatMessage = 0L

        @JvmField
        var displayScreen: GuiScreen? = null

        @JvmField
        val threadPool = Executors.newFixedThreadPool(10)
    }


    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        // Must use reflection otherwise the "constant" value will be inlined by compiler
        val forgeVersion = try {
            ForgeVersion::class.java.getDeclaredField("buildVersion").get(null) as Int
        } catch (e: Exception) {
            e.printStackTrace()
            2318
        }
        // Asbyth's forge fork uses version 0
        if (!(forgeVersion >= 2318 || forgeVersion == 0)) {
            Desktop.getDesktop()
                .browse(URL("https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html").toURI())
            throw RuntimeException("Skytils can't run on this Minecraft Forge version! Please use the latest Forge build 2318 for 1.8.9.")
        }
        if (!modDir.exists()) modDir.mkdirs()
        File(modDir, "trackers").mkdirs()
        guiManager = GuiManager()
        jarFile = event.sourceFile
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        config = Config()
        config.preload()

        UpdateChecker.downloadDeleteTask()

        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(ChatListener())
        MinecraftForge.EVENT_BUS.register(DungeonListener)
        MinecraftForge.EVENT_BUS.register(guiManager)
        MinecraftForge.EVENT_BUS.register(MayorInfo)
        MinecraftForge.EVENT_BUS.register(SBInfo)
        MinecraftForge.EVENT_BUS.register(SoundQueue)
        MinecraftForge.EVENT_BUS.register(UpdateChecker)

        MinecraftForge.EVENT_BUS.register(SpamHider())

        MinecraftForge.EVENT_BUS.register(AlignmentTaskSolver())
        MinecraftForge.EVENT_BUS.register(ArmorColor())
        MinecraftForge.EVENT_BUS.register(AuctionData())
        MinecraftForge.EVENT_BUS.register(AuctionPriceOverlay())
        MinecraftForge.EVENT_BUS.register(BlazeSolver())
        MinecraftForge.EVENT_BUS.register(BlockAbility())
        MinecraftForge.EVENT_BUS.register(BossHPDisplays())
        MinecraftForge.EVENT_BUS.register(BoulderSolver())
        MinecraftForge.EVENT_BUS.register(ChestProfit())
        MinecraftForge.EVENT_BUS.register(ClickInOrderSolver())
        MinecraftForge.EVENT_BUS.register(CreeperSolver())
        MinecraftForge.EVENT_BUS.register(CommandAliases())
        MinecraftForge.EVENT_BUS.register(DamageSplash())
        MinecraftForge.EVENT_BUS.register(DarkModeMist())
        MinecraftForge.EVENT_BUS.register(DungeonFeatures())
        MinecraftForge.EVENT_BUS.register(DungeonMap())
        MinecraftForge.EVENT_BUS.register(DungeonTimer())
        MinecraftForge.EVENT_BUS.register(FarmingFeatures())
        MinecraftForge.EVENT_BUS.register(FavoritePets())
        MinecraftForge.EVENT_BUS.register(GlintCustomizer())
        MinecraftForge.EVENT_BUS.register(GriffinBurrows())
        MinecraftForge.EVENT_BUS.register(IceFillSolver())
        MinecraftForge.EVENT_BUS.register(IcePathSolver())
        MinecraftForge.EVENT_BUS.register(ItemFeatures())
        MinecraftForge.EVENT_BUS.register(KeyShortcuts())
        MinecraftForge.EVENT_BUS.register(LockOrb())
        MinecraftForge.EVENT_BUS.register(MayorDiana())
        MinecraftForge.EVENT_BUS.register(MayorJerry())
        MinecraftForge.EVENT_BUS.register(MayorJerryTracker)
        MinecraftForge.EVENT_BUS.register(MiningFeatures())
        MinecraftForge.EVENT_BUS.register(MinionFeatures())
        MinecraftForge.EVENT_BUS.register(MiscFeatures())
        MinecraftForge.EVENT_BUS.register(MythologicalTracker())
        MinecraftForge.EVENT_BUS.register(PetFeatures())
        MinecraftForge.EVENT_BUS.register(ProtectItems())
        MinecraftForge.EVENT_BUS.register(RainTimer())
        MinecraftForge.EVENT_BUS.register(RelicWaypoints())
        MinecraftForge.EVENT_BUS.register(ScoreCalculation())
        MinecraftForge.EVENT_BUS.register(SelectAllColorSolver())
        MinecraftForge.EVENT_BUS.register(ShootTheTargetSolver())
        MinecraftForge.EVENT_BUS.register(SimonSaysSolver())
        MinecraftForge.EVENT_BUS.register(SlayerFeatures())
        MinecraftForge.EVENT_BUS.register(SpidersDenFeatures())
        MinecraftForge.EVENT_BUS.register(StartsWithSequenceSolver())
        MinecraftForge.EVENT_BUS.register(TankDisplayStuff())
        MinecraftForge.EVENT_BUS.register(TechnoMayor())
        MinecraftForge.EVENT_BUS.register(TeleportMazeSolver())
        MinecraftForge.EVENT_BUS.register(TerminalFeatures())
        MinecraftForge.EVENT_BUS.register(ThreeWeirdosSolver())
        MinecraftForge.EVENT_BUS.register(TicTacToeSolver())
        MinecraftForge.EVENT_BUS.register(TreasureHunter())
        MinecraftForge.EVENT_BUS.register(TriviaSolver())
        MinecraftForge.EVENT_BUS.register(WaterBoardSolver())
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        usingDungeonRooms = Loader.isModLoaded("dungeonrooms")
        usingLabymod = Loader.isModLoaded("labymod")
        usingNEU = Loader.isModLoaded("notenoughupdates")

        val cch = ClientCommandHandler.instance

        if (cch is AccessorCommandHandler) {
            cch.registerCommand(SkytilsCommand)

            cch.registerCommand(CataCommand)

            if (!cch.commands.containsKey("armorcolor")) {
                cch.registerCommand(ArmorColorCommand)
            }

            if (!cch.commands.containsKey("blockability")) {
                //cch.registerCommand(BlockAbilityCommand);
            }

            if (!cch.commands.containsKey("glintcustomize")) {
                cch.registerCommand(GlintCustomizeCommand)
            }

            if (config.overrideReparty || !cch.commands.containsKey("reparty")) {
                cch.commandSet.add(RepartyCommand)
                cch.commandMap["reparty"] = RepartyCommand
            }

            if (config.overrideReparty || !cch.commands.containsKey("rp")) {
                cch.commandSet.add(RepartyCommand)
                cch.commandMap["rp"] = RepartyCommand
            }
        } else throw RuntimeException("Skytils was unable to mixin to the CommandHandler. Please report this on our Discord at discord.gg/skytils.")

        MayorInfo.fetchMayorData()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        ScreenRenderer.refresh()

        if (displayScreen != null) {
            mc.displayGuiScreen(displayScreen)
            displayScreen = null
        }

        if (mc.thePlayer != null && sendMessageQueue.size > 0 && System.currentTimeMillis() - lastChatMessage > 200) {
            mc.thePlayer.sendChatMessage(sendMessageQueue.removeFirst())
        }

        if (ticks % 20 == 0) {
            if (mc.thePlayer != null) {
                Utils.checkForSkyblock()
                Utils.checkForDungeons()
            }
            ticks = 0
        }

        ticks++
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketEvent.SendEvent) {
        if (event.packet is C01PacketChatMessage) {
            lastChatMessage = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent) {
        if (mc.currentScreen is OptionsGui && event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onGuiInitPost(event: GuiScreenEvent.InitGuiEvent.Post) {
        if (config.configButtonOnPause && event.gui is GuiIngameMenu) {
            val x = event.gui.width - 105
            val x2 = x + 100
            var y = event.gui.height - 22
            var y2 = y + 20
            val sorted = Lists.newArrayList(event.buttonList)
            sorted.sortWith { a, b -> b.yPosition + b.height - a.yPosition + a.height }
            for (button in sorted) {
                val otherX = button.xPosition
                val otherX2 = button.xPosition + button.width
                val otherY = button.yPosition
                val otherY2 = button.yPosition + button.height
                if (otherX2 > x && otherX < x2 && otherY2 > y && otherY < y2) {
                    y = otherY - 20 - 2
                    y2 = y + 20
                }
            }
            event.buttonList.add(GuiButton(6969420, x, 0.coerceAtLeast(y), 100, 20, "Skytils"))
        }
    }

    @SubscribeEvent
    fun onGuiAction(event: GuiScreenEvent.ActionPerformedEvent.Post) {
        if (config.configButtonOnPause && event.gui is GuiIngameMenu && event.button.id == 6969420) {
            displayScreen = OptionsGui()
        }
    }

    @SubscribeEvent
    fun onGuiChange(event: GuiOpenEvent) {
        val old = mc.currentScreen
        if (event.gui == null && config.reopenOptionsMenu) {
            if (old is CommandAliasesGui || old is LocationEditGui || old is KeyShortcutsGui || (old is SettingsGui && (old as AccessorSettingsGui).config is Config)) {
                TickTask(1) {
                    displayScreen = OptionsGui()
                }
            }
        }
    }
}
