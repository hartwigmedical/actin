package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadLimitedTreatmentsWithCategory(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>?,
    private val maxTreatmentLines: Int,
    private val treatmentIsRequired: Boolean
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = if (types == null) {
            TreatmentSummaryForCategory.createForTreatmentHistory(record.oncologicalHistory, category)
        } else {
            TreatmentSummaryForCategory.createForTreatmentHistory(
                record.oncologicalHistory, category, { historyEntry -> historyEntry.matchesTypeFromSet(types) }
            )
        }
        val treatmentString = if (types == null) {
            category.display()
        } else {
            "${Format.concatItemsWithOr(types)} ${category.display()}"
        }
        val messageEnding = "received at most $maxTreatmentLines lines of $treatmentString"

        return when {
            treatmentSummary.numSpecificMatches() + treatmentSummary.numApproximateMatches + treatmentSummary.numPossibleTrialMatches <= maxTreatmentLines && (!treatmentIsRequired || treatmentSummary.hasSpecificMatch()) -> {
                EvaluationFactory.pass("Has $messageEnding")
            }

            treatmentIsRequired && !treatmentSummary.hasSpecificMatch() && !treatmentSummary.hasApproximateMatch() && !treatmentSummary.hasPossibleTrialMatch() -> {
                EvaluationFactory.fail("Has not received $treatmentString treatment")
            }

            treatmentSummary.numSpecificMatches() <= maxTreatmentLines -> {
                EvaluationFactory.undetermined("Undetermined if $messageEnding")
            }

            else -> {
                EvaluationFactory.fail("Has not $messageEnding")
            }
        }
    }
}