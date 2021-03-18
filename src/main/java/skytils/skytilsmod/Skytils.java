package skytils.skytilsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.commands.*;
import skytils.skytilsmod.core.Config;
import skytils.skytilsmod.core.DataFetcher;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.core.UpdateChecker;
import skytils.skytilsmod.events.SendPacketEvent;
import skytils.skytilsmod.features.impl.SlayerFeatures;
import skytils.skytilsmod.features.impl.dungeons.BossHPDisplays;
import skytils.skytilsmod.features.impl.dungeons.DungeonsFeatures;
import skytils.skytilsmod.features.impl.dungeons.ScoreCalculation;
import skytils.skytilsmod.features.impl.dungeons.solvers.*;
import skytils.skytilsmod.features.impl.dungeons.solvers.terminals.*;
import skytils.skytilsmod.features.impl.events.GriffinBurrows;
import skytils.skytilsmod.features.impl.events.MayorJerry;
import skytils.skytilsmod.features.impl.handlers.ArmorColor;
import skytils.skytilsmod.features.impl.handlers.BlockAbility;
import skytils.skytilsmod.features.impl.handlers.CommandAliases;
import skytils.skytilsmod.features.impl.handlers.GlintCustomizer;
import skytils.skytilsmod.features.impl.mining.MiningFeatures;
import skytils.skytilsmod.features.impl.misc.*;
import skytils.skytilsmod.features.impl.spidersden.RelicWaypoints;
import skytils.skytilsmod.features.impl.spidersden.SpidersDenFeatures;
import skytils.skytilsmod.listeners.ChatListener;
import skytils.skytilsmod.mixins.AccessorCommandHandler;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Objects;

@Mod(modid = Skytils.MODID, name = Skytils.MOD_NAME, version = Skytils.VERSION, acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class Skytils {
    public static final String MODID = "skytils";
    public static final String MOD_NAME = "Skytils";
    public static final String VERSION = "0.1.2-pre5";
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static Config config = new Config();
    public static File modDir;
    public static GuiManager GUIMANAGER;

    public static int ticks = 0;

    public static ArrayDeque<String> sendMessageQueue = new ArrayDeque<>();
    public static boolean usingDungeonRooms = false;
    public static boolean usingLabymod = false;
    public static boolean usingNEU = false;
    public static File jarFile = null;
    private static long lastChatMessage = 0;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        modDir = new File(event.getModConfigurationDirectory(), "skytils");
        if (!modDir.exists()) modDir.mkdirs();
        GUIMANAGER = new GuiManager();
        jarFile = event.getSourceFile();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModCoreInstaller.initializeModCore(mc.mcDataDir);

        config.preload();

        ClientCommandHandler.instance.registerCommand(new SkytilsCommand());

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ChatListener());
        MinecraftForge.EVENT_BUS.register(new DataFetcher());
        MinecraftForge.EVENT_BUS.register(GUIMANAGER);
        MinecraftForge.EVENT_BUS.register(SBInfo.getInstance());
        MinecraftForge.EVENT_BUS.register(new UpdateChecker());

        MinecraftForge.EVENT_BUS.register(new SpamHider());

        MinecraftForge.EVENT_BUS.register(new ArmorColor());
        MinecraftForge.EVENT_BUS.register(new BlazeSolver());
        MinecraftForge.EVENT_BUS.register(new BlockAbility());
        MinecraftForge.EVENT_BUS.register(new BossHPDisplays());
        MinecraftForge.EVENT_BUS.register(new BoulderSolver());
        MinecraftForge.EVENT_BUS.register(new ClickInOrderSolver());
        MinecraftForge.EVENT_BUS.register(new CommandAliases());
        MinecraftForge.EVENT_BUS.register(new DamageSplash());
        MinecraftForge.EVENT_BUS.register(new DungeonsFeatures());
        MinecraftForge.EVENT_BUS.register(new FarmingFeatures());
        MinecraftForge.EVENT_BUS.register(new GlintCustomizer());
        MinecraftForge.EVENT_BUS.register(new GriffinBurrows());
        MinecraftForge.EVENT_BUS.register(new IceFillSolver());
        MinecraftForge.EVENT_BUS.register(new IcePathSolver());
        MinecraftForge.EVENT_BUS.register(new ItemFeatures());
        MinecraftForge.EVENT_BUS.register(new LockOrb());
        MinecraftForge.EVENT_BUS.register(new MayorJerry());
        MinecraftForge.EVENT_BUS.register(new MiningFeatures());
        MinecraftForge.EVENT_BUS.register(new MinionFeatures());
        MinecraftForge.EVENT_BUS.register(new MiscFeatures());
        MinecraftForge.EVENT_BUS.register(new PetFeatures());
        MinecraftForge.EVENT_BUS.register(new RelicWaypoints());
        MinecraftForge.EVENT_BUS.register(new ScoreCalculation());
        MinecraftForge.EVENT_BUS.register(new SelectAllColorSolver());
        MinecraftForge.EVENT_BUS.register(new SimonSaysSolver());
        MinecraftForge.EVENT_BUS.register(new SlayerFeatures());
        MinecraftForge.EVENT_BUS.register(new SpidersDenFeatures());
        MinecraftForge.EVENT_BUS.register(new StartsWithSequenceSolver());
        MinecraftForge.EVENT_BUS.register(new TeleportMazeSolver());
        MinecraftForge.EVENT_BUS.register(new TerminalFeatures());
        MinecraftForge.EVENT_BUS.register(new ThreeWeirdosSolver());
        MinecraftForge.EVENT_BUS.register(new TriviaSolver());
        MinecraftForge.EVENT_BUS.register(new WaterBoardSolver());

        ScreenRenderer.refresh();

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        usingDungeonRooms = Loader.isModLoaded("dungeonrooms");
        usingLabymod = Loader.isModLoaded("labymod");
        usingNEU = Loader.isModLoaded("notenoughupdates");

        ClientCommandHandler cch = ClientCommandHandler.instance;

        if (!cch.getCommands().containsKey("armorcolor")) {
            ClientCommandHandler.instance.registerCommand(new ArmorColorCommand());
        }

        if (!cch.getCommands().containsKey("blockability")) {
            ClientCommandHandler.instance.registerCommand(new BlockAbilityCommand());
        }

        if (!cch.getCommands().containsKey("glintcustomize")) {
            ClientCommandHandler.instance.registerCommand(new GlintCustomizeCommand());
        }

        if (!cch.getCommands().containsKey("reparty")) {
            cch.registerCommand(new RepartyCommand());
        }
        if (!cch.getCommands().containsKey("rp")) {
            ((AccessorCommandHandler) cch).getCommandSet().add(new RepartyCommand());
            ((AccessorCommandHandler) cch).getCommandMap().put("rp", new RepartyCommand());
        }
        if (Skytils.config.overrideReparty) {
            if (!cch.getCommands().containsKey("rp")) {
                ((AccessorCommandHandler) cch).getCommandSet().add(new RepartyCommand());
                ((AccessorCommandHandler) cch).getCommandMap().put("rp", new RepartyCommand());
            }
            for (Map.Entry<String, ICommand> entry : cch.getCommands().entrySet()) {
                if (Objects.equals(entry.getKey(), "reparty") || Objects.equals(entry.getKey(), "rp")) {
                    entry.setValue(new RepartyCommand());
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        ScreenRenderer.refresh();

        if (mc.thePlayer != null && sendMessageQueue.size() > 0 && System.currentTimeMillis() - lastChatMessage > 200) {
            mc.thePlayer.sendChatMessage(sendMessageQueue.pollFirst());
        }

        if (ticks % 20 == 0) {
            if (mc.thePlayer != null) {
                Utils.checkForSkyblock();
                Utils.checkForDungeons();
            }
            ticks = 0;
        }

        ticks++;
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (event.packet.getClass() == C01PacketChatMessage.class) {
            lastChatMessage = System.currentTimeMillis();
        }
    }

}
