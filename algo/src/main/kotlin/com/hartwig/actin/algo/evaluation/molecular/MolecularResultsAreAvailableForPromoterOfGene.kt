package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import java.util.*

class MolecularResultsAreAvailableForPromoterOfGene internal constructor(private val gene: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasValidPriorTest = false
        var hasIndeterminatePriorTest = false
        for (priorMolecularTest in record.clinical().priorMolecularTests()) {
            val test = priorMolecularTest.item()
            if (test.contains(gene) && test.lowercase(Locale.getDefault()).contains(PROMOTER.lowercase(Locale.getDefault()))) {
                if (priorMolecularTest.impliesPotentialIndeterminateStatus()) {
                    hasIndeterminatePriorTest = true
                } else {
                    hasValidPriorTest = true
                }
            }
        }
        if (hasValidPriorTest) {
            return EvaluationFactory.pass("$gene promoter has been tested in a prior molecular test", "$gene promotor tested before")
        } else if (hasIndeterminatePriorTest) {
            return EvaluationFactory.undetermined(
                "$gene promoter has been tested in a prior molecular test but with indeterminate status",
                "$gene promotor tested before but indeterminate status"
            )
        }
        return EvaluationFactory.recoverableFail("$gene has not been tested", "$gene not tested")
    }

    companion object {
        const val PROMOTER = "promoter"
    }
}