package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadSomeSpecificTreatmentsWithDoseReduction(
    private val treatment: Treatment,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasHadSpecificTreatmentResult = HasHadLimitedWeeksOfSpecificTreatment(treatment, null, labels).evaluate(record).result
        val treatmentName = treatment.name.lowercase()

        return when (hasHadSpecificTreatmentResult) {
            EvaluationResult.PASS, EvaluationResult.WARN -> {
                EvaluationFactory.undetermined(labels.hasHadSomeSpecificTreatmentsWithDoseReductionUndeterminedReceived(treatmentName))
            }

            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.undetermined(labels.hasHadSomeSpecificTreatmentsWithDoseReductionUndeterminedMayHaveReceived(treatmentName))
            }

            EvaluationResult.FAIL -> EvaluationFactory.fail(labels.hasHadSomeSpecificTreatmentsWithDoseReductionFail(treatmentName))
        }
    }
}
