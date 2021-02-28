package skytils.skytilsmod.mixins;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.events.DamageBlockEvent;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Final
    @Shadow
    private Minecraft mc;

    @Inject(method = "isPlayerRightClickingOnEntity", at = @At("HEAD"), cancellable = true)
    private void onRightClickEntity(EntityPlayer player, Entity target, MovingObjectPosition movingObject, CallbackInfoReturnable<Boolean> cir) {
        handleRightClickEntity(player, target, cir);
    }

    @Inject(method = "interactWithEntitySendPacket", at = @At("HEAD"), cancellable = true)
    private void onInteractWithEntitySendPacket(EntityPlayer player, Entity target, CallbackInfoReturnable<Boolean> cir) {
        handleRightClickEntity(player, target, cir);
    }

    private void handleRightClickEntity(EntityPlayer player, Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (!Skytils.config.prioritizeItemAbilities || !Utils.inSkyblock || Utils.inDungeons) return;
        ItemStack item = player.getHeldItem();
        if (item != null && !(target instanceof EntityArmorStand)) {
            if (ItemUtil.hasRightClickAbility(item)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    private void onPlayerDamageBlock(BlockPos pos, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftForge.EVENT_BUS.post(new DamageBlockEvent(pos, directionFacing))) cir.cancel();
    }
}
