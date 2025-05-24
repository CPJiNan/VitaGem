package com.github.cpjinan.plugin.vitagem.command

import com.github.cpjinan.plugin.vitagem.VitaGem
import com.github.cpjinan.plugin.vitagem.VitaGemSettings
import com.github.cpjinan.plugin.vitagem.event.PluginReloadEvent
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
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
            execute<ProxyCommandSender> { sender: ProxyCommandSender, context: CommandContext<ProxyCommandSender>, _: String ->
                if (sender.hasPermission("vitagem.open.*") || sender.hasPermission("vitagem.open.${context["table"]}")) {
                    val serviceAPI = VitaGem.api().getService()
                    sender.castSafely<Player>()?.let { serviceAPI.openGUI(it, context["table"]) }
                } else sender.sendLang("Error-No-Permission")
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