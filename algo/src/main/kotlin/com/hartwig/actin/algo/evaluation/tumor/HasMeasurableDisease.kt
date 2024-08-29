package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMeasurableDisease : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasMeasurableDisease = record.tumor.hasMeasurableDisease
            ?: return EvaluationFactory.recoverableUndetermined(
                "Data regarding measurable disease is missing, unknown if measurable",
                "Undetermined measurable disease"
            )
        return if (hasMeasurableDisease) {
            EvaluationFactory.recoverablePass("Patient has measurable disease", "Has measurable disease")
        } else {
            EvaluationFactory.recoverableFail("Patient has no measurable disease", "No measurable disease")
        }
    }
}