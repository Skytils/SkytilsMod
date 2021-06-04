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

package skytils.skytilsmod.mixins.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.events.RenderBlockInWorldEvent;

@Mixin(BlockRendererDispatcher.class)
public abstract class MixinBlockRendererDispatcher implements IResourceManagerReloadListener {

    @Shadow private BlockModelShapes blockModelShapes;

    @Inject(method = "getModelFromBlockState", at = @At("RETURN"), cancellable = true)
    private void modifyGetModelFromBlockState(IBlockState state, IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<IBakedModel> cir) {
        try {
            RenderBlockInWorldEvent event = new RenderBlockInWorldEvent(state, worldIn, pos);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.state != state) {
                cir.setReturnValue(this.blockModelShapes.getModelForState(event.state));
            }
        } catch (Throwable e) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§cSkytils caught and logged an exception at RenderBlockInWorldEvent. Please report this on the Discord server."));
            e.printStackTrace();
        }

    }
}
