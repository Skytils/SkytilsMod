package skytils.skytilsmod.commands;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import skytils.skytilsmod.features.impl.handlers.GlintCustomizer;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GlintCustomizeCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "glintcustomize";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("customizeglint");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "glintcustomize <override/color>";
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
        if (!Utils.inSkyblock) throw new WrongUsageException("You must be in Skyblock to use this command!");
        EntityPlayerSP player = (EntityPlayerSP) sender;
        ItemStack item = player.getHeldItem();
        if (item == null) {
            throw new WrongUsageException("You need to hold an item that you wish to customize!");
        }
        String itemId = ItemUtil.getSkyBlockItemID(item);
        if (itemId == null) {
            throw new WrongUsageException("That isn't a valid item!");
        }
        if (args.length == 0) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        String originalMessage = String.join(" ", args);
        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        if (subcommand.equals("override")) {
            if (originalMessage.contains("on")) {
                GlintCustomizer.overrides.put(itemId, true);
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("§aForced an enchant glint for your item."));
                return;
            } else if (originalMessage.contains("off")) {
                GlintCustomizer.overrides.put(itemId, false);
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("§aForce disabled an enchant glint for your item."));
                return;
            } else if (originalMessage.contains("clearall")) {
                GlintCustomizer.overrides.clear();
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("§aRemoved all your glint overrides."));
                return;
            } else if (originalMessage.contains("clear")) {
                GlintCustomizer.overrides.remove(itemId);
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("§aCleared glint overrides for your item."));
                return;
            } else {
                throw new WrongUsageException("glintcustomize override <on/off/clear/clearall>");
            }
        } else if (subcommand.equals("color")) {
            if (originalMessage.contains("set")) {
                if (args.length != 3) throw new WrongUsageException("You must specify a valid hex color!");
                try {
                    GlintCustomizer.glintColors.put(itemId, Color.decode(args[2]));
                    GlintCustomizer.writeSave();
                    sender.addChatMessage(new ChatComponentText("§aForced an enchant glint color for your item."));
                } catch (NumberFormatException e) {
                 throw new WrongUsageException("You must specify a valid hex color!");
                }
                return;
            } else if (originalMessage.contains("clearall")) {
                GlintCustomizer.glintColors.clear();
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("§aRemoved all your custom glint colors."));
                return;
            } else if (originalMessage.contains("clear")) {
                GlintCustomizer.glintColors.remove(itemId);
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("§aCleared the custom glint color for your item."));
                return;
            } else {
                throw new WrongUsageException("glintcustomize color <set/clearall/clear>");
            }
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }
}
