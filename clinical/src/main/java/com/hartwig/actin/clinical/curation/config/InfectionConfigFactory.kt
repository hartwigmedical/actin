package com.hartwig.actin.clinical.curation.config

class InfectionConfigFactory : CurationConfigFactory<InfectionConfig> {
    override fun create(fields: Map<String?, Int?>, parts: Array<String>): InfectionConfig {
        return ImmutableInfectionConfig.builder()
            .input(parts[fields["input"]!!])
            .interpretation(parts[fields["interpretation"]!!])
            .build()
    }
}