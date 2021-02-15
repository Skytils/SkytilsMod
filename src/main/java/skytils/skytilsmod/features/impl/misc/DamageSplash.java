package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.EntityManager;
import skytils.skytilsmod.features.impl.misc.damagesplash.DamageSplashEntity;
import skytils.skytilsmod.features.impl.misc.damagesplash.Location;
import skytils.skytilsmod.utils.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class DamageSplash {

    @SubscribeEvent
    public void renderFakeEntity(RenderWorldLastEvent e) {
        float partialTicks = ObfuscationReflectionHelper.getPrivateValue(RenderWorldLastEvent.class, e, "partialTicks");
        RenderGlobal context = ObfuscationReflectionHelper.getPrivateValue(RenderWorldLastEvent.class, e, "context");
        EntityManager.tickEntities(partialTicks, context);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderLiving(RenderLivingEvent.Specials.Pre<EntityLivingBase> e) {
        if(!Utils.inSkyblock || !Skytils.config.customDamageSplash) return;
        Entity entity = e.entity;


        if (!(e.entity instanceof EntityArmorStand)) return;
        if (!entity.hasCustomName()) return;
        if (e.entity.isDead) return;

        Pattern damagePattern = Pattern.compile("✧*(\\d+✧?❤?♞?)");
        String strippedName = StringUtils.stripControlCodes(entity.getCustomNameTag());
        Matcher damageMatcher = damagePattern.matcher(strippedName);

        if (damageMatcher.matches()) {

            e.setCanceled(true);
            e.entity.worldObj.removeEntity(e.entity);

            String name = entity.getCustomNameTag();
            String damage = (name.startsWith("§0")) ? damageMatcher.group(1) + "☠" :
                    (name.startsWith("§f") && !name.contains("§e")) ? damageMatcher.group(1) + "❂" :
                            (name.startsWith("§6") && !name.contains("§e")) ? damageMatcher.group(1) + "火" :
                                    damageMatcher.group(1);
            EntityManager.spawnEntity(new DamageSplashEntity(damage,
                    new Location(entity.posX, entity.posY + 1.5, entity.posZ)));
        }
    }
}
