package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.Actionable

sealed class ActionabilityMatchResult {

    object Failure : ActionabilityMatchResult()
    data class Success(val actionables: List<Actionable> = emptyList()) : ActionabilityMatchResult()

    companion object {
        fun combine(results: Sequence<ActionabilityMatchResult>): ActionabilityMatchResult {
            return buildList<Actionable> {
                for (result in results) {
                    when (result) {
                        is Failure -> return Failure
                        is Success -> addAll(result.actionables)
                    }
                }
            }.let(::Success)
        }

        fun combine(results: Iterable<ActionabilityMatchResult>): ActionabilityMatchResult {
            return combine(results.asSequence())
        }
    }
}

