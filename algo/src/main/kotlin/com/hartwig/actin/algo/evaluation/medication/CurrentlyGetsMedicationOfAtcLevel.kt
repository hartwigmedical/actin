package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel

class CurrentlyGetsMedicationOfAtcLevel(
    private val selector: MedicationSelector, private val categoryName: String, private val categoryAtcLevels: Set<AtcLevel>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val (activeMedicationsWithAtcLevel, plannedMedicationsWithAtcLevel) = selector.extractActiveAndPlannedWithCategory(
            medications,
            categoryAtcLevels
        )

        return when {
            activeMedicationsWithAtcLevel.isNotEmpty() -> {
                val foundMedicationString = concatLowercaseWithCommaAndAnd(activeMedicationsWithAtcLevel)
                EvaluationFactory.recoverablePass("Active $categoryName medication in provided medications ($foundMedicationString)")
            }

            plannedMedicationsWithAtcLevel.isNotEmpty() -> {
                val foundMedicationString = concatLowercaseWithCommaAndAnd(plannedMedicationsWithAtcLevel)
                EvaluationFactory.warn("Planned $categoryName medication in provided medications ($foundMedicationString)")
            }

            else -> {
                EvaluationFactory.recoverableFail("No active $categoryName medication in provided medications")
            }
        }
    }
}