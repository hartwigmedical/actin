package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk

class CurrentlyGetsQTProlongatingMedication internal constructor(private val selector: MedicationSelector) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val activeMedications = selector.active(record.clinical().medications())
        val QTMedication = activeMedications.groupBy({ it.qtProlongatingRisk() }, { it.name() })
        val knownQTMedication = QTMedication.getValue(QTProlongatingRisk.KNOWN)
        val possibleQTMedication = QTMedication.getValue(QTProlongatingRisk.POSSIBLE)
        val conditionalQTMedication = QTMedication.getValue(QTProlongatingRisk.CONDITIONAL)
        return if (knownQTMedication.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets known QT prolongating medication: " + Format.concatLowercaseWithAnd(knownQTMedication),
                "Known QT prolongating medication use: " + Format.concatLowercaseWithAnd(knownQTMedication)
            )
        } else if (possibleQTMedication.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets possible QT prolongating medication: " + Format.concatLowercaseWithAnd(possibleQTMedication),
                "Potential QT prolongating medication use: " + Format.concatLowercaseWithAnd(possibleQTMedication)
            )
        } else if (conditionalQTMedication.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets conditional QT prolongating medication: " + Format.concatLowercaseWithAnd(
                    conditionalQTMedication
                ),
                "Conditional QT prolongating medication use: " + Format.concatLowercaseWithAnd(conditionalQTMedication)
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get QT prolongating medication ",
                "No QT prolongating medication use "
            )
        }
    }
}