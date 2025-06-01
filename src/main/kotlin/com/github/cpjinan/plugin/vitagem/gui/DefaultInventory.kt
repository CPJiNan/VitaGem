package com.github.cpjinan.plugin.vitagem.gui

import com.github.cpjinan.plugin.vitagem.DefaultVitaGemService.getSlot
import com.github.cpjinan.plugin.vitagem.VitaGem
import com.github.cpjinan.plugin.vitagem.data.GemConfigData
import com.github.cpjinan.plugin.vitagem.event.PlayerSocketEvent
import com.github.cpjinan.plugin.vitagem.utils.KetherUtils.evalKether
import com.github.cpjinan.plugin.vitagem.utils.RandomUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.compat.replacePlaceholder
import taboolib.platform.util.buildItem
import taboolib.platform.util.isAir
import taboolib.platform.util.modifyLore
import taboolib.platform.util.sendLang
import top.maplex.arim.Arim

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.gui
 *
 * @author 季楠
 * @since 2025/5/31 11:37
 */
object DefaultInventory {
    /** 打开界面 **/
    fun openGUI(player: Player, table: String) {
        val tableConfigData = VitaGem.api().getService().tableConfigDataMap[table] ?: return

        player.openMenu<Chest> {
            handLocked(false)

            title = tableConfigData.title

            map(*tableConfigData.layout.toTypedArray())

            val tableSection = tableConfigData.section
            val tableType = tableSection.getString("Type", "")
            val tableOptions = when (tableType) {
                "Socket" -> {
                    hashMapOf(
                        "Slot.Item" to tableSection.getString("Slot.Item", "I")!!,
                        "Slot.Gem" to tableSection.getString("Slot.Gem", "G")!!
                    )
                }

                else -> hashMapOf()
            }

            tableConfigData.icon.getKeys(false).forEach { icon ->
                val section = tableConfigData.icon.getConfigurationSection(icon)!!

                val type = section.getString("Type")?.let { XMaterial.valueOf(it) } ?: XMaterial.AIR
                val item = buildItem(type) {
                    section.getInt("Data", 0).let { damage = it }
                    section.getString("Name")?.let { name = it }
                    section.getStringList("Lore").let { lore.addAll(it) }
                    colored()
                }

                when (section.getString("Bind")) {
                    // 镶嵌宝石
                    "Socket", "VitaGem:Socket" -> {
                        set(icon[0], item) {
                            clickEvent().isCancelled = true
                            clickEvent().run {
                                val result = socketButton(
                                    player,
                                    table,
                                    player.openInventory.topInventory,
                                    getSlots(tableOptions["Slot.Item"].toString()[0])[0],
                                    getSlots(tableOptions["Slot.Gem"].toString()[0])[0]
                                )
                                section.getStringList("Kether").evalKether(player)
                            }
                        }
                    }

                    // 关闭界面
                    "Close", "VitaGem:Close" -> {
                        set(icon[0], item) {
                            clickEvent().isCancelled = true
                            clickEvent().run {
                                player.closeInventory()
                                section.getStringList("Kether").evalKether(player)
                            }
                        }
                    }

                    else -> {
                        set(icon[0], item) {
                            clickEvent().isCancelled = true
                            clickEvent().run {
                                section.getStringList("Kether").evalKether(player)
                            }
                        }
                    }
                }
            }

            onClose { event: InventoryCloseEvent ->
                when (tableType) {
                    "Socket" -> {
                        mutableListOf<Int>().apply {
                            addAll(getSlots((tableOptions["Slot.Item"] as String)[0]))
                            addAll(getSlots((tableOptions["Slot.Gem"] as String)[0]))
                        }
                    }

                    else -> mutableListOf()
                }.forEach { slot ->
                    val item = event.inventory.getItem(slot)
                    if (item != null) {
                        player.inventory.addItem(item).values.forEach {
                            player.world.dropItem(player.location, it)
                        }
                    }
                }
            }
        }
    }

    /** 镶嵌按钮
     *
     * Result Boolean 镶嵌是否成功
     *
     * Data GemConfigData 宝石配置数据
     * Enable.Result Boolean 镶嵌是否启用
     * Slot.Name String 镶嵌槽位名称
     * Slot.Result Boolean 物品有无镶嵌槽位
     * Table.Name String 限制界面名称
     * Table.Result Boolean 界面是否匹配
     * Kether.Result Boolean 脚本条件是否满足
     * Money.Result Boolean 金钱是否足够
     * Money.Amount Double 消耗金钱数量
     * Point.Result Boolean 点券是否足够
     * Point.Amount Double 消耗点券数量
     *
     * Item.Result Boolean 物品是否放入槽位
     * Item.Item ItemStack 放入槽位的物品
     * Gem.Result Boolean 宝石是否放入槽位
     * Gem.Item ItemStack 放入槽位的宝石
     * Match.Result Boolean 物品是否有宝石槽位
     * Table.Result Boolean 宝石是否与界面匹配
     * Chance.Result Boolean 镶嵌随机结果
     * Chance.Amount Double 镶嵌成功概率
     * Cancel.Result Boolean 事件是否取消
     *
     */
    fun socketButton(
        player: Player,
        table: String,
        inv: Inventory,
        itemSlot: Int,
        gemSlot: Int
    ): Map<String, Any> {
        val resultMap = hashMapOf<String, Any>("Result" to false)

        val item = inv.getItem(itemSlot)
        if (item == null || item.isAir || item.type == Material.AIR) {
            resultMap["Item.Result"] = false
            player.sendLang("Socket-No-Item")
            return resultMap
        } else {
            resultMap["Item.Result"] = true
            resultMap["Item.Item"] = item
        }

        val gemItem = inv.getItem(gemSlot)
        if (gemItem == null || gemItem.isAir || gemItem.type == Material.AIR) {
            resultMap["Gem.Result"] = false
            player.sendLang("Socket-No-Gem")
            return resultMap
        } else {
            resultMap["Gem.Result"] = true
            resultMap["Gem.Item"] = gemItem
        }

        val serviceAPI = VitaGem.api().getService()
        val hookAPI = VitaGem.api().getHook()

        var gemConfigList =
            serviceAPI.getGem(gemItem).filter { it.slot in getSlot(item).keys.map { gemData -> gemData.slot } }
        if (gemConfigList.isEmpty()) {
            resultMap["Match.Result"] = false
            player.sendLang("Socket-Gem-Not-Match")
            return resultMap
        } else resultMap["Match.Result"] = true

        gemConfigList = gemConfigList.filter {
            val gemTable = it.section.getString("Condition.Table", "")!!
            gemTable.isEmpty() || table == gemTable
        }
        if (gemConfigList.isEmpty()) {
            resultMap["Table.Result"] = false
            player.sendLang("Socket-Table-Not-Match")
            return resultMap
        } else resultMap["Table.Result"] = true

        val gemConfig = gemConfigList[0]
        val section = gemConfig.socketSection

        resultMap.putAll(isSocketConditionMet(player, item, gemConfig, table) as HashMap<String, Any>)
        val result = resultMap["Result"] as Boolean
        val enableResult = resultMap["Enable.Result"] as Boolean
        val slotResult = resultMap["Slot.Result"] as Boolean
        val tableResult = resultMap["Table.Result"] as Boolean
        val ketherResult = resultMap["Kether.Result"] as Boolean
        val moneyResult = resultMap["Money.Result"] as Boolean
        val moneyAmount = resultMap["Money.Amount"] as Double
        val pointResult = resultMap["Point.Result"] as Boolean
        val pointAmount = resultMap["Point.Amount"] as Int

        resultMap["Chance.Amount"] =
            Arim.fixedCalculator.evaluate(section.getString("Chance", "1.0")!!.replacePlaceholder(player))
        resultMap["Chance.Result"] = RandomUtils.randomBoolean(resultMap["Chance.Amount"] as Double)

        val event = PlayerSocketEvent(player, resultMap)
        event.call()
        if (event.isCancelled) {
            resultMap["Cancel.Result"] = true
            return resultMap
        } else resultMap["Cancel.Result"] = false

        if (result) {
            if (resultMap["Chance.Result"] as Boolean) {
                player.sendLang("Socket-Success")

                item.modifyLore {
                    for ((index, element) in this.withIndex()) {
                        if (element == gemConfig.slot) {
                            set(index, gemConfig.display)
                            addAll(index + 1, gemConfig.attribute)
                            break
                        }
                    }
                }

                if (!gemConfig.socketSection.getBoolean("Return.Success.Item", true)) inv.setItem(itemSlot, null)
                if (!gemConfig.socketSection.getBoolean("Return.Success.Slot", false)) inv.setItem(gemSlot, null)
                if (!gemConfig.socketSection.getBoolean("Return.Success.Money", false)) hookAPI.getVault()
                    .takeMoney(player, moneyAmount)
                if (!gemConfig.socketSection.getBoolean("Return.Success.Point", false)) hookAPI.getPlayerPoints()
                    .takePoint(player, pointAmount)
            } else {
                player.sendLang("Socket-Fail")

                if (!gemConfig.socketSection.getBoolean("Return.Fail.Item", true)) inv.setItem(itemSlot, null)
                if (!gemConfig.socketSection.getBoolean("Return.Fail.Slot", false)) inv.setItem(gemSlot, null)
                if (!gemConfig.socketSection.getBoolean("Return.Fail.Money", false)) hookAPI.getVault()
                    .takeMoney(player, moneyAmount)
                if (!gemConfig.socketSection.getBoolean("Return.Fail.Point", false)) hookAPI.getPlayerPoints()
                    .takePoint(player, pointAmount)
            }
        } else {
            if (!enableResult) player.sendLang("Socket-Disable")
            if (!slotResult) player.sendLang("Socket-No-Slot")
            if (!tableResult) player.sendLang("Socket-Table-Not-Match")
            if (!ketherResult) player.sendLang("Error-Condition-Not-Met")
            if (!moneyResult) player.sendLang(
                "Error-Money-Not-Enough",
                moneyAmount,
                hookAPI.getVault().lookMoney(player)
            )
            if (!pointResult) player.sendLang(
                "Error-Money-Not-Enough",
                pointAmount,
                hookAPI.getPlayerPoints().lookPoint(player)
            )
        }

        return resultMap
    }

    /** 是否满足镶嵌条件 **/
    fun isSocketConditionMet(
        player: Player,
        item: ItemStack,
        data: GemConfigData,
        table: String
    ): Map<String, Any> {
        val section = data.socketSection
        val result = hashMapOf<String, Any>("Result" to true)

        result["Data"] = data

        if (!section.getBoolean("Enable", false)) {
            result["Result"] = false
            result["Enable.Result"] = false
            return result
        } else result["Enable.Result"] = true

        result["Slot.Name"] = data.slot
        if (getSlot(item, data) == 0) {
            result["Result"] = false
            result["Slot.Result"] = false
            return result
        } else result["Slot.Result"] = true

        val tableCondition = section.getString("Condition.Table", "")!!
        result["Table.Name"] = tableCondition
        if (tableCondition.isNotEmpty() && table != tableCondition) {
            result["Result"] = false
            result["Table.Result"] = false
            return result
        } else result["Table.Result"] = true

        val ketherCondition = section.getStringList("Condition.Kether")
        if (!ketherCondition.all { it.evalKether(player).toString().toBoolean() }) {
            result["Result"] = false
            result["Kether.Result"] = false
            return result
        } else result["Kether.Result"] = true

        val hookAPI = VitaGem.api().getHook()

        val money = section.getDouble("Money", 0.0)
        result["Money.Amount"] = money
        if (hookAPI.getVault().isPluginEnabled() &&
            !hookAPI.getVault().isMoneyEnough(player, money)
        ) {
            result["Result"] = false
            result["Money.Result"] = false
        } else result["Money.Result"] = true

        val point = section.getInt("Point", 0)
        result["Point.Amount"] = point
        if (hookAPI.getPlayerPoints().isPluginEnabled() &&
            !hookAPI.getPlayerPoints().isPointEnough(player, point)
        ) {
            result["Result"] = false
            result["Point.Result"] = false
        } else result["Point.Result"] = true

        return result
    }
}