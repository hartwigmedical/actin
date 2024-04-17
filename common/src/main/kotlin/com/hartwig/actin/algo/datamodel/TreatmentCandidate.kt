package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.trial.datamodel.EligibilityFunction

data class TreatmentCandidate(
    val treatment: Treatment,
    val optional: Boolean,
    val eligibilityFunctions: Set<EligibilityFunction>,
    val additionalCriteriaForRequirement: Set<EligibilityFunction> = emptySet()
) {

    override fun toString(): String {
        val requiredFunctionsString = if (additionalCriteriaForRequirement.isEmpty()) "" else {
            " when:\n${functionString(additionalCriteriaForRequirement)}"
        }
        return "${treatment.display()} eligible if:\n${functionString(eligibilityFunctions)}" +
                if (!optional) "\n   Treatment is required$requiredFunctionsString" else ""
    }

    private fun functionString(functions: Set<EligibilityFunction>) = functions.joinToString("\n") { "    $it" }

    fun eligibilityFunctionsForRequirement(): Set<EligibilityFunction> {
        if (optional) {
            throw IllegalStateException("Requirement criteria undefined for optional treatment")
        }
        return eligibilityFunctions union additionalCriteriaForRequirement
    }
}