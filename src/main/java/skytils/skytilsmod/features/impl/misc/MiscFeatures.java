package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.BossBarEvent;
import skytils.skytilsmod.events.ReceivePacketEvent;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

public class MiscFeatures {

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

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.packet instanceof S45PacketTitle) {
            S45PacketTitle packet = (S45PacketTitle) event.packet;
            if (packet.getMessage() != null) {
                String unformatted = StringUtils.stripControlCodes(packet.getMessage().getUnformattedText());
                if (Skytils.config.hideFarmingRNGTitles && Utils.inSkyblock && unformatted.contains("DROP!")) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouseInputPre(GuiScreenEvent.MouseInputEvent.Pre event) {
        if (!Utils.inSkyblock) return;
        if (Mouse.getEventButton() != 0 && Mouse.getEventButton() != 1 && Mouse.getEventButton() != 2) return;
        if (!Mouse.getEventButtonState()) return;

        if (event.gui instanceof GuiChest) {
            Container containerChest = ((GuiChest) event.gui).inventorySlots;
            if (containerChest instanceof ContainerChest) {
                GuiChest chest = (GuiChest) event.gui;

                IInventory inventory = ((ContainerChest) containerChest).getLowerChestInventory();
                Slot mouseSlot = Utils.getSlotUnderMouse(chest);
                if (mouseSlot == null) return;
                ItemStack item = mouseSlot.getStack();
                String inventoryName = inventory.getDisplayName().getUnformattedText();
                if (item == null) return;
                NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(item);

                if (inventoryName.equals("Ophelia")) {
                    if (Skytils.config.dungeonPotLock > 0) {
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
    }
}
