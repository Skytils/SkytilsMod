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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.commands.RepartyCommand;
import skytils.skytilsmod.commands.SkytilsCommand;
import skytils.skytilsmod.core.Config;
import skytils.skytilsmod.core.UpdateChecker;
import skytils.skytilsmod.events.SendPacketEvent;
import skytils.skytilsmod.features.impl.dungeons.DungeonsFeatures;
import skytils.skytilsmod.features.impl.dungeons.solvers.BlazeSolver;
import skytils.skytilsmod.features.impl.dungeons.solvers.BoulderSolver;
import skytils.skytilsmod.features.impl.dungeons.solvers.SimonSaysSolver;
import skytils.skytilsmod.features.impl.dungeons.solvers.TriviaSolver;
import skytils.skytilsmod.features.impl.events.GriffinBurrows;
import skytils.skytilsmod.features.impl.mining.MiningFeatures;
import skytils.skytilsmod.features.impl.misc.ItemFeatures;
import skytils.skytilsmod.listeners.ChatListener;
import skytils.skytilsmod.mixins.AccessorCommandHandler;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.Utils;

import java.util.ArrayList;
import java.util.Map;

@Mod(modid = Skytils.MODID, name = Skytils.MOD_NAME, version = Skytils.VERSION, acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class Skytils {
    public static final String MODID = "skytils";
    public static final String MOD_NAME = "Skytils";
    public static final String VERSION = "0.0.3";
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static Config config = new Config();

    public static int ticks = 0;

    public static ArrayList<String> sendMessageQueue = new ArrayList<>();
    private static long lastChatMessage = 0;
    public static boolean usingNEU = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModCoreInstaller.initializeModCore(mc.mcDataDir);

        config.preload();

        ClientCommandHandler.instance.registerCommand(new SkytilsCommand());

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ChatListener());
        MinecraftForge.EVENT_BUS.register(SBInfo.getInstance());
        MinecraftForge.EVENT_BUS.register(new UpdateChecker());

        MinecraftForge.EVENT_BUS.register(new BlazeSolver());
        MinecraftForge.EVENT_BUS.register(new BoulderSolver());
        MinecraftForge.EVENT_BUS.register(new DungeonsFeatures());
        MinecraftForge.EVENT_BUS.register(new GriffinBurrows());
        MinecraftForge.EVENT_BUS.register(new ItemFeatures());
        MinecraftForge.EVENT_BUS.register(new MiningFeatures());
        MinecraftForge.EVENT_BUS.register(new SimonSaysSolver());
        MinecraftForge.EVENT_BUS.register(new TriviaSolver());

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if(!ClientCommandHandler.instance.getCommands().containsKey("reparty")) {
            ClientCommandHandler.instance.registerCommand(new RepartyCommand());
        }
        if(!ClientCommandHandler.instance.getCommands().containsKey("rp")) {
            ((AccessorCommandHandler)ClientCommandHandler.instance).getCommandSet().add(new RepartyCommand());
            ((AccessorCommandHandler)ClientCommandHandler.instance).getCommandMap().put("rp", new RepartyCommand());
        }
        if (Skytils.config.overrideReparty) {
            if(!ClientCommandHandler.instance.getCommands().containsKey("rp")) {
                ((AccessorCommandHandler)ClientCommandHandler.instance).getCommandSet().add(new RepartyCommand());
                ((AccessorCommandHandler)ClientCommandHandler.instance).getCommandMap().put("rp", new RepartyCommand());
            }
            for(Map.Entry<String, ICommand> entry : ClientCommandHandler.instance.getCommands().entrySet()) {
                if ("reparty".equals(entry.getKey()) || "rp".equals(entry.getKey())) {
                    entry.setValue(new RepartyCommand());
                }
            }
        }
        if (Loader.isModLoaded("notenoughupdates")) usingNEU = true;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (mc.thePlayer != null && sendMessageQueue.size() > 0 && System.currentTimeMillis() - lastChatMessage > 200) {
            mc.thePlayer.sendChatMessage(sendMessageQueue.remove(0));
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
