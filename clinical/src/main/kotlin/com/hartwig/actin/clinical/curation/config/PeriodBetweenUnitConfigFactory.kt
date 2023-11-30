package com.hartwig.actin.clinical.curation.config

class PeriodBetweenUnitConfigFactory : CurationConfigFactory<PeriodBetweenUnitConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): CurationConfigValidatedResponse<PeriodBetweenUnitConfig> {
        return CurationConfigValidatedResponse(
            PeriodBetweenUnitConfig(
                input = parts[fields["input"]!!],
                interpretation = parts[fields["interpretation"]!!]
            )
        )
    }
}