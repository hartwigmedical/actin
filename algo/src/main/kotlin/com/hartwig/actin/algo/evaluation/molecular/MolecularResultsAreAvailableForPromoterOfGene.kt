package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
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
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("$gene promoter has been tested in a prior molecular test")
                .addPassGeneralMessages("Molecular requirements")
                .build()
        } else if (hasIndeterminatePriorTest) {
            return unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                    "$gene promoter has been tested in a prior molecular test but with indeterminate status"
                )
                .addUndeterminedGeneralMessages("Molecular requirements")
                .build()
        }
        return recoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("$gene has not been tested")
            .addFailGeneralMessages("Molecular requirements")
            .build()
    }

    companion object {
        const val PROMOTER = "promoter"
    }
}