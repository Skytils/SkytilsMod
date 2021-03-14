package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

import java.util.HashSet;
import java.util.List;

public class LockOrb {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (!Utils.inSkyblock || !Skytils.config.powerOrbLock) return;
        if (event.entity != mc.thePlayer || event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
        ItemStack item = mc.thePlayer.getHeldItem();
        String itemId = ItemUtil.getSkyBlockItemID(item);
        if (itemId == null || !itemId.endsWith("_POWER_ORB")) return;
        PowerOrbs heldOrb = PowerOrbs.getPowerOrbMatchingItemId(itemId);
        if (heldOrb == null) return;
        List<EntityArmorStand> orbs = mc.theWorld.getEntities(EntityArmorStand.class, (entity) -> {
            PowerOrbs orb = PowerOrbs.getPowerOrbMatchingName(entity.getDisplayName().getFormattedText());
            return orb != null;
        });
        for (EntityArmorStand orbEntity : orbs) {
            if (orbEntity == null) return;
            PowerOrbs orb = PowerOrbs.getPowerOrbMatchingName(orbEntity.getDisplayName().getFormattedText());
            if (orb != null && orb.ordinal() >= heldOrb.ordinal()) {
                if (orbEntity.getDistanceSqToEntity(mc.thePlayer) <= Math.pow(orb.radius, 2)) {
                    mc.thePlayer.playSound("random.orb", 0.8f, 1f);
                    event.setCanceled(true);
                    break;
                }
            }
        }
    }

    private enum PowerOrbs {
        RADIANT("§aRadiant", 18, "RADIANT_POWER_ORB"),
        MANAFLUX("§9Mana Flux", 18, "MANA_FLUX_POWER_ORB"),
        OVERFLUX("§5Overflux", 18, "OVERFLUX_POWER_ORB"),
        PLASMAFLUX("§d§lPlasmaflux", 20, "PLASMAFLUX_POWER_ORB");

        public String name;
        public double radius;
        public String itemId;

        PowerOrbs(String name, double radius, String itemId) {
            this.name = name;
            this.radius = radius;
            this.itemId = itemId;
        }

        public static PowerOrbs getPowerOrbMatchingName(String name) {
            for (PowerOrbs orb : PowerOrbs.values()) {
                if (name.startsWith(orb.name)) {
                    return orb;
                }
            }
            return null;
        }

        public static PowerOrbs getPowerOrbMatchingItemId(String itemid) {
            for (PowerOrbs orb : PowerOrbs.values()) {
                if (orb.itemId.equals(itemid)) return orb;
            }
            return null;
        }

    }
}
