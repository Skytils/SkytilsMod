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
import skytils.skytilsmod.features.impl.misc.CommandAliases;
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
            case "reload":
                if (args.length == 1) {
                    player.addChatMessage(new ChatComponentText("/skytils reload <aliases/data>"));
                } else {
                    String action = args[1].toLowerCase(Locale.ENGLISH);
                    switch (action) {
                        case "aliases":
                            CommandAliases.reloadAliases();
                            player.addChatMessage(new ChatComponentText("Skytils reloaded your Command Aliases."));
                            break;
                        case "data":
                            DataFetcher.reloadData();
                            player.addChatMessage(new ChatComponentText("Skytils reloaded the repository data."));
                            break;
                        default:
                            player.addChatMessage(new ChatComponentText("/skytils reload <aliases/data>"));
                    }
                }
            case "help":
                if (args.length == 1) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "Skytils" + " Version " + Skytils.VERSION + "\n" +
                            EnumChatFormatting.GOLD + " /skytils" + EnumChatFormatting.AQUA + " - Opens the main GUI" + "\n" +
                            EnumChatFormatting.GOLD + " /skytils help" + EnumChatFormatting.AQUA + " - Returns this message" + "\n" +
                            EnumChatFormatting.GOLD + " /skytils setkey <key>" + EnumChatFormatting.AQUA + " - Sets your API key" + "\n" +
                            EnumChatFormatting.GOLD + " /skytils griffin refresh" + EnumChatFormatting.AQUA + " - Forces a refresh for the Griffin Burrow waypoints" + "\n" +
                            EnumChatFormatting.GOLD + " /skytils reload <aliases/data>" + EnumChatFormatting.AQUA + " - Forces Skytils to re-fetch your command aliases or solutions from the data repository." + "\n" +
                            EnumChatFormatting.GOLD + "/reparty" + EnumChatFormatting.AQUA + " - Disbands and sends a party invite to everyone who was in your party." + "\n" +
                            EnumChatFormatting.GOLD + "/armorcolor <set/clear/clearall>" + EnumChatFormatting.AQUA + " - Changes the color of an armor piece to the hexcode or decimal color provided. (Alias is /armourcolour)"));
                    return;
                }
                break;
            default:
                player.addChatMessage(new ChatComponentText("/" + getCommandName()));
        }
    }
}
