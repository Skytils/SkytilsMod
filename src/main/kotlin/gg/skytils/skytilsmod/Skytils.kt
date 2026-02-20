package gg.skytils.skytilsmod

import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.TickEvent
import gg.skytils.event.register
import gg.skytils.skytilsmod.commands.SkytilsCommands
import gg.skytils.skytilsmod.core.Config
import gg.skytils.skytilsmod.core.HypixelApi
import gg.skytils.skytilsmod.core.PersistentSave
import gg.skytils.skytilsmod.features.impl.handlers.KeyShortcuts
import gg.skytils.skytilsmod.util.SBInfo
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import java.nio.file.Path
import kotlin.io.path.createDirectories

object Skytils : EventSubscriber {
    val mc: MinecraftClient
        get() = MinecraftClient.getInstance()

    val modDir: Path by lazy {
        FabricLoader.getInstance().configDir.resolve("skytils").apply {
            createDirectories()
        }
    }

    val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    var displayScreen: Screen? = null

    fun init() {
        Config.init()

        listOf(
            this,
            SBInfo,

            KeyShortcuts,
        ).forEach(EventSubscriber::setup)

        HypixelApi.setup()
        //noinspection UnusedExpression
        SkytilsCommands
        PersistentSave.loadData()
    }

    fun onTick(event: TickEvent) {
        displayScreen?.let { screen ->
            MinecraftClient.getInstance().setScreen(screen)
            displayScreen = null
        }
    }

    override fun setup() {
        register(::onTick)
    }
}