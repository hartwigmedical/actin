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
        val antimicrobialsAtcLevels = MedicationCategories.create(atcTree).resolve("Systemic antimicrobials")
        val currentlyUsesAntimicrobials = record.medications?.any {
            medicationSelector.isActive(it) && (it.allLevels() intersect antimicrobialsAtcLevels).isNotEmpty()
        } == true

        val infection = record.clinicalStatus.infectionStatus

        return when {
            infection?.hasActiveInfection == true -> {
                EvaluationFactory.pass("Has active infection '${description(infection)}'")
            }

            currentlyUsesAntimicrobials -> {
                EvaluationFactory.warn("Possible active infection (antimicrobials usage)")
            }

            infection == null -> {
                EvaluationFactory.recoverableUndetermined("Infection status data is missing")
            }

            else -> {
                EvaluationFactory.fail("No active infection present")
            }
        }
    }

    companion object {
        private fun description(infection: InfectionStatus): String {
            return infection.description ?: "Unknown"
        }
    }
}