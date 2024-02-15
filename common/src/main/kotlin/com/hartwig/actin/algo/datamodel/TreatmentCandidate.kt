package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.trial.datamodel.EligibilityFunction

data class TreatmentCandidate(
    val treatment: Treatment,
    val optional: Boolean,
    val eligibilityFunctions: Set<EligibilityFunction>
) {

    override fun toString(): String {
        return "${treatment.display()} eligible if:\n${functionString(eligibilityFunctions)}" +
                if (!optional) "\n   Treatment is required" else ""
    }

    private fun functionString(functions: Set<EligibilityFunction>) = functions.joinToString("\n") { "    $it" }
}