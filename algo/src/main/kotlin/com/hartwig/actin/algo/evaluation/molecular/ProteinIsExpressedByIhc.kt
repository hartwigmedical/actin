package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinIsExpressedByIhc internal constructor(private val protein: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestsForItem = IhcTestFilter.mostRecentAndUnknownDateIhcTestsForItem(record.ihcTests, protein)
        val (hasCertainPositiveExpression, hasPossiblePositiveExpression) =
            IhcTestEvaluation.hasPositiveIhcTestResultsForItem(protein, record.ihcTests)

        return when {
            ihcTestsForItem.isEmpty() -> {
                EvaluationFactory.undetermined(
                    "No $protein IHC test result",
                    isMissingMolecularResultForEvaluation = true
                )
            }

            hasCertainPositiveExpression -> EvaluationFactory.pass("$protein has expression by IHC")

            !hasPossiblePositiveExpression -> EvaluationFactory.fail("$protein is not expressed by IHC")

            else -> EvaluationFactory.warn("Undetermined if $protein IHC result indicates $protein expression by IHC")
        }
    }
}