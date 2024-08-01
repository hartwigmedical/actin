package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentSinceDateFunctions.treatmentSinceMinDate
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
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

            minDate?.let { treatmentSummary.specificMatches.any { treatmentSinceMinDate(it, minDate, false) } } == true -> {
                EvaluationFactory.pass("Received adjuvant treatment(s) of ${category.display()} within the last $weeksAgo weeks")
            }

            minDate?.let { treatmentSummary.specificMatches.any { treatmentSinceMinDate(it, minDate, true) } } == true -> {
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
}