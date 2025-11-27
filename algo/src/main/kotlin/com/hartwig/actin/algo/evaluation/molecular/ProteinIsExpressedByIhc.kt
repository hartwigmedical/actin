package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinIsExpressedByIhc(private val protein: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create(protein, record.ihcTests)

        return when {
            ihcTestEvaluation.filteredTests.isEmpty() -> {
                EvaluationFactory.undetermined(
                    "No $protein IHC test result",
                    isMissingMolecularResultForEvaluation = true
                )
            }

            ihcTestEvaluation.hasCertainBroadPositiveResultsForItem() -> {
                EvaluationFactory.pass(
                    "$protein has expression by IHC",
                    inclusionEvents = setOf("IHC $protein expression")
                )
            }

            !ihcTestEvaluation.hasPossiblePositiveResultsForItem() -> EvaluationFactory.fail("$protein is not expressed by IHC")

            else -> {
                EvaluationFactory.warn(
                    "Undetermined if $protein IHC result indicates $protein expression by IHC",
                    inclusionEvents = setOf("Potential IHC $protein expression")
                )
            }
        }
    }
}