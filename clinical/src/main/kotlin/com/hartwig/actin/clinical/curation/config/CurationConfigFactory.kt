package com.hartwig.actin.clinical.curation.config

interface CurationConfigFactory<T : CurationConfig> {
    fun create(fields: Map<String, Int>, parts: Array<String>): CurationConfigValidatedResponse<T>
}