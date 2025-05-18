package com.github.cpjinan.plugin.vitagem

import taboolib.common.LifeCycle
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.disablePlugin
import taboolib.common.platform.function.registerLifeCycleTask
import taboolib.platform.BukkitPlugin

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem
 *
 * @author 季楠
 * @since 2025/5/10 22:55
 */
object VitaGem : Plugin() {
    val plugin by lazy { BukkitPlugin.getInstance() }
    private var api: VitaGemAPI? = null

    init {
        registerLifeCycleTask(LifeCycle.INIT) {
            try {
                VitaGemLoader.startup()
            } catch (ex: Throwable) {
                ex.printStackTrace()
                disablePlugin()
            }
        }
    }

    /** 获取开发者接口 **/
    fun api(): VitaGemAPI {
        return api ?: error("VitaGemAPI has not finished loading, or failed to load!")
    }

    /** 注册开发者接口 **/
    fun register(api: VitaGemAPI) {
        VitaGem.api = api
    }
}