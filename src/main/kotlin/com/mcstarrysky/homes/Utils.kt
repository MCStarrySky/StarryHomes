package com.mcstarrysky.homes

import org.bukkit.command.CommandSender
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.component
import java.text.SimpleDateFormat

/**
 * StarryHomes
 * com.mcstarrysky.homes.Utils
 *
 * @author mical
 * @since 2024/7/30 11:22
 */
fun CommandSender.prettyInfo(message: String, vararg args: Any) {
    "&8\\[&{#a1caf1}传送&8\\] &{#e0edfa}$message".replaceWithOrder(*args)
        .component().buildColored()
        .sendTo(adaptCommandSender(this))
}

fun CommandSender.prettyError(message: String, vararg args: Any) {
    "&8\\[&{#a1caf1}传送&8\\] &{#f07f5e}$message".replaceWithOrder(*args)
        .component().buildColored()
        .sendTo(adaptCommandSender(this))
}

val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")