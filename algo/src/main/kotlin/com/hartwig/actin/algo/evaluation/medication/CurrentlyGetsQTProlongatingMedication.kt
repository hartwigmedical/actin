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
        val knownQTMedication =
            selector.activeWithQTProlongatingMedication(record.clinical().medications(), QTProlongatingRisk.KNOWN)
                .map { it.name() }
        val potentialQTMedication =
            selector.activeWithQTProlongatingMedication(record.clinical().medications(), QTProlongatingRisk.POSSIBLE)
                .map { it.name() }
        val conditionalQTMedication =
            selector.activeWithQTProlongatingMedication(record.clinical().medications(), QTProlongatingRisk.CONDITIONAL)
                .map { it.name() }
        return if (knownQTMedication.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets known QT prolongating medication: " + Format.concat(knownQTMedication),
                "Known QT prolongating medication use: " + Format.concat(knownQTMedication)
            )
        } else if (potentialQTMedication.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets potential QT prolongating medication: " + Format.concat(potentialQTMedication),
                "Potential QT prolongating medication use: " + Format.concat(potentialQTMedication)
            )
        } else if (conditionalQTMedication.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets conditional QT prolongating medication: " + Format.concat(
                    conditionalQTMedication
                ),
                "Conditional QT prolongating medication use: " + Format.concat(conditionalQTMedication)
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get QT prolongating medication ",
                "No QT prolongating medication use "
            )
        }
    }
}