package com.github.cpjinan.plugin.vitagem.hook

import org.bukkit.entity.Player

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.hook
 *
 * @author 季楠
 * @since 2025/5/31 13:53
 */
interface PlayerPointsHook {
    /** 插件 PlayerPoints 是否启用 **/
    fun isPluginEnabled(): Boolean

    /** 给予玩家点券 **/
    fun givePoint(player: Player, amount: Int): Boolean

    /** 扣除玩家点券 **/
    fun takePoint(player: Player, amount: Int): Boolean

    /** 玩家点券转账 **/
    fun payPoint(sourcePlayer: Player, targetPlayer: Player, amount: Int): Boolean

    /** 设置玩家点券 **/
    fun setPoint(player: Player, amount: Int): Boolean

    /** 重置玩家点券 **/
    fun resetPoint(player: Player): Boolean

    /** 查看玩家点券 **/
    fun lookPoint(player: Player): Int

    /** 点券是否足够 **/
    fun isPointEnough(player: Player, amount: Int): Boolean
}