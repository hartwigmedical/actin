package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinIsLostByIhc(private val protein: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create(protein, record.ihcTests)

        return when {
            ihcTestEvaluation.filteredTests.isEmpty() -> {
                EvaluationFactory.undetermined(
                    "No $protein IHC test result",
                    isMissingMolecularResultForEvaluation = true
                )
            }

            ihcTestEvaluation.hasCertainNegativeResultsForItem() -> {
                EvaluationFactory.pass(
                    "$protein is lost by IHC",
                    inclusionEvents = setOf("IHC $protein loss")
                )
            }

            !ihcTestEvaluation.hasPossibleNegativeResultsForItem() -> EvaluationFactory.fail("$protein is not lost by IHC")

            else -> EvaluationFactory.warn(
                "Undetermined if $protein IHC result indicates $protein loss by IHC",
                inclusionEvents = setOf("Potential IHC $protein loss")
            )
        }
    }
}
