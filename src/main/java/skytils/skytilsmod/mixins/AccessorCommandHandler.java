package skytils.skytilsmod.mixins;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

@Mixin(CommandHandler.class)
public interface AccessorCommandHandler {
    @Accessor("commandSet")
    Set<ICommand> getCommandSet();

    @Accessor("commandMap")
    Map<String, ICommand> getCommandMap();

}
