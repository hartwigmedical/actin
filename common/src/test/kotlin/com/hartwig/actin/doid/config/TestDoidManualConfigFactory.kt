package com.hartwig.actin.doid.config

object TestDoidManualConfigFactory {

    fun createMinimalTestDoidManualConfig(): DoidManualConfig {
        return DoidManualConfig(
            mainCancerDoids = emptySet(),
            adenoSquamousMappings = emptySet(),
            additionalDoidsPerDoid = emptyMap(),
            childToParentRelationshipsToExclude = emptySet()
        )
    }

    fun createWithOneMainCancerDoid(mainCancerDoid: String): DoidManualConfig {
        return createMinimalTestDoidManualConfig().copy(mainCancerDoids = setOf(mainCancerDoid))
    }

    fun createWithOneAdenoSquamousMapping(mapping: AdenoSquamousMapping): DoidManualConfig {
        return createMinimalTestDoidManualConfig().copy(adenoSquamousMappings = setOf(mapping))
    }

    fun createWithOneAdditionalDoid(baseDoid: String, expandedDoid: String): DoidManualConfig {
        return createMinimalTestDoidManualConfig().copy(additionalDoidsPerDoid = mapOf(baseDoid to expandedDoid))
    }
}
