package com.github.cpjinan.plugin.vitagem.gui

import com.github.cpjinan.plugin.vitagem.VitaGem
import com.github.cpjinan.plugin.vitagem.utils.KetherUtils.evalKether
import com.github.cpjinan.plugin.vitagem.utils.RandomUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import taboolib.common5.util.replace
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
                        "Socket.Symbol.Item" to tableSection.getString("Socket.Symbol.Item", "I")!!,
                        "Socket.Symbol.Gem" to tableSection.getString("Socket.Symbol.Gem", "G")!!
                    )
                }

                "Extract" -> {
                    hashMapOf(
                        "Extract.Symbol.Item" to tableSection.getString("Extract.Symbol.Item", "I")!!,
                        "Extract.Symbol.Gem" to tableSection.getString("Extract.Symbol.Gem", "G")!!
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
                                    getSlots(tableOptions["Socket.Symbol.Item"]!![0])[0],
                                    getSlots(tableOptions["Socket.Symbol.Gem"]!![0])[0]
                                )
                                section.getStringList("Kether")
                                    .replace(
                                        "%Result%" to (result["Result"] ?: "false"),
                                        "%Enable%" to (result["Enable"] ?: "true"),
                                        "%Slot%" to (result["Slot"] ?: "true"),
                                        "%Table%" to (result["Table"] ?: "true"),
                                        "%Kether%" to (result["Kether"] ?: "true"),
                                        "%Chance.Result%" to (result["Chance.Result"] ?: "true"),
                                        "%Chance.Amount%" to (result["Chance.Amount"] ?: "1.0"),
                                        "%Money.Enough%" to (result["Money.Enough"] ?: "true"),
                                        "%Money.Amount%" to (result["Money.Amount"] ?: "0.0"),
                                        "%Point.Enough%" to (result["Point.Enough"] ?: "true"),
                                        "%Point.Amount%" to (result["Point.Amount"] ?: "0")
                                    )
                                    .evalKether(player)
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
                            addAll(getSlots(tableOptions["Socket.Symbol.Item"]!![0]))
                            addAll(getSlots(tableOptions["Socket.Symbol.Gem"]!![0]))
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

    /** 出售按钮 **/
    fun socketButton(
        player: Player,
        table: String,
        inv: Inventory,
        itemSlot: Int,
        gemSlot: Int
    ): Map<String, Any> {
        var resultMap = hashMapOf<String, Any>("Result" to false)
        val item = inv.getItem(itemSlot)
        if (item == null || item.isAir || item.type == Material.AIR) {
            resultMap["Item"] = false
            player.sendLang("Socket-No-Item")
            return resultMap
        }
        val gemItem = inv.getItem(gemSlot)
        if (gemItem == null || item.isAir || item.type == Material.AIR) {
            resultMap["Gem"] = false
            player.sendLang("Socket-No-Gem")
            return resultMap
        }
        val serviceAPI = VitaGem.api().getService()
        val hookAPI = VitaGem.api().getHook()

        val gemConfig = serviceAPI.getGem(gemItem).filter {
            val gemTable = it.section.getString("Condition.Table", "")!!
            gemTable.isEmpty() || table == gemTable
        }[0]
        val section = gemConfig.socketSection

        resultMap = serviceAPI.isSocketConditionMet(player, item, gemConfig, table) as HashMap<String, Any>
        val result = (resultMap["Result"] ?: "false").toString().toBoolean()
        val enableResult = (resultMap["Enable"] ?: "true").toString().toBoolean()
        val slotResult = (resultMap["Slot"] ?: "true").toString().toBoolean()
        val tableResult = (resultMap["Table"] ?: "true").toString().toBoolean()
        val ketherResult = (resultMap["Kether"] ?: "true").toString().toBoolean()
        val moneyEnoughResult = (resultMap["Money.Enough"] ?: "true").toString().toBoolean()
        val moneyAmountResult = (resultMap["Money.Amount"] ?: "0.0").toString().toDouble()
        val pointEnoughResult = (resultMap["Point.Enough"] ?: "true").toString().toBoolean()
        val pointAmountResult = (resultMap["Point.Amount"] ?: "0").toString().toInt()

        if (result) {
            val chance = Arim.fixedCalculator.evaluate(section.getString("Chance", "1.0")!!.replacePlaceholder(player))
            if (RandomUtils.randomBoolean(chance)) {
                resultMap["Chance.Result"] = true
                resultMap["Chance.Amount"] = chance

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
                    .takeMoney(player, moneyAmountResult)
                if (!gemConfig.socketSection.getBoolean("Return.Success.Point", false)) hookAPI.getPlayerPoints()
                    .takePoint(player, pointAmountResult)
            } else {
                resultMap["Chance.Result"] = false
                resultMap["Chance.Amount"] = chance

                player.sendLang("Socket-Fail")

                if (!gemConfig.socketSection.getBoolean("Return.Fail.Item", true)) inv.setItem(itemSlot, null)
                if (!gemConfig.socketSection.getBoolean("Return.Fail.Slot", false)) inv.setItem(gemSlot, null)
                if (!gemConfig.socketSection.getBoolean("Return.Fail.Money", false)) hookAPI.getVault()
                    .takeMoney(player, moneyAmountResult)
                if (!gemConfig.socketSection.getBoolean("Return.Fail.Point", false)) hookAPI.getPlayerPoints()
                    .takePoint(player, pointAmountResult)
            }
        } else {
            if (!enableResult) player.sendLang("Socket-Disable")
            if (!slotResult) player.sendLang("Socket-No-Slot")
            if (!tableResult) player.sendLang("Socket-Table-Not-Match")
            if (!ketherResult) player.sendLang("Error-Condition-Not-Met")
            if (!moneyEnoughResult) player.sendLang(
                "Error-Money-Not-Enough",
                moneyAmountResult,
                hookAPI.getVault().lookMoney(player)
            )
            if (!pointEnoughResult) player.sendLang(
                "Error-Money-Not-Enough",
                pointAmountResult,
                hookAPI.getPlayerPoints().lookPoint(player)
            )
        }

        return resultMap
    }
}