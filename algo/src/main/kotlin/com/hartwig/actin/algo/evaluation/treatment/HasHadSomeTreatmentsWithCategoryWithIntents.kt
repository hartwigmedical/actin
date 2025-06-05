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
        val oncologicalHistory = if (minDate == null) record.oncologicalHistory else historyAfterDate(record, false)
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(oncologicalHistory, category, ::hasAnyMatchingIntent)

        val intentsList = Format.concatItemsWithOr(intentsToFind)

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                val treatmentDisplay = treatmentSummary.specificMatches.joinToString(", ") { it.treatmentDisplay() }
                EvaluationFactory.pass("Has received $intentsList ${category.display()} ($treatmentDisplay)")
            }

            treatmentSummary.hasApproximateMatch() -> {
                EvaluationFactory.undetermined("Undetermined if received ${category.display()} is $intentsList")
            }

            treatmentSummary.hasPossibleTrialMatch() -> {
                EvaluationFactory.undetermined("Undetermined if treatment received in previous trial included $intentsList ${category.display()}")
            }

            else -> {
                minDate?.let {
                    TreatmentSummaryForCategory.createForTreatmentHistory(
                        historyAfterDate(record, true), category, ::hasAnyMatchingIntent
                    ).specificMatches.ifEmpty { null }
                }?.let { unknownDateMatches ->
                    EvaluationFactory.undetermined("Has received $intentsList ${category.display()} (${unknownDateMatches.joinToString(", ")}) with unknown date")
                } ?: EvaluationFactory.fail("Has not received $intentsList ${category.display()}")
            }
        }
    }

    private fun hasAnyMatchingIntent(entry: TreatmentHistoryEntry) = entry.intents?.intersect(intentsToFind)?.isNotEmpty()

    private fun historyAfterDate(record: PatientRecord, includeUnknown: Boolean): List<TreatmentHistoryEntry> {
        return record.oncologicalHistory.filter { TreatmentVersusDateFunctions.treatmentSinceMinDate(it, minDate!!, includeUnknown) }
    }
}
