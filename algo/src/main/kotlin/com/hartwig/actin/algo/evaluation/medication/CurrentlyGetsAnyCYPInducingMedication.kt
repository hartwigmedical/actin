package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsAnyCYPInducingMedication internal constructor(private val selector: MedicationSelector) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasReceivedAnyCYPInducer =
            selector.activeWithCYPInteraction(record.clinical().medications(), "Any", CypInteraction.Type.INDUCER)
                .isNotEmpty()
        return if (hasReceivedAnyCYPInducer) {
            EvaluationFactory.pass(
                "Patient currently gets any CYP inducing medication",
                "CYP inducing medication use "
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get any CYP inducing medication ",
                "No CYP inducing medication use "
            )
        }
    }
}