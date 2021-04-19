/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.features.impl.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.EntityManager;
import skytils.skytilsmod.features.impl.dungeons.DungeonsFeatures;
import skytils.skytilsmod.features.impl.misc.damagesplash.DamageSplashEntity;
import skytils.skytilsmod.features.impl.misc.damagesplash.Location;
import skytils.skytilsmod.utils.StringUtils;
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

    private static final Pattern damagePattern = Pattern.compile("✧*(\\d+✧?❤?♞?)");

    @SubscribeEvent
    public void renderFakeEntity(RenderWorldLastEvent e) {
        EntityManager.tickEntities(e.partialTicks, e.context);
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre<EntityLivingBase> e) {
        if(!Utils.inSkyblock || !Skytils.config.customDamageSplash) return;
        Entity entity = e.entity;


        if (!(e.entity instanceof EntityArmorStand)) return;
        if (!entity.hasCustomName()) return;
        if (e.entity.isDead) return;

        String strippedName = StringUtils.stripControlCodes(entity.getCustomNameTag());
        Matcher damageMatcher = damagePattern.matcher(strippedName);

        if (damageMatcher.matches()) {

            e.setCanceled(true);
            e.entity.worldObj.removeEntity(e.entity);

            if (Skytils.config.hideDamageInBoss && DungeonsFeatures.hasBossSpawned) return;

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
