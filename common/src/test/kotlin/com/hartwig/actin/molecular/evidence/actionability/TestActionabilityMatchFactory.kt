package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence

object TestActionabilityMatchFactory {

    fun createEmpty(): ActionabilityMatch {
        return ActionabilityMatch(
            onLabelEvidence = emptyList(),
            offLabelEvidence = emptyList(),
            onLabelTrials = emptyList(),
            offLabelTrials = emptyList()
        )
    }

    fun withOnLabelEvidence(evidence: EfficacyEvidence): ActionabilityMatch {
        return ActionabilityMatch(
            onLabelEvidence = listOf(evidence),
            offLabelEvidence = emptyList(),
            onLabelTrials = emptyList(),
            offLabelTrials = emptyList()
        )
    }
}