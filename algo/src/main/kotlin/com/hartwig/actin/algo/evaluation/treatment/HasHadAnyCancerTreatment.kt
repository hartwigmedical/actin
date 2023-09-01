package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHadAnyCancerTreatment : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.clinical().treatmentHistory().isEmpty()) {
            EvaluationFactory.fail("Patient has not had any prior cancer treatments", "Has not had any cancer treatment")
        } else {
            EvaluationFactory.pass("Patient has had prior cancer treatment", "Had had any cancer treatment")
        }
    }
}