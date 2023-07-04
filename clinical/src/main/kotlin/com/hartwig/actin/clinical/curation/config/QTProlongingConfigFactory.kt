package com.hartwig.actin.clinical.curation.config

class QTProlongingConfigFactory : CurationConfigFactory<QTProlongingConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): QTProlongingConfig {
        return QTProlongingConfig(parts[fields["Name"]!!], false)
    }
}