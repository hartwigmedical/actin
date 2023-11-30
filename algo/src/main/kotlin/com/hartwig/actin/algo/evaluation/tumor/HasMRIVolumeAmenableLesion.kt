package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasMRIVolumeAmenableLesion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "MRI volume measurement amenability of lesions currently cannot be automatically determined",
            "Undetermined MRI volume measurement amenability"
        )
    }
}