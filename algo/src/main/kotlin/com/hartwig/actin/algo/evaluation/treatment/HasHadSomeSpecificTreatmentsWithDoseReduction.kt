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
        val treatmentName = treatment.name

        return when (hasHadSpecificTreatmentResult) {
            EvaluationResult.PASS, EvaluationResult.WARN -> {
                EvaluationFactory.undetermined("$treatmentName in provided treatments but unknown if there may have been a dose reduction")
            }

            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.undetermined("Undetermined if ${treatmentName.lowercase()} in provided treatments and if there may have been a dose reduction")
            }

            EvaluationResult.FAIL -> EvaluationFactory.fail("$treatmentName not in provided treatments hence no dose reduction")
        }
    }
}