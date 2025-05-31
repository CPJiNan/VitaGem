package com.github.cpjinan.plugin.vitagem.hook

import org.black_ixx.playerpoints.PlayerPoints
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.hook
 *
 * @author 季楠
 * @since 2025/5/31 13:53
 */
object DefaultPlayerPointsHook : PlayerPointsHook {
    /** 插件 PlayerPoints 是否启用 **/
    override fun isPluginEnabled(): Boolean {
        return Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")
    }

    /** 给予玩家点券 **/
    override fun givePoint(player: Player, amount: Int): Boolean {
        return PlayerPoints.getInstance().api.give(player.uniqueId, amount)
    }

    /** 扣除玩家点券 **/
    override fun takePoint(player: Player, amount: Int): Boolean {
        return PlayerPoints.getInstance().api.take(player.uniqueId, amount)
    }

    /** 玩家点券转账 **/
    override fun payPoint(sourcePlayer: Player, targetPlayer: Player, amount: Int): Boolean {
        return PlayerPoints.getInstance().api.pay(sourcePlayer.uniqueId, targetPlayer.uniqueId, amount)
    }

    /** 设置玩家点券 **/
    override fun setPoint(player: Player, amount: Int): Boolean {
        return PlayerPoints.getInstance().api.set(player.uniqueId, amount)
    }

    /** 重置玩家点券 **/
    override fun resetPoint(player: Player): Boolean {
        return PlayerPoints.getInstance().api.reset(player.uniqueId)
    }

    /** 查看玩家点券 **/
    override fun lookPoint(player: Player): Int {
        return PlayerPoints.getInstance().api.look(player.uniqueId)
    }

    /** 点券是否足够 **/
    override fun isPointEnough(player: Player, amount: Int): Boolean {
        return lookPoint(player) >= amount
    }
}