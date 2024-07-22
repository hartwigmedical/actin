package com.hartwig.actin.algo.soc

import com.hartwig.actin.algo.datamodel.ResistanceEvidence
import com.hartwig.actin.personalization.datamodel.Treatment
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.ActionableEvents

class ResistanceEvidenceMatcher {

    fun match(actionableEvents: ActionableEvents, treatment: Treatment): List<ResistanceEvidence> {
        return findMatches(actionableEvents.hotspots(), treatment) + findMatches(actionableEvents.codons(), treatment) + findMatches(
            actionableEvents.exons(),
            treatment
        ) + findMatches(actionableEvents.genes(), treatment) + findMatches(actionableEvents.fusions(), treatment) + findMatches(
            actionableEvents.hla(),
            treatment
        ) + findMatches(actionableEvents.characteristics(), treatment)
    }

    fun findMatches(actionableEvents: List<ActionableEvent>, treatment: Treatment): List<ResistanceEvidence> {
        return actionableEvents.filter { it.direction().isResistant }.map { it.evidenceUrls() }
    }
}
}