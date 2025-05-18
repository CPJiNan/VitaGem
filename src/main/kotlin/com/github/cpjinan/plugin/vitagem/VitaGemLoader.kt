package com.github.cpjinan.plugin.vitagem

import com.github.cpjinan.plugin.vitagem.VitaGem.plugin
import com.github.cpjinan.plugin.vitagem.utils.LoggerUtils
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.function.console
import taboolib.common.util.unsafeLazy
import taboolib.module.chat.colored
import taboolib.module.lang.sendLang
import taboolib.module.metrics.Metrics
import taboolib.platform.BukkitPlugin

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem
 *
 * @author 季楠
 * @since 2025/5/10 22:55
 */
object VitaGemLoader {
    val api by unsafeLazy { DefaultVitaGemAPI() }

    /** 启动 VitaGem 服务 **/
    fun startup() {
        VitaGem.register(api)
    }

    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        console().sendLang("Plugin-Loading", plugin.description.version)
        if (VitaGemSettings.sendMetrics) Metrics(
            18992,
            BukkitPlugin.getInstance().description.version,
            Platform.BUKKIT
        )
    }

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        LoggerUtils.message(
            "",
            "&o  __     ___ _         ____                ".colored(),
            "&o  \\ \\   / (_) |_ __ _ / ___| ___ _ __ ___  ".colored(),
            "&o   \\ \\ / /| | __/ _` | |  _ / _ \\ '_ ` _ \\ ".colored(),
            "&o    \\ V / | | || (_| | |_| |  __/ | | | | |".colored(),
            "&o     \\_/  |_|\\__\\__,_|\\____|\\___|_| |_| |_|".colored(),
            ""
        )
        console().sendLang("Plugin-Enabled")
    }

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        console().sendLang("Plugin-Disable")
    }
}