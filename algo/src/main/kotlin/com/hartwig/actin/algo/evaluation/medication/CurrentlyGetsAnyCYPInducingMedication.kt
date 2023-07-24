package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsAnyCYPInducingMedication internal constructor(private val selector: MedicationSelector) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val receivedAnyCYPInducer =
            selector.activeWithCYPInteraction(record.clinical().medications(), "Any", CypInteraction.Type.INDUCER).map { it.name() }
        return if (receivedAnyCYPInducer.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets CYP inducing medication: " + Format.concatLowercaseWithAnd(receivedAnyCYPInducer),
                "CYP inducing medication use: " + Format.concatLowercaseWithAnd(receivedAnyCYPInducer)
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get CYP inducing medication ",
                "No CYP inducing medication use "
            )
        }
    }
}