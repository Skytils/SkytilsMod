package skytils.skytilsmod.features.impl.events;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.PacketEvent;
import skytils.skytilsmod.utils.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static skytils.skytilsmod.Skytils.mc;

public class TechnoMayor {

    private static final List<Vec3> orbLocations = new ArrayList<>();

    @SubscribeEvent
    public void onRenderSpecialLivingPre(RenderLivingEvent.Specials.Pre<EntityLivingBase> event) {
        if(!Utils.inSkyblock) return;
        Entity entity = event.entity;

        if (!(event.entity instanceof EntityArmorStand) || !entity.hasCustomName() ||event.entity.isDead) return;
        EntityArmorStand e = (EntityArmorStand) entity;
        if (!e.getCustomNameTag().equals("§6§lSHINY ORB")) return;

        BlockPos origin = e.getPosition();
        List<Entity> nearbyEntities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(e, new AxisAlignedBB(origin, new BlockPos(origin.getX() + 1, origin.getY() + 1, origin.getZ() + 1)));
        for(Entity ent : nearbyEntities) {
            if(ent instanceof EntityArmorStand && ent.hasCustomName() && ent.getCustomNameTag().contains(mc.thePlayer.getName())) {
                ent.worldObj.removeEntity(ent);
                e.worldObj.removeEntity(e);
                orbLocations.add(new Vec3(origin.getX() + 0.5, origin.getY() - 2, origin.getZ() + 0.5));
                break;
            }
        }

    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!Utils.inSkyblock) return;
        if (SBInfo.getInstance().getLocation() == null || !SBInfo.getInstance().getLocation().equalsIgnoreCase("hub")) return;
        if (!Skytils.config.shinyOrbWaypoints) return;
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        for (Vec3 orb : ImmutableList.copyOf(orbLocations)) {
            double x = orb.xCoord - viewerX;
            double y = orb.yCoord - viewerY;
            double z = orb.zCoord - viewerZ;
            double distSq = x*x + y*y + z*z;

            GlStateManager.disableCull();
            GlStateManager.disableTexture2D();
            if (distSq > 5*5) RenderUtil.renderBeaconBeam(x, y, z, new Color(114, 245, 82).getRGB(), 0.75f, event.partialTicks);
            GlStateManager.disableDepth();
            RenderUtil.renderWaypointText("Orb", orb.xCoord, orb.yCoord + 1.5f, orb.zCoord, event.partialTicks);
            GlStateManager.disableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onChatPacket(PacketEvent.ReceiveEvent event) {
        if (!(event.packet instanceof S02PacketChat)) return;
        S02PacketChat packet = (S02PacketChat) event.packet;
        if (packet.getType() == 2) return;
        String unformatted = StringUtils.stripControlCodes(packet.getChatComponent().getUnformattedText());
        String formatted = packet.getChatComponent().getFormattedText();

        if(unformatted.equals("Your Shiny Orb and associated pig expired and disappeared.") || unformatted.equals("SHINY! The orb is charged! Click on it for loot!")) {
            orbLocations.removeIf(pos -> mc.thePlayer.getPosition().distanceSq(pos.xCoord, pos.yCoord, pos.zCoord) < 7 * 7);
        }
    }

    @SubscribeEvent
    public void onWorldChange (WorldEvent.Load event) {
        orbLocations.removeIf(e -> true);
    }
}
