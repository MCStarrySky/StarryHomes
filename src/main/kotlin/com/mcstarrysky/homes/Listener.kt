package com.mcstarrysky.homes

import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerTeleportEvent
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent

/**
 * StarryHomes
 * com.mcstarrysky.homes.Listener
 *
 * @author mical
 * @since 2024/7/30 18:31
 */
object Listener {

    private val causes = listOf(
        PlayerTeleportEvent.TeleportCause.COMMAND,
        PlayerTeleportEvent.TeleportCause.PLUGIN,
        PlayerTeleportEvent.TeleportCause.NETHER_PORTAL,
        PlayerTeleportEvent.TeleportCause.END_PORTAL,
        PlayerTeleportEvent.TeleportCause.END_GATEWAY
    )

    @SubscribeEvent(priority = EventPriority.MONITOR)
    fun e(e: PlayerTeleportEvent) {
        if (e.cause in causes) {
            PluginDatabase.updateLastPosition(e.player, e.from)
        }
    }

    @SubscribeEvent
    fun e(e: PlayerDeathEvent) {
        PluginDatabase.updateLastPosition(e.player, e.player.location)
        e.player.prettyInfo("[你可以使用 /back 或点击这句话来返回上一地点](command=/back)")
    }
}