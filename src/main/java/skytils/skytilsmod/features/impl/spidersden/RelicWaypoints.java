package skytils.skytilsmod.features.impl.spidersden;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.DataFetcher;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.events.SendPacketEvent;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class RelicWaypoints {
    public static final LinkedHashSet<BlockPos> relicLocations = new LinkedHashSet<>();
    public static final HashSet<BlockPos> foundRelics = new HashSet<>();

    private static final HashSet<BlockPos> rareRelicLocations = new HashSet<>();

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static File saveFile = null;
    
    public RelicWaypoints() {
        saveFile = new File(Skytils.modDir, "found_spiders_den_relics.json");
        reloadSave();
    }

    public static void reloadSave() {
        foundRelics.clear();
        JsonArray dataArray;
        try (FileReader in = new FileReader(saveFile)) {
            dataArray = gson.fromJson(in, JsonArray.class);
            for (String serializedPosition : DataFetcher.getStringArrayFromJsonArray(dataArray)) {
                String[] parts = serializedPosition.split(",");
                foundRelics.add(new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
            }
        } catch (Exception e) {
            dataArray = new JsonArray();
            try (FileWriter writer = new FileWriter(saveFile)) {
                gson.toJson(dataArray, writer);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void writeSave() {
        try (FileWriter writer = new FileWriter(saveFile)) {
            JsonArray arr = new JsonArray();
            for (BlockPos found : foundRelics) {
                arr.add(new JsonPrimitive(found.getX() + "," + found.getY() + "," + found.getZ()));
            }
            gson.toJson(arr, writer);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!Utils.inSkyblock) return;
        if (event.packet instanceof S2APacketParticles) {
            S2APacketParticles packet = (S2APacketParticles) event.packet;

            EnumParticleTypes type = packet.getParticleType();

            boolean longDistance = packet.isLongDistance();
            int count = packet.getParticleCount();
            float speed = packet.getParticleSpeed();
            float xOffset = packet.getXOffset();
            float yOffset = packet.getYOffset();
            float zOffset = packet.getZOffset();

            double x = packet.getXCoordinate();
            double y = packet.getYCoordinate();
            double z = packet.getZCoordinate();

            if (Skytils.config.rareRelicFinder) {
                boolean filter = type == EnumParticleTypes.SPELL_WITCH && count == 2 && longDistance && speed == 0f && xOffset == 0.3f && yOffset == 0.3f && zOffset == 0.3f;
                if (filter && relicLocations.contains(new BlockPos(x, y, z))) {
                    rareRelicLocations.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (!Utils.inSkyblock) return;
        if (SBInfo.getInstance().getLocation() == null || !SBInfo.getInstance().getLocation().equalsIgnoreCase("combat_1")) return;
        if (event.packet instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) event.packet;
            if (relicLocations.contains(packet.getPosition())) {
                foundRelics.add(packet.getPosition());
                rareRelicLocations.remove(packet.getPosition());
                writeSave();
            }
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!Utils.inSkyblock) return;
        if (SBInfo.getInstance().getLocation() == null || !SBInfo.getInstance().getLocation().equalsIgnoreCase("combat_1")) return;
        Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
        double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * event.partialTicks;
        double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * event.partialTicks;
        double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * event.partialTicks;

        if (Skytils.config.relicWaypoints) {
            for (BlockPos relic : ImmutableSet.copyOf(relicLocations)) {
                if (foundRelics.contains(relic)) continue;
                double x = relic.getX() - viewerX;
                double y = relic.getY() - viewerY;
                double z = relic.getZ() - viewerZ;
                double distSq = x*x + y*y + z*z;

                GlStateManager.disableDepth();
                GlStateManager.disableCull();
                RenderUtil.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), new Color(114, 245, 82), 1f);
                GlStateManager.disableTexture2D();
                if (distSq > 5*5) RenderUtil.renderBeaconBeam(x, y + 1, z, new Color(114, 245, 82).getRGB(), 1.0f, event.partialTicks);
                RenderUtil.renderWaypointText("Relic", relic, event.partialTicks);
                GlStateManager.disableLighting();
                GlStateManager.enableTexture2D();
                GlStateManager.enableDepth();
                GlStateManager.enableCull();
            }
        }

        if (Skytils.config.rareRelicFinder) {
            for (BlockPos relic : ImmutableSet.copyOf(rareRelicLocations)) {
                double x = relic.getX() - viewerX;
                double y = relic.getY() - viewerY;
                double z = relic.getZ() - viewerZ;
                double distSq = x*x + y*y + z*z;

                GlStateManager.disableDepth();
                GlStateManager.disableCull();
                RenderUtil.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), new Color(152, 41, 222), 1f);
                GlStateManager.disableTexture2D();
                if (distSq > 5*5) RenderUtil.renderBeaconBeam(x, y + 1, z, new Color(152, 41, 222).getRGB(), 1.0f, event.partialTicks);
                RenderUtil.renderWaypointText("Rare Relic", relic, event.partialTicks);
                GlStateManager.disableLighting();
                GlStateManager.enableTexture2D();
                GlStateManager.enableDepth();
                GlStateManager.enableCull();
            }
        }
    }

}
