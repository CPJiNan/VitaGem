package com.github.cpjinan.plugin.vitagem

import org.bukkit.command.CommandSender

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem
 *
 * @author 季楠
 * @since 2025/5/10 22:55
 */
interface VitaGemLanguage {
    /** 发送语言文本 **/
    fun sendLang(sender: CommandSender, key: String, vararg args: Any)

    /** 获取语言文本 **/
    fun getLang(sender: CommandSender, key: String, vararg args: Any): String?

    /** 获取语言文本 **/
    fun getLangList(sender: CommandSender, key: String, vararg args: Any): List<String>

    /** 释放 i18n 资源 **/
    fun releaseResource()

    /** 重载语言文件 **/
    fun reload()
}