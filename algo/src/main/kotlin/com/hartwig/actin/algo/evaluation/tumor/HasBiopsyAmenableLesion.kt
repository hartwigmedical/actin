package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.ExperimentType

class HasBiopsyAmenableLesion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.molecularHistory.latestOrangeMolecularRecord()?.experimentType != ExperimentType.HARTWIG_WHOLE_GENOME) {
            EvaluationFactory.recoverableUndetermined("Undetermined if biopsy amenable lesions present")
        } else {
            EvaluationFactory.pass("Biopsy amenability assumed because of WGS analysis")
        }
    }
}