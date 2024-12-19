package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.util.MapFunctions
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
            matchingCriteriaPerTrialMatch = MapFunctions.mergeMapsOfSets(matchingCriteriaPerTrialMatchLists)
        )
    }
}