package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasPostOperativeBaselineContrastEnhancedMRIScan internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Currently presence of post-operative baseline contrast enhancing MRI scan is undetermined",
            "Undetermined presence post-operative baseline contrast enhancing MRI scan"
        )
    }
}