package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import java.time.LocalDate

class HasHadSomeTreatmentsWithCategoryWithIntents(
    private val category: TreatmentCategory,
    private val intentsToFind: Set<Intent>,
    private val minDate: LocalDate? = null
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val oncologicalHistory = if (minDate == null) {
            record.oncologicalHistory
        } else {
            record.oncologicalHistory.filter { TreatmentSinceDateFunctions.treatmentSinceMinDate(it, minDate, true) }
        }

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            oncologicalHistory,
            category,
            { historyEntry -> historyEntry.intents?.intersect(intentsToFind)?.isNotEmpty() }
        )
        val intentsList = Format.concatItemsWithOr(intentsToFind)

        return when {
            treatmentSummary.hasSpecificMatch() &&
                    (minDate == null || treatmentSummary.specificMatches.filter { it.startYear != null}.isNotEmpty()) -> {
                val treatmentDisplay = treatmentSummary.specificMatches.joinToString(", ") { it.treatmentDisplay() }
                EvaluationFactory.pass("Has received $intentsList ${category.display()} ($treatmentDisplay)")
            }

            treatmentSummary.hasSpecificMatch() -> {
                val treatmentDisplay = treatmentSummary.specificMatches.joinToString(", ") { it.treatmentDisplay() }
                EvaluationFactory.undetermined("Has received $intentsList ${category.display()} ($treatmentDisplay) with unknown date")
            }

            treatmentSummary.hasApproximateMatch() -> {
                EvaluationFactory.undetermined("Undetermined if received ${category.display()} is $intentsList")
            }

            treatmentSummary.hasPossibleTrialMatch() -> {
                EvaluationFactory.undetermined("Undetermined if treatment received in previous trial included $intentsList ${category.display()}")
            }

            else -> {
                EvaluationFactory.fail("Has not received $intentsList ${category.display()}")
            }
        }
    }
}
