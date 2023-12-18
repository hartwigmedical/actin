package com.hartwig.actin.doid.config

object TestDoidManualConfigFactory {
    @JvmStatic
    fun createMinimalTestDoidManualConfig(): DoidManualConfig {
        return ImmutableDoidManualConfig.builder().build()
    }

    @JvmStatic
    fun createWithOneMainCancerDoid(mainCancerDoid: String): DoidManualConfig {
        return ImmutableDoidManualConfig.builder().addMainCancerDoids(mainCancerDoid).build()
    }

    @JvmStatic
    fun createWithOneAdenoSquamousMapping(mapping: AdenoSquamousMapping): DoidManualConfig {
        return ImmutableDoidManualConfig.builder().addAdenoSquamousMappings(mapping).build()
    }

    @JvmStatic
    fun createWithOneAdditionalDoid(baseDoid: String, expandedDoid: String): DoidManualConfig {
        return ImmutableDoidManualConfig.builder().putAdditionalDoidsPerDoid(baseDoid, expandedDoid).build()
    }
}
