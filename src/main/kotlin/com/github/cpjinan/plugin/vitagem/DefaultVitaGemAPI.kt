package com.github.cpjinan.plugin.vitagem

import taboolib.common.platform.PlatformFactory

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem
 *
 * @author 季楠
 * @since 2025/5/10 22:59
 */
class DefaultVitaGemAPI : VitaGemAPI {
    /** 语言拓展接口 **/
    var localLanguage = PlatformFactory.getAPI<VitaGemLanguage>()

    /** 获取语言文件接口 **/
    override fun getLanguage(): VitaGemLanguage {
        return localLanguage
    }
}