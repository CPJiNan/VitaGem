package com.github.cpjinan.plugin.vitagem.listener

import com.github.cpjinan.plugin.vitagem.event.PlayerExtractEvent
import com.github.cpjinan.plugin.vitagem.event.PlayerSocketEvent
import com.github.cpjinan.plugin.vitagem.utils.LoggerUtils.debug
import taboolib.common.platform.event.SubscribeEvent

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.listener
 *
 * @author 季楠
 * @since 2025/6/2 20:00
 */
object PlayerListener {
    @SubscribeEvent
    fun onPlayerSocket(event: PlayerSocketEvent) {
        val result = event.result.toMutableMap()
        result.remove("Data")
        result.remove("Item.ItemStack")
        result.remove("Gem.ItemStack")
        debug("&8[&3Vita&bGem&8] &5调试&7#2 &8| &6触发玩家镶嵌事件，正在展示处理逻辑。")
        debug("&r| &b◈ &r#2 玩家名称: ${event.player.name}。")
        debug("&r| &b◈ &r#2 镶嵌结果: ${result}。")
        debug("&r| &b◈ &r#2 事件处理完毕。")
    }

    @SubscribeEvent
    fun onPlayerExtract(event: PlayerExtractEvent) {
        val result = event.result.toMutableMap()
        result.remove("Data")
        result.remove("Item.ItemStack")
        result.remove("Gem.ItemStack")
        debug("&8[&3Vita&bGem&8] &5调试&7#3 &8| &6触发玩家拆卸事件，正在展示处理逻辑。")
        debug("&r| &b◈ &r#3 玩家名称: ${event.player.name}。")
        debug("&r| &b◈ &r#3 拆卸结果: ${event.result}。")
        debug("&r| &b◈ &r#3 事件处理完毕。")
    }
}