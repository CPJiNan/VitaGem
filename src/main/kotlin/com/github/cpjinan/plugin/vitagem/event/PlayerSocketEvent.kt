package com.github.cpjinan.plugin.vitagem.event

import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.event
 *
 * @author 季楠
 * @since 2025/6/1 12:12
 */
class PlayerSocketEvent(
    val player: Player,
    val result: Map<String, Any>
) : BukkitProxyEvent()