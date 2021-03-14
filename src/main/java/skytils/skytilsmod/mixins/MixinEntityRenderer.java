package skytils.skytilsmod.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.utils.Utils;

import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow private Minecraft mc;

    @ModifyVariable(method = "getMouseOver", at = @At(value = "STORE"))
    private List<Entity> modifyInteractables(List<Entity> entityList) {
        if (Utils.inSkyblock) {
            if (!Utils.inDungeons && Skytils.config.hideCreeperVeilNearNPCs) {
                List<EntityOtherPlayerMP> npcs = this.mc.theWorld.getPlayers(EntityOtherPlayerMP.class, p -> p.getUniqueID().version() == 2 && p.getHealth() == 20 && !p.isPlayerSleeping());
                entityList.removeIf(e -> {
                    if (e instanceof EntityCreeper && e.isInvisible()) {
                        EntityCreeper entity = (EntityCreeper) e;
                        if (entity.getMaxHealth() == 20 && entity.getHealth() == 20 && entity.getPowered()) {
                            return npcs.stream().anyMatch(npc -> npc.getDistanceSqToEntity(entity) <= 7 * 7);
                        }
                    }
                    return false;
                });
            }
        }
        return entityList;
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void onHurtcam(float partialTicks, CallbackInfo ci) {
        if (Utils.inSkyblock && Skytils.config.noHurtcam) ci.cancel();
    }

    @Redirect(method = "updateLightmap", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getLastLightningBolt()I"))
    private int getLastLightningBolt(World world) {
        if (Skytils.config.hideLightning && Utils.inSkyblock) return 0;
        return world.getLastLightningBolt();
    }
}
