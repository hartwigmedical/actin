package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class ProteinIsWildTypeByIhc(private val protein: String, private val labels: EvaluationLabels.Molecular) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create(protein, record.ihcTests)

        return when {
            ihcTestEvaluation.filteredTests.isEmpty() -> {
                EvaluationFactory.undetermined(
                    labels.proteinIsWildTypeByIhcUndeterminedNoResult(protein),
                    isMissingMolecularResultForEvaluation = true
                )
            }

            ihcTestEvaluation.hasCertainWildtypeResultsForItem() -> {
                EvaluationFactory.pass(
                    labels.proteinIsWildTypeByIhcPass(protein),
                    inclusionEvents = setOf("IHC $protein wildtype")
                )
            }

            else -> EvaluationFactory.warn(
                labels.proteinIsWildTypeByIhcWarn(protein),
                inclusionEvents = setOf("Potential IHC $protein wildtype")
            )
        }
    }
}