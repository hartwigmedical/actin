package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluationConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinIsWildTypeByIhc(private val protein: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestsForItem = IhcTestFilter.mostRecentAndUnknownDateIhcTestsForItem(record.ihcTests, protein)
        val hasCertainWildtypeResults = ihcTestsForItem.isNotEmpty() && ihcTestsForItem.all { test ->
            IhcTestEvaluationConstants.WILD_TYPE_TERMS.any { it == test.scoreText?.lowercase() }
        }

        return when {
            ihcTestsForItem.isEmpty() -> {
                EvaluationFactory.undetermined(
                    "No $protein IHC test result",
                    isMissingMolecularResultForEvaluation = true
                )
            }

            hasCertainWildtypeResults -> EvaluationFactory.pass("$protein is wild type by IHC")

            else -> EvaluationFactory.warn("Undetermined if $protein IHC result indicates wild type status")
        }
    }
}