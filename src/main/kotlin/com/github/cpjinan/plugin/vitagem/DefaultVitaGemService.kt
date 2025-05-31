package com.github.cpjinan.plugin.vitagem

import com.github.cpjinan.plugin.vitagem.VitaGem.plugin
import com.github.cpjinan.plugin.vitagem.data.GemConfigData
import com.github.cpjinan.plugin.vitagem.data.TableConfigData
import com.github.cpjinan.plugin.vitagem.gui.DefaultInventory
import com.github.cpjinan.plugin.vitagem.utils.FileUtils.releaseResource
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
    override fun getGemData(item: ItemStack): List<GemConfigData> {
        return gemConfigDataMap.values.filter { gemConfigData ->
            isItemGem(item, gemConfigData)
        }
    }

    /** 物品是否为宝石 **/
    override fun isItemGem(item: ItemStack, data: GemConfigData): Boolean {
        return Arim.itemMatch.match(item, data.item)
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