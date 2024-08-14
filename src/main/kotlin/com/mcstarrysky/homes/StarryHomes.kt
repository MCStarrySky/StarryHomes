package com.mcstarrysky.homes

import taboolib.common.platform.Plugin
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration
import taboolib.module.ui.virtual.InventoryHandler

/**
 * StarryHomes
 * com.mcstarrysky.homes.StarryHomes
 *
 * @author mical
 * @since 2024/7/30 11:19
 */
object StarryHomes : Plugin() {

    @Config(autoReload = true)
    lateinit var config: Configuration
        private set

    @ConfigNode("Settings.regex")
    var regex = "^[a-zA-Z0-9_\\-\\u4e00-\\u9fff]+\$"

    override fun onEnable() {
        InventoryHandler.instance
    }
}