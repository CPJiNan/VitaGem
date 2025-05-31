package com.github.cpjinan.plugin.vitagem.hook

import org.bukkit.entity.Player

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.hook
 *
 * @author 季楠
 * @since 2025/5/31 13:53
 */
interface VaultHook {
    /** 插件 Vault 是否启用 **/
    fun isPluginEnabled(): Boolean

    /** 给予玩家金钱 **/
    fun giveMoney(player: Player, amount: Double): Boolean

    /** 扣除玩家金钱 **/
    fun takeMoney(player: Player, amount: Double): Boolean

    /** 查看玩家金钱 **/
    fun lookMoney(player: Player): Double

    /** 金钱是否足够 **/
    fun isMoneyEnough(player: Player, amount: Double): Boolean
}