package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk

class CurrentlyGetsQTProlongatingMedication internal constructor(private val selector: MedicationSelector) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasReceivedKnownQT =
            selector.activeWithQTProlongatingMedication(record.clinical().medications(), QTProlongatingRisk.KNOWN)
                .isNotEmpty()
        val hasReceivedPotentialQT =
            selector.activeWithQTProlongatingMedication(record.clinical().medications(), QTProlongatingRisk.POSSIBLE)
                .isNotEmpty()
        val hasReceivedConditionalQT =
            selector.activeWithQTProlongatingMedication(record.clinical().medications(), QTProlongatingRisk.CONDITIONAL)
                .isNotEmpty()
        return if (hasReceivedKnownQT) {
            EvaluationFactory.pass(
                "Patient currently gets known QT prolongating medication",
                "Known QT prolongating medication use "
            )
        } else if (hasReceivedPotentialQT) {
            EvaluationFactory.pass(
                "Patient currently gets potential QT prolongating medication",
                "Potential QT prolongating medication use "
            )
        } else if (hasReceivedConditionalQT) {
            EvaluationFactory.pass(
                "Patient currently gets conditional QT prolongating medication",
                "Conditional QT prolongating medication use "
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get QT prolongating medication ",
                "No QT prolongating medication use "
            )
        }
    }
}