package com.github.cpjinan.plugin.vitagem

import com.github.cpjinan.plugin.vitagem.VitaGem.plugin
import com.github.cpjinan.plugin.vitagem.data.GemConfigData
import com.github.cpjinan.plugin.vitagem.data.TableConfigData
import com.github.cpjinan.plugin.vitagem.gui.DefaultInventory
import com.github.cpjinan.plugin.vitagem.utils.FileUtils.releaseResource
import com.github.cpjinan.plugin.vitagem.utils.KetherUtils.evalKether
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.module.configuration.Type
import top.maplex.arim.Arim
import top.maplex.arim.tools.folderreader.readFolderWalkConfig
import java.io.File

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem
 *
 * @author 季楠
 * @since 2025/5/25 00:40
 */
object DefaultVitaGemService : VitaGemService {
    override val gemConfigDataMap: HashMap<String, GemConfigData> = hashMapOf()
    override val tableConfigDataMap: HashMap<String, TableConfigData> = hashMapOf()

    init {
        reload()
    }

    /** 重载业务配置 **/
    override fun reload() {
        gemConfigDataMap.clear()
        readFolderWalkConfig(File("./plugins/VitaGem/gem")) {
            setReadType(Type.YAML)
            walk {
                getKeys(false).forEach { id ->
                    val item = getString("$id.Item") ?: ""
                    val slot = getString("$id.Slot") ?: "「」"
                    val display = getString("$id.Display") ?: "「」"
                    val attribute = getStringList("$id.Attribute")
                    val socketSection = getConfigurationSection("$id.Socket")!!
                    val extractSection = getConfigurationSection("$id.Extract")!!
                    val section = getConfigurationSection(id)!!
                    gemConfigDataMap[id] = GemConfigData(
                        id = id,
                        item = item,
                        slot = slot,
                        display = display,
                        attribute = attribute,
                        socketSection = socketSection,
                        extractSection = extractSection,
                        section = section
                    )
                }
            }
        }
        tableConfigDataMap.clear()
        readFolderWalkConfig(File("./plugins/VitaGem/table")) {
            setReadType(Type.YAML)
            walk {
                getKeys(false).forEach { id ->
                    val title = getString("$id.Title") ?: ""
                    val layout = getStringList("$id.Layout")
                    val icon = getConfigurationSection("$id.Icon")!!
                    val section = getConfigurationSection(id)!!
                    tableConfigDataMap[id] = TableConfigData(
                        id = id,
                        title = title,
                        layout = layout,
                        icon = icon,
                        section = section
                    )
                }
            }
        }
    }

    /** 打开界面 **/
    override fun openGUI(player: Player, table: String) {
        DefaultInventory.openGUI(player, table)
    }

    /** 获取宝石配置 **/
    override fun getGem(item: ItemStack): List<GemConfigData> {
        return gemConfigDataMap.values.filter { gemConfigData ->
            isGem(item, gemConfigData)
        }
    }

    /** 物品是否为宝石 **/
    override fun isGem(item: ItemStack, data: GemConfigData): Boolean {
        return Arim.itemMatch.match(item, data.item)
    }

    /** 获取所有镶嵌槽位 **/
    override fun getSlot(): List<String> {
        return gemConfigDataMap.values.map { it.slot }
    }

    /** 物品的所有镶嵌槽位数量 **/
    override fun getSlot(item: ItemStack): Map<GemConfigData, Int> {
        return gemConfigDataMap.values.associateWith {
            getSlot(item, it)
        }
    }

    /** 物品的指定镶嵌槽位数量 **/
    override fun getSlot(item: ItemStack, data: GemConfigData): Int {
        return item.itemMeta.lore.count {
            it.contains(data.slot)
        }
    }

    /** 获取所有宝石槽位 **/
    override fun getDisplay(): List<String> {
        return gemConfigDataMap.values.map { it.display }
    }

    /** 物品的所有宝石槽位数量 **/
    override fun getDisplay(item: ItemStack): Map<GemConfigData, Int> {
        return gemConfigDataMap.values.associateWith {
            getDisplay(item, it)
        }
    }

    /** 物品的指定宝石槽位数量 **/
    override fun getDisplay(item: ItemStack, data: GemConfigData): Int {
        return item.itemMeta.lore.count {
            it.contains(data.display)
        }
    }

    /** 是否满足镶嵌条件 **/
    override fun isSocketConditionMet(
        player: Player,
        item: ItemStack,
        data: GemConfigData,
        table: String
    ): Map<String, Any> {
        val section = data.socketSection
        val map = hashMapOf<String, Any>("Result" to true)

        if (!section.getBoolean("Enable")) {
            map["Result"] = false
            map["Enable"] = true
            return map
        }

        if (getSlot(item, data) == 0) {
            map["Result"] = false
            map["Slot"] = true
            return map
        }

        val tableList = section.getStringList("Condition.Table")
        if (tableList.isNotEmpty() && table !in tableList) {
            map["Result"] = false
            map["Table"] = true
            return map
        }

        if (!section.getStringList("Condition.Kether").all { it.evalKether(player).toString().toBoolean() }) {
            map["Result"] = false
            map["Kether"] = true
            return map
        }

        val hookAPI = VitaGem.api().getHook()

        val money = section.getDouble("Money")
        if (hookAPI.getVault().isPluginEnabled() &&
            !hookAPI.getVault().isMoneyEnough(player, money)
        ) {
            map["Result"] = false
            map["Money"] = true
            map["MoneyAmount"] = money
        }

        val point = section.getInt("Point")
        if (hookAPI.getPlayerPoints().isPluginEnabled() &&
            !hookAPI.getPlayerPoints().isPointEnough(player, point)
        ) {
            map["Result"] = false
            map["Point"] = true
            map["PointAmount"] = point
        }

        return map
    }

    @Awake(LifeCycle.CONST)
    fun onConst() {
        PlatformFactory.registerAPI<VitaGemService>(DefaultVitaGemService)
    }

    @Awake(LifeCycle.ENABLE)
    fun onEnable() {
        plugin.releaseResource(
            "gem/Example.yml"
        )
        plugin.releaseResource(
            "table/Socket.yml"
        )
        plugin.releaseResource(
            "table/Extract.yml"
        )
        reload()
    }
}