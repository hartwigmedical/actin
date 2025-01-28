package com.hartwig.actin.algo.soc

import com.hartwig.actin.datamodel.algo.EvaluatedTreatment
import com.hartwig.actin.datamodel.algo.Evaluation

class StandardOfCareEvaluation(val evaluatedTreatments: List<EvaluatedTreatment>) {

    fun potentiallyEligibleTreatments() = evaluatedTreatments.filter(EvaluatedTreatment::eligible)

    fun isMissingMolecularResultForEvaluation() = evaluatedTreatments.any { evaluatedTreatment ->
        evaluatedTreatment.evaluations.any(Evaluation::isMissingGenesForSufficientEvaluation)
    }
}