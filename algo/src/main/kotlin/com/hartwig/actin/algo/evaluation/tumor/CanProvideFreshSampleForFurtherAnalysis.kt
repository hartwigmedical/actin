package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularHistory

class CanProvideFreshSampleForFurtherAnalysis(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val molecularRecord = MolecularHistory(record.molecularTests).latestOrangeMolecularRecord()
        return if (molecularRecord?.experimentType != ExperimentType.HARTWIG_WHOLE_GENOME) {
            EvaluationFactory.recoverableUndetermined(labels.canProvideFreshSampleForFurtherAnalysisRecoverableUndetermined())
        } else
            EvaluationFactory.pass(labels.canProvideFreshSampleForFurtherAnalysisPass())
    }
}