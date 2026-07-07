package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadSomeTreatmentsWithCategoryAndTypeWithIntents(
    private val category: TreatmentCategory,
    private val intentsToFind: Set<Intent>,
    private val allowedTypes: Set<TreatmentType>? = null,
    private val minDate: LocalDate? = null
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val oncologicalHistory = if (minDate == null) record.oncologicalHistory else historyAfterDate(record, false)
        val treatmentSummary =
            TreatmentSummaryForCategory.createForTreatmentHistory(oncologicalHistory, category, ::hasAnyMatchingTypeAndIntent)

        val intentsList = Format.concatItemsWithOr(intentsToFind, toLowerCase = true)
        val allowedTypesString = allowedTypes?.let { " ${Format.concatItemsWithOr(it)}" } ?: ""

        return with(treatmentSummary) {
            when {
                hasSpecificMatch() -> {
                    EvaluationFactory.pass(
                        "Has received $intentsList${drugTypeString(specificMatches)} ${category.display()} " +
                                "(${specificMatches.joinToString(", ") { it.treatmentDisplay() }})"
                    )
                }

                hasApproximateMatch() -> {
                    EvaluationFactory.undetermined("Undetermined if received$allowedTypesString ${category.display()} is $intentsList")
                }

                hasPossibleTrialMatch() -> {
                    EvaluationFactory.undetermined(
                        "Undetermined if treatment received in previous trial included $intentsList$allowedTypesString ${category.display()}"
                    )
                }

                else -> {
                    minDate?.let {
                        TreatmentSummaryForCategory.createForTreatmentHistory(
                            historyAfterDate(record, true), category, ::hasAnyMatchingTypeAndIntent
                        ).specificMatches.ifEmpty { null }
                    }?.let { unknownDateMatches ->
                        EvaluationFactory.undetermined(
                            "Has received $intentsList${drugTypeString(unknownDateMatches)} ${category.display()} " +
                                    "(${unknownDateMatches.joinToString(", ") { it.treatmentDisplay()}}) with unknown date"
                        )
                    } ?: EvaluationFactory.fail("Has not received $intentsList$allowedTypesString ${category.display()}")
                }
            }
        }
    }

    private fun hasAnyMatchingTypeAndIntent(entry: TreatmentHistoryEntry): Boolean? {
        val typeMatches = allowedTypes?.let { entry.matchesTypeFromSet(it) } ?: true
        return if (typeMatches) entry.intents?.intersect(intentsToFind)?.isNotEmpty() else false
    }

    private fun drugTypeString(entries: List<TreatmentHistoryEntry>): String {
        return allowedTypes?.let {
            " ${Format.concatItemsWithAnd(it.intersect(entries.flatMap { e -> e.treatments }.flatMap { t -> t.types() }.toSet()))}"
        } ?: ""
    }

    private fun historyAfterDate(record: PatientRecord, includeUnknown: Boolean): List<TreatmentHistoryEntry> {
        return record.oncologicalHistory.filter { TreatmentVersusDateFunctions.treatmentSinceMinDate(it, minDate!!, includeUnknown) }
    }
}
