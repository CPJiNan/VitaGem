package com.github.cpjinan.plugin.vitagem.data

import taboolib.library.configuration.ConfigurationSection

/**
 * VitaGem
 * com.github.cpjinan.plugin.vitagem.data
 *
 * @author 季楠
 * @since 2025/5/24 13:11
 */
data class TableConfigData(
    val id: String,
    val title: String,
    val layout: List<String>,
    val icon: ConfigurationSection,
    val section: ConfigurationSection
)