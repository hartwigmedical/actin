package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadSomeSpecificTreatmentsWithDoseReduction(private val treatment: Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasHadSpecificTreatmentResult = HasHadLimitedWeeksOfSpecificTreatment(treatment, null).evaluate(record).result
        val treatmentName = treatment.name.lowercase()

        return when (hasHadSpecificTreatmentResult) {
            EvaluationResult.PASS, EvaluationResult.NOT_EVALUATED, EvaluationResult.WARN -> {
                EvaluationFactory.undetermined("Has received $treatmentName but unknown if there may have been a dose reduction")
            }

            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.undetermined("Undetermined if patient may have received $treatmentName and if there may have been a dose reduction")
            }

            EvaluationResult.FAIL -> EvaluationFactory.fail("Has not received $treatmentName hence also not received dose reduction")
        }
    }
}