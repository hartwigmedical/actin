package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class ActionableTrialMatcher(
    private val applicableTrials: List<ActionableTrial>,
    private val molecularPredicate: Predicate<MolecularCriterium>
) {

    fun matchTrials(matchPredicate: Predicate<MolecularCriterium>): Map<ActionableTrial, Set<MolecularCriterium>> {
        return applicableTrials.map { trial ->
            val matchingCriteria = trial.anyMolecularCriteria()
                .filter { molecularPredicate.test(it) }
                .filter { matchPredicate.test(it) }
            trial to matchingCriteria
        }
            .filter { it.second.isNotEmpty() }
            .associate { it.first to it.second.toSet() }
    }
}