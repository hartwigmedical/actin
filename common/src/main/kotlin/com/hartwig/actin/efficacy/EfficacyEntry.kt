package com.hartwig.actin.efficacy

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

data class EfficacyEntry(
    val acronym: String,
    val phase: String?,
    val treatments: List<Treatment>,
    val therapeuticSetting: Intent?,
    val variantRequirements: List<VariantRequirement>,
    val trialReferences: List<TrialReference> // you can have multiple papers per trial
)