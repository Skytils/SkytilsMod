package gg.skytils.skytilsmod.features.impl.handlers

import gg.essential.universal.UMouse
import gg.skytils.event.EventSubscriber
import gg.skytils.event.impl.ScreenOpenEvent
import gg.skytils.event.register
import net.minecraft.client.MinecraftClient
import java.time.Duration
import java.time.Instant

object MiscFeatures : EventSubscriber {
    override fun setup() {
        register(::onScreenOpen)
    }

    //region Prevent Cursor Reset
    private var lastScreenClose: Instant = Instant.EPOCH
    var cachedMouseX = 0.0
        private set
    var cachedMouseY = 0.0
        private set

    fun onScreenOpen(event: ScreenOpenEvent) {
        if (event.screen == null && MinecraftClient.getInstance().currentScreen != null) {
            lastScreenClose = Instant.now()
            cachedMouseX = UMouse.Raw.x
            cachedMouseY = UMouse.Raw.y
        }
    }

    fun shouldPreventCursorReset() =
        Duration.between(lastScreenClose, Instant.now()).toMillis() <= 500
    //endregion
}