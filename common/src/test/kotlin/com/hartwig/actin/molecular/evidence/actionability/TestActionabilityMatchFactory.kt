package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence

object TestActionabilityMatchFactory {

    fun createEmpty(): ActionabilityMatch {
        return ActionabilityMatch(
            onLabelEvidences = emptyList(),
            offLabelEvidences = emptyList(),
            onLabelTrials = emptyList(),
            offLabelTrials = emptyList()
        )
    }

    fun withOnLabelEvidence(evidence: EfficacyEvidence): ActionabilityMatch {
        return ActionabilityMatch(
            onLabelEvidences = listOf(evidence),
            offLabelEvidences = emptyList(),
            onLabelTrials = emptyList(),
            offLabelTrials = emptyList()
        )
    }
}