package com.hartwig.actin.trial.config

interface TrialConfigFactory<T : TrialConfig> {

    fun create(fields: Map<String, Int>, parts: List<String>): T
}