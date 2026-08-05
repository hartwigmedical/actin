package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
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
    private val minDate: LocalDate? = null,
    private val labels: EvaluationLabels.Treatment
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
                        labels.hasHadSomeTreatmentsWithCategoryAndTypeWithIntentsPass(
                            intentsList,
                            drugTypeString(specificMatches),
                            category.display(),
                            specificMatches.map { it.treatmentDisplay() }
                        )
                    )
                }

                hasApproximateMatch() -> {
                    EvaluationFactory.undetermined(
                        labels.hasHadSomeTreatmentsWithCategoryAndTypeWithIntentsUndeterminedApproximate(
                            allowedTypesString, category.display(), intentsList
                        )
                    )
                }

                hasPossibleTrialMatch() -> {
                    EvaluationFactory.undetermined(
                        labels.hasHadSomeTreatmentsWithCategoryAndTypeWithIntentsUndeterminedTrial(
                            intentsList, allowedTypesString, category.display()
                        )
                    )
                }

                else -> {
                    minDate?.let {
                        TreatmentSummaryForCategory.createForTreatmentHistory(
                            historyAfterDate(record, true), category, ::hasAnyMatchingTypeAndIntent
                        ).specificMatches.ifEmpty { null }
                    }?.let { unknownDateMatches ->
                        EvaluationFactory.undetermined(
                            labels.hasHadSomeTreatmentsWithCategoryAndTypeWithIntentsUndeterminedUnknownDate(
                                intentsList,
                                drugTypeString(unknownDateMatches),
                                category.display(),
                                unknownDateMatches.map { it.treatmentDisplay() }
                            )
                        )
                    } ?: EvaluationFactory.fail(
                        labels.hasHadSomeTreatmentsWithCategoryAndTypeWithIntentsFail(intentsList, allowedTypesString, category.display())
                    )
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
