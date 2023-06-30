package com.hartwig.actin.clinical.curation.config

class PeriodBetweenUnitConfigFactory : CurationConfigFactory<PeriodBetweenUnitConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): PeriodBetweenUnitConfig {
        return PeriodBetweenUnitConfig(
            input = parts[fields["input"]!!],
            interpretation = parts[fields["interpretation"]!!]
        )
    }
}