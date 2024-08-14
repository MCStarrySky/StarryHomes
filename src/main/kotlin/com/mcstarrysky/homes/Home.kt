package com.mcstarrysky.homes

import org.bukkit.Bukkit
import org.bukkit.Location
import taboolib.common5.util.parseUUID
import java.util.Date
import java.util.UUID

/**
 * StarryHomes
 * com.mcstarrysky.homes.Home
 *
 * @author mical
 * @since 2024/7/30 11:51
 */
data class Home(
    val id: Int,
    val user: UUID,
    val name: String,
    val position: Location,
    val timestamp: Long
) {

    constructor(id: Int, user: String, name: String, world: String, x: Double, y: Double, z: Double, pitch: Float, yaw: Float, timestamp: Long) :
            this(id, user.parseUUID()!!, name, Location(Bukkit.getWorld(world), x, y, z, yaw, pitch), timestamp)

    val date = Date(timestamp)
}
