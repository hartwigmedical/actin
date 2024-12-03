package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.trial.ActionableTrial

data class ActionabilityMatch(
    val onLabelEvidence: ActionableEvents = ActionableEvents(),
    val offLabelEvidence: ActionableEvents = ActionableEvents(),

    val onLabelEvidences: List<EfficacyEvidence>,
    val offLabelEvidences: List<EfficacyEvidence>,

    val onLabelTrials: List<ActionableTrial>,
    val offLabelTrials: List<ActionableTrial>
)
