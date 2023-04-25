package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

class ProteinIsWildTypeByIHC internal constructor(private val protein: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasOnlyWildTypeResults = PriorMolecularTestFunctions.allIHCTestsForProtein(record.clinical().priorMolecularTests(), protein)
            .all { test: PriorMolecularTest -> WILD_TYPE_QUERY_STRINGS.any { it.equals(test.scoreText(), ignoreCase = true) } }

        return if (hasOnlyWildTypeResults) {
            unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages(String.format("Protein %s is wild type according to IHC", protein))
                .addPassGeneralMessages(String.format("%s wild type", protein))
                .build()
        } else {
            unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                    String.format(
                        "Could not determine if protein %s is wild type according to IHC",
                        protein
                    )
                )
                .addUndeterminedGeneralMessages(String.format("%s wild type status unknown", protein))
                .build()
        }
    }

    companion object {
        private val WILD_TYPE_QUERY_STRINGS = setOf("wildtype", "wild-type", "wild type")
    }
}