package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

class HasHadAdjuvantSpecificTreatment(private val names: Set<String>, private val warnCategory: TreatmentCategory) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val adjuvantTreatmentHistory = record.clinical().treatmentHistory().filter { it.intents()?.contains(Intent.ADJUVANT) == true }

        val treatmentSummary =
            TreatmentSummaryForCategory.createForTreatmentHistory(adjuvantTreatmentHistory, warnCategory) { treatmentHistoryEntry ->
                treatmentHistoryEntry.treatments().flatMap { it.synonyms() + it.name() }.intersect(names).isNotEmpty()
            }

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                val matchingTreatmentNames = treatmentSummary.specificMatches
                    .map { it.treatments().joinToString(";") { treatment -> treatment.name() } }
                val treatmentsString = Format.concatLowercaseWithAnd(matchingTreatmentNames)
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
                val namesString = Format.concatLowercaseWithAnd(names)
                EvaluationFactory.fail(
                    "Patient has not received adjuvant treatment with name $namesString",
                    "Not received adjuvant $namesString"
                )
            }
        }
    }
}