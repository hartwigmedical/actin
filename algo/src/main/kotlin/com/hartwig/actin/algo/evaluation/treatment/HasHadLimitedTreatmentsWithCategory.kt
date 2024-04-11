package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadLimitedTreatmentsWithCategory(
    private val category: TreatmentCategory,
    private val maxTreatmentLines: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(record.oncologicalHistory, category)

        return when {
            treatmentSummary.numSpecificMatches() + treatmentSummary.numPossibleTrialMatches <= maxTreatmentLines -> {
                EvaluationFactory.pass(
                    "Patient has received at most " + maxTreatmentLines + " lines of " + category.display() + " treatment",
                    "Has received at most " + maxTreatmentLines + " lines of " + category.display()
                )
            }

            treatmentSummary.numSpecificMatches() <= maxTreatmentLines -> {
                EvaluationFactory.undetermined(
                    "Patient may have received more than " + maxTreatmentLines + " lines of " + category.display() + " treatment",
                    "Undetermined if received at most " + maxTreatmentLines + " lines of " + category.display()
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has received more than " + maxTreatmentLines + " lines of " + category.display(),
                    "Has not received at most " + maxTreatmentLines + " lines of " + category.display()
                )
            }
        }
    }
}