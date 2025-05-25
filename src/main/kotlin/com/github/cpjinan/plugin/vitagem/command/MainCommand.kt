package com.github.cpjinan.plugin.vitagem.command

import com.github.cpjinan.plugin.vitagem.VitaGem
import com.github.cpjinan.plugin.vitagem.VitaGemSettings
import com.github.cpjinan.plugin.vitagem.event.PluginReloadEvent
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
            PluginReloadEvent.Pre().call()

            VitaGemSettings.settings.reload()

            val languageAPI = VitaGem.api().getLanguage()
            languageAPI.reload()

            val serviceAPI = VitaGem.api().getService()
            serviceAPI.reload()

            PluginReloadEvent.Post().call()
            sender.sendLang("Plugin-Reloaded")
        }
    }
}