package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

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
        val oncologicalHistory = if (minDate == null) record.oncologicalHistory else certainHistoryAfterDate(record)
        val treatmentSummary =
            TreatmentSummaryForCategory.createForTreatmentHistory(oncologicalHistory, category, ::hasAnyMatchingTypeAndIntent)

        val intentsList = Format.concatItemsWithOr(intentsToFind, toLowerCase = true)
        val allowedTypesString = allowedTypes?.let { " ${Format.concatItemsWithOr(it)}" } ?: ""

        return with(treatmentSummary) {
            when {
                hasSpecificMatch() -> {
                    EvaluationFactory.pass(
                        "$intentsList${drugTypeString(specificMatches)} ${category.display()} " +
                                "(${specificMatches.joinToString(", ") { it.treatmentDisplay() }}) in provided treatments"
                    )
                }

                hasApproximateMatch() -> {
                    EvaluationFactory.undetermined("Undetermined if $allowedTypesString ${category.display()} in provided treatments is $intentsList")
                }

                hasPossibleTrialMatch() -> {
                    EvaluationFactory.undetermined(
                        "Undetermined if trial treatment in provided treatments included $intentsList$allowedTypesString ${category.display()}"
                    )
                }

                else -> {
                    minDate?.let {
                        TreatmentSummaryForCategory.createForTreatmentHistory(
                            potentialHistoryAfterDate(record), category, ::hasAnyMatchingTypeAndIntent
                        ).specificMatches.ifEmpty { null }
                    }?.let { unknownDateMatches ->
                        EvaluationFactory.undetermined(
                            "$intentsList${drugTypeString(unknownDateMatches)} ${category.display()} " +
                                    "(${unknownDateMatches.joinToString(", ") { it.treatmentDisplay() }}) " +
                                    "with unknown date in provided treatments"
                        )
                    }
                        ?: EvaluationFactory.fail("No $intentsList$allowedTypesString ${category.display()} in provided treatments")
                }
            }
        }
    }

    private fun hasAnyMatchingTypeAndIntent(entry: TreatmentHistoryEntry): Boolean? {
        val typeMatches = allowedTypes?.let { entry.matchesTypeFromSet(it) } ?: true
        return if (typeMatches) entry.intents?.intersect(intentsToFind)?.isNotEmpty() else false
    }

    private fun drugTypeString(entries: List<TreatmentHistoryEntry>): String {
        return allowedTypes?.let { allowedTypes ->
            entries.flatMap(TreatmentHistoryEntry::treatments)
                .flatMap(Treatment::types)
                .filter(allowedTypes::contains)
                .ifEmpty { null }
                ?.let { " ${Format.concatItemsWithAnd(it)}" }
        } ?: ""
    }

    private fun certainHistoryAfterDate(record: PatientRecord): List<TreatmentHistoryEntry> {
        return record.oncologicalHistory.filter { TreatmentVersusDateFunctions.certainTreatmentSinceMinDate(it, minDate!!) }
    }

    private fun potentialHistoryAfterDate(record: PatientRecord): List<TreatmentHistoryEntry> {
        return record.oncologicalHistory.filter { TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate(it, minDate!!) }
    }
}
