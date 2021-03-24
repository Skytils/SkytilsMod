package skytils.skytilsmod.commands;

import com.google.common.collect.Lists;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import skytils.skytilsmod.features.impl.handlers.ArmorColor;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;

import java.awt.*;
import java.util.List;
import java.util.Locale;

public class ArmorColorCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "armorcolor";
    }

    @Override
    public List<String> getCommandAliases() {
        return Lists.newArrayList("armourcolour");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/armorcolor <clearall/clear/set>";
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
            player.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }
        String subcommand = args[0].toLowerCase(Locale.ENGLISH);
        if (subcommand.equals("clearall")) {
            ArmorColor.armorColors.clear();
            ArmorColor.saveColors();
            sender.addChatMessage(new ChatComponentText("§aCleared all your custom armor colors!"));
        } else if (subcommand.equals("clear")) {
            if (!Utils.inSkyblock) throw new WrongUsageException("You must be in Skyblock to use this command!");
            ItemStack item = ((EntityPlayerSP) sender).getHeldItem();
            if (item == null) throw new WrongUsageException("You must hold a leather armor piece to use this command");
            if (!(item.getItem() instanceof ItemArmor)) throw new WrongUsageException("You must hold a leather armor piece to use this command");
            if (((ItemArmor) item.getItem()).getArmorMaterial() != ItemArmor.ArmorMaterial.LEATHER) throw new WrongUsageException("You must hold a leather armor piece to use this command");
            NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(item);
            if (extraAttributes == null || !extraAttributes.hasKey("uuid")) throw new WrongUsageException("This item does not have a UUID!");
            String uuid = extraAttributes.getString("uuid");
            if (ArmorColor.armorColors.containsKey(uuid)) {
                ArmorColor.armorColors.remove(uuid);
                ArmorColor.saveColors();
                sender.addChatMessage(new ChatComponentText("§aCleared the custom color for your " + item.getDisplayName() + "§a!"));
            } else sender.addChatMessage(new ChatComponentText("§cThat item doesn't have a custom color!"));
        } else if (subcommand.equals("set")) {
            if (!Utils.inSkyblock) throw new WrongUsageException("You must be in Skyblock to use this command!");
            ItemStack item = ((EntityPlayerSP) sender).getHeldItem();
            if (item == null) throw new WrongUsageException("You must hold a leather armor piece to use this command");
            if (!(item.getItem() instanceof ItemArmor)) throw new WrongUsageException("You must hold a leather armor piece to use this command");
            if (((ItemArmor) item.getItem()).getArmorMaterial() != ItemArmor.ArmorMaterial.LEATHER) throw new WrongUsageException("You must hold a leather armor piece to use this command");
            NBTTagCompound extraAttributes = ItemUtil.getExtraAttributes(item);
            if (extraAttributes == null || !extraAttributes.hasKey("uuid")) throw new WrongUsageException("This item does not have a UUID!");
            if (args.length != 2) throw new WrongUsageException("You must specify a valid hex color!");
            String uuid = extraAttributes.getString("uuid");
            Color color;
            try {
                 color = Color.decode(args[1]);
            } catch (NumberFormatException e) {
                throw new WrongUsageException("You must specify a valid hex color!");
            }
            ArmorColor.armorColors.put(uuid, color);
            ArmorColor.saveColors();
            sender.addChatMessage(new ChatComponentText("§aSet the color of your " + item.getDisplayName() + "§a to " + args[1] + "!"));
        } else player.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
    }
}
