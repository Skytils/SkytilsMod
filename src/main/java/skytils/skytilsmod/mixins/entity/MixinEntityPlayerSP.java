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

package skytils.skytilsmod.mixins.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.events.AddChatMessageEvent;
import skytils.skytilsmod.events.ItemTossEvent;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
    @Shadow protected Minecraft mc;

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "addChatMessage", at = @At("HEAD"), cancellable = true)
    private void onAddChatMessage(IChatComponent message, CallbackInfo ci) {
        try {
            if (MinecraftForge.EVENT_BUS.post(new AddChatMessageEvent(message))) ci.cancel();
        } catch (Throwable e) {
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§cSkytils caught and logged an exception at AddChatMessageEvent. Please report this on the Discord server."));
            e.printStackTrace();
        }
    }

    @Inject(method = "dropOneItem", at = @At("HEAD"), cancellable = true)
    private void onDropItem(boolean dropAll, CallbackInfoReturnable<EntityItem> cir) {
        try {
            ItemStack stack = inventory.getCurrentItem();
            if (stack != null && MinecraftForge.EVENT_BUS.post(new ItemTossEvent(stack, dropAll))) cir.setReturnValue(null);
        } catch (Throwable e) {
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§cSkytils caught and logged an exception at PlayerDropItemEvent. Please report this on the Discord server."));
            e.printStackTrace();
        }
    }
}
