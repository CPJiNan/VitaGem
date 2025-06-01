package com.github.cpjinan.plugin.vitagem

import com.github.cpjinan.plugin.vitagem.hook.*
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem
 *
 * @author 季楠
 * @since 2025/5/31 13:55
 */
object DefaultVitaGemHook : VitaGemHook {
    override fun getVault(): VaultHook {
        return DefaultVaultHook
    }

    override fun getPlayerPoints(): PlayerPointsHook {
        return DefaultPlayerPointsHook
    }

    override fun getItemTools(): ItemToolsHook {
        return DefaultItemToolsHook
    }

    @Awake(LifeCycle.CONST)
    fun onConst() {
        PlatformFactory.registerAPI<VitaGemHook>(DefaultVitaGemHook)
    }
}