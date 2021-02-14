package skytils.skytilsmod.features.impl.events;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
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
import skytils.skytilsmod.utils.APIUtil;
import skytils.skytilsmod.utils.RenderUtil;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class GriffinBurrows {

    public static ArrayList<Burrow> burrows = new ArrayList<>();
    public static ArrayList<BlockPos> dugBurrows = new ArrayList<>();
    public static BlockPos lastDugBurrow = null;
    public static ArrayList<PartialBurrow> partialBurrows = new ArrayList<>();

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
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        burrows.clear();
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
            receivedBurrows.removeIf(burrow -> dugBurrows.contains(burrow.getBlockPos()));

            burrows.clear();
            burrows.addAll(receivedBurrows);
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Skytils loaded " + EnumChatFormatting.DARK_GREEN + receivedBurrows.size() + EnumChatFormatting.GREEN + " burrows!"));

        }).start();
    }

    public static class GriffinGuiElement extends GuiElement {

        public GriffinGuiElement() {
            super("Griffin Timer", 1.0F, new FloatPair(100, 10));
            Skytils.GUIMANAGER.registerElement(this);
        }

        public void render() {
            if (SBInfo.getInstance().getLocation() == null || !SBInfo.getInstance().getLocation().equalsIgnoreCase("hub")) return;
            EntityPlayerSP player = mc.thePlayer;
            if (Skytils.config.showGriffinCountdown && Utils.inSkyblock && player != null) {
                for (int i = 0; i < 8; i++) {
                    ItemStack hotbarItem = player.inventory.getStackInSlot(i);
                    if (hotbarItem == null) continue;
                    if (hotbarItem.getDisplayName().contains("Ancestral Spade")) {
                        long diff = Math.round((60_000L - GriffinBurrows.burrowRefreshTimer.getTime()) / 1000L);
                        ScaledResolution sr = new ScaledResolution(mc);
                        float x = this.getPos().getX() * sr.getScaledWidth();
                        float y = this.getPos().getY() * sr.getScaledHeight();

                        GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                        Minecraft.getMinecraft().fontRendererObj.drawString("Time until refresh: " + diff + "s", x, y, 0xFFFFFF, true);
                        GlStateManager.scale(1/this.getScale(), 1/this.getScale(), 1.0F);
                        break;
                    }
                }
            }
        }

        public boolean getToggled() {
            return Skytils.config.showGriffinBurrows;
        }
    }

    public static class PartialBurrow {
        public int x, y, z;
        public boolean hasFootstep, hasOtherParticle;

        public PartialBurrow(int x, int y, int z, boolean hasFootstep, boolean hasOtherParticle) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.hasFootstep = hasFootstep;
            this.hasOtherParticle = hasOtherParticle;
        }

        public PartialBurrow(Vec3i vec3, boolean hasFootstep, boolean hasOtherParticle) {
            this(vec3.getX(), vec3.getY(), vec3.getZ(), hasFootstep, hasOtherParticle);
        }

        public BlockPos getBlockPos() {
            return new BlockPos(x, y, z);
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
