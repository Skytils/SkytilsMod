package skytils.skytilsmod.features.impl.events;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.time.StopWatch;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.events.DamageBlockEvent;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.APIUtil;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class GriffinBurrows {

    public static ArrayList<Burrow> burrows = new ArrayList<>();
    public static ArrayList<BlockPos> dugBurrows = new ArrayList<>();
    public static BlockPos lastDugBurrow = null;
    public static ArrayList<ParticleBurrow> particleBurrows = new ArrayList<>();
    public static BlockPos lastDugParticleBurrow = null;

    public static StopWatch burrowRefreshTimer = new StopWatch();
    public static boolean shouldRefreshBurrows = false;

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new GriffinGuiElement();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        EntityPlayerSP player = mc.thePlayer;
        if (SBInfo.getInstance().getLocation() == null || !SBInfo.getInstance().getLocation().equalsIgnoreCase("hub")) return;
        if (!burrowRefreshTimer.isStarted()) burrowRefreshTimer.start();

        if (burrowRefreshTimer.getTime() >= 60000 || shouldRefreshBurrows) {
            burrowRefreshTimer.reset();
            shouldRefreshBurrows = false;
            if (Skytils.config.showGriffinBurrows && Utils.inSkyblock && player != null) {
                for (int i = 0; i < 8; i++) {
                    ItemStack hotbarItem = player.inventory.getStackInSlot(i);
                    if (hotbarItem == null) continue;
                    if (hotbarItem.getDisplayName().contains("Ancestral Spade")) {
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Looking for burrows..."));
                        refreshBurrows();
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (Skytils.config.showGriffinBurrows && (unformatted.contains("You died!") || unformatted.contains("You dug out a Griffin Burrow") || unformatted.contains("You finished the Griffin burrow chain!"))) {
            if (lastDugBurrow != null) {
                dugBurrows.add(lastDugBurrow);
                burrows.removeIf(burrow -> burrow.getBlockPos().equals(lastDugBurrow));
                lastDugBurrow = null;
            }
            if (lastDugParticleBurrow != null) {
                ParticleBurrow particleBurrow = particleBurrows.stream().filter(pb -> pb.getBlockPos().equals(lastDugParticleBurrow)).findFirst().orElse(null);
                if (particleBurrow != null) {
                    particleBurrow.dug = true;
                    dugBurrows.add(lastDugParticleBurrow);
                    particleBurrows.remove(particleBurrow);
                    lastDugParticleBurrow = null;
                }
            }
        }
    }

    @SubscribeEvent
    public void onDamageBlock(DamageBlockEvent event) {

        if (mc.theWorld == null || mc.thePlayer == null) return;

        IBlockState blockState = mc.theWorld.getBlockState(event.pos);
        ItemStack item = mc.thePlayer.getHeldItem();

        if (Utils.inSkyblock) {
            if (Skytils.config.showGriffinBurrows && item != null) {
                if (item.getDisplayName().contains("Ancestral Spade") && blockState.getBlock() == Blocks.grass) {
                    if (GriffinBurrows.burrows.stream().anyMatch(burrow -> burrow.getBlockPos().equals(event.pos))) {
                        GriffinBurrows.lastDugBurrow = event.pos;
                    }
                    if (GriffinBurrows.particleBurrows.stream().anyMatch(pb -> pb.getBlockPos().equals(event.pos))) {
                        lastDugParticleBurrow = event.pos;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (Skytils.config.showGriffinBurrows && burrows.size() > 0) {
            List<Burrow> burrows = ImmutableList.copyOf(GriffinBurrows.burrows);
            for (Burrow burrow : burrows) {
                burrow.drawWaypoint(event.partialTicks);
            }
            List<ParticleBurrow> particleBurrows = ImmutableList.copyOf(GriffinBurrows.particleBurrows);
            for (ParticleBurrow pb : particleBurrows) {
                if (pb.hasEnchant && pb.hasFootstep && pb.type != -1) {
                    pb.drawWaypoint(event.partialTicks);
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        burrows.clear();
        particleBurrows.clear();
        shouldRefreshBurrows = true;
    }

    public static void refreshBurrows() {
        new Thread(() -> {
            System.out.println("Finding burrows");
            String uuid = mc.thePlayer.getGameProfile().getId().toString().replaceAll("[\\-]", "");
            String apiKey = Skytils.config.apiKey;
            if (apiKey.length() == 0) {
                mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7c\u00a7lYour API key is required in order to use the burrow feature. \u00a7cPlease set it with /api new or /st setkey <key>"));
                Skytils.config.showGriffinBurrows = false;
                return;
            }

            String latestProfile = APIUtil.getLatestProfileID(uuid, apiKey);
            if (latestProfile == null) return;

            JsonObject profileResponse = APIUtil.getJSONResponse("https://api.hypixel.net/skyblock/profile?profile=" + latestProfile + "&key=" + apiKey);
            if (!profileResponse.get("success").getAsBoolean()) {
                String reason = profileResponse.get("cause").getAsString();
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed getting burrows with reason: " + reason));
                return;
            }

            JsonObject playerObject = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject();

            if (!playerObject.has("griffin")) {
                mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed getting burrows with reason: No griffin object."));
                return;
            }

            JsonArray burrowArray = playerObject.get("griffin").getAsJsonObject().get("burrows").getAsJsonArray();

            ArrayList<Burrow> receivedBurrows = new ArrayList<>();
            burrowArray.forEach(jsonElement -> {
                JsonObject burrowObject = jsonElement.getAsJsonObject();
                int x = burrowObject.get("x").getAsInt();
                int y = burrowObject.get("y").getAsInt();
                int z = burrowObject.get("z").getAsInt();
                int type = burrowObject.get("type").getAsInt();
                int tier = burrowObject.get("tier").getAsInt();
                int chain = burrowObject.get("chain").getAsInt();
                Burrow burrow = new Burrow(x, y, z, type, tier, chain);
                receivedBurrows.add(burrow);
            });

            dugBurrows.removeIf(dug -> receivedBurrows.stream().noneMatch(burrow -> burrow.getBlockPos().equals(dug)));
            particleBurrows.removeIf(pb -> receivedBurrows.stream().anyMatch(rb -> rb.getBlockPos().equals(pb.getBlockPos())));
            boolean removedDupes = receivedBurrows.removeIf(burrow -> dugBurrows.contains(burrow.getBlockPos()) || particleBurrows.stream().anyMatch(pb -> pb.dug && pb.getBlockPos().equals(burrow.getBlockPos())));

            burrows.clear();
            burrows.addAll(receivedBurrows);
            if (receivedBurrows.size() == 0) {
                if (!removedDupes) mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7cSkytils failed to load griffin burrows. Try manually digging a burrow and switching hubs."));
                else mc.thePlayer.addChatMessage(new ChatComponentText("\u00a7cSkytils was unable to load fresh burrows. Please wait for the API refresh or switch hubs."));
            } else mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Skytils loaded " + EnumChatFormatting.DARK_GREEN + receivedBurrows.size() + EnumChatFormatting.GREEN + " burrows!"));

        }).start();
    }

    public static class GriffinGuiElement extends GuiElement {

        public GriffinGuiElement() {
            super("Griffin Timer", new FloatPair(100, 10));
            Skytils.GUIMANAGER.registerElement(this);
        }

        public void render() {
            if (SBInfo.getInstance().getLocation() == null || !SBInfo.getInstance().getLocation().equalsIgnoreCase("hub")) return;
            EntityPlayerSP player = mc.thePlayer;
            if (this.getToggled() && Utils.inSkyblock && player != null) {
                for (int i = 0; i < 8; i++) {
                    ItemStack hotbarItem = player.inventory.getStackInSlot(i);
                    if (hotbarItem == null) continue;
                    if (hotbarItem.getDisplayName().contains("Ancestral Spade")) {
                        long diff = Math.round((60_000L - GriffinBurrows.burrowRefreshTimer.getTime()) / 1000L);
                        float x = this.getActualX();
                        float y = this.getActualY();

                        GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                        Minecraft.getMinecraft().fontRendererObj.drawString("Time until refresh: " + diff + "s", x, y, 0xFFFFFF, true);
                        GlStateManager.scale(1/this.getScale(), 1/this.getScale(), 1.0F);
                        break;
                    }
                }
            }
        }

        public void demoRender() {
            ScreenRenderer.fontRenderer.drawString("Time until refresh: 10s", this.getActualX(), this.getActualY(), CommonColors.WHITE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NORMAL);
        }

        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT;
        }

        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("Time until refresh: 10s");
        }

        public boolean getToggled() {
            return Skytils.config.showGriffinBurrows && Skytils.config.showGriffinCountdown;
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (Skytils.config.showGriffinBurrows && Skytils.config.particleBurrows && event.packet instanceof S2APacketParticles) {
            if (SBInfo.getInstance().getLocation() == null || !SBInfo.getInstance().getLocation().equalsIgnoreCase("hub")) return;
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

            BlockPos pos = new BlockPos(x, y, z).down();

            boolean footstepFilter = type == EnumParticleTypes.FOOTSTEP && count == 1 && speed == 0.0f && xOffset == 0.05f && yOffset == 0.0f && zOffset == 0.05f;
            boolean enchantFilter  = type == EnumParticleTypes.ENCHANTMENT_TABLE && count == 5 && speed == 0.05f && xOffset == 0.5f && yOffset == 0.4f && zOffset == 0.5f;

            boolean startFilter = type == EnumParticleTypes.CRIT_MAGIC && count == 4 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f;
            boolean mobFilter = type == EnumParticleTypes.CRIT && count == 3 && speed == 0.01f && xOffset == 0.5f && yOffset == 0.1f && zOffset == 0.5f;
            boolean treasureFilter = type == EnumParticleTypes.DRIP_LAVA && count == 2 && speed == 0.01f && xOffset == 0.35f && yOffset == 0.1f && zOffset == 0.35f;

            if (longDistance && (footstepFilter || enchantFilter || startFilter || mobFilter || treasureFilter)) {
                if (burrows.stream().noneMatch(b -> b.getBlockPos().equals(pos)) && dugBurrows.stream().noneMatch(b -> b.equals(pos))) {
                    ParticleBurrow burrow = particleBurrows.stream().filter(b -> b.getBlockPos().equals(pos)).findFirst().orElse(new ParticleBurrow(pos, false, false, -1));
                    if (!particleBurrows.contains(burrow)) particleBurrows.add(burrow);
                    if (!burrow.hasFootstep && footstepFilter) {
                        burrow.hasFootstep = true;
                    } else if (!burrow.hasEnchant && enchantFilter) {
                        burrow.hasEnchant = true;
                    } else if (burrow.type == -1 && type != EnumParticleTypes.FOOTSTEP && type != EnumParticleTypes.ENCHANTMENT_TABLE) {
                        if (startFilter) burrow.type = 0;
                        else if (mobFilter) burrow.type = 1;
                        else if (treasureFilter) burrow.type = 2;
                    }
                }
                //System.out.println(String.format("%s %s %s particles with %s speed at %s, %s, %s, offset by %s %s %s", count, longDistance ? "long-distance" : "", type.getParticleName(), speed, x, y, z, xOffset, yOffset, zOffset));
            }

        }
    }

    public static class ParticleBurrow {
        public int x, y, z;
        public boolean hasFootstep, hasEnchant;
        public int type = -1;
        public long timestamp;
        public boolean dug = false;

        public ParticleBurrow(int x, int y, int z, boolean hasFootstep, boolean hasEnchant, int type) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.hasFootstep = hasFootstep;
            this.hasEnchant = hasEnchant;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public ParticleBurrow(Vec3i vec3, boolean hasFootstep, boolean hasEnchant, int type) {
            this(vec3.getX(), vec3.getY(), vec3.getZ(), hasFootstep, hasEnchant, type);
        }

        public BlockPos getBlockPos() {
            return new BlockPos(x, y, z);
        }

        public String getWaypointText() {

            String type = "Burrow";

            switch (this.type) {
                case 0:
                    type = EnumChatFormatting.GREEN + "Start";
                    break;
                case 1:
                    type = EnumChatFormatting.RED + "Mob";
                    break;
                case 2:
                    type = EnumChatFormatting.GOLD + "Treasure";
                    break;
            }

            return String.format("%s \u00a7a(Particle)", type);
        }

        public void drawWaypoint(float partialTicks) {

            Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
            double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
            double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
            double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

            BlockPos pos = this.getBlockPos();
            double x = pos.getX() - viewerX;
            double y = pos.getY() - viewerY;
            double z = pos.getZ() - viewerZ;
            double distSq = x*x + y*y + z*z;

            GlStateManager.disableDepth();
            GlStateManager.disableCull();
            RenderUtil.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), new Color(2, 250, 39), 1f);
            GlStateManager.disableTexture2D();
            if (distSq > 5*5) RenderUtil.renderBeaconBeam(x, y + 1, z, new Color(2, 250, 39).getRGB(), 1.0f, partialTicks);
            RenderUtil.renderWaypointText(getWaypointText(), getBlockPos().up(5), partialTicks);
            GlStateManager.disableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
        }

    }

    public static class Burrow {
        public int x, y, z;
        /**
         * This variable seems to hold whether or not the burrow is the start/empty, a mob, or treasure
         */
        public int type;
        /**
         * This variable holds the Griffin used, -1 means no Griffin, 0 means Common, etc.
         */
        public int tier;
        /**
         * This variable appears to hold what order the burrow is in the chain
         */
        public int chain;

        public Burrow(int x, int y, int z, int type, int tier, int chain) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.type = type;
            this.tier = tier;
            this.chain = chain;
        }

        public BlockPos getBlockPos() {
            return new BlockPos(x, y, z);
        }

        public String getWaypointText() {

            String type = "Burrow";

            switch (this.type) {
                case 0:
                    type = chain == 0 ? EnumChatFormatting.GREEN + "Start" : EnumChatFormatting.WHITE + "Empty";
                    break;
                case 1:
                    type = EnumChatFormatting.RED + "Mob";
                    break;
                case 2:
                case 3:
                    type = EnumChatFormatting.GOLD + "Treasure";
                    break;
            }


            FastTravelLocations closest = null;

            if (Skytils.config.showBurrowFastTravel) {
                double distance = mc.thePlayer.getPosition().distanceSq(getBlockPos());
                for (FastTravelLocations warp : FastTravelLocations.values()) {
                    double warpDistance = getBlockPos().distanceSq(warp.pos);
                    if (warpDistance < distance) {
                        distance = warpDistance;
                        closest = warp;
                    }
                }
            }

            return String.format("%s \u00a7bPosition: %s/4%s", type, this.chain + 1, closest != null ? " " + closest.getNameWithColor() : "");
        }

        public void drawWaypoint(float partialTicks) {

            Entity viewer = Minecraft.getMinecraft().getRenderViewEntity();
            double viewerX = viewer.lastTickPosX + (viewer.posX - viewer.lastTickPosX) * partialTicks;
            double viewerY = viewer.lastTickPosY + (viewer.posY - viewer.lastTickPosY) * partialTicks;
            double viewerZ = viewer.lastTickPosZ + (viewer.posZ - viewer.lastTickPosZ) * partialTicks;

            BlockPos pos = this.getBlockPos();
            double x = pos.getX() - viewerX;
            double y = pos.getY() - viewerY;
            double z = pos.getZ() - viewerZ;
            double distSq = x*x + y*y + z*z;

            GlStateManager.disableDepth();
            GlStateManager.disableCull();
            RenderUtil.drawFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), new Color(173, 216, 230), 1f);
            GlStateManager.disableTexture2D();
            if (distSq > 5*5) RenderUtil.renderBeaconBeam(x, y + 1, z, new Color(173, 216, 230).getRGB(), 1.0f, partialTicks);
            RenderUtil.renderWaypointText(getWaypointText(), getBlockPos().up(5), partialTicks);
            GlStateManager.disableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
        }

    }

    public enum FastTravelLocations {
        CASTLE(-250, 130, 45),
        CRYPTS(-162, 60, -100),
        DA(91, 74, 173),
        HUB(-3, 70, -70);

        int x, y, z;
        BlockPos pos;
        FastTravelLocations(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.pos = new BlockPos(x, y, z);
        }

        public String getNameWithColor() {
            switch (this) {
                case CASTLE:
                    return EnumChatFormatting.GRAY + "CASTLE";
                case CRYPTS:
                    return EnumChatFormatting.DARK_GREEN + "CRYPTS";
                case DA:
                    return EnumChatFormatting.DARK_PURPLE + "DA";
                case HUB:
                    return EnumChatFormatting.WHITE + "HUB";
                default:
                    return "";
            }
        }
    }
}
