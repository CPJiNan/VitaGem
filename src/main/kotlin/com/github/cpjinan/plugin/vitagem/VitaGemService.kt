package com.github.cpjinan.plugin.vitagem

import com.github.cpjinan.plugin.vitagem.data.GemConfigData
import com.github.cpjinan.plugin.vitagem.data.TableConfigData
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem
 *
 * @author 季楠
 * @since 2025/5/24 13:14
 */
interface VitaGemService {
    val gemConfigDataMap: HashMap<String, GemConfigData>
    val tableConfigDataMap: HashMap<String, TableConfigData>

    /** 重载业务配置 **/
    fun reload()

    /** 打开界面 **/
    fun openGUI(player: Player, table: String)

    /** 获取宝石配置 **/
    fun getGem(item: ItemStack): List<GemConfigData>

    /** 物品是否为宝石 **/
    fun isGem(item: ItemStack, data: GemConfigData): Boolean

    /** 获取所有镶嵌槽位 **/
    fun getSlot(): List<String>

    /** 物品的所有槽位数量 **/
    fun hasSlot(item: ItemStack): Map<GemConfigData, Int>

    /** 物品的指定槽位数量 **/
    fun hasSlot(item: ItemStack, data: GemConfigData): Int

    /** 获取所有宝石槽位 **/
    fun getDisplay(): List<String>

    /** 物品的所有宝石槽位数量 **/
    fun hasDisplay(item: ItemStack): Map<GemConfigData, Int>

    /** 物品的指定宝石槽位数量 **/
    fun hasDisplay(item: ItemStack, data: GemConfigData): Int
}