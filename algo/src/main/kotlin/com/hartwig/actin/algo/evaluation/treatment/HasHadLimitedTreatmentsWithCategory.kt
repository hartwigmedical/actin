package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadLimitedTreatmentsWithCategory(
    private val category: TreatmentCategory,
    private val maxTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(record.oncologicalHistory, category)

        return when {
            treatmentSummary.numSpecificMatches() + treatmentSummary.numPossibleTrialMatches <= maxTreatmentLines -> {
                EvaluationFactory.pass("Has received at most " + maxTreatmentLines + " lines of " + category.display())
            }

            treatmentSummary.numSpecificMatches() <= maxTreatmentLines -> {
                EvaluationFactory.undetermined("Undetermined if received at most " + maxTreatmentLines + " lines of " + category.display())
            }

            else -> {
                EvaluationFactory.fail("Has not received at most " + maxTreatmentLines + " lines of " + category.display())
            }
        }
    }
}