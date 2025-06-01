package com.github.cpjinan.plugin.vitagem.hook

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.hook
 *
 * @author 季楠
 * @since 2025/6/1 23:29
 */
interface ItemToolsHook {
    /** 插件 ItemTools 是否启用 **/
    fun isPluginEnabled(): Boolean

    /** 获取物品 **/
    fun getItem(id: String): ItemStack?

    /** 给予玩家物品 **/
    fun giveItem(player: Player, id: String, amount: Int = 1)

    /** 检查背包是否有足够空位 **/
    fun countSlot(player: Player): Int
}