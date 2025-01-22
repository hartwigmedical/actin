package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadLimitedTreatmentsWithCategory(
    private val category: TreatmentCategory,
    private val maxTreatmentLines: Int,
    private val treatmentIsRequired: Boolean
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(record.oncologicalHistory, category)
        val messageEnding = "received at most " + maxTreatmentLines + " lines of " + category.display()

        return when {
            treatmentSummary.numSpecificMatches() + treatmentSummary.numPossibleTrialMatches <= maxTreatmentLines && (!treatmentIsRequired || treatmentSummary.hasSpecificMatch()) -> {
                EvaluationFactory.pass("Has $messageEnding")
            }

            treatmentSummary.numSpecificMatches() <= maxTreatmentLines && (!treatmentIsRequired || treatmentSummary.hasSpecificMatch() || treatmentSummary.hasPossibleTrialMatch()) -> {
                EvaluationFactory.undetermined("Undetermined if $messageEnding")
            }

            else -> {
                EvaluationFactory.fail("Has not $messageEnding")
            }
        }
    }
}