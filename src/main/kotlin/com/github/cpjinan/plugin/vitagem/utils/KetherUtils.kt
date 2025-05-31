package com.github.cpjinan.plugin.vitagem.utils

import taboolib.common.platform.function.adaptPlayer
import taboolib.common5.util.replace
import taboolib.module.kether.KetherShell
import taboolib.module.kether.ScriptOptions
import taboolib.module.kether.printKetherErrorMessage
import taboolib.platform.BukkitPlugin

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.utils
 *
 * @author 季楠
 * @since 2025/5/31 13:47
 */
object KetherUtils {
    @JvmStatic
    fun String.evalKether(
        sender: Any,
        namespace: List<String> = listOf(BukkitPlugin.getInstance().name),
        args: Map<String, Any>? = null,
    ): Any? {
        return eval(listOf(this), sender, namespace, args)
    }

    @JvmStatic
    fun List<String>.evalKether(
        sender: Any,
        namespace: List<String> = listOf(BukkitPlugin.getInstance().name),
        args: Map<String, Any>? = null,
    ): Any? {
        return if (this.isNotEmpty()) eval(this, sender, namespace, args)
        else null
    }

    private fun eval(
        script: List<String>,
        sender: Any,
        namespace: List<String> = listOf(BukkitPlugin.getInstance().name),
        args: Map<String, Any>? = null,
    ): Any? {
        var scriptList = script
        args?.forEach { (k, v) -> scriptList = scriptList.replace(Pair(k, v)) }
        try {
            return KetherShell.eval(
                scriptList,
                ScriptOptions.builder().namespace(namespace).sender(adaptPlayer(sender)).build()
            ).thenApply { it }.get()
        } catch (e: Exception) {
            e.printKetherErrorMessage()
            return null
        }
    }
}