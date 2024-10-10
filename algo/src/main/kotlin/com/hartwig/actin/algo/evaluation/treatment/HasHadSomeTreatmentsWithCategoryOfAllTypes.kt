package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.MedicationFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadSomeTreatmentsWithCategoryOfAllTypes(
    private val category: TreatmentCategory, private val types: Set<TreatmentType>, private val minTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(record.medications)

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            effectiveTreatmentHistory, category, { historyEntry -> types.all { type -> historyEntry.isOfType(type) == true } }
        )

        val typesList = Format.concatItemsWithAnd(types)
        val baseMessage = "received at least $minTreatmentLines line(s) of $typesList combination ${category.display()}"

        return when {
            treatmentSummary.numSpecificMatches() >= minTreatmentLines -> {
                EvaluationFactory.pass("Has $baseMessage")
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches >= minTreatmentLines -> {
                EvaluationFactory.undetermined(
                    "Can't determine whether patient has $baseMessage",
                    "Undetermined if $baseMessage"
                )
            }

            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches
                    >= minTreatmentLines || (minTreatmentLines == 1 && record.medications?.any { it.isTrialMedication } == true) -> {
                EvaluationFactory.undetermined(
                    "Patient may have received at least $minTreatmentLines line(s) of ${category.display()} due to trial participation",
                    "Trial medication in history - undetermined if received at least $minTreatmentLines line(s) of ${category.display()}"
                )
            }

            else -> {
                EvaluationFactory.fail("Has not $baseMessage")
            }
        }
    }
}