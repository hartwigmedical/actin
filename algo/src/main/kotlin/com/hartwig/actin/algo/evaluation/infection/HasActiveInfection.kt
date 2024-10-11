package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreterOnEvaluationDate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.InfectionStatus
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import java.time.LocalDate

class HasActiveInfection(private val atcTree: AtcTree, private val referenceDate: LocalDate) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {

        val medicationSelector = MedicationSelector(MedicationStatusInterpreterOnEvaluationDate(referenceDate, null))
        val antibioticAtcLevels = MedicationCategories.create(atcTree).resolve("Systemic antibiotics")
        val currentlyUsesAntibiotics = record.medications?.any {
            medicationSelector.isActive(it) && (it.allLevels() intersect antibioticAtcLevels).isNotEmpty()
        } ?: false

        val infection = record.clinicalStatus.infectionStatus

        return when {
            infection?.hasActiveInfection == true -> {
                EvaluationFactory.pass(
                    "Patient has active infection: " + description(infection),
                    "Infection presence: " + description(infection)
                )
            }
            currentlyUsesAntibiotics -> {
                EvaluationFactory.warn(
                    "Patient uses antibiotics which might indicate an active infection",
                    "Possible active infection (antibiotics usage)"
                )
            }
            infection == null -> {
                EvaluationFactory.recoverableUndetermined("Infection status data is missing", "Unknown infection status")
            } else -> {
                EvaluationFactory.fail("Patient has no active infection", "No infection present")
            }
        }
    }

    companion object {
        private fun description(infection: InfectionStatus): String {
            return infection.description ?: "Unknown"
        }
    }
}