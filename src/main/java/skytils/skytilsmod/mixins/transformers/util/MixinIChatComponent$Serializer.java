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

package skytils.skytilsmod.mixins.transformers.util;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import gg.essential.universal.wrappers.message.UTextComponent;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import skytils.skytilsmod.mixins.hooks.util.IChatComponent_SerializerHookKt;

@Mixin(IChatComponent.Serializer.class)
public abstract class MixinIChatComponent$Serializer implements JsonDeserializer<IChatComponent>, JsonSerializer<IChatComponent>  {
    @ModifyVariable(method = "serialize", at = @At("HEAD"), argsOnly = true)
    private IChatComponent fixUTextComponentSerialize(IChatComponent component) {
        return IChatComponent_SerializerHookKt.fixUTextComponentSerialize(component);
    }
}
