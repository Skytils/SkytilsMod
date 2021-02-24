package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;
import skytils.skytilsmod.utils.toasts.BlessingToast;
import skytils.skytilsmod.utils.toasts.KeyToast;
import skytils.skytilsmod.utils.toasts.SuperboomToast;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpamHider {

    private String lastBlessingType = "";

    static ArrayList<SpamMessage> spamMessages = new ArrayList<>();

    static {
        new SpamGuiElement();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (unformatted.contains(":")) return;

        // Implosion
        if (unformatted.contains("Your Implosion hit ")) {
            switch(Skytils.config.implosionHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Midas Staff
        if (unformatted.contains("Your Molten Wave hit ")) {
            switch (Skytils.config.midasStaffHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Spirit Sceptre
        if (unformatted.contains("Your Spirit Sceptre hit ")) {
            switch (Skytils.config.spiritSceptreHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Giant Sword
        if (unformatted.contains("Your Giant's Sword hit ")) {
            switch (Skytils.config.giantSwordHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Livid Dagger
        if (unformatted.contains("Your Livid Dagger hit ")) {
            switch (Skytils.config.lividHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Blessings
        if (unformatted.contains("DUNGEON BUFF!")) {
            switch (Skytils.config.blessingHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    Matcher blessingTypeMatcher = Pattern.compile("Blessing of (?<blessing>\\w+)").matcher(unformatted);
                    blessingTypeMatcher.find();
                    lastBlessingType = blessingTypeMatcher.group("blessing").toLowerCase(Locale.ROOT);
                    event.setCanceled(true);
                    break;
                default:
            }
        } else if (Pattern.compile("Grant.{1,2} you .* and .*\\.").matcher(unformatted).find()) {
            switch (Skytils.config.blessingHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    Matcher blessingBuffMatcher = Pattern.compile("(?<buff1>\\d[\\d,.%]+?) (?<symbol1>\\S{1,2})").matcher(unformatted);
                    List<BlessingToast.BlessingBuff> buffs = new ArrayList<>();

                    while (blessingBuffMatcher.find()) {
                        String symbol = blessingBuffMatcher.group("symbol1").equals("he") ? "\u2764" : blessingBuffMatcher.group("symbol1");
                        buffs.add(new BlessingToast.BlessingBuff(blessingBuffMatcher.group("buff1"), symbol));
                    }

                    GuiManager.toastGui.add(new BlessingToast(lastBlessingType, buffs));
                    event.setCanceled(true);
                    break;
                default:
            }
        } else if (unformatted.contains("Blessing of ")) {
            switch (Skytils.config.blessingHider) {
                case 1:
                case 2:
                    event.setCanceled(true);
                default:
            }
        }

        // Keys
        // Wither
        if (unformatted.contains("Wither Key") && Utils.inDungeons) {
            switch (Skytils.config.witherKeyHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                case 3:
                    event.setCanceled(true);
                    if (unformatted.contains("was picked up")) {
                        GuiManager.toastGui.add(new KeyToast("wither", ""));
                    } else {
                        String player = event.message.getFormattedText().substring(0,event.message.getFormattedText().indexOf("§r§f"));
                        GuiManager.toastGui.add(new KeyToast("wither", player));
                    }
                    break;
                default:
            }
        } else if (unformatted.contains("RIGHT CLICK on a WITHER door to open it.")) {
            switch (Skytils.config.witherKeyHider) {
                case 1:
                case 2:
                case 3:
                    event.setCanceled(true);
                    break;
                default:
            }
        }
        // Blood
        if (unformatted.contains("Blood Key") && Utils.inDungeons) {
            switch (Skytils.config.bloodKeyHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                case 3:
                    event.setCanceled(true);
                    if (unformatted.contains("was picked up")) {
                        GuiManager.toastGui.add(new KeyToast("blood", ""));
                    } else {
                        String player = event.message.getFormattedText().substring(0,event.message.getFormattedText().indexOf("§r§f"));
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
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Superboom tnt
        if (unformatted.contains("Superboom TNT!") && Utils.inDungeons) {
            switch (Skytils.config.superboomHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                case 3:
                    event.setCanceled(true);
                    String username = Minecraft.getMinecraft().thePlayer.getName();
                    String player = event.message.getFormattedText().substring(0,event.message.getFormattedText().indexOf("§r§f"));
                    if (StringUtils.stripControlCodes(player.substring(player.indexOf(" "))).equals(username)) return;
                    GuiManager.toastGui.add(new SuperboomToast());
                    break;
                default:
            }

        }

        // Blocks in the way
        if (unformatted.contains("There are blocks in the way")) {
            switch (Skytils.config.inTheWayHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Cooldown
        if (unformatted.contains("cooldown")) {
            switch (Skytils.config.cooldownHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Out of mana
        if (unformatted.contains("You do not have enough mana to do this!")) {
            switch (Skytils.config.manaMessages) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(event.message.getFormattedText());
                    event.setCanceled(true);
                    break;
                default:
            }
        }
    }

    static void newMessage(String message) {
        spamMessages.add(new SpamMessage(message, 0, 0));
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

    public static class SpamGuiElement extends GuiElement{
        static long lastTimeRender = new Date().getTime();

        public SpamGuiElement() {
            super("Spam Gui", 1.0F, new FloatPair(0.8F, 0.7F));
            Skytils.GUIMANAGER.registerElement(this);
        }


        /**
         * Based off of Soopyboo32's SoopyApis module
         * https://github.com/Soopyboo32
         * @author Soopyboo32
         */
        public void render() {
            long now = new Date().getTime();

            long timePassed = now - lastTimeRender;
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int width = sr.getScaledWidth();
            int height = sr.getScaledHeight();

            double animDiv = (double) timePassed / 1000.0;
            lastTimeRender = now;

            Collections.reverse(spamMessages);

            for(int i = 0; i < spamMessages.size(); i++) {
                SpamMessage message = spamMessages.get(i);
                int messageWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(StringUtils.stripControlCodes(message.message));

                double x;
                double y;
                x = width - 20 - messageWidth;
                message.height = (message.height + ((i * 10) - message.height) * (animDiv * 5));

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

                x += (animOnOff * (messageWidth + 30));
                y = height - 30 - (message.height);

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

                ScreenRenderer.fontRenderer.drawString(message.message, (float) x, (float) y, CommonColors.WHITE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, shadow);

                if (message.time > 5000) {
                    spamMessages.remove(message);
                }

                message.time += timePassed;
            }

            Collections.reverse(spamMessages);
        }

        public boolean getToggled() {
            return true;
        }
    }
}
