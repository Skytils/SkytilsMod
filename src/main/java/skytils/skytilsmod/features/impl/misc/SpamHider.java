package skytils.skytilsmod.features.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.events.SetActionBarEvent;
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
    private int abilityUses = 0;
    private String lastAbilityUsed = "";

    static ArrayList<SpamMessage> spamMessages = new ArrayList<>();

    static {
        new SpamGuiElement();
    }

    @SubscribeEvent
    public void onActionBarDisplay(SetActionBarEvent event) {
        Matcher manaUsageMatcher = Pattern.compile("(§b-\\d+ Mana \\(§6.+§b\\))").matcher(event.message);

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

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChat(ClientChatReceivedEvent event) {
        if (event.type == 2) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        String formatted = event.message.getFormattedText();

        // Hide Mort Messages
        if (Utils.inDungeons && unformatted.startsWith("[NPC] Mort")) {
            switch (Skytils.config.hideMortMessages) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Hide Boss Messages
        if (Utils.inDungeons && unformatted.startsWith("[BOSS]") && !unformatted.startsWith("[BOSS] The Watcher")) {
            switch (Skytils.config.hideBossMessages) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        if (unformatted.contains(":")) return;
        // CantUseAbilityHider
        if(unformatted.startsWith("You cannot use abilities in this room!")) {
            switch (Skytils.config.CantUseAbilityHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }
      
        //No enemies nearby
        if (formatted.startsWith("§r§cThere are no enemies nearby!")) {
            switch (Skytils.config.hideNoEnemiesNearby) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Implosion
        if (formatted.contains("§r§7Your Implosion hit ")) {
            switch(Skytils.config.implosionHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Midas Staff
        if (formatted.contains("§r§7Your Molten Wave hit ")) {
            switch (Skytils.config.midasStaffHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Spirit Sceptre
        if (formatted.contains("§r§7Your Spirit Sceptre hit ")) {
            switch (Skytils.config.spiritSceptreHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Giant Sword
        if (formatted.contains("§r§7Your Giant's Sword hit ")) {
            switch (Skytils.config.giantSwordHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Livid Dagger
        if (formatted.contains("§r§7Your Livid Dagger hit")) {
            switch (Skytils.config.lividHider) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Blessings
        if (formatted.contains("§r§6§lDUNGEON BUFF!")) {
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
        if (formatted.contains("§r§8Wither Key") && Utils.inDungeons) {
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
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Superboom tnt
        if (formatted.contains("§r§9Superboom TNT") && Utils.inDungeons) {
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
                    String player = formatted.substring(0, formatted.indexOf("§r§f"));
                    if (!StringUtils.stripControlCodes(player.substring(player.indexOf(" ") + 1)).equals(username)) return;
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
                    newMessage(formatted);
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
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Out of mana
        if (unformatted.contains("You do not have enough mana to do this!") || unformatted.startsWith("Not enough mana!")) {
            switch (Skytils.config.manaMessages) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        //Hide Abilities
        if (Utils.inDungeons && unformatted.contains("is now available!") && !unformatted.contains("Mining Speed Boost") && !unformatted.contains("Pickobulus") || unformatted.contains("is ready to use!") || unformatted.startsWith("Used") || unformatted.contains("Your Guided Sheep hit") || unformatted.contains("Your Thunderstorm hit") || unformatted.contains("Your Wish healed") || unformatted.contains("Your Throwing Axe hit") || unformatted.contains("Your Explosive Shot hit") || unformatted.contains("Your Seismic Wave hit")) {
            switch (Skytils.config.hideDungeonAbilities) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
                    event.setCanceled(true);
                    break;
                default:
            }
        }

        // Hide Dungeon Countdown / Ready messages
        if (Utils.inDungeons && unformatted.contains("has started the dungeon countdown. The dungeon will begin in 1 minute.") || unformatted.contains("is now ready!") || unformatted.contains("Dungeon starts in") || unformatted.contains("selected the")) {
            switch (Skytils.config.hideDungeonCountdownAndReady) {
                case 1:
                    event.setCanceled(true);
                    break;
                case 2:
                    newMessage(formatted);
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
            super("Spam Gui", 1.0F, new FloatPair(0.65F, 0.925F));
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

            double animDiv = (double) timePassed / 1000.0;
            lastTimeRender = now;

            Collections.reverse(spamMessages);

            for(int i = 0; i < spamMessages.size(); i++) {
                SpamMessage message = spamMessages.get(i);
                int messageWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(StringUtils.stripControlCodes(message.message));

                double x= this.getActualX();
                double y = 0;
                if (this.getActualY() > sr.getScaledHeight() / 2) {
                    message.height = (message.height + ((i * 10) - message.height) * (animDiv * 5));
                } else if (this.getActualY() < sr.getScaledHeight() / 2) {
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

                if (x < sr.getScaledWidth() / 2) {
                    x += ((animOnOff * -1) * (messageWidth + 30));
                } else {
                    x += (animOnOff * (messageWidth + 30));
                }
                y = this.getActualY() - (message.height);

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

                ScreenRenderer.fontRenderer.drawString(message.message, (float) (x < sr.getScaledWidth() / 2f ? x : getActualX() + getWidth()), (float) y, CommonColors.WHITE, alignment, shadow);

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

            double x = this.getActualX()  + ((Math.sin(90 * Math.PI / 180) * -1 + 1) * (messageWidth + 30));
            double y = this.getActualY();
            ScreenRenderer.fontRenderer.drawString("§r§7Your Implosion hit §r§c3 §r§7enemies for §r§c1,000,000.0 §r§7damage.§r", (float) x, (float) y, CommonColors.WHITE, SmartFontRenderer.TextAlignment.LEFT_RIGHT, shadow);
        }

        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT;
        }

        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("§r§7Your Implosion hit §r§c3 §r§7enemies for §r§c1,000,000.0 §r§7damage.§r");
        }

        public boolean getToggled() {
            return true;
        }
    }
}
