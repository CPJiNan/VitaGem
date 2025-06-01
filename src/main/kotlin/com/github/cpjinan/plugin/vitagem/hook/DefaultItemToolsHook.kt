package com.github.cpjinan.plugin.vitagem.hook

import com.github.cpjinan.plugin.itemtools.ItemTools
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.hook
 *
 * @author 季楠
 * @since 2025/6/1 23:29
 */
object DefaultItemToolsHook : ItemToolsHook {
    /** 插件 ItemTools 是否启用 **/
    override fun isPluginEnabled(): Boolean {
        return Bukkit.getPluginManager().isPluginEnabled("ItemTools")
    }

    /** 获取物品 **/
    override fun getItem(id: String): ItemStack? {
        return ItemTools.api().getManager().getItem(id)
    }

    /** 给予玩家物品 **/
    override fun giveItem(player: Player, id: String, amount: Int) {
        ItemTools.api().getManager().giveItem(player, id, amount)
    }

    /** 检查背包是否有足够空位 **/
    override fun countSlot(player: Player): Int {
        return player.inventory.count { ItemTools.api().getService().isAir(it) }
    }
}