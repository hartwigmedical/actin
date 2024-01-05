package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk

class CurrentlyGetsQTProlongatingMedication(private val selector: MedicationSelector) : EvaluationFunction {
        
    override fun evaluate(record: PatientRecord): Evaluation {
        val activeMedications = selector.active(record.clinical.medications)
        val qtMedication = activeMedications.filter { it.qtProlongatingRisk != QTProlongatingRisk.NONE }
        
        return if (qtMedication.isNotEmpty()) {
            EvaluationFactory.recoverablePass(
                "Patient currently gets QT prolongating medication (risk type): " + concatWithType(qtMedication),
                "QT prolongating medication use (risk type): " + concatWithType(qtMedication)
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient currently does not get QT prolongating medication ",
                "No QT prolongating medication use "
            )
        }
    }

    private fun concatWithType(medications: List<Medication>): String {
        return medications.joinToString(" and ") { it -> "${it.name} (${it.qtProlongatingRisk})".lowercase() }
    }
}