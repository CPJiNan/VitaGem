@file:Suppress("DEPRECATION")

package com.github.cpjinan.plugin.vitagem.command.subcommand

import com.github.cpjinan.plugin.vitagem.VitaGem
import com.github.cpjinan.plugin.vitagem.gui.DefaultInventory
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandContext
import taboolib.common.platform.command.subCommand

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.command.subcommand
 *
 * @author 季楠
 * @since 2025/5/31 14:06
 */
object APICommand {
    val api = subCommand {
        // 业务逻辑接口
        literal("service") {
            val serviceAPI = VitaGem.api().getService()
            // 宝石配置
            literal("gemConfigData").dynamic("id") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    sender.sendMessage(
                        serviceAPI.gemConfigDataMap[context["id"]].toString()
                    )
                }
            }
            // 界面配置
            literal("tableConfigData").dynamic("id") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    sender.sendMessage(
                        serviceAPI.tableConfigDataMap[context["id"]].toString()
                    )
                }
            }
            // 打开界面
            literal("openGUI").dynamic("table") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    serviceAPI.openGUI(sender.cast(), context["table"])
                }
            }
            // 获取宝石配置
            literal("getGem") {
                execute<ProxyCommandSender> { sender, _, _ ->
                    sender.sendMessage(serviceAPI.getGem(sender.cast<Player>().itemInHand).toString())
                }
            }
            // 获取镶嵌槽位列表
            literal("getSlot") {
                execute<ProxyCommandSender> { sender, _, _ ->
                    sender.sendMessage(serviceAPI.getSlot().toString())
                }
            }
            // 获取物品镶嵌槽位数量
            literal("getItemSlot") {
                execute<ProxyCommandSender> { sender, _, _ ->
                    sender.sendMessage(serviceAPI.getSlot(sender.cast<Player>().itemInHand).toString())
                }
            }
            // 获取宝石槽位列表
            literal("getDisplay") {
                execute<ProxyCommandSender> { sender, _, _ ->
                    sender.sendMessage(serviceAPI.getDisplay().toString())
                }
            }
            // 获取物品宝石槽位数量
            literal("getItemDisplay") {
                execute<ProxyCommandSender> { sender, _, _ ->
                    sender.sendMessage(serviceAPI.getDisplay(sender.cast<Player>().itemInHand).toString())
                }
            }
            // 是否满足镶嵌条件
            literal("isSocketConditionMet").dynamic("data").dynamic("table") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    val player = sender.cast<Player>()
                    sender.sendMessage(
                        DefaultInventory.isSocketConditionMet(
                            player,
                            player.itemInHand,
                            serviceAPI.gemConfigDataMap[context["data"]]!!,
                            context["table"]
                        ).toString()
                    )
                }
            }
            // 是否满足拆卸条件
            literal("isExtractConditionMet").dynamic("data").dynamic("table") {
                execute<ProxyCommandSender> { sender, context, _ ->
                    val player = sender.cast<Player>()
                    sender.sendMessage(
                        DefaultInventory.isExtractConditionMet(
                            player,
                            player.itemInHand,
                            serviceAPI.gemConfigDataMap[context["data"]]!!,
                            context["table"]
                        ).toString()
                    )
                }
            }
            // 重载
            literal("reload") {
                execute<ProxyCommandSender> { _, _, _ ->
                    serviceAPI.reload()
                }
            }
        }
        // 语言拓展接口
        literal("language") {
            val languageAPI = VitaGem.api().getLanguage()
            // 发送语言文本
            literal("sendLang").dynamic("key") {
                execute<ProxyCommandSender> { sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, _: String ->
                    sender.castSafely<CommandSender>()?.let { languageAPI.sendLang(it, context["key"]) }
                }
            }
            // 获取语言文本
            literal("getLang").dynamic("key") {
                execute<ProxyCommandSender> { sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, _: String ->
                    sender.castSafely<CommandSender>()
                        ?.let { sender.sendMessage(languageAPI.getLang(it, context["key"]).toString()) }
                }
            }
            // 获取语言文本
            literal("getLangList").dynamic("key") {
                execute<ProxyCommandSender> { sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, _: String ->
                    sender.castSafely<CommandSender>()
                        ?.let { sender.sendMessage(languageAPI.getLangList(it, context["key"]).toString()) }
                }
            }
            // 释放 i18n 资源
            literal("releaseResource") {
                execute<ProxyCommandSender> { _: ProxyCommandSender, _: CommandContext<ProxyCommandSender>, _: String ->
                    languageAPI.releaseResource()
                }
            }
            // 重载语言文件
            literal("reload") {
                execute<ProxyCommandSender> { _: ProxyCommandSender, _: CommandContext<ProxyCommandSender>, _: String ->
                    languageAPI.reload()
                }
            }
        }
    }
}