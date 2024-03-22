package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsAnyCypInducingMedication(private val selector: MedicationSelector) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return medicationWhenProvidedEvaluation(record) { medications ->
            val cypInducersReceived =
                selector.activeWithCypInteraction(medications, null, CypInteraction.Type.INDUCER).map { it.name }

            val cypInducersPlanned =
                selector.plannedWithCypInteraction(medications, null, CypInteraction.Type.INDUCER).map { it.name }

            when {
                cypInducersReceived.isNotEmpty() -> {
                    EvaluationFactory.recoverablePass(
                        "Patient currently gets CYP inducing medication: ${Format.concatLowercaseWithAnd(cypInducersReceived)}",
                        "CYP inducing medication use: ${Format.concatLowercaseWithAnd(cypInducersReceived)}"
                    )
                }

                cypInducersPlanned.isNotEmpty() -> {
                    EvaluationFactory.recoverableWarn(
                        "Patient plans to get CYP inducing medication: ${Format.concatLowercaseWithAnd(cypInducersPlanned)}",
                        "Planned CYP inducing medication use: ${Format.concatLowercaseWithAnd(cypInducersPlanned)}"
                    )
                }

                else -> {
                    EvaluationFactory.recoverableFail(
                        "Patient currently does not get CYP inducing medication ",
                        "No CYP inducing medication use "
                    )
                }
            }
        }
    }
}