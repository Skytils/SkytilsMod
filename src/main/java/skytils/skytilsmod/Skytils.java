package skytils.skytilsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.commands.SkytilsCommand;
import skytils.skytilsmod.core.Config;
import skytils.skytilsmod.events.SendPacketEvent;
import skytils.skytilsmod.features.impl.dungeons.DungeonsFeatures;
import skytils.skytilsmod.utils.Utils;

import java.util.ArrayList;

@Mod(modid = Skytils.MODID, name = Skytils.MOD_NAME, version = Skytils.VERSION, acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class Skytils {
    public static final String MODID = "skytils";
    public static final String MOD_NAME = "Skytils";
    public static final String VERSION = "1.0";
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static Config config = new Config();

    public static int ticks = 0;

    public static ArrayList<String> chatMessageQueue = new ArrayList<>();
    private static long lastChatMessage = 0;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModCoreInstaller.initializeModCore(mc.mcDataDir);

        config.preload();

        ClientCommandHandler.instance.registerCommand(new SkytilsCommand());

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DungeonsFeatures());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (chatMessageQueue.size() > 0 && System.currentTimeMillis() - lastChatMessage > 200) {
            mc.thePlayer.sendChatMessage(chatMessageQueue.remove(0));
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
