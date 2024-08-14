package com.mcstarrysky.homes

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import taboolib.module.database.ColumnOptionSQL
import taboolib.module.database.ColumnTypeSQL
import taboolib.module.database.Table
import taboolib.module.database.getHost
import java.util.*

/**
 * StarryHomes
 * com.mcstarrysky.homes.PluginDatabase
 *
 * @author mical
 * @since 2024/7/30 11:31
 */
object PluginDatabase {

    private val host = StarryHomes.config.getHost("Settings.database")

    private val homesTable = Table("starryhomes_data", host) {
        add("id") {
            type(ColumnTypeSQL.INT) {
                options(
                    ColumnOptionSQL.PRIMARY_KEY,
                    ColumnOptionSQL.AUTO_INCREMENT,
                    ColumnOptionSQL.NOTNULL,
                    ColumnOptionSQL.UNIQUE_KEY
                )
            }
        }
        add("user") {
            type(ColumnTypeSQL.VARCHAR, 255)
        }
        add("name") {
            type(ColumnTypeSQL.TEXT)
        }
        add("world") {
            type(ColumnTypeSQL.TEXT)
        }
        add("x") {
            type(ColumnTypeSQL.DOUBLE)
        }
        add("y") {
            type(ColumnTypeSQL.DOUBLE)
        }
        add("z") {
            type(ColumnTypeSQL.DOUBLE)
        }
        add("pitch") {
            type(ColumnTypeSQL.FLOAT)
        }
        add("yaw") {
            type(ColumnTypeSQL.FLOAT)
        }
        add("timestamp") {
            type(ColumnTypeSQL.BIGINT)
        }
    }

    private val lastPositionTable = Table("starryhomes_last_positions", host) {
        add("user") {
            type(ColumnTypeSQL.VARCHAR, 255) {
                options(
                    ColumnOptionSQL.PRIMARY_KEY, ColumnOptionSQL.NOTNULL, ColumnOptionSQL.UNIQUE_KEY
                )
            }
        }
        add("world") {
            type(ColumnTypeSQL.TEXT)
        }
        add("x") {
            type(ColumnTypeSQL.DOUBLE)
        }
        add("y") {
            type(ColumnTypeSQL.DOUBLE)
        }
        add("z") {
            type(ColumnTypeSQL.DOUBLE)
        }
        add("pitch") {
            type(ColumnTypeSQL.FLOAT)
        }
        add("yaw") {
            type(ColumnTypeSQL.FLOAT)
        }
    }

    private val dataSource = host.createDataSource()

    fun setHome(user: Player, name: String = "home") {
        homesTable.insert(dataSource, "user", "name", "world", "x", "y", "z", "pitch", "yaw", "timestamp") {
            value(
                user.uniqueId.toString(),
                name,
                user.location.world!!.name,
                user.location.x,
                user.location.y,
                user.location.z,
                user.location.pitch,
                user.location.yaw,
                System.currentTimeMillis()
            )
        }
    }

    fun import(home: Home) {
        homesTable.insert(dataSource, "user", "name", "world", "x", "y", "z", "pitch", "yaw", "timestamp") {
            value(
                home.user.toString(),
                home.name,
                home.position.world!!.name,
                home.position.x,
                home.position.y,
                home.position.z,
                home.position.pitch,
                home.position.yaw,
                home.timestamp
            )
        }
    }

    fun hasHome(user: UUID, name: String = "home"): Boolean {
        return homesTable.find(dataSource) {
            where {
                "user" eq user.toString()
                "name" eq name
            }
        }
    }

    fun getHome(user: Player, name: String = "home"): Home? {
        return homesTable.select(dataSource) {
            where {
                "user" eq user.uniqueId.toString()
                "name" eq name
            }
        }.firstOrNull {
            Home(
                getInt("id"),
                getString("user"),
                getString("name"),
                getString("world"),
                getDouble("x"),
                getDouble("y"),
                getDouble("z"),
                getFloat("pitch"),
                getFloat("yaw"),
                getLong("timestamp")
            )
        }
    }

    fun listHomes(user: UUID): List<Home> {
        return homesTable.select(dataSource) {
            where {
                "user" eq user.toString()
            }
        }.map {
            Home(
                getInt("id"),
                getString("user"),
                getString("name"),
                getString("world"),
                getDouble("x"),
                getDouble("y"),
                getDouble("z"),
                getFloat("pitch"),
                getFloat("yaw"),
                getLong("timestamp")
            )
        }.sortedByDescending { it.timestamp } // 最后设置的家放在最前面
    }

    fun deleteHome(user: UUID, name: String) {
        homesTable.delete(dataSource) {
            where {
                "user" eq user.toString()
                "name" eq name
            }
        }
    }

    fun renameHome(user: UUID, home: String, name: String) {
        homesTable.update(dataSource) {
            set("name", name)
            where {
                "user" eq user.toString()
                "name" eq home
            }
        }
    }

    fun updateLastPosition(user: Player, position: Location) {
        if (lastPositionTable.find(dataSource) {
                where { "user" eq user.uniqueId.toString() }
            }) {
            lastPositionTable.update(dataSource) {
                set("world", position.world.name)
                set("x", position.x)
                set("y", position.y)
                set("z", position.z)
                set("pitch", position.pitch)
                set("yaw", position.yaw)
                where { "user" eq user.uniqueId.toString() }
            }
        } else {
            lastPositionTable.insert(dataSource, "user", "world", "x", "y", "z", "pitch", "yaw") {
                value(
                    user.uniqueId.toString(),
                    position.world.name,
                    position.x,
                    position.y,
                    position.z,
                    position.pitch,
                    position.yaw
                )
            }
        }
    }

    fun getLastPosition(user: Player): Location? {
        return lastPositionTable.select(dataSource) {
            where { "user" eq user.uniqueId.toString() }
        }.firstOrNull {
            Location(
                Bukkit.getWorld(getString("world")),
                getDouble("x"),
                getDouble("y"),
                getDouble("z"),
                getFloat("yaw"),
                getFloat("pitch")
            )
        }
    }

    init {
        homesTable.workspace(dataSource) { createTable(true) }.run()
        lastPositionTable.workspace(dataSource) { createTable(true) }.run()
    }
}