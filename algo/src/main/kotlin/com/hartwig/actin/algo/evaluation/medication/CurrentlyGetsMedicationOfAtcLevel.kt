package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
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
                val foundMedicationString = concatLowercaseWithAnd(activeMedicationsWithAtcLevel)
                EvaluationFactory.recoverablePass("$categoryName medication use: $foundMedicationString")
            }

            plannedMedicationsWithAtcLevel.isNotEmpty() -> {
                val foundMedicationString = concatLowercaseWithAnd(plannedMedicationsWithAtcLevel)
                EvaluationFactory.recoverableWarn("Planned $categoryName medication use: $foundMedicationString")
            }

            else -> {
                EvaluationFactory.recoverableFail("No $categoryName medication use")
            }
        }
    }
}