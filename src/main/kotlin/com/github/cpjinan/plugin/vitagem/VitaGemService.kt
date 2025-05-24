package com.github.cpjinan.plugin.vitagem

import com.github.cpjinan.plugin.vitagem.data.TableConfigData
import org.bukkit.entity.Player

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem
 *
 * @author 季楠
 * @since 2025/5/24 13:14
 */
interface VitaGemService {
    val tableConfigDataMap: HashMap<String, TableConfigData>

    /** 重载业务配置 **/
    fun reload()

    /** 打开界面 **/
    fun openGUI(player: Player, table: String)
}