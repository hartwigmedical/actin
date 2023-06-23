package com.hartwig.actin.treatment.trial.config

data class InclusionCriteriaReferenceConfig(
    override val trialId: String,
    val referenceId: String,
    val referenceText: String
) : TrialConfig