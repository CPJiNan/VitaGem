package com.github.cpjinan.plugin.vitagem

import com.github.cpjinan.plugin.vitagem.hook.PlayerPointsHook
import com.github.cpjinan.plugin.vitagem.hook.VaultHook

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem
 *
 * @author 季楠
 * @since 2025/5/31 13:55
 */
interface VitaGemHook {
    fun getVault(): VaultHook

    fun getPlayerPoints(): PlayerPointsHook
}