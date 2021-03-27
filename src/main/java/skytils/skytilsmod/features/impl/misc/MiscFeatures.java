package skytils.skytilsmod.features.impl.misc;

import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.events.BossBarEvent;
import skytils.skytilsmod.events.CheckRenderEntityEvent;
import skytils.skytilsmod.events.GuiContainerEvent;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.*;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MiscFeatures {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static long golemSpawnTime = 0;

    @SubscribeEvent
    public void onBossBarSet(BossBarEvent.Set event) {
        IBossDisplayData displayData = event.displayData;

        if(Utils.inSkyblock) {
            if(Skytils.config.bossBarFix && StringUtils.stripControlCodes(displayData.getDisplayName().getUnformattedText()).equals("Wither")) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText()).trim();

        if (unformatted.equals("The ground begins to shake as an Endstone Protector rises from below!")) {
            golemSpawnTime = System.currentTimeMillis() + 20_000;
        }
    }

    @SubscribeEvent
    public void onCheckRender(CheckRenderEntityEvent event) {
        if (!Utils.inSkyblock) return;

        if (event.entity instanceof EntityCreeper) {
            EntityCreeper entity = (EntityCreeper) event.entity;
            if (!Utils.inDungeons && Skytils.config.hideCreeperVeilNearNPCs && entity.getMaxHealth() == 20 && entity.getHealth() == 20 && entity.getPowered()) {
                if (mc.theWorld.playerEntities.stream().anyMatch(p -> p instanceof EntityOtherPlayerMP && p.getUniqueID().version() == 2 && p.getHealth() == 20 && !p.isPlayerSleeping() && p.getDistanceSqToEntity(event.entity) <= 49)) {
                    event.setCanceled(true);
                }
            }
        }

        if (event.entity instanceof EntityFallingBlock) {
            EntityFallingBlock entity = (EntityFallingBlock) event.entity;
            if (Skytils.config.hideMidasStaffGoldBlocks && entity.getBlock().getBlock() == Blocks.gold_block) {
                event.setCanceled(true);
            }
        }

        if (event.entity instanceof EntityItem) {
            EntityItem entity = (EntityItem) event.entity;
            if (Skytils.config.hideJerryRune) {
                ItemStack item = entity.getEntityItem();
                if(item.getItem() == Items.spawn_egg && Objects.equals(ItemMonsterPlacer.getEntityName(item), "Villager") && item.getDisplayName().equals("Spawn Villager") && entity.lifespan == 6000) {
                    event.setCanceled(true);
                }
            }
        }

        if (event.entity instanceof EntityLightningBolt) {
            if (Skytils.config.hideLightning) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (!Utils.inSkyblock) return;
        if (event.type == RenderGameOverlayEvent.ElementType.AIR && Skytils.config.hideAirDisplay && !Utils.inDungeons) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
        if (Utils.inSkyblock && Skytils.config.noFire && event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!Utils.inSkyblock) return;
        if (event.packet instanceof S29PacketSoundEffect) {
            S29PacketSoundEffect packet = (S29PacketSoundEffect) event.packet;
            if (Skytils.config.disableCooldownSounds && packet.getSoundName().equals("mob.endermen.portal") && packet.getPitch() == 0 && packet.getVolume() == 8) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onSlotClick(GuiContainerEvent.SlotClickEvent event) {
        if (!Utils.inSkyblock) return;

        if (event.container instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) event.container;

            IInventory inventory = chest.getLowerChestInventory();
            Slot slot = event.slot;
            if (slot == null) return;
            ItemStack item = slot.getStack();
            String inventoryName = inventory.getDisplayName().getUnformattedText();
            if (item == null) return;
            NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(item);

            if (inventoryName.equals("Ophelia")) {
                if (Skytils.config.dungeonPotLock > 0) {
                    if (slot.inventory == mc.thePlayer.inventory || slot.slotNumber == 49) return;
                    if (item.getItem() != Items.potionitem || extraAttributes == null || !extraAttributes.hasKey("potion_level")) {
                        event.setCanceled(true);
                        return;
                    }
                    if (extraAttributes.getInteger("potion_level") != Skytils.config.dungeonPotLock) {
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }

    static {
        new GolemSpawnTimerElement();
        new LegionPlayerDisplay();
        new PlacedSummoningEyeDisplay();
    }

    public static class GolemSpawnTimerElement extends GuiElement {

        public GolemSpawnTimerElement() {
            super("Endstone Protector Spawn Timer", new FloatPair(150, 20));
            Skytils.GUIMANAGER.registerElement(this);
        }

        @Override
        public void render() {
            EntityPlayerSP player = mc.thePlayer;
            if (this.getToggled() && Utils.inSkyblock && player != null && ((golemSpawnTime - System.currentTimeMillis()) > 0)) {
                ScaledResolution sr = new ScaledResolution(mc);

                boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

                GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                String text = "§cGolem spawn in: §a" + NumberUtil.round((golemSpawnTime - System.currentTimeMillis()) / 1000d, 1) + "s";
                SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                ScreenRenderer.fontRenderer.drawString(text, leftAlign ? this.getActualX() : this.getActualX() + getWidth(), this.getActualY(), CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
                GlStateManager.scale(1/this.getScale(), 1/this.getScale(), 1.0F);
            }
        }

        @Override
        public void demoRender() {
            ScreenRenderer.fontRenderer.drawString("§cGolem spawn in: §a20.0s", this.getActualX(), this.getActualY(), CommonColors.WHITE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NORMAL);
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("§cGolem spawn in: §a20.0s");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.golemSpawnTimer;
        }
    }

    public static class LegionPlayerDisplay extends GuiElement {

        public LegionPlayerDisplay() {
            super("Legion Player Display", new FloatPair(50, 50));
            Skytils.GUIMANAGER.registerElement(this);
        }

        @Override
        public void render() {
            EntityPlayerSP player = mc.thePlayer;
            if (this.getToggled() && Utils.inSkyblock && player != null && mc.theWorld != null) {
                float x = getActualX();
                float y = getActualY();

                boolean hasLegion = false;
                for (ItemStack armor : player.inventory.armorInventory) {
                    NBTTagCompound extraAttr = ItemUtil.getExtraAttributes(armor);
                    if (extraAttr != null && extraAttr.hasKey("enchantments") && extraAttr.getCompoundTag("enchantments").hasKey("ultimate_legion")) {
                        hasLegion = true;
                        break;
                    }
                }

                if (!hasLegion) return;

                GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                RenderUtil.renderItem(new ItemStack(Items.enchanted_book), (int)x, (int)y);
                List<EntityPlayer> players = mc.theWorld.getPlayers(EntityOtherPlayerMP.class, p -> p.getDistanceToEntity(player) <= 30 && p.getUniqueID().version() != 2 && p != player && Utils.isInTablist(p));
                ScreenRenderer.fontRenderer.drawString(String.valueOf(Skytils.config.legionCap && players.size() > 20 ? 20 : players.size()), x + 20, y + 5, CommonColors.ORANGE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NORMAL);
                GlStateManager.scale(1/this.getScale(), 1/this.getScale(), 1.0F);
            }
        }

        @Override
        public void demoRender() {
            float x = getActualX();
            float y = getActualY();
            RenderUtil.renderItem(new ItemStack(Items.enchanted_book), (int)x, (int)y);
            ScreenRenderer.fontRenderer.drawString("30", x + 20, y + 5, CommonColors.ORANGE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NORMAL);
        }

        @Override
        public int getHeight() {
            return 16;
        }

        @Override
        public int getWidth() {
            return 20 + ScreenRenderer.fontRenderer.getStringWidth("30");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.legionPlayerDisplay;
        }
    }

    public static class PlacedSummoningEyeDisplay extends GuiElement {

        private static final BlockPos[] SUMMONING_EYE_FRAMES = { new BlockPos(-669, 9, -275), new BlockPos(-669, 9, -277), new BlockPos(-670, 9, -278), new BlockPos(-672, 9, -278), new BlockPos(-673, 9, -277), new BlockPos(-673, 9, -275), new BlockPos(-672, 9, -274), new BlockPos(-670, 9, -274) };
        private static final ResourceLocation ICON = new ResourceLocation("skytils", "icons/SUMMONING_EYE.png");

        public PlacedSummoningEyeDisplay() {
            super("Placed Summoning Eye Display", new FloatPair(50, 60));
            Skytils.GUIMANAGER.registerElement(this);
        }

        @Override
        public void render() {
            EntityPlayerSP player = mc.thePlayer;
            if (this.getToggled() && Utils.inSkyblock && player != null && mc.theWorld != null) {
                if (SBInfo.getInstance().getLocation() == null || !SBInfo.getInstance().getLocation().equalsIgnoreCase("combat_3")) return;
                float x = getActualX();
                float y = getActualY();

                boolean invalid = false;
                int placedEyes = 0;

                for (BlockPos pos : SUMMONING_EYE_FRAMES) {
                    IBlockState block = mc.theWorld.getBlockState(pos);
                    if (block.getBlock() != Blocks.end_portal_frame) {
                        invalid = true;
                        break;
                    } else if (block.getValue(BlockEndPortalFrame.EYE)) {
                        placedEyes++;
                    }
                }

                if (invalid) return;

                GlStateManager.scale(this.getScale(), this.getScale(), 1.0);
                RenderUtil.renderTexture(ICON, (int)x, (int)y);
                ScreenRenderer.fontRenderer.drawString(placedEyes + "/8", x + 20, y + 5, CommonColors.ORANGE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NORMAL);
                GlStateManager.scale(1/this.getScale(), 1/this.getScale(), 1.0F);
            }
        }

        @Override
        public void demoRender() {
            float x = getActualX();
            float y = getActualY();
            RenderUtil.renderTexture(ICON, (int)x, (int)y);
            ScreenRenderer.fontRenderer.drawString("6/8", x + 20, y + 5, CommonColors.ORANGE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NORMAL);
        }

        @Override
        public int getHeight() {
            return 16;
        }

        @Override
        public int getWidth() {
            return 20 + ScreenRenderer.fontRenderer.getStringWidth("6/8");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.summoningEyeDisplay;
        }
    }

}
