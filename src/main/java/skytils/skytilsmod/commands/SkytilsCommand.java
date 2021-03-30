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
import skytils.skytilsmod.features.impl.mining.MiningFeatures;
import skytils.skytilsmod.features.impl.handlers.CommandAliases;
import skytils.skytilsmod.gui.LocationEditGui;
import skytils.skytilsmod.gui.OptionsGui;
import skytils.skytilsmod.gui.commandaliases.CommandAliasesGui;
import skytils.skytilsmod.utils.APIUtil;
import skytils.skytilsmod.utils.MayorInfo;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SkytilsCommand extends CommandBase {

    private static ArmorColorCommand acc = new ArmorColorCommand();

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
            ModCore.getInstance().getGuiHandler().open(new OptionsGui());
            return;
        }
        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        switch (subcommand) {
            case "setkey":
                if (args.length == 1) {
                    player.addChatMessage(new ChatComponentText("§c§l[ERROR] §8» §cPlease provide your Hypixel API key!"));
                    return;
                }
                new Thread(() -> {
                    String apiKey = args[1];
                    if (APIUtil.getJSONResponse("https://api.hypixel.net/key?key=" + apiKey).get("success").getAsBoolean()) {
                        Skytils.config.apiKey = apiKey;
                        Skytils.config.markDirty();
                        player.addChatMessage(new ChatComponentText("§a§l[SUCCESS] §8» §aYour Hypixel API key has been set to §f" + apiKey + "§a."));
                        Skytils.config.writeData();
                    } else {
                        player.addChatMessage(new ChatComponentText("§c§l[ERROR] §8» §cThe Hypixel API key you provided was §finvalid§c."));
                    }
                }).start();
                break;
            case "config":
                ModCore.getInstance().getGuiHandler().open(Skytils.config.gui());
                break;
            case "fetchur":
                player.addChatMessage(new ChatComponentText("§e§l[FETCHUR] §8» §eToday's Fetchur item is: §f" + MiningFeatures.fetchurItems.values().toArray()[(ZonedDateTime.now(ZoneId.of("America/New_York")).getDayOfMonth() - 1) % MiningFeatures.fetchurItems.size()]));
                break;
            case "griffin":
                if (args.length == 1) {
                    player.addChatMessage(new ChatComponentText("/skytils griffin <refresh>"));
                } else {
                    String action = args[1].toLowerCase(Locale.ENGLISH);
                    switch (action) {
                        case "refresh":
                            GriffinBurrows.particleBurrows.removeIf(pb -> !pb.dug);
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
                            player.addChatMessage(new ChatComponentText("§b§l[RELOAD] §8» §bSkytils command aliases have been §freloaded§b successfully."));
                            break;
                        case "data":
                            DataFetcher.reloadData();
                            player.addChatMessage(new ChatComponentText("§b§l[RELOAD] §8» §bSkytils repository data has been §freloaded§b successfully."));
                            break;
                        case "mayor":
                            MayorInfo.fetchMayorData();
                            player.addChatMessage(new ChatComponentText("§b§l[RELOAD] §8» §bSkytils mayor data has been §freloaded§b successfully."));
                            break;
                        default:
                            player.addChatMessage(new ChatComponentText("/skytils reload <aliases/data>"));
                    }
                }
            case "help":
                if (args.length == 1) {
                    player.addChatMessage(new ChatComponentText("§9➜ Skytils Commands and Info" + "\n" +
                            " §2§l ❣ §7§oCommands marked with a §a§o✯ §7§orequire an §f§oAPI key§7§o to work correctly." + "\n" +
                            " §2§l ❣ §7§oThe current mod version is §f§o" + Skytils.VERSION + "§7§o." + "\n" +
                            "§9§l➜ Setup:" + "\n" +
                            " §3/skytils §l➡ §bOpens the main mod GUI." + "\n" +
                            " §3/skytils config §l➡ §bOpens the configuration GUI." + "\n" +
                            " §3/skytils setkey §l➡ §bSets your Hypixel API key." + "\n" +
                            " §3/skytils help §l➡ §bShows this help menu." + "\n" +
                            " §3/skytils reload <aliases/data> §l➡ §bForces a refresh of command aliases or solutions from the data repository." + "\n" +
                            " §3/skytils editlocations §l➡ §bOpens the location editing GUI." + "\n" +
                            " §3/skytils aliases §l➡ §bOpens the command alias editing GUI." + "\n" +
                            "§9§l➜ Events:" + "\n" +
                            " §3/skytils griffin refresh §l➡ §bForcefully refreshes Griffin Burrow waypoints. §a§o✯" + "\n" +
                            " §3/skytils fetchur §l➡ §bShows the item that Fetchur wants." + "\n" +
                            "§9§l➜ Color and Glint" + "\n" +
                    	      " §3/armorcolor <set/clear/clearall> §l➡ §bChanges the color of an armor piece to the hexcode or decimal color. §7(Alias: §f/armorcolour§7)" + "\n" +
                    	      " §3/glintcustomize override <on/off/clear/clearall> §l➡ §bEnables or disables the enchantment glint on an item." + "\n" +
                    	      " §3/glintcustomize color <set/clear/clearall> §l➡ §bChange the enchantment glint color for an item." + "\n" +
                            "§9§l➜ Miscellaneous:" + "\n" +
                            " §3/reparty §l➡ §bDisbands and re-invites everyone in your party." + "\n" +
                            " §3/blockability <clearall> §l➡ §bDisables the ability for the item in your hand."));
                    return;
                }
                break;
            case "aliases":
            case "alias":
            case "editaliases":
            case "commandaliases":
                ModCore.getInstance().getGuiHandler().open(new CommandAliasesGui());
                break;
            case "editlocation":
            case "editlocations":
            case "location":
            case "locations":
            case "loc":
            case "gui":
                ModCore.getInstance().getGuiHandler().open(new LocationEditGui());
                break;
            case "armorcolor":
            case "armorcolour":
            case "armourcolor":
            case "armourcolour":
                acc.processCommand(sender, Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                player.addChatMessage(new ChatComponentText("§bSkytils ➜ §cThis command doesn't exist!\n  §cUse §b/Skytils help§c for a full list of commands"));
        }
    }
}