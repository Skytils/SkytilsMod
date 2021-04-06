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

package skytils.skytilsmod.features.impl.misc.damagesplash;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CustomColor;
import skytils.skytilsmod.utils.NumberUtil;
import skytils.skytilsmod.utils.Utils;

import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.client.renderer.GlStateManager.*;

/**
 * Taken from Wynntils under GNU Affero General Public License v3.0
 * Modified
 * https://github.com/Wynntils/Wynntils/blob/development/LICENSE
 * @author Wynntils
 */
public class DamageSplashEntity extends FakeEntity {

    private static final ScreenRenderer renderer = new ScreenRenderer();
    private static final WeakHashMap<String, UUID> added = new WeakHashMap<>();

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("(\\d+)(.*)");

    String displayText;
    private float scale = 1f;
    private CustomColor color;
    private boolean love = false;

    public DamageSplashEntity(String damage, Location currentLocation) {
        super(currentLocation);

        Matcher symbolMatcher = SYMBOL_PATTERN.matcher(damage);
        if (symbolMatcher.matches()) {
            String symbol = symbolMatcher.group(2);
            damage = symbolMatcher.group(1);
            if (symbol.contains("❤")) {
                love = true;
                symbol = symbol.replace("❤", "");
            }

            color = Damage.fromSymbol(symbol).getColor();
        }

        displayText = NumberUtil.format(Long.parseLong(damage));

        if (love) {
            displayText += '❤';
        }

        UUID uuid = new UUID(Utils.getRandom().nextLong(), Utils.getRandom().nextLong());

        if (added.containsValue(uuid)) {
            remove();
            return;
        }

        added.put(displayText, uuid);
    }

    @Override
    public String getName() {
        return "EntityDamageSplash";
    }

    @Override
    public void tick(float partialTicks, Random r, EntityPlayerSP player) {
        int maxLiving = 150;
        if (livingTicks > maxLiving) {
            remove();
            return;
        }

        float initialScale = 2.5f;

        // makes the text goes down and resize
        currentLocation.subtract(0, 2 / (double)maxLiving, 0);
        scale = initialScale - ((livingTicks * initialScale) / maxLiving);
    }

    @Override
    public void render(float partialTicks, RenderGlobal context, RenderManager render) {
        boolean thirdPerson = render.options.thirdPersonView == 2;

        ScreenRenderer.setRendering(true);
        {
            { // setting up
                rotate(-render.playerViewY, 0f, 1f, 0f); // rotates yaw
                rotate((float) (thirdPerson ? -1 : 1) * render.playerViewX, 1.0F, 0.0F, 0.0F); // rotates pitch
                scale(-0.025F, -0.025F, 0.025F); // size the text to the same size as a nametag

                scale(scale, scale, scale);

                color(1.0f, 1.0f, 1.0f, 1.0f);
            }

            renderer.drawString(displayText, 0, 0, color/*CommonColors.RAINBOW*/,
                    SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
        }
        ScreenRenderer.setRendering(false);
    }
}
