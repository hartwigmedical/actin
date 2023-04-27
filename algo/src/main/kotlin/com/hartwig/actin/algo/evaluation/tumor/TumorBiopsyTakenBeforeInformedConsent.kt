package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.ExperimentType

class TumorBiopsyTakenBeforeInformedConsent internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.molecular().type() != ExperimentType.WGS) {
            EvaluationFactory.undetermined(
                "Currently can't determine whether patient has taken a biopsy prior to IC without WGS",
                "Undetermined if biopsy has been obtained before IC"
            )
        } else
            EvaluationFactory.pass(
                "It is currently assumed that patient has taken a tumor biopsy prior to IC",
                "Biopsy taken before provided IC"
            )
    }
}