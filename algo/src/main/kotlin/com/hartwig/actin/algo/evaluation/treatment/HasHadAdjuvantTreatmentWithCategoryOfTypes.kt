package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

class HasHadAdjuvantTreatmentWithCategoryOfTypes(private val types: Set<TreatmentType>, private val warnCategory: TreatmentCategory) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val adjuvantTreatmentHistory = record.clinical.oncologicalHistory.filter { it.intents?.contains(Intent.ADJUVANT) == true }

        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            adjuvantTreatmentHistory, warnCategory, { it.matchesTypeFromSet(types) }
        )

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                val treatmentsString = Format.concatLowercaseWithAnd(
                    treatmentSummary.specificMatches.map(TreatmentHistoryEntryFunctions::fullTreatmentDisplay)
                )
                EvaluationFactory.pass("Patient has received adjuvant $treatmentsString", "Received adjuvant $treatmentsString")
            }

            treatmentSummary.hasApproximateMatch() -> {
                val categoryString = warnCategory.display()
                EvaluationFactory.warn(
                    "Patient has received adjuvant $categoryString but not of specific type",
                    "Received adjuvant $categoryString but not of specific type)"
                )
            }

            treatmentSummary.hasPossibleTrialMatch() -> {
                EvaluationFactory.warn(
                    "Patient has received adjuvant trial treatment",
                    "Received adjuvant trial treatment"
                )
            }

            else -> {
                val namesString = Format.concatItemsWithAnd(types)
                EvaluationFactory.fail(
                    "Patient has not received adjuvant treatment with type $namesString",
                    "Not received adjuvant $namesString"
                )
            }
        }
    }
}