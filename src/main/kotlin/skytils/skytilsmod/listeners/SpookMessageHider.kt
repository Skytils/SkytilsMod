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

package skytils.skytilsmod.listeners

import gg.essential.lib.caffeine.cache.Caffeine
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import skytils.skytilsmod.Skytils.Companion.mc
import skytils.skytilsmod.events.impl.MainReceivePacketEvent
import skytils.skytilsmod.utils.Utils
import java.util.concurrent.TimeUnit

object SpookMessageHider {
    private val spooked = Regex(
        "(§r§cYou died and lost [\\d,.]+ coins!§r)|(§r§dJust kidding! .+ §r§7spooked you!§r)|(§r§aAll your coins are fine, this was just a big mean spook :\\)§r)|(§r§c§lDO YOU REALLY WANT TO DELETE YOUR CURRENT PROFILE\\?§r)|(§r§cIt will delete in 10 seconds...§r)|(§r§c(?:[1-5]|\\.\\.\\.)§r)|(§r§7You just got spooked! .+ §r§7is the culprit!§r)|(§r§7False! .+ §r§7just §r§7spooked §r§7you!§r)|(§r§cYou had a blacklisted .+ §r§cin your inventory, we had to delete it! Sorry!§r)|(§r§aJK! Your items are fine\\. This was just a big spook :\\)§r)|(§r§[9-b]§l▬+§r)|(§r§eFriend request from §r§d\\[PIG§r§b\\+\\+\\+§r§d\\] Technoblade§r)|(§r§a§l\\[ACCEPT\\] §r§8- §r§c§l\\[DENY\\] §r§8- §r§7§l\\[IGNORE\\]§r)|(§r§7Nope! .+ §r§7just §r§7spooked §r§7you!§r)|(§r§aOnly kidding! We won't give you op ;\\)§r)|(§r§eYou are now op!§r)|(§r§aYour profile is fine! This was just an evil spook :\\)§r)|(§r§aYou're fine! Nothing changed with your guild status! :\\)§r)|(§r§cYou were kicked from your guild with reason '.+'§r)|(§r§aSorry, its just a spook bro\\. :\\)§r)"
    )

    private val spookers = Caffeine.newBuilder()
        .maximumSize(20L)
        .expireAfterWrite(5L, TimeUnit.SECONDS)
        .build<EntityOtherPlayerMP, Boolean>()

    private val deadBush by lazy {
        Item.getItemFromBlock(Blocks.deadbush)
    }

    @SubscribeEvent
    fun onPacket(event: MainReceivePacketEvent<*, *>) {
        if (!Utils.inSkyblock) return
        event.apply {
            if (packet is S0BPacketAnimation && packet.animationType == 0) {
                val entity = mc.theWorld.getEntityByID(packet.entityID) ?: return
                if (entity !is EntityOtherPlayerMP) return
                if (entity.heldItem?.item != deadBush || entity.getDistanceSqToEntity(mc.thePlayer) > 4 * 4) return
                spookers.put(entity, true)
            }
            if (packet is S02PacketChat && spookers.estimatedSize() > 0) {
                if (spooked.matchEntire(packet.chatComponent.formattedText) != null) event.isCanceled = true
            }
        }
    }
}