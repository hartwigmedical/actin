package com.hartwig.actin.doid.config

data class DoidManualConfig(
    val mainCancerDoids: Set<String>,
    val adenoSquamousMappings: Set<AdenoSquamousMapping>,
    val additionalDoidsPerDoid: Map<String, String>
)
