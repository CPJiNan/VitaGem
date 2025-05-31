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

    /** 获取镶嵌槽位列表 **/
    override fun getSlot(): List<String> {
        return gemConfigDataMap.values.map { it.slot }
    }

    /** 获取物品镶嵌槽位数量 **/
    override fun getSlot(item: ItemStack): Map<GemConfigData, Int> {
        return gemConfigDataMap.values.associateWith {
            getSlot(item, it)
        }
    }

    /** 获取物品镶嵌槽位数量 **/
    override fun getSlot(item: ItemStack, data: GemConfigData): Int {
        return item.itemMeta?.lore?.count {
            it.contains(data.slot)
        } ?: 0
    }

    /** 获取宝石槽位列表 **/
    override fun getDisplay(): List<String> {
        return gemConfigDataMap.values.map { it.display }
    }

    /** 获取物品宝石槽位数量 **/
    override fun getDisplay(item: ItemStack): Map<GemConfigData, Int> {
        return gemConfigDataMap.values.associateWith {
            getDisplay(item, it)
        }
    }

    /** 获取物品宝石槽位数量 **/
    override fun getDisplay(item: ItemStack, data: GemConfigData): Int {
        return item.itemMeta?.lore?.count {
            it.contains(data.display)
        } ?: 0
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

        if (!section.getBoolean("Enable", false)) {
            map["Result"] = false
            map["Enable"] = false
            return map
        }

        if (getSlot(item, data) == 0) {
            map["Result"] = false
            map["Slot"] = false
            return map
        }

        val tableCondition = section.getString("Condition.Table", "")!!
        if (tableCondition.isNotEmpty() && table != tableCondition) {
            map["Result"] = false
            map["Table"] = false
            return map
        }

        val ketherCondition = section.getStringList("Condition.Kether")
        if (!ketherCondition.all { it.evalKether(player).toString().toBoolean() }) {
            map["Result"] = false
            map["Kether"] = false
            return map
        }

        val hookAPI = VitaGem.api().getHook()

        val money = section.getDouble("Money", 0.0)
        if (hookAPI.getVault().isPluginEnabled() &&
            !hookAPI.getVault().isMoneyEnough(player, money)
        ) {
            map["Result"] = false
            map["Money.Enough"] = false
            map["Money.Amount"] = money
        }

        val point = section.getInt("Point", 0)
        if (hookAPI.getPlayerPoints().isPluginEnabled() &&
            !hookAPI.getPlayerPoints().isPointEnough(player, point)
        ) {
            map["Result"] = false
            map["Point.Enough"] = false
            map["Point.Amount"] = point
        }

        return map
    }

    /** 是否满足拆卸条件 **/
    override fun isExtractConditionMet(
        player: Player,
        item: ItemStack,
        data: GemConfigData,
        table: String
    ): Map<String, Any> {
        val section = data.extractSection
        val map = hashMapOf<String, Any>("Result" to true)

        if (!section.getBoolean("Enable", false)) {
            map["Result"] = false
            map["Enable"] = false
            return map
        }

        if (getDisplay(item, data) == 0) {
            map["Result"] = false
            map["Display"] = false
            return map
        }

        val tableCondition = section.getString("Condition.Table", "")!!
        if (tableCondition.isNotEmpty() && table != tableCondition) {
            map["Result"] = false
            map["Table"] = false
            return map
        }

        val ketherCondition = section.getStringList("Condition.Kether")
        if (!ketherCondition.all { it.evalKether(player).toString().toBoolean() }) {
            map["Result"] = false
            map["Kether"] = false
            return map
        }

        val hookAPI = VitaGem.api().getHook()

        val money = section.getDouble("Money", 0.0)
        if (hookAPI.getVault().isPluginEnabled() &&
            !hookAPI.getVault().isMoneyEnough(player, money)
        ) {
            map["Result"] = false
            map["Money.Enough"] = false
            map["Money.Amount"] = money
        }

        val point = section.getInt("Point", 0)
        if (hookAPI.getPlayerPoints().isPluginEnabled() &&
            !hookAPI.getPlayerPoints().isPointEnough(player, point)
        ) {
            map["Result"] = false
            map["Point.Enough"] = false
            map["Point.Amount"] = point
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