package com.mcstarrysky.homes

import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.module.configuration.util.getLocation
import taboolib.module.configuration.util.setLocation
import taboolib.platform.util.toBukkitLocation
import taboolib.platform.util.toProxyLocation

/**
 * StarryHomes
 * com.mcstarrysky.homes.Spawn
 *
 * @author mical
 * @since 2024/7/31 20:01
 */
object Spawn {

    private var spawn: Location? = null

    @Awake(LifeCycle.ENABLE)
    fun init() {
        spawn = StarryHomes.config.getLocation("Settings.spawn")?.toBukkitLocation()
        command("spawn", permission = "starrysky.spawn", permissionDefault = PermissionDefault.TRUE) {
            execute<Player> { sender, _, _ ->
                if (spawn == null) {
                    sender.prettyError("服务器尚未设置主城!")
                    return@execute
                }
                sender.teleportAsync(spawn!!)
                sender.prettyInfo("传送完成!")
            }
        }
        command("setspawn", permission = "starrysky.setspawn") {
            execute<Player> { sender, _, _ ->
                spawn = sender.location
                StarryHomes.config.setLocation("Settings.spawn", spawn!!.toProxyLocation())
                StarryHomes.config.saveToFile()
                sender.prettyInfo("已设置你脚下 ({0},{1},{2},{3}) 为主城!",
                    spawn!!.world.name, spawn!!.blockX, spawn!!.blockY, spawn!!.blockZ)
            }
        }
    }
}