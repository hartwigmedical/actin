package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadAdjuvantTreatmentWithCategory(
    private val category: TreatmentCategory,
    private val minDate: LocalDate?,
    private val weeksAgo: Int?
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
            weeksAgo == null && treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.pass("Received adjuvant treatment(s) of ${category.display()}")
            }

            treatmentSummary.specificMatches.any { treatmentSinceMinDate(it, false) } -> {
                EvaluationFactory.pass("Received adjuvant treatment(s) of ${category.display()} within the last $weeksAgo weeks")
            }

            treatmentSummary.specificMatches.any { treatmentSinceMinDate(it, true) } -> {
                EvaluationFactory.undetermined("Received adjuvant treatment(s) of ${category.display()} but date unknown")
            }

            !treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.fail("Has not received adjuvant treatment(s) of ${category.display()}")
            }

            else -> {
                EvaluationFactory.fail("All received adjuvant treatment(s) of ${category.display()} are administered more than $weeksAgo weeks ago")
            }
        }
    }

    private fun treatmentSinceMinDate(treatment: TreatmentHistoryEntry, includeUnknown: Boolean): Boolean {
        return DateComparison.isAfterDate(
            minDate!!,
            treatment.treatmentHistoryDetails?.stopYear,
            treatment.treatmentHistoryDetails?.stopMonth
        )
            ?: DateComparison.isAfterDate(minDate, treatment.startYear, treatment.startMonth)
            ?: includeUnknown
    }
}