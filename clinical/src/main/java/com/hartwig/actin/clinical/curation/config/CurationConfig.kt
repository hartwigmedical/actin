package com.hartwig.actin.clinical.curation.config

interface CurationConfig {
    fun input(): String
    fun ignore(): Boolean
}