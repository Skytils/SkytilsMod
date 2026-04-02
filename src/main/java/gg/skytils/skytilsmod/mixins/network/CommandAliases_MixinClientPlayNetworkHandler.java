package gg.skytils.skytilsmod.mixins.network;

import com.mojang.brigadier.CommandDispatcher;
import gg.skytils.skytilsmod.features.impl.handlers.CommandAliases;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class CommandAliases_MixinClientPlayNetworkHandler {

    @Shadow
    private CommandDispatcher<ClientCommandSource> commandDispatcher;

    @ModifyVariable(method = {"runClickEventCommand", "sendChatCommand"}, at = @At("HEAD"), argsOnly = true)
    private String onSendCommand(String command) {
        String possibleAlias = CommandAliases.INSTANCE.getAliases().get(command);
        return (possibleAlias != null) ? possibleAlias : command;
    }

    @Inject(method = "onCommandTree", at = @At("RETURN"))
    private void injectAliasCompletions(CommandTreeS2CPacket packet, CallbackInfo ci) {
        CommandAliases.INSTANCE.onDispatcherUpdated(commandDispatcher);
    }
}
