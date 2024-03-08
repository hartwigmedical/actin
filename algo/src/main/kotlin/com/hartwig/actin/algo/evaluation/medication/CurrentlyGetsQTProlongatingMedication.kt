package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk

class CurrentlyGetsQTProlongatingMedication(private val selector: MedicationSelector) : EvaluationFunction {
        
    override fun evaluate(record: PatientRecord): Evaluation {
        val qtMedication = record.medications.filter { it.qtProlongatingRisk != QTProlongatingRisk.NONE }
        val activeQtMedication = qtMedication.filter(selector::isActive)
        val plannedQtMedication = qtMedication.filter(selector::isPlanned)

        return when {
            activeQtMedication.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "Patient currently gets QT prolongating medication (risk type): " + concatWithType(activeQtMedication),
                    "QT prolongating medication use (risk type): " + concatWithType(activeQtMedication)
                )
            }

            plannedQtMedication.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn(
                    "Patient plans to get QT prolongating medication (risk type): " + concatWithType(plannedQtMedication),
                    "Planned QT prolongating medication use (risk type): " + concatWithType(plannedQtMedication)
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient currently does not get QT prolongating medication ",
                    "No QT prolongating medication use "
                )
            }
        }
    }

    private fun concatWithType(medications: List<Medication>): String {
        return medications.joinToString(" and ") { "${it.name} (${it.qtProlongatingRisk})".lowercase() }
    }
}