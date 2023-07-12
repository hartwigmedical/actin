package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHadAnyCancerTreatment internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.clinical().priorTumorTreatments().isEmpty()) {
            EvaluationFactory.fail("Patient has not had any prior cancer treatments", "Has not had any cancer treatment")
        } else {
            EvaluationFactory.pass("Patient has had prior cancer treatment", "Had had any cancer treatment")
        }
    }
}