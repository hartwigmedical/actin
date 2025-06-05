package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.Actionable
import com.hartwig.serve.datamodel.molecular.MolecularCriterium

sealed class ActionabilityMatchResult {

    data object Failure : ActionabilityMatchResult()
    data class Success(val actionables: List<Actionable> = emptyList(), val criterium: List<MolecularCriterium> = emptyList()) :
        ActionabilityMatchResult()

    companion object {
        fun combine(results: Sequence<ActionabilityMatchResult>): ActionabilityMatchResult {
            val combinedActionables = mutableListOf<Actionable>()
            val combinedCriteria = mutableListOf<MolecularCriterium>()

            for (result in results) {
                when (result) {
                    is Failure -> return Failure
                    is Success -> {
                        combinedActionables.addAll(result.actionables)
                        combinedCriteria.addAll(result.criterium)
                    }
                }
            }

            return Success(
                actionables = combinedActionables,
                criterium = combinedCriteria
            )
        }

        fun combine(results: Iterable<ActionabilityMatchResult>): ActionabilityMatchResult {
            return combine(results.asSequence())
        }
    }
}

