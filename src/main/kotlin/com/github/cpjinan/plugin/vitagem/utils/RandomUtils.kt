package com.github.cpjinan.plugin.vitagem.utils

import java.util.concurrent.ThreadLocalRandom

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.utils
 *
 * @author 季楠
 * @since 2025/5/31 18:55
 */
object RandomUtils {
    /** 概率随机 **/
    @JvmStatic
    fun randomBoolean(chance: Double): Boolean {
        return ThreadLocalRandom.current().nextDouble() < chance
    }
}