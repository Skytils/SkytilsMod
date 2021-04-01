package skytils.skytilsmod.mixins;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.events.RenderBlockInWorldEvent;

@Mixin(BlockRendererDispatcher.class)
public class MixinBlockRendererDispatcher {

    @Shadow private BlockModelShapes blockModelShapes;

    @Inject(method = "getModelFromBlockState", at = @At("RETURN"), cancellable = true)
    private void modifyGetModelFromBlockState(IBlockState state, IBlockAccess worldIn, BlockPos pos, CallbackInfoReturnable<IBakedModel> cir) {
        RenderBlockInWorldEvent event = new RenderBlockInWorldEvent(state, worldIn, pos);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.state != state) {
            cir.setReturnValue(this.blockModelShapes.getModelForState(event.state));
        }
    }
}
