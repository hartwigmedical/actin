package com.hartwig.actin.clinical.curation.config

class PeriodBetweenUnitConfigFactory : CurationConfigFactory<PeriodBetweenUnitConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<PeriodBetweenUnitConfig> {
        return ValidatedCurationConfig(
            PeriodBetweenUnitConfig(
                input = parts[fields["input"]!!],
                interpretation = parts[fields["interpretation"]!!]
            )
        )
    }
}