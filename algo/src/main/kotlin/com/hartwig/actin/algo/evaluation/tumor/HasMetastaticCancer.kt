package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasMetastaticCancer(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val metastaticCancerEvaluation = MetastaticCancerEvaluator.isMetastatic(record, doidModel)
        val stageDisplay = record.tumor.stage?.display()

        return when (metastaticCancerEvaluation) {
            MetastaticCancerEvaluation.DATA_MISSING -> {
                EvaluationFactory.undetermined(labels.hasMetastaticCancerUndeterminedStageMissing())
            }

            MetastaticCancerEvaluation.METASTATIC -> EvaluationFactory.pass(labels.hasMetastaticCancerPass(stageDisplay!!))

            MetastaticCancerEvaluation.UNDETERMINED -> {
                EvaluationFactory.undetermined(labels.hasMetastaticCancerUndetermined(stageDisplay!!))
            }

            MetastaticCancerEvaluation.NON_METASTATIC -> EvaluationFactory.fail(labels.hasMetastaticCancerFail(stageDisplay!!))
        }
    }
}