package com.github.cpjinan.plugin.vitagem.data

import taboolib.library.configuration.ConfigurationSection

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.data
 *
 * @author 季楠
 * @since 2025/5/31 09:33
 */
data class GemConfigData(
    val id: String,
    val slot: String,
    val display: String,
    val attribute: List<String>,
    val socketSection: ConfigurationSection,
    val extractSection: ConfigurationSection,
    val section: ConfigurationSection
)
