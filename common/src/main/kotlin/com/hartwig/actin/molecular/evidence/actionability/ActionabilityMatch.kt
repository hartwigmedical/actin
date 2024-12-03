package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.trial.ActionableTrial

data class ActionabilityMatch(
    val onLabelEvidence: List<EfficacyEvidence>,
    val offLabelEvidence: List<EfficacyEvidence>,

    val onLabelTrials: List<ActionableTrial>,
    val offLabelTrials: List<ActionableTrial>
)
