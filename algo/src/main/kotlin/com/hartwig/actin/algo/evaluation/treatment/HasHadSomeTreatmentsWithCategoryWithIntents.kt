package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadSomeTreatmentsWithCategoryWithIntents(
    private val category: TreatmentCategory,
    private val intentsToFind: Set<Intent>,
    private val minDate: LocalDate? = null
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = if (minDate == null) {
            createTreatmentSummary(record) { it.intents?.intersect(intentsToFind)?.isNotEmpty() }
        } else {
            createTreatmentSummary(record) {
                it.intents?.intersect(intentsToFind)?.isNotEmpty() == true &&
                        TreatmentSinceDateFunctions.treatmentSinceMinDate(it, minDate, false)
            }
        }
        val treatmentSummaryDateNull =
            createTreatmentSummary(record) { it.intents?.intersect(intentsToFind)?.isNotEmpty() == true && it.startYear == null }

        val intentsList = Format.concatItemsWithOr(intentsToFind)

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                val treatmentDisplay = treatmentSummary.specificMatches.joinToString(", ") { it.treatmentDisplay() }
                EvaluationFactory.pass("Has received $intentsList ${category.display()} ($treatmentDisplay)")
            }

            treatmentSummary.hasApproximateMatch() -> {
                EvaluationFactory.undetermined("Undetermined if received ${category.display()} is $intentsList")
            }

            treatmentSummaryDateNull.hasSpecificMatch() -> {
                val treatmentDisplay = treatmentSummaryDateNull.specificMatches.joinToString(", ") { it.treatmentDisplay() }
                EvaluationFactory.undetermined("Has received $intentsList ${category.display()} ($treatmentDisplay) with unknown date")
            }

            treatmentSummary.hasPossibleTrialMatch() -> {
                EvaluationFactory.undetermined("Undetermined if treatment received in previous trial included $intentsList ${category.display()}")
            }

            else -> {
                EvaluationFactory.fail("Has not received $intentsList ${category.display()}")
            }
        }
    }

    private fun createTreatmentSummary(
        record: PatientRecord,
        classifier: (TreatmentHistoryEntry) -> Boolean? = { true }
    ): TreatmentSummaryForCategory {
        return TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory,
            category,
            classifier
        )
    }
}
