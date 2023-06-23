package com.hartwig.actin.clinical.curation.config

data class CurationConfigFactoryResult<T : CurationConfig>(val configs: List<T>, val inputs: Set<String>) {
    operator fun plus(other: CurationConfigFactoryResult<T>): CurationConfigFactoryResult<T> {
        return CurationConfigFactoryResult(configs + other.configs, inputs + other.inputs)
    }
}
