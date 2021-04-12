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

package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.events.PacketEvent;
import skytils.skytilsmod.events.SetActionBarEvent;
import skytils.skytilsmod.mixins.AccessorGuiNewChat;
import skytils.skytilsmod.utils.StringUtils;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;
import skytils.skytilsmod.utils.toasts.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpamHider {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static ArrayList<SpamMessage> spamMessages = new ArrayList<>();

    static {
        new SpamGuiElement();
    }

    private String lastBlessingType = "";
    private int abilityUses = 0;
    private String lastAbilityUsed = "";

    private static void cancelChatPacket(PacketEvent.ReceiveEvent ReceivePacketEvent, boolean addToSpam) {
        if (!(ReceivePacketEvent.packet instanceof S02PacketChat)) return;
        ReceivePacketEvent.setCanceled(true);
        S02PacketChat packet = ((S02PacketChat) ReceivePacketEvent.packet);
        if (addToSpam) newMessage(packet.getChatComponent().getFormattedText());
        MinecraftForge.EVENT_BUS.post(new ClientChatReceivedEvent(packet.getType(), packet.getChatComponent()));
    }

    private static void newMessage(String message) {
        spamMessages.add(new SpamMessage(message, 0, 0));
    }

    @SubscribeEvent
    public void onActionBarDisplay(SetActionBarEvent event) {
        if (!Utils.inSkyblock) return;
        Matcher manaUsageMatcher = Regexs.MANAUSED.pattern.matcher(event.message);

        if (Skytils.config.manaUseHider != 0 && manaUsageMatcher.find()) {
            event.setCanceled(true);
            String manaUsage = manaUsageMatcher.group(1);
            if (Skytils.config.manaUseHider == 2) {
                if ((!lastAbilityUsed.equals(manaUsage) || abilityUses % 3 == 0)) {
                    lastAbilityUsed = manaUsage;
                    abilityUses = 1;
                    newMessage(manaUsage);
                } else abilityUses++;
            }
        }
    }

    private enum Regexs {

        BLESSINGBUFF(Pattern.compile("(?<buff1>\\d[\\d,.%]+?) (?<symbol1>\\S{1,2})")),
        BLESSINGGRANT(Pattern.compile("Grant.{1,2} you .* and .*\\.")),
        BLESSINGNAME(Pattern.compile("Blessing of (?<blessing>\\w+)")),
        BUILDINGTOOLS(Pattern.compile("(§eZapped §a\\d+ §eblocks! §a§lUNDO§r)|(§r§eUnzapped §r§c\\d+ §r§eblocks away!§r)|(§r§cYou may not Grand Architect that many blocks! \\(\\d+\\/\\d+\\)§r)|(§r§cYou have \\(\\d+\\/\\d+\\) of what you're attempting to place!§r)|(§eYou built §a\\d+ §eblocks! §a§lUNDO§r)|(§r§eUndid latest Grand Architect use of §r§c\\d+ §r§eblocks!§r)")),
        MANAUSED(Pattern.compile("(§b-\\d+ Mana \\(§6.+§b\\))")),
        ;

        Pattern pattern;

        Regexs(Pattern pattern) {
            this.pattern = pattern;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onChatPacket(PacketEvent.ReceiveEvent event) {
        if (!Utils.inSkyblock) return;
        if (!(event.packet instanceof S02PacketChat)) return;
        S02PacketChat packet = (S02PacketChat) event.packet;
        if (packet.getType() == 2) return;
        String unformatted = StringUtils.stripControlCodes(packet.getChatComponent().getUnformattedText());
        String formatted = packet.getChatComponent().getFormattedText();

        try {
            // Hide Mort Messages
            if (Utils.inDungeons && unformatted.startsWith("[NPC] Mort")) {
                switch (Skytils.config.hideMortMessages) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.hideMortMessages == 2);
                        break;
                    default:
                }
            }

            // Hide Boss Messages
            if (Utils.inDungeons && unformatted.startsWith("[BOSS]") && !unformatted.startsWith("[BOSS] The Watcher")) {
                switch (Skytils.config.hideBossMessages) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.hideBossMessages == 2);
                        break;
                    default:
                }
            }

            if (Skytils.config.hideOruoMessages && Utils.inDungeons && unformatted.startsWith("[STATUE] Oruo the Omniscient: ") && !unformatted.contains("You should have listened") && !unformatted.contains("Yikes") && !unformatted.contains("chose the wrong answer") && !unformatted.contains("thinks the answer is") && !(unformatted.contains("answered Question #") && unformatted.endsWith("correctly!"))) {
                cancelChatPacket(event, false);
            }

            if (unformatted.contains(":")) return;

            //Autopet hider
            if (unformatted.startsWith("Autopet equipped your")) {
                switch (Skytils.config.hideAutopetMessages) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.hideAutopetMessages == 2);
                        break;
                    default:
                }
            }

            // CantUseAbilityHider
            if (unformatted.startsWith("You cannot use abilities in this room!")) {
                switch (Skytils.config.hideCantUseAbility) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.hideCantUseAbility == 2);
                        break;
                    default:
                }
            }

            //No enemies nearby
            if (formatted.startsWith("§r§cThere are no enemies nearby!")) {
                switch (Skytils.config.hideNoEnemiesNearby) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.hideNoEnemiesNearby == 2);
                        break;
                    default:
                }
            }

            // Implosion
            if (formatted.contains("§r§7Your Implosion hit ")) {
                switch (Skytils.config.implosionHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.implosionHider == 2);
                        break;
                    default:
                }
            }

            // Midas Staff
            if (formatted.contains("§r§7Your Molten Wave hit ")) {
                switch (Skytils.config.midasStaffHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.midasStaffHider == 2);
                        break;
                    default:
                }
            }

            // Spirit Sceptre
            if (formatted.contains("§r§7Your Spirit Sceptre hit ")) {
                switch (Skytils.config.spiritSceptreHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.spiritSceptreHider == 2);
                        break;
                    default:
                }
            }

            // Giant Sword
            if (formatted.contains("§r§7Your Giant's Sword hit ")) {
                switch (Skytils.config.giantSwordHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.giantSwordHider == 2);
                        break;
                    default:
                }
            }

            // Livid Dagger
            if (formatted.contains("§r§7Your Livid Dagger hit")) {
                switch (Skytils.config.lividHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.lividHider == 2);
                        break;
                    default:
                }
            }

            // Blessings
            if (formatted.contains("§r§6§lDUNGEON BUFF!")) {
                switch (Skytils.config.blessingHider) {
                    case 1:
                        cancelChatPacket(event, false);
                        break;
                    case 2:
                        Matcher blessingTypeMatcher = Regexs.BLESSINGNAME.pattern.matcher(unformatted);
                        if (blessingTypeMatcher.find()) {
                            lastBlessingType = blessingTypeMatcher.group("blessing").toLowerCase(Locale.ENGLISH);
                            cancelChatPacket(event, false);
                        }
                        break;
                    default:
                }
            } else if (unformatted.contains("Grant")) {
                if (Regexs.BLESSINGGRANT.pattern.matcher(unformatted).find()) {
                    switch (Skytils.config.blessingHider) {
                        case 1:
                            cancelChatPacket(event, false);
                            break;
                        case 2:
                            Matcher blessingBuffMatcher = Regexs.BLESSINGBUFF.pattern.matcher(unformatted);
                            List<BlessingToast.BlessingBuff> buffs = new ArrayList<>();

                            while (blessingBuffMatcher.find()) {
                                String symbol = blessingBuffMatcher.group("symbol1").equals("he") ? "\u2764" : blessingBuffMatcher.group("symbol1");
                                buffs.add(new BlessingToast.BlessingBuff(blessingBuffMatcher.group("buff1"), symbol));
                            }

                            if (!lastBlessingType.equals(""))
                                GuiManager.toastGui.add(new BlessingToast(lastBlessingType, buffs));
                            cancelChatPacket(event, false);
                            break;
                        default:
                    }
                }
            } else if (unformatted.contains("Blessing of ")) {
                switch (Skytils.config.blessingHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, false);
                    default:
                }
            }

            // Keys
            // Wither
            if (formatted.contains("§r§8Wither Key") && Utils.inDungeons) {
                switch (Skytils.config.witherKeyHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.witherKeyHider == 2);
                        break;
                    case 3:
                        cancelChatPacket(event, false);
                        if (unformatted.contains("was picked up")) {
                            GuiManager.toastGui.add(new KeyToast("wither", ""));
                        } else {
                            String player = formatted.substring(0, formatted.indexOf("§r§f §r§ehas"));
                            GuiManager.toastGui.add(new KeyToast("wither", player));
                        }
                        break;
                    default:
                }
            } else if (formatted.contains("§r§e§lRIGHT CLICK §r§7on §r§7a §r§8WITHER §r§7door§r§7 to open it.")) {
                switch (Skytils.config.witherKeyHider) {
                    case 1:
                    case 2:
                    case 3:
                        cancelChatPacket(event, false);
                        break;
                    default:
                }
            }
            // Blood
            if (unformatted.contains("Blood Key") && Utils.inDungeons) {
                switch (Skytils.config.bloodKeyHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.bloodKeyHider == 2);
                        break;
                    case 3:
                        cancelChatPacket(event, false);
                        if (unformatted.contains("was picked up")) {
                            GuiManager.toastGui.add(new KeyToast("blood", ""));
                        } else {
                            String player = formatted.substring(0, formatted.indexOf("§r§f §r§ehas"));
                            GuiManager.toastGui.add(new KeyToast("blood", player));
                        }
                        break;
                    default:
                }
            } else if (unformatted.contains("RIGHT CLICK on the BLOOD DOOR to open it.")) {
                switch (Skytils.config.bloodKeyHider) {
                    case 1:
                    case 2:
                    case 3:
                        cancelChatPacket(event, false);
                        break;
                    default:
                }
            }

            // Superboom tnt
            if (formatted.contains("§r§9Superboom TNT") && Utils.inDungeons) {
                switch (Skytils.config.superboomHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.superboomHider == 2);
                        break;
                    case 3:
                        cancelChatPacket(event, false);
                        String username = Minecraft.getMinecraft().thePlayer.getName();
                        if (!formatted.contains(username)) return;
                        GuiManager.toastGui.add(new SuperboomToast());
                        break;
                    default:
                }

            }

            // Revive Stone
            if (formatted.contains("§r§6Revive Stone") && Utils.inDungeons) {
                switch (Skytils.config.reviveStoneHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.reviveStoneHider == 2);
                        break;
                    case 3:
                        cancelChatPacket(event, false);
                        String username = Minecraft.getMinecraft().thePlayer.getName();
                        if (!formatted.contains(username)) return;
                        GuiManager.toastGui.add(new ReviveStoneToast());
                        break;
                    default:
                }

            }

            // Combo
            if (unformatted.contains("Combo")) {
                switch (Skytils.config.comboHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.comboHider == 2);
                        break;
                    case 3:
                        if (unformatted.startsWith("Your Kill Combo has expired!")) {
                            GuiManager.toastGui.add(new ComboEndToast());
                        } else {
                            GuiManager.toastGui.add(new ComboToast(formatted));
                        }
                        cancelChatPacket(event, false);
                        break;
                    default:
                }
            }

            // Blessing enchant
            if (formatted.startsWith("§r§aYour Blessing enchant")) {
                switch (Skytils.config.blessingEnchantHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.blessingEnchantHider == 2);
                        break;
                    default:
                }
            }

            // Blessing bair
            if (formatted.startsWith("§r§aYour bait got you double")) {
                switch (Skytils.config.blessingBaitHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.blessingBaitHider == 2);
                        break;
                    default:
                }
            }

            // Blocks in the way
            if (unformatted.contains("There are blocks in the way")) {
                switch (Skytils.config.inTheWayHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.inTheWayHider == 2);
                        break;
                    default:
                }
            }

            // Cooldown
            if (unformatted.contains("cooldown")) {
                switch (Skytils.config.cooldownHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.cooldownHider == 2);
                        break;
                    default:
                }
            }

            // Out of mana
            if (unformatted.contains("You do not have enough mana to do this!") || unformatted.startsWith("Not enough mana!")) {
                switch (Skytils.config.manaMessages) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.manaMessages == 2);
                        break;
                    default:
                }
            }

            //Hide Abilities
            if (Utils.inDungeons && unformatted.contains("is now available!") && !unformatted.contains("Mining Speed Boost") && !unformatted.contains("Pickobulus") || unformatted.contains("is ready to use!") || unformatted.startsWith("Used") || unformatted.contains("Your Guided Sheep hit") || unformatted.contains("Your Thunderstorm hit") || unformatted.contains("Your Wish healed") || unformatted.contains("Your Throwing Axe hit") || unformatted.contains("Your Explosive Shot hit") || unformatted.contains("Your Seismic Wave hit")) {
                switch (Skytils.config.hideDungeonAbilities) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.hideDungeonAbilities == 2);
                        break;
                    default:
                }
            }

            // Hide Dungeon Countdown / Ready messages
            if (Utils.inDungeons && unformatted.contains("has started the dungeon countdown. The dungeon will begin in 1 minute.") || unformatted.contains("is now ready!") || unformatted.contains("Dungeon starts in") || unformatted.contains("selected the")) {
                switch (Skytils.config.hideDungeonCountdownAndReady) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.hideDungeonCountdownAndReady == 2);
                        break;
                    default:
                }
            }

            // Compact Building Tools
            if (Skytils.config.compactBuildingTools && (formatted.contains("blocks") || formatted.contains("build") || formatted.contains("place") || formatted.contains("zap"))) {
                if (Regexs.BUILDINGTOOLS.pattern.matcher(formatted).matches()) {
                    GuiNewChat chatGui = mc.ingameGUI.getChatGUI();
                    List<ChatLine> lines = ((AccessorGuiNewChat) chatGui).getChatLines();
                    List<ChatLine> drawnLines = ((AccessorGuiNewChat) chatGui).getDrawnChatLines();
                    for (int i = 0; i < 100 && i < lines.size(); i++) {
                        ChatLine line = lines.get(i);
                        if (line.getChatComponent().getFormattedText().replaceAll("\\d", "").equals(formatted.replaceAll("\\d", ""))) {
                            drawnLines.remove(i);
                        }
                    }
                }
            }

            // Healer Tethers
            if (formatted.startsWith("§r§eYou formed a tether")) {
                switch (Skytils.config.tetherHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.tetherHider == 2);
                        break;
                    default:
                }
            }

            // Self Orb Pickups
            if (formatted.startsWith("§r§c◕ §r§eYou picked up a")) {
                switch (Skytils.config.selfOrbHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.selfOrbHider == 2);
                        break;
                    default:
                }
            }

            // Other Orb Pickups
            if (formatted.contains("§r§epicked up your ")) {
                switch (Skytils.config.otherOrbHider) {
                    case 1:
                    case 2:
                        cancelChatPacket(event, Skytils.config.otherOrbHider == 2);
                        break;
                    default:
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SpamMessage {
        public String message;
        public long time;
        public double height;

        public SpamMessage(String message, long time, double height) {
            this.message = message;
            this.time = time;
            this.height = height;
        }
    }

    public static class SpamGuiElement extends GuiElement {
        static long lastTimeRender = new Date().getTime();

        public SpamGuiElement() {
            super("Spam Gui", 1.0F, new FloatPair(0.65F, 0.925F));
            Skytils.GUIMANAGER.registerElement(this);
        }


        /**
         * Based off of Soopyboo32's SoopyApis module
         * https://github.com/Soopyboo32
         *
         * @author Soopyboo32
         */
        public void render() {
            long now = new Date().getTime();

            long timePassed = now - lastTimeRender;
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            double animDiv = (double) timePassed / 1000.0;
            lastTimeRender = now;

            Collections.reverse(spamMessages);

            for (int i = 0; i < spamMessages.size(); i++) {
                SpamMessage message = spamMessages.get(i);
                int messageWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(StringUtils.stripControlCodes(message.message));

                if (this.getActualY() > sr.getScaledHeight() / 2f) {
                    message.height = (message.height + ((i * 10) - message.height) * (animDiv * 5));
                } else if (this.getActualY() < sr.getScaledHeight() / 2f) {
                    message.height = (message.height + ((i * -10) - message.height) * (animDiv * 5));
                }

                double animOnOff = 0;
                if (message.time < 500) {
                    animOnOff = 1 - (message.time / 500.0);
                }
                if (message.time > 3500) {
                    animOnOff = ((message.time - 3500) / 500.0);
                }

                animOnOff *= 90;
                animOnOff += 90;

                animOnOff = animOnOff * Math.PI / 180;

                animOnOff = Math.sin(animOnOff);

                animOnOff *= -1;
                animOnOff += 1;

                double x = (animOnOff * (messageWidth + 30)) * ((this.getActualX() < sr.getScaledWidth() / 2f) ? -1 : 1);
                double y = -1 * (message.height);

                SmartFontRenderer.TextShadow shadow;
                switch (Skytils.config.spamShadow) {
                    case 1:
                        shadow = SmartFontRenderer.TextShadow.NONE;
                        break;
                    case 2:
                        shadow = SmartFontRenderer.TextShadow.OUTLINE;
                        break;
                    default:
                        shadow = SmartFontRenderer.TextShadow.NORMAL;
                }

                SmartFontRenderer.TextAlignment alignment = x < sr.getScaledWidth() / 2f ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;

                ScreenRenderer.fontRenderer.drawString(message.message, (float) (x < sr.getScaledWidth() / 2f ? x : x + getWidth()), (float) y, CommonColors.WHITE, alignment, shadow);

                if (message.time > 4000) {
                    spamMessages.remove(message);
                }

                message.time += timePassed;
            }

            Collections.reverse(spamMessages);
        }

        public void demoRender() {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int messageWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(StringUtils.stripControlCodes("§r§7Your Implosion hit §r§c3 §r§7enemies for §r§c1,000,000.0 §r§7damage.§r"));
            SmartFontRenderer.TextShadow shadow;
            switch (Skytils.config.spamShadow) {
                case 1:
                    shadow = SmartFontRenderer.TextShadow.NONE;
                    break;
                case 2:
                    shadow = SmartFontRenderer.TextShadow.OUTLINE;
                    break;
                default:
                    shadow = SmartFontRenderer.TextShadow.NORMAL;
            }

            double x = ((Math.sin(90 * Math.PI / 180) * -1 + 1) * (messageWidth + 30));
            double y = 0;
            ScreenRenderer.fontRenderer.drawString("§r§7Your Implosion hit §r§c3 §r§7enemies for §r§c1,000,000.0 §r§7damage.§r", (float) x, (float) y, CommonColors.WHITE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, shadow);
        }

        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT;
        }

        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("§r§7Your Implosion hit §r§c3 §r§7enemies for §r§c1,000,000.0 §r§7damage.§r");
        }

        public boolean getToggled() {
            return Skytils.config.implosionHider == 2 ||
                    Skytils.config.midasStaffHider == 2 ||
                    Skytils.config.spiritSceptreHider == 2 ||
                    Skytils.config.giantSwordHider == 2 ||
                    Skytils.config.lividHider == 2 ||
                    Skytils.config.manaUseHider == 2 ||
                    Skytils.config.bloodKeyHider == 2 ||
                    Skytils.config.hideBossMessages == 2 ||
                    Skytils.config.hideDungeonCountdownAndReady == 2 ||
                    Skytils.config.hideDungeonAbilities == 2 ||
                    Skytils.config.hideMortMessages == 2 ||
                    Skytils.config.superboomHider == 2 ||
                    Skytils.config.reviveStoneHider == 2 ||
                    Skytils.config.witherKeyHider == 2 ||
                    Skytils.config.inTheWayHider == 2 ||
                    Skytils.config.hideCantUseAbility == 2 ||
                    Skytils.config.comboHider == 2 ||
                    Skytils.config.cooldownHider == 2 ||
                    Skytils.config.hideNoEnemiesNearby == 2 ||
                    Skytils.config.manaMessages == 2 ||
                    Skytils.config.blessingEnchantHider == 2 ||
                    Skytils.config.blessingBaitHider == 2;
        }
    }
}
