package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.Medication

class CurrentlyGetsMedicationOfAtcLevel(
    private val selector: MedicationSelector,
    private val categoryName: String,
    private val categoryAtcLevels: Set<AtcLevel>,
    private val onlyCheckSystemic: Boolean
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medicationsWithAtcLevel = record.clinical.medications.filter {
            (it.allLevels() intersect categoryAtcLevels).isNotEmpty()
        }

        val activeMedicationsWithAtcLevel =
            filteredSystemicMedicationNames(medicationsWithAtcLevel, if (onlyCheckSystemic) selector::isSystemicAndActive else selector::isActive)
        val plannedMedicationsWithAtcLevel =
            filteredSystemicMedicationNames(medicationsWithAtcLevel, if (onlyCheckSystemic) selector::isSystemicAndPlanned else selector::isPlanned)

        return when {
            activeMedicationsWithAtcLevel.isNotEmpty() -> {
                val foundMedicationString = concatLowercaseWithAnd(activeMedicationsWithAtcLevel)
                EvaluationFactory.recoverablePass(
                    "Patient currently gets medication: $foundMedicationString which belong(s) to category '$categoryName'",
                    "$categoryName medication use: $foundMedicationString"
                )
            }

            plannedMedicationsWithAtcLevel.isNotEmpty() -> {
                val foundMedicationString = concatLowercaseWithAnd(plannedMedicationsWithAtcLevel)
                EvaluationFactory.recoverableWarn(
                    "Patient plans to get medication: $foundMedicationString which belong(s) to category '$categoryName'",
                    "Planned $categoryName medication use: $foundMedicationString"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient currently does not get medication of category '$categoryName'",
                    "No $categoryName medication use"
                )
            }
        }
    }

    private fun filteredSystemicMedicationNames(
        medications: List<Medication>, filter: (Medication) -> Boolean
    ) = medications.filter(filter::invoke).map(Medication::name)
}

