package com.hartwig.actin.datamodel.efficacy

import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

data class EfficacyEntry(
    val acronym: String,
    val phase: String?,
    val treatments: List<Treatment>,
    val therapeuticSetting: Intent?,
    val variantRequirements: List<VariantRequirement>,
    val trialReferences: List<TrialReference> // you can have multiple papers per trial
)