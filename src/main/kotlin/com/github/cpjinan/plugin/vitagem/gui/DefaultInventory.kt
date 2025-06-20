package com.github.cpjinan.plugin.vitagem.gui

import com.github.cpjinan.plugin.vitagem.VitaGem
import com.github.cpjinan.plugin.vitagem.data.GemConfigData
import com.github.cpjinan.plugin.vitagem.event.PlayerExtractEvent
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

                "Extract" -> {
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
                                socketButton(
                                    player,
                                    table,
                                    player.openInventory.topInventory,
                                    getSlots((tableOptions["Slot.Item"] as String)[0])[0],
                                    getSlots((tableOptions["Slot.Gem"] as String)[0])[0]
                                )
                                section.getStringList("Kether").evalKether(player)
                            }
                        }
                    }

                    // 刷新宝石列表按钮
                    "Refresh", "VitaGem:Refresh" -> {
                        set(icon[0], item) {
                            clickEvent().isCancelled = true
                            clickEvent().run {
                                refreshButton(
                                    table,
                                    player.openInventory.topInventory,
                                    getSlots((tableOptions["Slot.Item"] as String)[0])[0],
                                    getSlots((tableOptions["Slot.Gem"] as String)[0])
                                )
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

                    "Extract" -> {
                        mutableListOf<Int>().apply {
                            addAll(getSlots((tableOptions["Slot.Item"] as String)[0]))
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

            when (tableType) {
                "Extract" -> {
                    onClick((tableOptions["Slot.Gem"] as String)[0]) {
                        it.isCancelled = true

                        val cursor = it.clickEvent().cursor
                        if (cursor != null && !cursor.isAir && cursor.type != Material.AIR) return@onClick

                        val inv = player.openInventory.topInventory
                        val itemSlot = getSlots((tableOptions["Slot.Item"] as String)[0])[0]
                        val item = inv.getItem(itemSlot)
                        if (item == null || item.isAir || item.type == Material.AIR) return@onClick

                        extractButton(player, table, inv, itemSlot, it.rawSlot)
                        refreshButton(table, inv, itemSlot, getSlots((tableOptions["Slot.Gem"] as String)[0]))
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
     * Display.Name String 宝石槽位名称
     * Table.Name String 限制界面名称
     * Table.Result Boolean 界面是否匹配
     * Kether.Result Boolean 脚本条件是否满足
     * Money.Result Boolean 金钱是否足够
     * Money.Amount Double 消耗金钱数量
     * Point.Result Boolean 点券是否足够
     * Point.Amount Double 消耗点券数量
     *
     * Item.Result Boolean 物品是否放入槽位
     * Item.ItemStack ItemStack 放入槽位的物品
     * Item.ItemStack.Amount Int 物品数量
     * Gem.Result Boolean 宝石是否放入槽位
     * Gem.ItemStack ItemStack 放入槽位的宝石
     * Gem.ItemStack.Amount Int 宝石数量
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
            resultMap["Item.ItemStack"] = item
        }

        resultMap["Item.ItemStack.Amount"] = item.amount
        if (item.amount != 1) {
            player.sendLang("Socket-Not-Single-Item")
            return resultMap
        }

        val gemItem = inv.getItem(gemSlot)
        if (gemItem == null || gemItem.isAir || gemItem.type == Material.AIR) {
            resultMap["Gem.Result"] = false
            player.sendLang("Socket-No-Gem")
            return resultMap
        } else {
            resultMap["Gem.Result"] = true
            resultMap["Gem.ItemStack"] = gemItem
        }

        resultMap["Gem.ItemStack.Amount"] = gemItem.amount
        if (gemItem.amount != 1) {
            player.sendLang("Socket-Not-Single-Gem")
            return resultMap
        }

        val serviceAPI = VitaGem.api().getService()
        val hookAPI = VitaGem.api().getHook()

        var gemConfigList =
            serviceAPI.getGem(gemItem)
                .filter { it.slot in serviceAPI.getSlot(item).keys.map { gemData -> gemData.slot } }
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
        val result = resultMap["Result"] as? Boolean ?: false
        val enableResult = resultMap["Enable.Result"] as? Boolean ?: true
        val slotResult = resultMap["Slot.Result"] as? Boolean ?: true
        val tableResult = resultMap["Table.Result"] as? Boolean ?: true
        val ketherResult = resultMap["Kether.Result"] as? Boolean ?: true
        val moneyResult = resultMap["Money.Result"] as? Boolean ?: true
        val moneyAmount = resultMap["Money.Amount"] as? Double ?: 0.0
        val pointResult = resultMap["Point.Result"] as? Boolean ?: true
        val pointAmount = resultMap["Point.Amount"] as? Int ?: 0

        resultMap["Chance.Amount"] =
            Arim.fixedCalculator.evaluate(section.getString("Chance", "1.0")!!.replacePlaceholder(player))
        resultMap["Chance.Result"] = RandomUtils.randomBoolean(resultMap["Chance.Amount"] as? Double ?: 1.0)

        val event = PlayerSocketEvent(player, resultMap)
        event.call()
        if (event.isCancelled) {
            resultMap["Cancel.Result"] = true
            return resultMap
        } else resultMap["Cancel.Result"] = false

        if (result) {
            if (resultMap["Chance.Result"] as? Boolean ?: true) {
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

                if (!section.getBoolean("Return.Success.Item", true)) inv.setItem(itemSlot, null)
                if (!section.getBoolean("Return.Success.Gem", false)) inv.setItem(gemSlot, null)
                if (hookAPI.getVault().isPluginEnabled() && !section.getBoolean("Return.Success.Money", false))
                    hookAPI.getVault().takeMoney(player, moneyAmount)
                if (hookAPI.getPlayerPoints().isPluginEnabled() && !section.getBoolean("Return.Success.Point", false))
                    hookAPI.getPlayerPoints().takePoint(player, pointAmount)
            } else {
                player.sendLang("Socket-Fail")

                if (!section.getBoolean("Return.Fail.Item", true)) inv.setItem(itemSlot, null)
                if (!section.getBoolean("Return.Fail.Gem", false)) inv.setItem(gemSlot, null)
                if (hookAPI.getVault().isPluginEnabled() && !section.getBoolean("Return.Fail.Money", false))
                    hookAPI.getVault().takeMoney(player, moneyAmount)
                if (hookAPI.getPlayerPoints().isPluginEnabled() && !section.getBoolean("Return.Fail.Point", false))
                    hookAPI.getPlayerPoints().takePoint(player, pointAmount)
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

    /** 拆卸按钮
     *
     * Result Boolean 拆卸是否成功
     *
     * Data GemConfigData 宝石配置数据
     * Enable.Result Boolean 镶嵌是否启用
     * Slot.Name String 镶嵌槽位名称
     * Display.Name String 宝石槽位名称
     * Display.Result Boolean 物品有无宝石槽位
     * Table.Name String 限制界面名称
     * Table.Result Boolean 界面是否匹配
     * Kether.Result Boolean 脚本条件是否满足
     * Money.Result Boolean 金钱是否足够
     * Money.Amount Double 消耗金钱数量
     * Point.Result Boolean 点券是否足够
     * Point.Amount Double 消耗点券数量
     *
     * Item.Result Boolean 物品是否放入槽位
     * Item.ItemStack ItemStack 放入槽位的物品
     * Item.ItemStack.Amount Int 物品数量
     * Gem.Result Boolean 宝石是否放入槽位
     * Gem.ItemStack ItemStack 放入槽位的宝石
     * Match.Result Boolean 物品是否有宝石槽位
     * Table.Result Boolean 宝石是否与界面匹配
     * Chance.Result Boolean 拆卸随机结果
     * Chance.Amount Double 拆卸成功概率
     * Cancel.Result Boolean 事件是否取消
     *
     */
    fun extractButton(
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
            player.sendLang("Extract-No-Item")
            return resultMap
        } else {
            resultMap["Item.Result"] = true
            resultMap["Item.ItemStack"] = item
        }

        resultMap["Item.ItemStack.Amount"] = item.amount
        if (item.amount != 1) {
            player.sendLang("Extract-Not-Single-Item")
            return resultMap
        }

        val gemItem = inv.getItem(gemSlot)
        if (gemItem == null || gemItem.isAir || gemItem.type == Material.AIR) {
            resultMap["Gem.Result"] = false
            return resultMap
        } else {
            resultMap["Gem.Result"] = true
            resultMap["Gem.ItemStack"] = gemItem
        }

        val serviceAPI = VitaGem.api().getService()
        val hookAPI = VitaGem.api().getHook()

        var gemConfigList =
            serviceAPI.getGem(gemItem)
                .filter { it.display in serviceAPI.getDisplay(item).keys.map { gemData -> gemData.display } }
        if (gemConfigList.isEmpty()) {
            resultMap["Match.Result"] = false
            player.sendLang("Extract-Gem-Not-Match")
            return resultMap
        } else resultMap["Match.Result"] = true

        gemConfigList = gemConfigList.filter {
            val gemTable = it.section.getString("Condition.Table", "")!!
            gemTable.isEmpty() || table == gemTable
        }
        if (gemConfigList.isEmpty()) {
            resultMap["Table.Result"] = false
            player.sendLang("Extract-Table-Not-Match")
            return resultMap
        } else resultMap["Table.Result"] = true

        val gemConfig = gemConfigList[0]
        val section = gemConfig.extractSection

        resultMap.putAll(isExtractConditionMet(player, item, gemConfig, table) as HashMap<String, Any>)
        val result = resultMap["Result"] as? Boolean ?: false
        val enableResult = resultMap["Enable.Result"] as? Boolean ?: true
        val displayResult = resultMap["Display.Result"] as? Boolean ?: true
        val tableResult = resultMap["Table.Result"] as? Boolean ?: true
        val ketherResult = resultMap["Kether.Result"] as? Boolean ?: true
        val moneyResult = resultMap["Money.Result"] as? Boolean ?: true
        val moneyAmount = resultMap["Money.Amount"] as? Double ?: 0.0
        val pointResult = resultMap["Point.Result"] as? Boolean ?: true
        val pointAmount = resultMap["Point.Amount"] as? Int ?: 0

        resultMap["Chance.Amount"] =
            Arim.fixedCalculator.evaluate(section.getString("Chance", "1.0")!!.replacePlaceholder(player))
        resultMap["Chance.Result"] = RandomUtils.randomBoolean(resultMap["Chance.Amount"] as? Double ?: 1.0)

        val event = PlayerExtractEvent(player, resultMap)
        event.call()
        if (event.isCancelled) {
            resultMap["Cancel.Result"] = true
            return resultMap
        } else resultMap["Cancel.Result"] = false

        fun extractGem() {
            item.modifyLore {
                var index = -1
                val display = gemConfig.display
                val attribute = gemConfig.attribute
                if (attribute.isEmpty()) index = this.indexOf(display)
                for (i in 0..this.size - 1 - attribute.size) {
                    if (this[i] == display) {
                        var match = true
                        for (j in attribute.indices) {
                            if (this[i + 1 + j] != attribute[j]) {
                                match = false
                                break
                            }
                        }
                        if (match) index = i
                    }
                }
                if (index != -1) {
                    set(index, gemConfig.slot)
                    repeat(attribute.size) { removeAt(index + 1) }
                }
            }
        }

        if (result) {
            if (resultMap["Chance.Result"] as? Boolean ?: true) {
                extractGem()
                player.sendLang("Extract-Success")
                if (!section.getBoolean("Return.Success.Item", true)) inv.setItem(itemSlot, null)
                if (hookAPI.getItemTools().isPluginEnabled() && section.getBoolean("Return.Success.Gem", true)) {
                    hookAPI.getItemTools().giveItem(player, section.getString("Item", "")!!)
                }
                if (hookAPI.getVault().isPluginEnabled() && !section.getBoolean("Return.Success.Money", false))
                    hookAPI.getVault().takeMoney(player, moneyAmount)
                if (hookAPI.getPlayerPoints().isPluginEnabled() && !section.getBoolean("Return.Success.Point", false))
                    hookAPI.getPlayerPoints().takePoint(player, pointAmount)
            } else {
                player.sendLang("Extract-Fail")
                if (!section.getBoolean("Return.Fail.Item", true)) inv.setItem(itemSlot, null)
                if (!section.getBoolean("Return.Fail.Gem", false)) extractGem()
                if (hookAPI.getVault().isPluginEnabled() && !section.getBoolean("Return.Fail.Money", false))
                    hookAPI.getVault().takeMoney(player, moneyAmount)
                if (hookAPI.getPlayerPoints().isPluginEnabled() && !section.getBoolean("Return.Fail.Point", false))
                    hookAPI.getPlayerPoints().takePoint(player, pointAmount)
            }
        } else {
            if (!enableResult) player.sendLang("Extract-Disable")
            if (!displayResult) player.sendLang("Extract-No-Display")
            if (!tableResult) player.sendLang("Extract-Table-Not-Match")
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

    /** 刷新宝石列表按钮 **/
    fun refreshButton(
        table: String,
        inv: Inventory,
        itemSlot: Int,
        gemSlot: List<Int>
    ) {
        val item = inv.getItem(itemSlot)
        if (item == null || item.isAir || item.type == Material.AIR) {
            gemSlot.forEach { inv.setItem(it, null) }
            return
        }

        val serviceAPI = VitaGem.api().getService()
        val hookAPI = VitaGem.api().getHook()

        val gemItemList = serviceAPI.gemConfigDataMap.filterValues {
            var index = -1
            val display = it.display
            val attribute = it.attribute
            item.modifyLore {
                if (attribute.isEmpty()) index = this.indexOf(display)
                for (i in 0..this.size - 1 - attribute.size) {
                    if (this[i] == display) {
                        var match = true
                        for (j in attribute.indices) {
                            if (this[i + 1 + j] != attribute[j]) {
                                match = false
                                break
                            }
                        }
                        if (match) index = i
                    }
                }
            }
            index != -1
        }.filterValues {
            val gemTable = it.section.getString("Condition.Table", "")!!
            gemTable.isEmpty() || table == gemTable
        }.mapNotNull {
            if (hookAPI.getItemTools().isPluginEnabled()) hookAPI.getItemTools()
                .getItem(it.value.extractSection.getString("Item", "")!!)
            else null
        }.distinct().toMutableList()

        gemSlot.forEach {
            if (gemItemList.isNotEmpty()) inv.setItem(it, gemItemList.removeFirstOrNull())
            else inv.setItem(it, null)
        }
    }

    /** 是否满足镶嵌条件 **/
    fun isSocketConditionMet(
        player: Player,
        item: ItemStack,
        data: GemConfigData,
        table: String
    ): Map<String, Any> {
        val section = data.socketSection
        val serviceAPI = VitaGem.api().getService()
        val result = hashMapOf<String, Any>("Result" to true)

        result["Data"] = data

        if (!section.getBoolean("Enable", false)) {
            result["Result"] = false
            result["Enable.Result"] = false
            return result
        } else result["Enable.Result"] = true

        result["Slot.Name"] = data.slot
        result["Display.Name"] = data.display
        if (serviceAPI.getSlot(item, data) == 0) {
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
        if (ketherCondition.isNotEmpty() && !ketherCondition.all { it.evalKether(player).toString().toBoolean() }) {
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

    /** 是否满足拆卸条件 **/
    fun isExtractConditionMet(
        player: Player,
        item: ItemStack,
        data: GemConfigData,
        table: String
    ): Map<String, Any> {
        val section = data.extractSection
        val serviceAPI = VitaGem.api().getService()
        val result = hashMapOf<String, Any>("Result" to true)

        result["Data"] = data

        if (!section.getBoolean("Enable", false)) {
            result["Result"] = false
            result["Enable.Result"] = false
            return result
        } else result["Enable.Result"] = true

        result["Slot.Name"] = data.slot
        result["Display.Name"] = data.display
        if (serviceAPI.getDisplay(item, data) == 0) {
            result["Result"] = false
            result["Display.Result"] = false
            return result
        } else result["Display.Result"] = true

        val tableCondition = section.getString("Condition.Table", "")!!
        result["Table.Name"] = tableCondition
        if (tableCondition.isNotEmpty() && table != tableCondition) {
            result["Result"] = false
            result["Table.Result"] = false
            return result
        } else result["Table.Result"] = true

        val ketherCondition = section.getStringList("Condition.Kether")
        if (ketherCondition.isNotEmpty() && !ketherCondition.all { it.evalKether(player).toString().toBoolean() }) {
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