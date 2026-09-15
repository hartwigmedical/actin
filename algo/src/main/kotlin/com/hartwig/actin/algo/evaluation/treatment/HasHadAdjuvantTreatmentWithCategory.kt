package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.certainTreatmentSinceMinDate
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.potentialTreatmentSinceMinDate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
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
            minDate == null && treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.pass("Adjuvant treatment(s) of ${category.display()} in provided treatments")
            }

            minDate?.let { treatmentSummary.specificMatches.any { certainTreatmentSinceMinDate(it, minDate) } } == true -> {
                EvaluationFactory.pass("Adjuvant treatment(s) of ${category.display()} within the last $weeksAgo weeks in provided treatments")
            }

            minDate?.let { treatmentSummary.specificMatches.any { potentialTreatmentSinceMinDate(it, minDate) } } == true  -> {
                EvaluationFactory.undetermined("Adjuvant treatment(s) of ${category.display()} in provided treatments but unknown if within the last $weeksAgo weeks")
            }

            !treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.fail("No adjuvant treatment(s) of ${category.display()} in provided treatments")
            }

            else -> {
                EvaluationFactory.fail("None of the adjuvant treatment(s) of ${category.display()} in provided treatments are within $weeksAgo weeks")
            }
        }
    }
}