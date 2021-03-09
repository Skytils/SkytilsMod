package skytils.skytilsmod.features.impl.spidersden;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;

public class SpidersDenFeatures {

    private static boolean shouldShowArachneSpawn = false;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (unformatted.startsWith("☄") && (unformatted.contains("placed an Arachne Fragment! (") || unformatted.contains("placed an Arachne Crystal! Something is awakening!"))) {
            shouldShowArachneSpawn = true;
        }
        if (unformatted.trim().startsWith("ARACHNE DOWN!")) {
            shouldShowArachneSpawn = false;
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (shouldShowArachneSpawn && Skytils.config.showArachneSpawn) {
            BlockPos spawnPos = new BlockPos(-282, 49, -178);

            GlStateManager.disableDepth();
            GlStateManager.disableCull();
            RenderUtil.renderWaypointText("Arachne Spawn", spawnPos, event.partialTicks);
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        shouldShowArachneSpawn = false;
    }

}
