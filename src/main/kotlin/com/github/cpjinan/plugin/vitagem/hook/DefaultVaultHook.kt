package com.github.cpjinan.plugin.vitagem.hook

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.platform.compat.depositBalance
import taboolib.platform.compat.getBalance
import taboolib.platform.compat.hasAccount
import taboolib.platform.compat.withdrawBalance

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.hook
 *
 * @author 季楠
 * @since 2025/5/31 13:53
 */
object DefaultVaultHook : VaultHook {
    /** 插件 Vault 是否启用 **/
    override fun isPluginEnabled(): Boolean {
        return Bukkit.getPluginManager().isPluginEnabled("Vault")
    }

    /** 给予玩家金钱 **/
    override fun giveMoney(player: Player, amount: Double): Boolean {
        player.depositBalance(amount)
        return true
    }

    /** 扣除玩家金钱 **/
    override fun takeMoney(player: Player, amount: Double): Boolean {
        if (!isMoneyEnough(player, amount)) return false
        player.withdrawBalance(amount)
        return true
    }

    /** 查看玩家金钱 **/
    override fun lookMoney(player: Player): Double {
        return player.getBalance()
    }

    /** 金钱是否足够 **/
    override fun isMoneyEnough(player: Player, amount: Double): Boolean {
        return player.hasAccount() && lookMoney(player) >= amount
    }
}