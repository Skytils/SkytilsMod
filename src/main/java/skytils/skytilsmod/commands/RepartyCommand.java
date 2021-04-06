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

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import skytils.skytilsmod.Skytils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepartyCommand extends CommandBase implements ICommand {
    public static boolean gettingParty = false;
    public static int Delimiter = 0;
    public static boolean disbanding = false;
    public static boolean inviting = false;
    public static boolean failInviting = false;
    public static List<String> party = new ArrayList<>();
    public static List<String> repartyFailList = new ArrayList<>();
    public static Thread partyThread = null;

    @Override
    public String getCommandName() {
        return "reparty";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/" + getCommandName();
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("rp");
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0 && (args[0].startsWith("fail") || args[0].equals("f"))) {
            partyThread = new Thread(() -> {
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
                GuiNewChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();


                try {
                    Skytils.sendMessageQueue.add("/p " + String.join(" ", repartyFailList));
                    String members = String.join(EnumChatFormatting.WHITE + "\n- " + EnumChatFormatting.YELLOW, repartyFailList);
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + "-----------------------------\n" +
                            EnumChatFormatting.GREEN + "Partying:" + EnumChatFormatting.WHITE + "\n- " +
                            EnumChatFormatting.YELLOW + members + "\n" +
                            EnumChatFormatting.BLUE + "-----------------------------"));
                    failInviting = true;
                    while (failInviting) {
                        Thread.sleep(10);
                    }
                    if (repartyFailList.size() > 0) {
                        String repartyFails = String.join("\n- " + EnumChatFormatting.RED, repartyFailList);
                        player.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + "-----------------------------\n" +
                                EnumChatFormatting.GREEN + "Failed to invite:" + EnumChatFormatting.WHITE + "\n- " +
                                EnumChatFormatting.RED + repartyFails + "\n" +
                                EnumChatFormatting.BLUE + "-----------------------------"));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            partyThread.start();

            return;
        }

        party.clear();
        repartyFailList.clear();

        partyThread = new Thread(() -> {
            EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;


            try {
                Skytils.sendMessageQueue.add("/pl");
                gettingParty = true;
                while (gettingParty) {
                    Thread.sleep(10);
                }
                if (party.size() == 0) return;
                Skytils.sendMessageQueue.add("/p disband");
                disbanding = true;
                while (disbanding) {
                    Thread.sleep(10);
                }
                String members = String.join(EnumChatFormatting.WHITE + "\n- " + EnumChatFormatting.YELLOW, RepartyCommand.party);
                player.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + "-----------------------------\n" +
                        EnumChatFormatting.GREEN + "Repartying:" + EnumChatFormatting.WHITE + "\n- " +
                        EnumChatFormatting.YELLOW + members + "\n" +
                        EnumChatFormatting.BLUE + "-----------------------------"));
                repartyFailList = new ArrayList<>(party);
                for (String invitee : party) {
                    Skytils.sendMessageQueue.add("/p " + invitee);
                    inviting = true;
                    while (inviting) {
                        Thread.sleep(10);
                    }
                    Thread.sleep(100);
                }
                while (inviting) {
                    Thread.sleep(10);
                }
                if (repartyFailList.size() > 0) {
                    String repartyFails = String.join("\n- " + EnumChatFormatting.RED, repartyFailList);
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.BLUE + "-----------------------------\n" +
                            EnumChatFormatting.GREEN + "Failed to invite:" + EnumChatFormatting.WHITE + "\n- " +
                            EnumChatFormatting.RED + repartyFails + "\n" +
                            EnumChatFormatting.BLUE + "-----------------------------"));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        partyThread.start();
    }
}