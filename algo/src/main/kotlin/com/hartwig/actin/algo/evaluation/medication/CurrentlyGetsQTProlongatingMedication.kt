package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class CurrentlyGetsQTProlongatingMedication internal constructor(private val selector: MedicationSelector) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasReceivedQT = selector.activeWithQTProlongatingMedication(record.clinical().medications()).isNotEmpty()
        return if (hasReceivedQT) {
            EvaluationFactory.pass(
                "Patient currently gets known QT prolongating medication",
                "QT prolongating medication use "
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get known QT prolongating medication ",
                "No QT prolongating medication use "
            )
        }
    }
}