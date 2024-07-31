package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class ProteinIsWildTypeByIHC(private val protein: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val allIHCTestsForProtein =
            PriorIHCTestFunctions.allIHCTestsForProtein(record.priorIHCTests, protein)
        val hasOnlyWildTypeResults = allIHCTestsForProtein.isNotEmpty() && allIHCTestsForProtein.all { test ->
            WILD_TYPE_QUERY_STRINGS.any { it.equals(test.scoreText, ignoreCase = true) }
        }

        return if (hasOnlyWildTypeResults) {
            EvaluationFactory.pass("Protein $protein is wild type according to IHC", "$protein is wild type by IHC")
        } else {
            EvaluationFactory.undetermined(
                "Could not determine if protein $protein is wild type according to IHC", "$protein wild type status unknown by IHC"
            )
        }
    }

    companion object {
        private val WILD_TYPE_QUERY_STRINGS = setOf("wildtype", "wild-type", "wild type")
    }
}