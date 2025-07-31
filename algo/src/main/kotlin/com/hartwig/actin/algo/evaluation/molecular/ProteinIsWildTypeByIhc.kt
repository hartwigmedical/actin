package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinIsWildTypeByIhc(private val protein: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create(protein, record.ihcTests)

        return when {
            ihcTestEvaluation.filteredTests.isEmpty() -> {
                EvaluationFactory.undetermined(
                    "No $protein IHC test result",
                    isMissingMolecularResultForEvaluation = true
                )
            }

            ihcTestEvaluation.hasCertainWildtypeResultsForItem() -> {
                EvaluationFactory.pass(
                    "$protein is wild type by IHC",
                    inclusionEvents = setOf("IHC $protein wildtype")
                )
            }

            else -> EvaluationFactory.warn(
                "Undetermined if $protein IHC result indicates wild type status",
                inclusionEvents = setOf("potential IHC $protein wildtype")
            )
        }
    }
}