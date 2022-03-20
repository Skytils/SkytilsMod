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

package skytils.skytilsmod

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import gg.essential.universal.UChat
import gg.essential.universal.UKeyboard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.settings.KeyBinding
import net.minecraft.inventory.ContainerChest
import net.minecraft.launchwrapper.Launch
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.*
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
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import skytils.hylin.HylinAPI.Companion.createHylinAPI
import skytils.skytilsmod.commands.impl.*
import skytils.skytilsmod.commands.stats.impl.CataCommand
import skytils.skytilsmod.commands.stats.impl.SlayerCommand
import skytils.skytilsmod.core.*
import skytils.skytilsmod.events.impl.MainReceivePacketEvent
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
import skytils.skytilsmod.features.impl.trackers.impl.DupeTracker
import skytils.skytilsmod.features.impl.trackers.impl.MayorJerryTracker
import skytils.skytilsmod.features.impl.trackers.impl.MythologicalTracker
import skytils.skytilsmod.gui.OptionsGui
import skytils.skytilsmod.gui.ReopenableGUI
import skytils.skytilsmod.listeners.ChatListener
import skytils.skytilsmod.listeners.DungeonListener
import skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase
import skytils.skytilsmod.mixins.transformers.accessors.AccessorCommandHandler
import skytils.skytilsmod.mixins.transformers.accessors.AccessorGuiStreamUnavailable
import skytils.skytilsmod.mixins.transformers.accessors.AccessorSettingsGui
import skytils.skytilsmod.utils.*
import skytils.skytilsmod.utils.graphics.ScreenRenderer
import sun.misc.Unsafe
import java.io.File
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import kotlin.coroutines.CoroutineContext


@Mod(
    modid = Skytils.MODID,
    name = Skytils.MOD_NAME,
    version = Skytils.VERSION,
    acceptedMinecraftVersions = "[1.8.9]",
    clientSideOnly = true
)
class Skytils {

    companion object : CoroutineScope {
        const val MODID = "skytils"
        const val MOD_NAME = "Skytils"
        const val VERSION = "1.2.0-pre6"

        @JvmField
        val gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Enchant::class.java, Enchant.Serializer())
            .create()

        @JvmStatic
        val mc: Minecraft by lazy {
            Minecraft.getMinecraft()
        }

        val config = Config

        @JvmField
        val modDir = File(File(mc.mcDataDir, "config"), "skytils")

        @JvmStatic
        lateinit var guiManager: GuiManager
        var ticks = 0

        @JvmField
        val sendMessageQueue = LinkedList<String>()

        @JvmField
        var usingLabymod = false

        @JvmField
        var usingNEU = false

        @JvmField
        var usingSBA = false

        @JvmField
        var jarFile: File? = null
        private var lastChatMessage = 0L

        @JvmField
        var displayScreen: GuiScreen? = null

        @JvmField
        val threadPool = Executors.newFixedThreadPool(10) as ThreadPoolExecutor

        @JvmField
        val dispatcher = threadPool.asCoroutineDispatcher()

        override val coroutineContext: CoroutineContext = dispatcher + SupervisorJob()

        val hylinAPI = createHylinAPI("", false)

        val deobfEnvironment by lazy {
            Launch.blackboard.getOrDefault("fml.deobfuscatedEnvironment", false) as Boolean
        }

        val unsafe by lazy {
            Unsafe::class.java.getDeclaredField("theUnsafe").apply {
                isAccessible = true
            }.get(null) as Unsafe
        }

        val areaRegex = Regex("§r§b§l(?<area>[\\w]+): §r§7(?<loc>[\\w ]+)§r")

        var domain = "api.skytils.gg"
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        DataFetcher.preload()
        modDir.mkdirs()
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

        arrayOf(
            this,
            ChatListener(),
            DungeonListener,
            guiManager,
            MayorInfo,
            SBInfo,
            SoundQueue,
            TickTaskManager,
            UpdateChecker,

            AlignmentTaskSolver,
            ArmorColor(),
            AuctionData(),
            AuctionPriceOverlay(),
            BlazeSolver(),
            BloodHelper,
            BossHPDisplays(),
            BoulderSolver(),
            ChatTabs,
            ChangeAllToSameColorSolver,
            ChestProfit(),
            ClickInOrderSolver,
            CreeperSolver(),
            CommandAliases(),
            CooldownTracker(),
            CustomNotifications(),
            DamageSplash(),
            DarkModeMist(),
            DungeonFeatures,
            DungeonMap(),
            DungeonTimer(),
            DupeTracker,
            EnchantNames(),
            EnterToConfirmSignPopup(),
            FarmingFeatures(),
            FavoritePets(),
            GlintCustomizer(),
            GriffinBurrows,
            IceFillSolver(),
            IcePathSolver(),
            ItemFeatures(),
            KeyShortcuts(),
            LockOrb(),
            MasterMode7Features,
            MayorDiana(),
            MayorJerry(),
            MayorJerryTracker,
            MiningFeatures(),
            MinionFeatures(),
            MiscFeatures(),
            MythologicalTracker(),
            PartyFinderStats,
            PetFeatures(),
            Ping,
            PricePaid,
            ProtectItems(),
            RainTimer(),
            RandomStuff,
            RelicWaypoints(),
            ScoreCalculation,
            SelectAllColorSolver,
            ShootTheTargetSolver,
            SimonSaysSolver,
            SlayerFeatures(),
            SpidersDenFeatures(),
            SpiritLeap(),
            StartsWithSequenceSolver,
            StupidTreasureChestOpeningThing,
            TankDisplayStuff(),
            TechnoMayor(),
            TeleportMazeSolver(),
            TerminalFeatures,
            ThreeWeirdosSolver(),
            TicTacToeSolver(),
            TreasureHunter(),
            TriviaSolver(),
            WaterBoardSolver(),
            Waypoints(),
        ).forEach(MinecraftForge.EVENT_BUS::register)
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        usingLabymod = Loader.isModLoaded("labymod")
        usingNEU = Loader.isModLoaded("notenoughupdates")
        usingSBA = Loader.isModLoaded("skyblockaddons")

        val cch = ClientCommandHandler.instance

        if (cch !is AccessorCommandHandler) throw RuntimeException("Skytils was unable to mixin to the CommandHandler. Please report this on our Discord at discord.gg/skytils.")
        cch.registerCommand(SkytilsCommand)

        cch.registerCommand(CataCommand)
        cch.registerCommand(CalcXPCommand)
        cch.registerCommand(LimboCommand)
        cch.registerCommand(HollowWaypointCommand)
        cch.registerCommand(SlayerCommand)

        if (!cch.commands.containsKey("armorcolor")) {
            cch.registerCommand(ArmorColorCommand)
        }

        if (!cch.commands.containsKey("glintcustomize")) {
            cch.registerCommand(GlintCustomizeCommand)
        }

        if (!cch.commands.containsKey("protectitem")) {
            cch.registerCommand(ProtectItemCommand)
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

        MayorInfo.fetchMayorData()

        MinecraftForge.EVENT_BUS.register(SpamHider())

        ModChecker.checkModdedForge()
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        ScreenRenderer.refresh()

        ScoreboardUtil.sidebarLines = ScoreboardUtil.fetchScoreboardLines().map { ScoreboardUtil.cleanSB(it) }
        TabListUtils.tabEntries = TabListUtils.fetchTabEntires().map { it to it?.text }
        if (displayScreen != null) {
            if (mc.thePlayer?.openContainer == mc.thePlayer?.inventoryContainer) {
                mc.displayGuiScreen(displayScreen)
                displayScreen = null
            }
        }

        if (mc.thePlayer != null && sendMessageQueue.isNotEmpty() && System.currentTimeMillis() - lastChatMessage > 250) {
            val msg = sendMessageQueue.pollFirst()
            if (!msg.isNullOrBlank()) mc.thePlayer.sendChatMessage(msg)
        }

        if (ticks % 20 == 0) {
            if (mc.thePlayer != null) {
                if (deobfEnvironment) {
                    if (DevTools.toggles.getOrDefault("forcehypixel", false)) Utils.isOnHypixel = true
                    if (DevTools.toggles.getOrDefault("forceskyblock", false)) Utils.skyblock = true
                    if (DevTools.toggles.getOrDefault("forcedungeons", false)) Utils.dungeons = true
                }
                if (DevTools.getToggle("sprint"))
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
            }
            ticks = 0
        }
        if (Utils.inSkyblock && DevTools.getToggle("copydetails") && UKeyboard.isCtrlKeyDown()) {
            if (UKeyboard.isKeyDown(UKeyboard.KEY_TAB)) {
                UChat.chat("Copied tab data to clipboard")
                GuiScreen.setClipboardString(TabListUtils.tabEntries.map { it.second }.toString())
            }
            if (UKeyboard.isKeyDown(UKeyboard.KEY_CAPITAL)) {
                UChat.chat("Copied scoreboard data to clipboard")
                GuiScreen.setClipboardString(ScoreboardUtil.sidebarLines.toString())
            }
            val container = mc.thePlayer?.openContainer
            if (UKeyboard.isKeyDown(UKeyboard.KEY_LMETA) && container is ContainerChest) {
                UChat.chat("Copied container data to clipboard")
                GuiScreen.setClipboardString(
                    "Name: '${container.lowerChestInventory.name}', Items: ${
                        container.inventorySlots.filter { it.inventory == container.lowerChestInventory }
                            .map { it.stack?.serializeNBT() }
                    }"
                )
            }
        }

        ticks++
    }

    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        Utils.isOnHypixel = mc.runCatching {
            !event.isLocal && (thePlayer?.clientBrand?.lowercase()?.contains("hypixel")
                ?: currentServerData?.serverIP?.lowercase()?.contains("hypixel") ?: false)
        }.onFailure { it.printStackTrace() }.getOrDefault(false)
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
        if (event.packet is S01PacketJoinGame) {
            Utils.skyblock = false
            Utils.dungeons = false
        }
        if (!Utils.inSkyblock && Utils.isOnHypixel && event.packet is S3DPacketDisplayScoreboard && event.packet.func_149371_c() == 1) {
            Utils.skyblock = event.packet.func_149370_d() == "SBScoreboard"
            printDevMessage("score ${event.packet.func_149370_d()}", "utils")
            printDevMessage("sb ${Utils.inSkyblock}", "utils")
        }
        if (event.packet is S1CPacketEntityMetadata && mc.thePlayer != null) {
            val nameObj = event.packet.func_149376_c()?.find { it.dataValueId == 2 } ?: return
            val entity = mc.theWorld.getEntityByID(event.packet.entityId)

            if (entity is ExtensionEntityLivingBase) {
                entity.skytilsHook.onNewDisplayName(nameObj.`object` as String)
            }
        }
        if (!Utils.isOnHypixel && event.packet is S3FPacketCustomPayload && event.packet.channelName == "MC|Brand") {
            if (event.packet.bufferData.readStringFromBuffer(Short.MAX_VALUE.toInt()).lowercase().contains("hypixel"))
                Utils.isOnHypixel = true
        }
        if (Utils.inDungeons || !Utils.isOnHypixel || event.packet !is S38PacketPlayerListItem ||
            (event.packet.action != S38PacketPlayerListItem.Action.UPDATE_DISPLAY_NAME &&
                    event.packet.action != S38PacketPlayerListItem.Action.ADD_PLAYER)
        ) return
        event.packet.entries.forEach { playerData ->
            val name = playerData?.displayName?.formattedText ?: playerData?.profile?.name ?: return@forEach
            areaRegex.matchEntire(name)?.let { result ->
                Utils.dungeons = Utils.inSkyblock && result.groups["area"]?.value == "Dungeon"
                printDevMessage("dungeons ${Utils.inDungeons} action ${event.packet.action}", "utils")
                if (Utils.inDungeons)
                    ScoreCalculation.updateText(ScoreCalculation.totalScore.get())
                return@forEach
            }
        }
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        Utils.isOnHypixel = false
        Utils.skyblock = false
        Utils.dungeons = false
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
            if (old is ReopenableGUI || (old is AccessorSettingsGui && old.config is Config)) {
                TickTask(1) {
                    if (mc.thePlayer?.openContainer == mc.thePlayer?.inventoryContainer)
                        displayScreen = OptionsGui()
                }
            }
        }
        if (old is AccessorGuiStreamUnavailable) {
            if (config.twitchFix && event.gui == null && !(Utils.skyblock && old.parentScreen is GuiGameOver)) {
                event.gui = old.parentScreen
            }
        }
    }
}
