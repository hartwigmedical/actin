package com.hartwig.actin.clinical.curation.config

class InfectionConfigFactory : CurationConfigFactory<InfectionConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<InfectionConfig> {
        return ValidatedCurationConfig(
            InfectionConfig(
                input = parts[fields["input"]!!],
                interpretation = parts[fields["interpretation"]!!]
            )
        )
    }
}