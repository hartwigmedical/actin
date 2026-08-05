package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.treatmentSinceMinDate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate

class HasHadAdjuvantTreatmentWithCategory(
    private val category: TreatmentCategory,
    private val minDate: LocalDate?,
    private val weeksAgo: Int?,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory,
            category,
            { historyEntry -> historyEntry.intents?.contains(Intent.ADJUVANT) == true },
            { true },
            { historyEntry -> historyEntry.intents?.contains(Intent.ADJUVANT) != false }
        )

        return when {
            minDate == null && treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.pass(labels.hasHadAdjuvantTreatmentWithCategoryPass(category.display()))
            }

            minDate?.let { treatmentSummary.specificMatches.any { treatmentSinceMinDate(it, minDate, false) } } == true -> {
                EvaluationFactory.pass(labels.hasHadAdjuvantTreatmentWithCategoryPassWithinWeeks(category.display(), weeksAgo))
            }

            minDate?.let { treatmentSummary.specificMatches.any { treatmentSinceMinDate(it, minDate, true) } } == true -> {
                EvaluationFactory.undetermined(labels.hasHadAdjuvantTreatmentWithCategoryUndetermined(category.display()))
            }

            !treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.fail(labels.hasHadAdjuvantTreatmentWithCategoryFail(category.display()))
            }

            else -> {
                EvaluationFactory.fail(
                    labels.hasHadAdjuvantTreatmentWithCategoryFailTooLongAgo(category.display(), weeksAgo)
                )
            }
        }
    }
}