package skytils.skytilsmod.commands;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import skytils.skytilsmod.features.impl.handlers.BlockAbility;
import skytils.skytilsmod.utils.ItemUtil;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class BlockAbilityCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "blockability";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("disableability");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/blockability [clearall]";
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
            ItemStack item = player.getHeldItem();
            if (item == null) {
                sender.addChatMessage(new ChatComponentText("\u00a7cYou need to hold the item with the ability that you want to block!"));
                return;
            }
            String itemId = ItemUtil.getSkyBlockItemID(item);
            if (itemId == null || !ItemUtil.hasRightClickAbility(item)) {
                sender.addChatMessage(new ChatComponentText("\u00a7cThat isn't a valid item!"));
                return;
            }
            if (BlockAbility.blockedItems.contains(itemId)) {
                BlockAbility.blockedItems.remove(itemId);
                BlockAbility.writeSave();
                sender.addChatMessage(new ChatComponentText("\u00a7aRemoved the block on " + itemId + "!"));
            } else {
                BlockAbility.blockedItems.add(itemId);
                BlockAbility.writeSave();
                sender.addChatMessage(new ChatComponentText("\u00a7aYou are now blocking abilities for " + itemId + "!"));
            }
            return;
        }
        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        if (subcommand.equals("clearall")) {
            BlockAbility.blockedItems.clear();
            BlockAbility.writeSave();
            sender.addChatMessage(new ChatComponentText("\u00a7aCleared all your custom ability blocks!"));
        } else {
            player.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
        }
    }
}
