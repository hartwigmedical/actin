package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.ExperimentType

class HasBiopsyAmenableLesion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.molecularHistory.latestOrangeMolecularRecord()?.experimentType != ExperimentType.HARTWIG_WHOLE_GENOME) {
            EvaluationFactory.recoverableUndetermined(
                "Currently biopsy-amenability of lesions cannot be determined without WGS",
                "Biopsy amenability unknown"
            )
        } else {
            EvaluationFactory.pass(
                "It is assumed that patient will have biopsy-amenable lesions (presence of WGS analysis)",
                "Biopsy amenability assumed"
            )
        }
    }
}