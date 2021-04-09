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

package skytils.skytilsmod;

import club.sk1er.mods.core.ModCore;
import club.sk1er.vigilance.gui.SettingsGui;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.ICommand;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import skytils.skytilsmod.commands.*;
import skytils.skytilsmod.core.Config;
import skytils.skytilsmod.core.DataFetcher;
import skytils.skytilsmod.core.GuiManager;
import skytils.skytilsmod.core.UpdateChecker;
import skytils.skytilsmod.events.PacketEvent;
import skytils.skytilsmod.features.impl.dungeons.*;
import skytils.skytilsmod.features.impl.dungeons.solvers.*;
import skytils.skytilsmod.features.impl.dungeons.solvers.terminals.*;
import skytils.skytilsmod.features.impl.events.GriffinBurrows;
import skytils.skytilsmod.features.impl.events.MayorJerry;
import skytils.skytilsmod.features.impl.handlers.*;
import skytils.skytilsmod.features.impl.mining.DarkModeMist;
import skytils.skytilsmod.features.impl.mining.MiningFeatures;
import skytils.skytilsmod.features.impl.misc.*;
import skytils.skytilsmod.features.impl.overlays.AuctionPriceOverlay;
import skytils.skytilsmod.features.impl.spidersden.RelicWaypoints;
import skytils.skytilsmod.features.impl.spidersden.SpidersDenFeatures;
import skytils.skytilsmod.gui.LocationEditGui;
import skytils.skytilsmod.gui.OptionsGui;
import skytils.skytilsmod.gui.commandaliases.CommandAliasesGui;
import skytils.skytilsmod.gui.keyshortcuts.KeyShortcutsGui;
import skytils.skytilsmod.listeners.ChatListener;
import skytils.skytilsmod.mixins.AccessorCommandHandler;
import skytils.skytilsmod.mixins.AccessorSettingsGui;
import skytils.skytilsmod.features.impl.handlers.MayorInfo;
import skytils.skytilsmod.utils.SBInfo;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@Mod(modid = Skytils.MODID, name = Skytils.MOD_NAME, version = Skytils.VERSION, acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true)
public class Skytils {
    public static final String MODID = "skytils";
    public static final String MOD_NAME = "Skytils";
    public static final String VERSION = "0.3-pre1";
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static Config config = new Config();
    public static File modDir = new File(new File(mc.mcDataDir, "config"), "skytils");
    public static GuiManager GUIMANAGER;

    public static int ticks = 0;

    public static ArrayDeque<String> sendMessageQueue = new ArrayDeque<>();
    public static boolean usingDungeonRooms = false;
    public static boolean usingLabymod = false;
    public static boolean usingNEU = false;
    public static File jarFile = null;
    private static long lastChatMessage = 0;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!modDir.exists()) modDir.mkdirs();
        GUIMANAGER = new GuiManager();
        jarFile = event.getSourceFile();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModCoreInstaller.initializeModCore(mc.mcDataDir);

        config.preload();

        ClientCommandHandler.instance.registerCommand(new SkytilsCommand());

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ChatListener());
        MinecraftForge.EVENT_BUS.register(new DataFetcher());
        MinecraftForge.EVENT_BUS.register(GUIMANAGER);
        MinecraftForge.EVENT_BUS.register(new MayorInfo());
        MinecraftForge.EVENT_BUS.register(SBInfo.getInstance());
        MinecraftForge.EVENT_BUS.register(new UpdateChecker());

        MinecraftForge.EVENT_BUS.register(new SpamHider());

        MinecraftForge.EVENT_BUS.register(new ArmorColor());
        MinecraftForge.EVENT_BUS.register(new AuctionData());
        MinecraftForge.EVENT_BUS.register(new AuctionPriceOverlay());
        MinecraftForge.EVENT_BUS.register(new BlazeSolver());
        MinecraftForge.EVENT_BUS.register(new BlockAbility());
        MinecraftForge.EVENT_BUS.register(new BossHPDisplays());
        MinecraftForge.EVENT_BUS.register(new BoulderSolver());
        MinecraftForge.EVENT_BUS.register(new ChestProfit());
        MinecraftForge.EVENT_BUS.register(new ClickInOrderSolver());
        MinecraftForge.EVENT_BUS.register(new CommandAliases());
        MinecraftForge.EVENT_BUS.register(new DamageSplash());
        MinecraftForge.EVENT_BUS.register(new DarkModeMist());
        MinecraftForge.EVENT_BUS.register(new DungeonsFeatures());
        MinecraftForge.EVENT_BUS.register(new DungeonTimer());
        MinecraftForge.EVENT_BUS.register(new FarmingFeatures());
        MinecraftForge.EVENT_BUS.register(new GlintCustomizer());
        MinecraftForge.EVENT_BUS.register(new GriffinBurrows());
        MinecraftForge.EVENT_BUS.register(new IceFillSolver());
        MinecraftForge.EVENT_BUS.register(new IcePathSolver());
        MinecraftForge.EVENT_BUS.register(new ItemFeatures());
        MinecraftForge.EVENT_BUS.register(new KeyShortcuts());
        MinecraftForge.EVENT_BUS.register(new LockOrb());
        MinecraftForge.EVENT_BUS.register(new MayorJerry());
        MinecraftForge.EVENT_BUS.register(new MiningFeatures());
        MinecraftForge.EVENT_BUS.register(new MinionFeatures());
        MinecraftForge.EVENT_BUS.register(new MiscFeatures());
        MinecraftForge.EVENT_BUS.register(new PetFeatures());
        MinecraftForge.EVENT_BUS.register(new RelicWaypoints());
        MinecraftForge.EVENT_BUS.register(new ScoreCalculation());
        MinecraftForge.EVENT_BUS.register(new SelectAllColorSolver());
        MinecraftForge.EVENT_BUS.register(new SimonSaysSolver());
        MinecraftForge.EVENT_BUS.register(new SlayerFeatures());
        MinecraftForge.EVENT_BUS.register(new SpidersDenFeatures());
        MinecraftForge.EVENT_BUS.register(new StartsWithSequenceSolver());
        MinecraftForge.EVENT_BUS.register(new TeleportMazeSolver());
        MinecraftForge.EVENT_BUS.register(new TerminalFeatures());
        MinecraftForge.EVENT_BUS.register(new ThreeWeirdosSolver());
        MinecraftForge.EVENT_BUS.register(new TriviaSolver());
        MinecraftForge.EVENT_BUS.register(new WaterBoardSolver());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        usingDungeonRooms = Loader.isModLoaded("dungeonrooms");
        usingLabymod = Loader.isModLoaded("labymod");
        usingNEU = Loader.isModLoaded("notenoughupdates");

        ClientCommandHandler cch = ClientCommandHandler.instance;

        if (!cch.getCommands().containsKey("armorcolor")) {
            cch.registerCommand(new ArmorColorCommand());
        }

        if (!cch.getCommands().containsKey("blockability")) {
            //cch.registerCommand(new BlockAbilityCommand());
        }

        if (!cch.getCommands().containsKey("glintcustomize")) {
            cch.registerCommand(new GlintCustomizeCommand());
        }

        if (!cch.getCommands().containsKey("reparty")) {
            cch.registerCommand(new RepartyCommand());
        }
        if (!cch.getCommands().containsKey("rp")) {
            ((AccessorCommandHandler) cch).getCommandSet().add(new RepartyCommand());
            ((AccessorCommandHandler) cch).getCommandMap().put("rp", new RepartyCommand());
        }
        if (Skytils.config.overrideReparty) {
            if (!cch.getCommands().containsKey("rp")) {
                ((AccessorCommandHandler) cch).getCommandSet().add(new RepartyCommand());
                ((AccessorCommandHandler) cch).getCommandMap().put("rp", new RepartyCommand());
            }
            for (Map.Entry<String, ICommand> entry : cch.getCommands().entrySet()) {
                if (Objects.equals(entry.getKey(), "reparty") || Objects.equals(entry.getKey(), "rp")) {
                    entry.setValue(new RepartyCommand());
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        ScreenRenderer.refresh();

        if (mc.thePlayer != null && sendMessageQueue.size() > 0 && System.currentTimeMillis() - lastChatMessage > 200) {
            String msg = sendMessageQueue.pollFirst();
            if (msg != null) {
                mc.thePlayer.sendChatMessage(msg);
            }
        }

        if (ticks % 20 == 0) {
            if (mc.thePlayer != null) {
                Utils.checkForSkyblock();
                Utils.checkForDungeons();
            }
            ticks = 0;
        }

        ticks++;
    }

    @SubscribeEvent
    public void onSendPacket(PacketEvent.SendEvent event) {
        if (event.packet instanceof C01PacketChatMessage) {
            lastChatMessage = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (mc.currentScreen instanceof OptionsGui && event.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiInitPost(GuiScreenEvent.InitGuiEvent.Post event) {
        if (Skytils.config.configButtonOnPause && event.gui instanceof GuiIngameMenu) {
            int x = event.gui.width - 105;
            int x2 = x + 100;
            int y = event.gui.height - 22;
            int y2 = y + 20;
            ArrayList<GuiButton> sorted = Lists.newArrayList(event.buttonList);
            sorted.sort((a, b) -> b.yPosition + b.height - a.yPosition + a.height);
            for (GuiButton button : sorted) {
                int otherX = button.xPosition;
                int otherX2 = button.xPosition + button.width;
                int otherY = button.yPosition;
                int otherY2 = button.yPosition + button.height;
                if (otherX2 > x && otherX < x2 && otherY2 > y && otherY < y2) {
                    y = otherY - 20 - 2;
                    y2 = y + 20;
                }
            }
            event.buttonList.add(new GuiButton(6969420, x, Math.max(0, y), 100, 20, "Skytils"));
        }
    }

    @SubscribeEvent
    public void onGuiAction(GuiScreenEvent.ActionPerformedEvent.Post event) {
        if (Skytils.config.configButtonOnPause && event.gui instanceof GuiIngameMenu && event.button.id == 6969420) {
            ModCore.getInstance().getGuiHandler().open(new OptionsGui());
        }
    }

    @SubscribeEvent
    public void onGuiChange(GuiOpenEvent event) {
        GuiScreen old = mc.currentScreen;
        if (event.gui == null && Skytils.config.reopenOptionsMenu) {
            boolean isSettingsGui = old instanceof CommandAliasesGui || old instanceof LocationEditGui || old instanceof KeyShortcutsGui || (old instanceof SettingsGui && ((AccessorSettingsGui) old).getConfig() instanceof Config);
            if (isSettingsGui) event.gui = new OptionsGui();
        }
    }

}
