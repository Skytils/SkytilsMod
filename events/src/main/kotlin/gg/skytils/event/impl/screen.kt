package gg.skytils.event.impl

import gg.skytils.event.CancellableEvent
import net.minecraft.client.gui.screen.Screen

class ScreenOpenEvent(var screen: Screen?) : CancellableEvent()