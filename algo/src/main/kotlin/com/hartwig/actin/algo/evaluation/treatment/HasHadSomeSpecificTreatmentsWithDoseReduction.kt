package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class HasHadSomeSpecificTreatmentsWithDoseReduction(private val treatments: List<Treatment>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasHadSpecificTreatmentResult = HasHadSomeSpecificTreatments(treatments, 1).evaluate(record).result
        val treatmentListing = Format.concatItemsWithAnd(treatments)

        return when (hasHadSpecificTreatmentResult) {
            EvaluationResult.PASS, EvaluationResult.NOT_EVALUATED, EvaluationResult.WARN -> {
                EvaluationFactory.undetermined("Has received $treatmentListing but unknown if there may have been a dose reduction")
            }

            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.undetermined("Undetermined if patient may have received $treatmentListing and if there may have been a dose reduction")
            }

            EvaluationResult.FAIL -> EvaluationFactory.fail("Has not received $treatmentListing hence also not received dose reduction")
        }
    }
}