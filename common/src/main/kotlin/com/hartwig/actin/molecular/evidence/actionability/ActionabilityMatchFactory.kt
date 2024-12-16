package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial

object ActionabilityMatchFactory {

    fun create(
        evidenceMatchLists: List<List<EfficacyEvidence>>,
        matchingCriteriaPerTrialMatchLists: List<Map<ActionableTrial, Set<MolecularCriterium>>>
    ): ActionabilityMatch {
        return ActionabilityMatch(
            evidenceMatches = evidenceMatchLists.flatten(),
            matchingCriteriaPerTrialMatch = mergeMapsOfSets(matchingCriteriaPerTrialMatchLists)
        )
    }

    private fun <T, R> mergeMapsOfSets(mapsOfSets: List<Map<R, Set<T>>>): Map<R, Set<T>> {
        return mapsOfSets
            .flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { it.value.flatten().toSet() }
    }
}