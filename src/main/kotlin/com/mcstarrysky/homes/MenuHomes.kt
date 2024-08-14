package com.mcstarrysky.homes

import org.bukkit.DyeColor
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import taboolib.common.util.sync
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.PageableChest
import taboolib.platform.util.buildItem
import taboolib.platform.util.nextChat
import java.util.function.Consumer
import kotlin.jvm.optionals.getOrDefault

/**
 * StarryHomes
 * com.mcstarrysky.homes.MenuHomes
 *
 * @author mical
 * @since 2024/7/30 15:21
 */
object MenuHomes {

    fun openMenu(player: Player, target: OfflinePlayer, back: Consumer<Player>? = null) {
        player.openMenu<PageableChest<Home>>("玩家 ${target.name} 的家 #%p") {
            map(
                "b======pn",
                "#########",
                "#########",
                "#########"
            )

            slotsBy('#')

            elements {
                PluginDatabase.listHomes(target.uniqueId)
            }

            set('=', buildItem(XMaterial.BLACK_STAINED_GLASS_PANE))

            if (back == null) {
                set('b', buildItem(XMaterial.BARRIER) {
                    name = "&c关闭菜单"
                    colored()
                }) {
                    clicker.closeInventory()
                }
            } else {
                set('b', buildItem(XMaterial.CLOCK) {
                    name = "&{#ddca57}返回上一菜单"
                    colored()
                }) {
                    back.accept(clicker)
                }
            }

            setPreviousPage(getFirstSlot('p')) { _, hasPreviousPage ->
                buildItem(if (hasPreviousPage) XMaterial.ARROW else XMaterial.FEATHER) {
                    name = if (hasPreviousPage) "&f上一页" else "&f没有上一页"
                    colored()
                }
            }

            setNextPage(getFirstSlot('n')) { _, hasNextPage ->
                buildItem(if (hasNextPage) XMaterial.ARROW else XMaterial.FEATHER) {
                    name = if (hasNextPage) "&f下一页" else "&f没有下一页"
                    colored()
                }
            }

            onGenerate { _, element, _, _ ->
                buildItem(XMaterial.matchXMaterial(DyeColor.values().random().name + "_BED").getOrDefault(XMaterial.WHITE_BED)) {
                    name = "&f" + element.name
                    lore += listOf(
                        "&7SHIFT + 左键更改名字",
                        "&7左键传送到对应地点",
                        "&7右键删除这个家",
                        "",
                        "&{#dcc44c}创建时间: &7" + DATE_FORMAT.format(element.date)
                    )
                    colored()
                }
            }

            onClick { event, element ->
                when (event.clickEvent().click) {
                    ClickType.SHIFT_LEFT -> {
                        event.clicker.closeInventory()
                        event.clicker.prettyInfo("请在聊天框内输入你要设置的新名字, 输入 '取消' 来取消操作")
                        event.clicker.nextChat { name ->
                            if (name == "取消") {
                                event.clicker.prettyInfo("已取消操作!")
                                sync { openMenu(player, target) }
                                return@nextChat
                            }
                            if (element.name == name) {
                                event.clicker.prettyError("新名字不能与旧名字相同!")
                                return@nextChat
                            }
                            if (PluginDatabase.hasHome(target.uniqueId, name)) {
                                event.clicker.prettyError("已有一个相同名字的家!")
                                return@nextChat
                            }
                            if (!Commands.checkNameVaild(event.clicker, name)) {
                                return@nextChat
                            }
                            PluginDatabase.renameHome(target.uniqueId, element.name, name)
                            event.clicker.prettyInfo("成功重命名! ({0} → {1})", element.name, name)
                        }
                    }
                    ClickType.LEFT -> {
                        event.clicker.closeInventory()
                        event.clicker.teleportAsync(element.position)
                        event.clicker.prettyInfo("传送完成!")
                    }
                    ClickType.RIGHT -> {
                        event.clicker.closeInventory()
                        event.clicker.prettyInfo("你确定要删除家 {0} 吗? 确定的话请输入 '确定', 输入 '取消' 来取消操作", element.name)
                        event.clicker.nextChat { action ->
                            when (action) {
                                "确定" -> {
                                    PluginDatabase.deleteHome(target.uniqueId, element.name)
                                    event.clicker.prettyInfo("成功删除家 {0}", element.name)
                                }
                                "取消" -> {
                                    event.clicker.prettyInfo("已取消操作!")
                                    sync { openMenu(player, target) }
                                    return@nextChat
                                }
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }
}