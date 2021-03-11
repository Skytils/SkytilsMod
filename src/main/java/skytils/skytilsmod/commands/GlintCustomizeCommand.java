package skytils.skytilsmod.commands;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import skytils.skytilsmod.features.impl.handlers.GlintCustomizer;
import skytils.skytilsmod.utils.ItemUtil;

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
        return "todo";
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
        ItemStack item = player.getHeldItem();
        if (item == null) {
            sender.addChatMessage(new ChatComponentText("\u00a7cYou need to hold an item that you wish to customize!"));
            return;
        }
        String itemId = ItemUtil.getSkyBlockItemID(item);
        if (itemId == null) {
            sender.addChatMessage(new ChatComponentText("\u00a7cThat isn't a valid item!"));
            return;
        }
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }
        String originalMessage = String.join(" ", args);
        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        if (subcommand.equals("override")) {
            if (originalMessage.contains("on")) {
                GlintCustomizer.overrides.put(itemId, true);
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("\u00a7aForced an enchant glint for your item."));
                return;
            } else if (originalMessage.contains("off")) {
                GlintCustomizer.overrides.put(itemId, false);
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("\u00a7aForce disabled an enchant glint for your item."));
                return;
            } else if (originalMessage.contains("clearall")) {
                GlintCustomizer.overrides.clear();
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("\u00a7aRemoved all your glint overrides."));
                return;
            } else if (originalMessage.contains("clear")) {
                GlintCustomizer.overrides.remove(itemId);
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("\u00a7aCleared glint overrides for your item."));
                return;
            } else {
                sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
                return;
            }
        } else if (subcommand.equals("color")) {
            if (originalMessage.contains("set")) {
                GlintCustomizer.glintColors.put(itemId, Color.decode(args[2]));
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("\u00a7aForced an enchant glint color for your item."));
                return;
            } else if (originalMessage.contains("clearall")) {
                GlintCustomizer.glintColors.clear();
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("\u00a7aRemoved all your custom glint colors."));
                return;
            } else if (originalMessage.contains("clear")) {
                GlintCustomizer.glintColors.remove(itemId);
                GlintCustomizer.writeSave();
                sender.addChatMessage(new ChatComponentText("\u00a7aCleared the custom glint color for your item."));
                return;
            } else {
                sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
                return;
            }
        } else {
            player.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
        }
    }
}
