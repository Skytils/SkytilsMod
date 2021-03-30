package skytils.skytilsmod.mixins;

import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.events.GetBlockModelEvent;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.Utils;

import java.util.Map;
import java.util.Objects;

@Mixin(BlockModelShapes.class)
public class MixinBlockModelShapes {

    @Shadow @Final private Map<IBlockState, IBakedModel> bakedModelStore;

    @Shadow @Final private ModelManager modelManager;

    @Inject(method = "getModelForState", at = @At("HEAD"), cancellable = true)
    private void changeModelForState(IBlockState state, CallbackInfoReturnable<IBakedModel> cir) {
        GetBlockModelEvent event = new GetBlockModelEvent(state);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.state != state) {
            IBakedModel ibakedmodel = this.bakedModelStore.get(event.state);
            if (ibakedmodel == null) cir.setReturnValue(this.modelManager.getMissingModel());
            cir.setReturnValue(ibakedmodel);
        }
    }

}
