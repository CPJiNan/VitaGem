package com.github.cpjinan.plugin.vitagem

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem
 *
 * @author 季楠
 * @since 2025/5/10 22:55
 */
interface VitaGemAPI {
    /** 获取业务逻辑接口 **/
    fun getService(): VitaGemService

    /** 获取语言文件接口 **/
    fun getLanguage(): VitaGemLanguage
}