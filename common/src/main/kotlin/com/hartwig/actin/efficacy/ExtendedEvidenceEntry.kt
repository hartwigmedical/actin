package com.hartwig.actin.efficacy

import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

data class ExtendedEvidenceEntry(
    val acronym: String,
    val phase: String?,
    val therapies: List<Therapy>,
    val therapeuticSetting: Intent?,
    val variantRequirements: List<VariantRequirement>,
    val trialReferences: List<TrialReference> // you can have multiple papers per trial
)