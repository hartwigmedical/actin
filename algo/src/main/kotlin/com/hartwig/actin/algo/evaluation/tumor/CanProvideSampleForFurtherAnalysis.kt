package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.ExperimentType

class CanProvideSampleForFurtherAnalysis : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.molecularHistory.latestOrangeMolecularRecord()?.experimentType != ExperimentType.HARTWIG_WHOLE_GENOME) {
            EvaluationFactory.recoverableUndetermined("Undetermined if sample for FFPE analysis can be provided")
        } else
            EvaluationFactory.pass("WGS results present so assumed that sample for FFPE analysis can be provided")
    }
}