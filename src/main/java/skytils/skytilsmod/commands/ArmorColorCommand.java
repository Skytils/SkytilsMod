/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.commands;

import com.google.common.collect.Lists;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.*;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import skytils.skytilsmod.features.impl.handlers.ArmorColor;
import skytils.skytilsmod.utils.ItemUtil;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.colors.ColorFactory;
import skytils.skytilsmod.utils.graphics.colors.CustomColor;

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
        return Lists.newArrayList("armourcolour", "armorcolour", "armourcolor");
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
            CustomColor color;
            try {
                color = Utils.customColorFromString(args[1]);
            } catch (IllegalArgumentException e) {
                throw new SyntaxErrorException("Unable to get a color from inputted string.");
            }
            ArmorColor.armorColors.put(uuid, color);
            ArmorColor.saveColors();
            sender.addChatMessage(new ChatComponentText("§aSet the color of your " + item.getDisplayName() + "§a to " + args[1] + "!"));
        } else player.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
    }
}
