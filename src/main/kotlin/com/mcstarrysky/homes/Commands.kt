package com.mcstarrysky.homes

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.command
import taboolib.common.platform.command.suggestPlayers

/**
 * StarryHomes
 * com.mcstarrysky.homes.Commands
 *
 * @author mical
 * @since 2024/7/30 14:30
 */
object Commands {

    @Awake(LifeCycle.ENABLE)
    fun init() {
        command("back", permission = "starrysky.back", permissionDefault = PermissionDefault.TRUE) {
            execute<Player> { sender, _, _ ->
                val location = PluginDatabase.getLastPosition(sender)
                if (location == null) {
                    sender.prettyError("没有已保存的上一地点")
                    return@execute
                }
                sender.teleportAsync(location)
                sender.prettyInfo("已返回至上一地点!")
            }
        }

        command("home", permission = "starrysky.home", permissionDefault = PermissionDefault.TRUE) {
            dynamic("home") {
                suggestionUncheck<Player> { sender, _ -> PluginDatabase.listHomes(sender.uniqueId).map { it.name } }
                execute<Player> { sender, _, argument ->
                    if (!PluginDatabase.hasHome(sender.uniqueId, argument)) {
                        sender.prettyError("你要传送的家 {0} 不存在!", argument)
                        return@execute
                    }
                    val home = PluginDatabase.getHome(sender, argument)!!
                    sender.teleportAsync(home.position)
                    sender.prettyInfo("传送完成!")
                }
            }
            execute<Player> { sender, _, _ ->
                val homes = PluginDatabase.listHomes(sender.uniqueId)
                if (homes.isEmpty()) {
                    sender.prettyError("你尚未设置家! 请使用 /sethome 来设置一个家")
                    return@execute
                }
                if (homes.size == 1) {
                    sender.teleportAsync(homes.first().position)
                    sender.prettyInfo("传送完成!")
                    return@execute
                }
                MenuHomes.openMenu(sender, sender)
            }
        }

        command("sethome", permission = "starrysky.sethome", permissionDefault = PermissionDefault.TRUE) {
            dynamic("home") {
                suggestionUncheck<Player> { _, _ -> listOf("<请在这里输入你要设置的家的名字>") }
                execute<Player> { sender, _, argument ->
                    if (!checkNameVaild(sender, argument)) {
                        return@execute
                    }
                    if (PluginDatabase.hasHome(sender.uniqueId, argument)) {
                        PluginDatabase.deleteHome(sender.uniqueId, argument)
                    }
                    PluginDatabase.setHome(sender, argument)
                    sender.prettyInfo("成功设置家 {0}", argument)
                }
            }
            execute<Player> { sender, _, _ ->
                if (PluginDatabase.hasHome(sender.uniqueId, "home")) {
                    sender.prettyInfo("&7指令 §fsethome §7参数不足.")
                    sender.prettyInfo("&7正确用法:")
                    sender.prettyInfo("§f/sethome §7\\[§8家名§7\\] §8- §7将当前脚下位置设置为家")
                    return@execute
                }
                PluginDatabase.setHome(sender)
                sender.prettyInfo("成功设置家 home")
            }
        }

        command("delhome", aliases = listOf("removehome", "deletehome"), permission = "starrysky.delhome", permissionDefault = PermissionDefault.TRUE) {
            dynamic("home", optional = false) {
                suggestionUncheck<Player> { sender, _ -> PluginDatabase.listHomes(sender.uniqueId).map { it.name } }
                execute<Player> { sender, _, argument ->
                    if (!PluginDatabase.hasHome(sender.uniqueId, argument)) {
                        sender.prettyError("你要删除的家 {0} 不存在!", argument)
                        return@execute
                    }
                    PluginDatabase.deleteHome(sender.uniqueId, argument)
                    sender.prettyInfo("成功删除家 {0}", argument)
                }
            }
        }

        command("renamehome", permission = "starrysky.renamehome", permissionDefault = PermissionDefault.TRUE) {
            dynamic("home", optional = false) {
                suggestionUncheck<Player> { sender, _ -> PluginDatabase.listHomes(sender.uniqueId).map { it.name } }
                dynamic("name", optional = false) {
                    suggestionUncheck<Player> { _, _ -> listOf("<请在这里输入你要修改成什么名字>") }
                    execute<Player> { sender, ctx, _ ->
                        val home = ctx["home"]
                        if (!PluginDatabase.hasHome(sender.uniqueId, home)) {
                            sender.prettyError("你要改名的家 {0} 不存在!", home)
                            return@execute
                        }
                        val name = ctx["name"]
                        if (home == name) {
                            sender.prettyError("新名字不能与旧名字相同!")
                            return@execute
                        }
                        if (PluginDatabase.hasHome(sender.uniqueId, name)) {
                            sender.prettyError("已有一个相同名字的家!")
                            return@execute
                        }
                        if (!checkNameVaild(sender, name)) {
                            return@execute
                        }
                        PluginDatabase.renameHome(sender.uniqueId, home, name)
                        sender.prettyInfo("成功重命名! ({0} → {1})", home, name)
                    }
                }
            }
            incorrectCommand { proxyCommandSender, context, index, state ->
                val sender = proxyCommandSender.cast<CommandSender>()
                when (state) {
                    1 -> {
                        sender.prettyInfo("&7指令 §frenamehome §7参数不足.")
                        sender.prettyInfo("&7正确用法:")
                        sender.prettyInfo("§f/tell §7\\[§8要改的家名§7\\] §7\\[§8新家名§7\\] §8- §7更改家的名字")
                    }
                    2 -> {
                        sender.prettyInfo("&7指令 §frenamehome §7参数有误.")
                        sender.prettyInfo("&7正确用法:")
                        sender.prettyInfo("§f/tell §7\\[§8要改的家名§7\\] §7\\[§8新家名§7\\] §8- §7更改家的名字")
                    }
                }
            }
        }

        command("homes", aliases = listOf("homelist"), permission = "starrysky.homes", permissionDefault = PermissionDefault.TRUE) {
            dynamic("player", permission = "starrysky.homes.other") {
                suggestPlayers()
                execute<Player> { sender, _, argument ->
                    val offline = Bukkit.getOfflinePlayerIfCached(argument)
                    if (offline == null) {
                        sender.prettyError("玩家 {0} 不存在!", argument)
                        return@execute
                    }
                    MenuHomes.openMenu(sender, offline)
                }
            }
            execute<Player> { sender, _, _ ->
                MenuHomes.openMenu(sender, sender)
            }
            incorrectSender { sender, ctx ->
                (sender.castSafely<CommandSender>() ?: return@incorrectSender).prettyInfo("&7指令 &f{0} &7只能由 &f玩家 &7执行.", ctx.args().first())
            }
        }
    }

    fun checkNameVaild(sender: CommandSender, name: String): Boolean {
        if (name.length > 32) {
            sender.prettyError("你输入的名字过长! 最多只允许 32 个字符")
            return false
        }
        if (!StarryHomes.regex.toRegex().matches(name)) {
            sender.prettyError("你输入的名字不合法! 名字中只允许出现中文、英文、数字、下划线和连字符")
            return false
        }
        return true
    }
}