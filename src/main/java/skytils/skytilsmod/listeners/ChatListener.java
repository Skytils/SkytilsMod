package skytils.skytilsmod.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.commands.RepartyCommand;
import skytils.skytilsmod.utils.Utils;

import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatListener {

    public static Minecraft mc = Minecraft.getMinecraft();
    private static Thread rejoinThread;
    private static String lastPartyDisbander = "";

    private boolean alreadyPranked = false;

    private static final Pattern invitePattern = Pattern.compile("(?:(?:\\[.+?] )?(?:\\w+) invited )(?:\\[.+?] )?(\\w+)");
    private static final Pattern playerPattern = Pattern.compile("(?:\\[.+?] )?(\\w+)");
    private static final Pattern party_start_pattern = Pattern.compile("^Party Members \\((\\d+)\\)$");
    private static final Pattern leader_pattern = Pattern.compile("^Party Leader: (?:\\[.+?] )?(\\w+) ●$");
    private static final Pattern members_pattern = Pattern.compile(" (?:\\[.+?] )?(\\w+) ●");

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.isOnHypixel()) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (unformatted.startsWith("Your new API key is ")) {
            String apiKey = event.message.getSiblings().get(0).getChatStyle().getChatClickEvent().getValue();
            Skytils.config.apiKey = apiKey;
            Skytils.config.markDirty();
            Skytils.config.writeData();
            mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Skytils updated your set Hypixel API key to " + EnumChatFormatting.DARK_GREEN + apiKey));
        }

        if (Skytils.config.autoReparty) {
            if (unformatted.contains("has disbanded the party!")) {
                Matcher matcher = playerPattern.matcher(unformatted);
                if (matcher.find()) {
                    lastPartyDisbander = matcher.group(1);
                    System.out.println("Party disbanded by " + lastPartyDisbander);
                    rejoinThread = new Thread(() -> {
                        if (Skytils.config.autoRepartyTimeout == 0) return;
                        try {
                            System.out.println("waiting for timeout");
                            Thread.sleep(Skytils.config.autoRepartyTimeout * 1000);
                            lastPartyDisbander = "";
                            System.out.println("cleared last party disbander");
                        } catch (Exception e) {

                        }
                    });
                    rejoinThread.start();
                    return;
                }
            }
            if (unformatted.contains("You have 60 seconds to accept") && lastPartyDisbander.length() > 0 && event.message.getSiblings().size() > 0) {
                ChatStyle acceptMessage = event.message.getSiblings().get(6).getChatStyle();
                if (acceptMessage.getChatHoverEvent().getValue().getUnformattedText().contains(lastPartyDisbander)) {
                    Skytils.sendMessageQueue.add("/p accept " + lastPartyDisbander);
                    rejoinThread.interrupt();
                    lastPartyDisbander = "";
                    return;
                }
            }
        }

        // Reparty command
        // Getting party
        if (RepartyCommand.gettingParty) {
            if (unformatted.contains("-----")) {
                switch(RepartyCommand.Delimiter) {
                    case 0:
                        System.out.println("Get Party Delimiter Cancelled");
                        RepartyCommand.Delimiter++;
                        event.setCanceled(true);
                        return;
                    case 1:
                        System.out.println("Done querying party");
                        RepartyCommand.gettingParty = false;
                        RepartyCommand.Delimiter = 0;
                        event.setCanceled(true);
                        return;
                }
            } else if (unformatted.startsWith("Party M") || unformatted.startsWith("Party Leader")){
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

                Matcher party_start = party_start_pattern.matcher(unformatted);
                Matcher leader = leader_pattern.matcher(unformatted);
                Matcher members = members_pattern.matcher(unformatted);

                if (party_start.matches() && Integer.parseInt(party_start.group(1)) == 1) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You cannot reparty yourself."));
                    RepartyCommand.partyThread.interrupt();
                } else if (leader.matches() && !(leader.group(1).equals(player.getName()))) {
                    player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "You are not party leader."));
                    RepartyCommand.partyThread.interrupt();
                } else {
                    while (members.find()) {
                        String partyMember = members.group(1);
                        if (!partyMember.equals(player.getName())) {
                            RepartyCommand.party.add(partyMember);
                            System.out.println(partyMember);
                        }
                    }
                }
                event.setCanceled(true);
                return;
            }
        }
        // Disbanding party
        if (RepartyCommand.disbanding) {
            if (unformatted.contains("-----")) {
                switch (RepartyCommand.Delimiter) {
                    case 0:
                        System.out.println("Disband Delimiter Cancelled");
                        RepartyCommand.Delimiter++;
                        event.setCanceled(true);
                        return;
                    case 1:
                        System.out.println("Done disbanding");
                        RepartyCommand.disbanding = false;
                        RepartyCommand.Delimiter = 0;
                        event.setCanceled(true);
                        return;
                }
            } else if (unformatted.endsWith("has disbanded the party!")) {
                event.setCanceled(true);
                return;
            }
        }
        // Inviting
        if (RepartyCommand.inviting) {
            if (unformatted.contains("-----")) {
                switch (RepartyCommand.Delimiter) {
                    case 1:
                        event.setCanceled(true);
                        RepartyCommand.Delimiter = 0;
                        System.out.println("Player Invited!");
                        RepartyCommand.inviting = false;
                        return;
                    case 0:
                        RepartyCommand.Delimiter++;
                        event.setCanceled(true);
                        return;
                }
            } else if (unformatted.endsWith(" to the party! They have 60 seconds to accept.")) {
                Matcher invitee = invitePattern.matcher(unformatted);
                if (invitee.find()) {
                    System.out.println("" + invitee.group(1) + ": " + RepartyCommand.repartyFailList.remove(invitee.group(1)));
                }
                event.setCanceled(true);
                return;
            } else if (unformatted.contains("Couldn't find a player") || unformatted.contains("You cannot invite that player")) {
                event.setCanceled(true);
                return;
            }
        }
        // Fail Inviting
        if (RepartyCommand.failInviting) {
            if (unformatted.contains("-----")) {
                switch (RepartyCommand.Delimiter) {
                    case 1:
                        event.setCanceled(true);
                        RepartyCommand.Delimiter = 0;
                        System.out.println("Player Invited!");
                        RepartyCommand.inviting = false;
                        return;
                    case 0:
                        RepartyCommand.Delimiter++;
                        event.setCanceled(true);
                        return;
                }
            } else if (unformatted.endsWith(" to the party! They have 60 seconds to accept.")) {
                Matcher invitee = invitePattern.matcher(unformatted);
                if (invitee.find()) {
                    System.out.println("" + invitee.group(1) + ": " + RepartyCommand.repartyFailList.remove(invitee.group(1)));
                }
                event.setCanceled(true);
                return;
            } else if (unformatted.contains("Couldn't find a player") || unformatted.contains("You cannot invite that player")) {
                event.setCanceled(true);
                return;
            }
        }

        if (!alreadyPranked && ZonedDateTime.now().getMonthValue() == 4 && ZonedDateTime.now().getDayOfMonth() == 1 && unformatted.equals("Welcome to Hypixel SkyBlock!")) {
            Skytils.sendMessageQueue.addFirst("/l");
            Skytils.sendMessageQueue.addFirst("/l");
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§c§l[WARNING] §fYou have been detected voting for §dDante§f.\n" +
                    "§fDue to this, your §aSkyBlock§f profiles have been §4§lwiped§f.\n" +
                    "§fDo §e§lNOT§f contact appeals, your profiles will not be restored."));
            alreadyPranked = true;
        }

        if (Skytils.config.firstLaunch && unformatted.equals("Welcome to Hypixel SkyBlock!")) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§bThank you for downloading Skytils!"));
            ClientCommandHandler.instance.executeCommand(mc.thePlayer, "/skytils help");

            Skytils.config.firstLaunch = false;
            Skytils.config.markDirty();
            Skytils.config.writeData();
        }
    }
}
