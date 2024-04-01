package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasMolecularEventWithTargetedTherapyForNSCLCAvailable(geneToIgnore: String?) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.recoverableUndetermined(
            "Molecular events in genes with approved therapy for NSCLC are currently not determined",
            "Undetermined if there are molecular events with approved therapy for NSCLC"
        )
    }
}