package com.github.cpjinan.plugin.vitagem.command

import com.github.cpjinan.plugin.vitagem.VitaGem
import com.github.cpjinan.plugin.vitagem.VitaGemSettings
import com.github.cpjinan.plugin.vitagem.event.PluginReloadEvent
import com.github.cpjinan.plugin.vitagem.utils.LoggerUtils.debug
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.util.isConsole
import taboolib.expansion.createHelper
import taboolib.module.lang.sendLang

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.command
 *
 * @author 季楠
 * @since 2025/5/25 00:54
 */
@CommandHeader(
    name = "vitagem",
    aliases = ["gem"],
    permission = "VitaGem.command.use",
    permissionDefault = PermissionDefault.OP
)
object MainCommand {
    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody(
        permission = "VitaGem.command.open.use",
        permissionDefault = PermissionDefault.OP
    )
    val open = subCommand {
        dynamic("table") {
            suggest { VitaGem.api().getService().tableConfigDataMap.keys.toList() }
            execute<ProxyCommandSender> { sender, context, _ ->
                if (sender.isConsole()) {
                    sender.sendLang("Error-Not-Player")
                    return@execute
                }

                if (!sender.hasPermission("vitagem.open.*") && !sender.hasPermission("vitagem.open.${context["table"]}")) {
                    sender.sendLang("Error-No-Permission")
                }

                VitaGem.api().getService().openGUI(sender.cast(), context["table"])
            }
        }
    }

    @CommandBody(
        permission = "VitaGem.command.reload.use",
        permissionDefault = PermissionDefault.OP
    )
    val reload = subCommand {
        execute { sender: ProxyCommandSender, _: CommandContext<ProxyCommandSender>, _: String ->
            debug("&8[&3Vita&bGem&8] &5调试&7#1 &8| &6触发插件重载命令，正在展示处理逻辑。")

            PluginReloadEvent.Pre().call()
            val start = System.currentTimeMillis()
            var time = start

            VitaGemSettings.settings.reload()
            debug("&r| &b◈ &r#1 配置文件重载完成，用时 ${System.currentTimeMillis() - time}ms。")
            time = System.currentTimeMillis()

            val languageAPI = VitaGem.api().getLanguage()
            languageAPI.reload()
            debug("&r| &b◈ &r#1 语言文件重载完成，用时 ${System.currentTimeMillis() - time}ms。")
            time = System.currentTimeMillis()

            val serviceAPI = VitaGem.api().getService()
            serviceAPI.reload()
            debug("&r| &b◈ &r#1 业务逻辑重载完成，共加载 ${serviceAPI.gemConfigDataMap.size} 个宝石、${serviceAPI.tableConfigDataMap.size} 个界面，用时 ${System.currentTimeMillis() - time}ms。")

            debug("&r| &a◈ &r#1 插件重载完毕，总计用时 ${System.currentTimeMillis() - start}ms。")

            PluginReloadEvent.Post().call()
            sender.sendLang("Plugin-Reloaded")
        }
    }
}