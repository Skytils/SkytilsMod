/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2020-2023 Skytils
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

package gg.skytils.skytilsmod.core

import gg.skytils.skytilsmod.Skytils.Companion.mc
import gg.skytils.skytilsmod.utils.Utils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Hopefully thread safe way to queue sounds with a delay
 */
object SoundQueue {
    private val soundQueue = ConcurrentLinkedQueue<QueuedSound>()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || mc.thePlayer == null || soundQueue.isEmpty()) return
        for (sound in soundQueue) {
            if (--sound.ticks <= 0) {
                if (sound.isLoud) {
                    Utils.playLoudSound(sound.sound, sound.pitch.toDouble())
                } else {
                    mc.thePlayer.playSound(sound.sound, sound.volume, sound.pitch)
                }
                soundQueue.remove(sound)
            }
        }
    }

    /**
     * Add a sound to the SoundQueue
     */
    fun addToQueue(queuedSound: QueuedSound) {
        soundQueue.add(queuedSound)
    }

    /**
     * Add a sound to the SoundQueue
     * @param sound the name of the sound to play
     * @param pitch the pitch of the sound to play
     * @param volume the volume of the sound to play
     * @param ticks the amount of ticks to delay the sound by
     * @param isLoud whether or not the sound should bypass the user's volume settings
     */
    fun addToQueue(
        sound: String,
        pitch: Float,
        volume: Float = 1f,
        ticks: Int = 0,
        isLoud: Boolean = false
    ) {
        soundQueue.add(QueuedSound(sound, pitch, volume, ticks, isLoud))
    }

    /**
     * Represents a sound in the queue
     * @param sound the name of the sound to play
     * @param pitch the pitch of the sound to play
     * @param volume the volume of the sound to play
     * @param ticks the amount of ticks to delay the sound by
     * @param isLoud whether or not the sound should bypass the user's volume settings
     */
    class QueuedSound(
        val sound: String,
        val pitch: Float,
        val volume: Float = 1f,
        var ticks: Int = 0,
        val isLoud: Boolean = false
    )
}