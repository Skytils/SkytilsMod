package skytils.skytilsmod.commands;

import club.sk1er.mods.core.ModCore;
import com.google.common.collect.Lists;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.DataFetcher;
import skytils.skytilsmod.features.impl.events.GriffinBurrows;
import skytils.skytilsmod.utils.APIUtil;

import java.util.List;
import java.util.Locale;

public class SkytilsCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "skytils";
    }

    @Override
    public List<String> getCommandAliases() {
        return Lists.newArrayList("st");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerSP player = (EntityPlayerSP) sender;
        if (args.length == 0) {
            ModCore.getInstance().getGuiHandler().open(Skytils.config.gui());
            return;
        }
        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        switch (subcommand) {
            case "setkey":
                if (args.length == 1) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Please provide your Hypixel API key!"));
                    return;
                }
                new Thread(() -> {
                    String apiKey = args[1];
                    if (APIUtil.getJSONResponse("https://api.hypixel.net/key?key=" + apiKey).get("success").getAsBoolean()) {
                        Skytils.config.apiKey = apiKey;
                        Skytils.config.markDirty();
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Updated your API key to " + apiKey));
                        Skytils.config.writeData();
                    } else {
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Please provide a valid Hypixel API key!"));
                    }
                }).start();
                break;
            case "griffin":
                if (args.length == 1) {
                    player.addChatMessage(new ChatComponentText("/skytils griffin <refresh>"));
                } else {
                    String action = args[1].toLowerCase(Locale.ENGLISH);
                    switch (action) {
                        case "refresh":
                            GriffinBurrows.burrows.clear();
                            GriffinBurrows.burrowRefreshTimer.reset();
                            GriffinBurrows.shouldRefreshBurrows = true;
                            break;
                        default:
                            player.addChatMessage(new ChatComponentText("/skytils griffin <refresh>"));
                    }
                }
                break;
            case "reloaddata":
                DataFetcher.reloadData();
                break;
            default:
                player.addChatMessage(new ChatComponentText("/" + getCommandName()));
        }
    }
}
