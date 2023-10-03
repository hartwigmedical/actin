package com.hartwig.actin.trial.trial.config

interface TrialConfigFactory<T : TrialConfig> {
    fun create(fields: Map<String, Int>, parts: List<String>): T
}