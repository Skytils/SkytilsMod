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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import gg.essential.vigilance.gui.SettingsGui
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.settings.KeyBinding
import net.minecraft.launchwrapper.Launch
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.S1CPacketEntityMetadata
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import skytils.hylin.HylinAPI.Companion.createHylinAPI
import skytils.skytilsmod.commands.impl.*
import skytils.skytilsmod.commands.stats.impl.CataCommand
import skytils.skytilsmod.commands.stats.impl.SlayerCommand
import skytils.skytilsmod.core.*
import skytils.skytilsmod.events.impl.PacketEvent
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
import skytils.skytilsmod.features.impl.mining.StupidTreasureChestOpeningThing
import skytils.skytilsmod.features.impl.misc.*
import skytils.skytilsmod.features.impl.overlays.AuctionPriceOverlay
import skytils.skytilsmod.features.impl.protectitems.ProtectItems
import skytils.skytilsmod.features.impl.spidersden.RainTimer
import skytils.skytilsmod.features.impl.spidersden.RelicWaypoints
import skytils.skytilsmod.features.impl.spidersden.SpidersDenFeatures
import skytils.skytilsmod.features.impl.trackers.impl.MayorJerryTracker
import skytils.skytilsmod.features.impl.trackers.impl.MythologicalTracker
import skytils.skytilsmod.gui.OptionsGui
import skytils.skytilsmod.gui.ReopenableGUI
import skytils.skytilsmod.listeners.ChatListener
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase
import skytils.skytilsmod.mixins.transformers.accessors.AccessorCommandHandler
import skytils.skytilsmod.mixins.transformers.accessors.AccessorSettingsGui
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import java.io.File
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor


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
        const val VERSION = "1.0.9-pre1.1"

        @JvmField
        val gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Enchant::class.java, Enchant.Serializer())
            .create()

        @JvmStatic
        val mc: Minecraft
            get() = Minecraft.getMinecraft()

        val config = Config

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
        val threadPool = Executors.newFixedThreadPool(10) as ThreadPoolExecutor

        val hylinAPI = createHylinAPI("", false)
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        if (!modDir.exists()) modDir.mkdirs()
        File(modDir, "trackers").mkdirs()
        guiManager = GuiManager()
        jarFile = event.sourceFile
        mc.framebuffer.enableStencil()
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        config.init()
        hylinAPI.key = config.apiKey

        UpdateChecker.downloadDeleteTask()

        MinecraftForge.EVENT_BUS.register(this)
        MinecraftForge.EVENT_BUS.register(ChatListener())
        MinecraftForge.EVENT_BUS.register(DungeonListener)
        MinecraftForge.EVENT_BUS.register(guiManager)
        MinecraftForge.EVENT_BUS.register(MayorInfo)
        MinecraftForge.EVENT_BUS.register(SBInfo)
        MinecraftForge.EVENT_BUS.register(SoundQueue)
        MinecraftForge.EVENT_BUS.register(TickTaskManager)
        MinecraftForge.EVENT_BUS.register(UpdateChecker)

        MinecraftForge.EVENT_BUS.register(AlignmentTaskSolver())
        MinecraftForge.EVENT_BUS.register(ArmorColor())
        MinecraftForge.EVENT_BUS.register(AuctionData())
        MinecraftForge.EVENT_BUS.register(AuctionPriceOverlay())
        MinecraftForge.EVENT_BUS.register(BlazeSolver())
        MinecraftForge.EVENT_BUS.register(BossHPDisplays())
        MinecraftForge.EVENT_BUS.register(BoulderSolver())
        MinecraftForge.EVENT_BUS.register(ChatTabs)
        MinecraftForge.EVENT_BUS.register(ChestProfit())
        MinecraftForge.EVENT_BUS.register(ClickInOrderSolver())
        MinecraftForge.EVENT_BUS.register(CreeperSolver())
        MinecraftForge.EVENT_BUS.register(CommandAliases())
        MinecraftForge.EVENT_BUS.register(CooldownTracker())
        MinecraftForge.EVENT_BUS.register(DamageSplash())
        MinecraftForge.EVENT_BUS.register(DarkModeMist())
        MinecraftForge.EVENT_BUS.register(DungeonFeatures())
        MinecraftForge.EVENT_BUS.register(DungeonMap())
        MinecraftForge.EVENT_BUS.register(DungeonTimer())
        MinecraftForge.EVENT_BUS.register(EnchantNames())
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
        MinecraftForge.EVENT_BUS.register(Ping)
        MinecraftForge.EVENT_BUS.register(ProtectItems())
        MinecraftForge.EVENT_BUS.register(RainTimer())
        MinecraftForge.EVENT_BUS.register(RelicWaypoints())
        MinecraftForge.EVENT_BUS.register(ScoreCalculation)
        MinecraftForge.EVENT_BUS.register(SelectAllColorSolver())
        MinecraftForge.EVENT_BUS.register(ShootTheTargetSolver())
        MinecraftForge.EVENT_BUS.register(SimonSaysSolver())
        MinecraftForge.EVENT_BUS.register(SlayerFeatures())
        MinecraftForge.EVENT_BUS.register(SpidersDenFeatures())
        MinecraftForge.EVENT_BUS.register(SpiritLeap())
        MinecraftForge.EVENT_BUS.register(StartsWithSequenceSolver())
        MinecraftForge.EVENT_BUS.register(StupidTreasureChestOpeningThing)
        MinecraftForge.EVENT_BUS.register(TankDisplayStuff())
        MinecraftForge.EVENT_BUS.register(TechnoMayor())
        MinecraftForge.EVENT_BUS.register(TeleportMazeSolver())
        MinecraftForge.EVENT_BUS.register(TerminalFeatures())
        MinecraftForge.EVENT_BUS.register(ThreeWeirdosSolver())
        MinecraftForge.EVENT_BUS.register(TicTacToeSolver())
        MinecraftForge.EVENT_BUS.register(TreasureHunter())
        MinecraftForge.EVENT_BUS.register(TriviaSolver())
        MinecraftForge.EVENT_BUS.register(WaterBoardSolver())
        MinecraftForge.EVENT_BUS.register(Waypoints())
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        usingDungeonRooms = Loader.isModLoaded("dungeonrooms")
        usingLabymod = Loader.isModLoaded("labymod")
        usingNEU = Loader.isModLoaded("notenoughupdates")

        if (usingDungeonRooms) {
            if (Loader.instance().indexedModList["dungeonrooms"]!!.version.startsWith("2.0")) {
                runCatching {
                    Class.forName("io.github.quantizr.utils.Utils").also {
                        ScoreCalculation.drmRoomScanMethod = MethodHandles.publicLookup().findStatic(
                            it, "roomList", MethodType.methodType(
                                List::class.java
                            )
                        )
                    }
                }
            } else {
                config.scoreCalculationMethod = 0
            }
        }

        val cch = ClientCommandHandler.instance

        if (cch !is AccessorCommandHandler) throw RuntimeException("Skytils was unable to mixin to the CommandHandler. Please report this on our Discord at discord.gg/skytils.")
        cch.registerCommand(SkytilsCommand)

        cch.registerCommand(CataCommand)
        cch.registerCommand(CalcXPCommand)
        cch.registerCommand(HollowWaypointCommand)
        cch.registerCommand(SlayerCommand)

        if (!cch.commands.containsKey("armorcolor")) {
            cch.registerCommand(ArmorColorCommand)
        }

        if (!cch.commands.containsKey("glintcustomize")) {
            cch.registerCommand(GlintCustomizeCommand)
        }

        if (!cch.commands.containsKey("trackcooldown")) {
            cch.registerCommand(TrackCooldownCommand)
        }

        cch.commandSet.add(RepartyCommand)
        cch.commandMap["skytilsreparty"] = RepartyCommand
        if (config.overrideReparty || !cch.commands.containsKey("reparty")) {
            cch.commandMap["reparty"] = RepartyCommand
        }

        if (config.overrideReparty || !cch.commands.containsKey("rp")) {
            cch.commandMap["rp"] = RepartyCommand
        }

        DataFetcher.preload()
        MayorInfo.fetchMayorData()

        MinecraftForge.EVENT_BUS.register(SpamHider())
        Launch.classLoader.findClass("net.minecraft.client.gui.ServerListEntryNormal")
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        ScreenRenderer.refresh()

        if (displayScreen != null) {
            mc.displayGuiScreen(displayScreen)
            displayScreen = null
        }

        if (mc.thePlayer != null && sendMessageQueue.isNotEmpty() && System.currentTimeMillis() - lastChatMessage > 250) {
            val msg = sendMessageQueue.removeFirstOrNull()
            if (!msg.isNullOrBlank()) mc.thePlayer.sendChatMessage(msg)
        }

        if (ticks % 20 == 0) {
            if (mc.thePlayer != null) {
                Utils.isOnHypixel = mc.runCatching {
                    theWorld != null && !isSingleplayer && (thePlayer?.clientBrand?.lowercase()?.contains("hypixel")
                        ?: currentServerData?.serverIP?.lowercase()?.contains("hypixel") ?: false)
                }.onFailure { it.printStackTrace() }.getOrDefault(false)
                Utils.inSkyblock = Utils.isOnHypixel && mc.theWorld.scoreboard.getObjectiveInDisplaySlot(1)
                    ?.let { ScoreboardUtil.cleanSB(it.displayName).contains("SKYBLOCK") } ?: false
                Utils.inDungeons = Utils.inSkyblock && ScoreboardUtil.sidebarLines.any {
                    ScoreboardUtil.cleanSB(it).run {
                        (contains("The Catacombs") && !contains("Queue")) || contains("Dungeon Cleared:")
                    }
                }
                if (DevTools.getToggle("sprint"))
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
            }
            ticks = 0
        }

        ticks++
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        Utils.isOnHypixel = false
        Utils.inSkyblock = false
        Utils.inDungeons = false
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
            val sorted = event.buttonList.sortedWith { a, b -> b.yPosition + b.height - a.yPosition + a.height }
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
            if (old is ReopenableGUI || (old is SettingsGui && (old as AccessorSettingsGui).config is Config)) {
                TickTask(1) {
                    displayScreen = OptionsGui()
                }
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketEvent.ReceiveEvent) {
        if (event.packet is S1CPacketEntityMetadata) {
            val nameObj = event.packet.func_149376_c()?.find { it.dataValueId == 2 }
            if (nameObj != null) {
                val entity = mc.theWorld.getEntityByID(event.packet.entityId)

                if (entity is ExtensionEntityLivingBase) {
                    entity.skytilsHook.onNewDisplayName(nameObj.`object` as String)
                }
            }
        }
    }
}
