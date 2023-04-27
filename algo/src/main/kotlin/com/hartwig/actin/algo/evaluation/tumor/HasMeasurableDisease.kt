package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasMeasurableDisease internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasMeasurableDisease = record.clinical().tumor().hasMeasurableDisease()
            ?: return EvaluationFactory.undetermined(
                "Data regarding measurable disease is missing, unknown if measurable",
                "Undetermined measurable disease"
            )
        return if (hasMeasurableDisease) {
            EvaluationFactory.pass("Patient has measurable disease", "Measurable disease")
        } else {
            EvaluationFactory.fail("Patient has no measurable disease", "No measurable disease")
        }
    }
}