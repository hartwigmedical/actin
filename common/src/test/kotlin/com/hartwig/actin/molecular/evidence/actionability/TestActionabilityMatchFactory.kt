package com.hartwig.actin.molecular.evidence.actionability

object TestActionabilityMatchFactory {

    fun createEmpty(): ActionabilityMatch {
        return ActionabilityMatch(
            onLabelEvidences = emptyList(),
            offLabelEvidences = emptyList(),
            onLabelTrials = emptyList(),
            offLabelTrials = emptyList()
        )
    }
}