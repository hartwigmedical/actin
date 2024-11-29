package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.trial.ActionableTrial

data class ActionableEvents(
    val evidences: List<EfficacyEvidence> = emptyList(),
    val trials: List<ActionableTrial> = emptyList(),
)
