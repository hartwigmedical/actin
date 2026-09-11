package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk

class CurrentlyGetsQTProlongatingMedication(private val selector: MedicationSelector) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val qtMedication = medications.filter { it.qtProlongatingRisk != QTProlongatingRisk.NONE }
        val activeQtMedication = qtMedication.filter(selector::isActive).distinctBy { it.name }
        val plannedQtMedication = qtMedication.filter(selector::isPlanned).distinctBy { it.name }

        return when {
            activeQtMedication.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "Active QT prolongating medication in provided medications (risk type): " + concatWithType(
                        activeQtMedication
                    )
                )
            }

            plannedQtMedication.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Planned QT prolongating medication in provided medications (risk type): " + concatWithType(
                        plannedQtMedication
                    )
                )
            }

            else -> {
                EvaluationFactory.recoverableFail("No active QT prolongating medication in provided medications")
            }
        }
    }

    private fun concatWithType(medications: List<Medication>): String {
        return medications.joinToString(" and ") { "${it.name} (${it.qtProlongatingRisk})".lowercase() }
    }
}