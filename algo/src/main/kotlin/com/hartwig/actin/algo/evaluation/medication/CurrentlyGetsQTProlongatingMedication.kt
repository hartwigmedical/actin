package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk

class CurrentlyGetsQTProlongatingMedication(private val selector: MedicationSelector, private val labels: EvaluationLabels.Medication) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return medicationNotProvided(labels)
        val qtMedication = medications.filter { it.qtProlongatingRisk != QTProlongatingRisk.NONE }
        val activeQtMedication = qtMedication.filter(selector::isActive).distinctBy { it.name }
        val plannedQtMedication = qtMedication.filter(selector::isPlanned).distinctBy { it.name }

        return when {
            activeQtMedication.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    labels.currentlyGetsQtProlongatingMedicationRecoverablePass(concatWithType(activeQtMedication))
                )
            }

            plannedQtMedication.isNotEmpty() -> {
                EvaluationFactory.warn(labels.currentlyGetsQtProlongatingMedicationWarn(concatWithType(plannedQtMedication)))
            }

            else -> {
                EvaluationFactory.recoverableFail(labels.currentlyGetsQtProlongatingMedicationRecoverableFail())
            }
        }
    }

    private fun concatWithType(medications: List<Medication>): String {
        return medications.joinToString(" and ") { "${it.name} (${it.qtProlongatingRisk})".lowercase() }
    }
}