package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

class HasHadAdjuvantTreatmentWithCategoryOfTypes(private val types: Set<TreatmentType>, private val warnCategory: TreatmentCategory) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val adjuvantTreatmentHistory = record.oncologicalHistory.filter { it.intents?.contains(Intent.ADJUVANT) == true }

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            adjuvantTreatmentHistory, warnCategory, { historyEntry -> historyEntry.matchesTypeFromSet(types) }
        )

        val categoryString = warnCategory.display()
        return when {
            treatmentSummary.hasSpecificMatch() -> {
                val treatmentsString = Format.concatLowercaseWithCommaAndAnd(
                    treatmentSummary.specificMatches.map(TreatmentHistoryEntryFunctions::fullTreatmentDisplay)
                )
                EvaluationFactory.pass("Received adjuvant $treatmentsString")
            }

            treatmentSummary.hasApproximateMatch() -> {
                EvaluationFactory.warn("Received adjuvant $categoryString but not of specific type)")
            }

            treatmentSummary.hasPossibleTrialMatch() -> {
                EvaluationFactory.undetermined("Undetermined if treatment received in previous trial included adjuvant $categoryString")
            }

            else -> {
                val namesString = Format.concatItemsWithAnd(types)
                EvaluationFactory.fail("Not received adjuvant $namesString")
            }
        }
    }
}