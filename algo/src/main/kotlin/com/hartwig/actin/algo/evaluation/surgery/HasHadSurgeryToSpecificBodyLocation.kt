package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.BodyLocationCategory

class HasHadSurgeryToSpecificBodyLocation(bodyLocations: Set<BodyLocationCategory>): EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        TODO("Not yet implemented")
    }
}