package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasEvidenceOfCNSHemorrhageByMRI internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Currently it not determined if there is evidence of CNS hemorrhage on MRI scan",
            "Undetermined CNS hemorrhage by MRI"
        )
    }
}