package com.github.cpjinan.plugin.vitagem.gui

import com.github.cpjinan.plugin.vitagem.VitaGem
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem
import taboolib.platform.util.sendLang

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
                    // 关闭界面
                    "Close", "VitaGem:Close" -> {
                        set(icon[0], item) {
                            clickEvent().isCancelled = true
                            player.closeInventory()
                        }
                    }

                    else -> {
                        set(icon[0], item) {
                            clickEvent().isCancelled = true
                        }
                    }
                }
            }

            onClose { event: InventoryCloseEvent ->
                getSlots(' ').forEach { slot ->
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
    ) {
        val item = inv.getItem(itemSlot) ?: return
        val gemItem = inv.getItem(gemSlot) ?: return
        val serviceAPI = VitaGem.api().getService()
        val hookAPI = VitaGem.api().getHook()

        val gemConfig = serviceAPI.getGem(gemItem).filter {
            val gemTable = it.section.getString("Condition.Table", "")!!
            gemTable.isNotEmpty() || table == gemTable
        }[0]

        val resultMap = serviceAPI.isSocketConditionMet(player, item, gemConfig, table)
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

        } else {
            if (!enableResult) player.sendLang("Socket-Not-Enable")
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

    }
}