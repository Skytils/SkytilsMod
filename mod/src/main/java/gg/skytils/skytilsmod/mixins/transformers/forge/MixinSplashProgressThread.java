/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2024 Skytils
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

package gg.skytils.skytilsmod.mixins.transformers.forge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gg.skytils.skytilsmod.utils.Utils;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.Point;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraftforge.fml.client.SplashProgress$3", remap = false)
public abstract class MixinSplashProgressThread {

    @Unique
    private static final Point skytils$delta = new Point(-10, -10);

    @Unique
    private static final Point skytils$diff = new Point(0, 0);

    @WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glTranslatef(FFF)V", ordinal = 3))
    private void skytils$dvdBounce(float x, float y, float z, Operation<Void> original) {
        if (Utils.INSTANCE.isBSMod()) {
            skytils$diff.translate(skytils$delta);

            x += skytils$diff.getX();
            y += skytils$diff.getY();

            if (x < -320 || x > Display.getWidth() / 2f + 320) {
                skytils$delta.setX(-skytils$delta.getX());
            }
            if (y < -240 || y > Display.getHeight() / 2f + 240) {
                skytils$delta.setY(-skytils$delta.getY());
            }
        }
        original.call(x, y, z);
    }
}
