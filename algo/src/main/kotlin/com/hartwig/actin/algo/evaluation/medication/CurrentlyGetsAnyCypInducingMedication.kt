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
        val cypInducersReceived =
            selector.activeWithCypInteraction(record.clinical().medications(), null, CypInteraction.Type.INDUCER).map { it.name() }
        return if (cypInducersReceived.isNotEmpty()) {
            EvaluationFactory.recoverablePass(
                "Patient currently gets CYP inducing medication: ${Format.concatLowercaseWithAnd(cypInducersReceived)}",
                "CYP inducing medication use: ${Format.concatLowercaseWithAnd(cypInducersReceived)}"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get CYP inducing medication ",
                "No CYP inducing medication use "
            )
        }
    }
}