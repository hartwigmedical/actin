package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.ExperimentType

class CanProvideFreshSampleForFurtherAnalysis : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.molecularHistory.mostRecentWGS()?.type != ExperimentType.WHOLE_GENOME) {
            EvaluationFactory.undetermined(
                "Can't determine whether patient can provide fresh sample for FFPE analysis without WGS",
                "Unknown if fresh sample available for FFPE analysis"
            )
        } else
            EvaluationFactory.pass(
                "It is assumed that patient can provide fresh sample for FFPE analysis (presence of WGS analysis)",
                "Fresh sample provision ability for FFPE analysis assumed"
            )
    }
}