package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasOligometastaticCancer(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val metastaticCancerEvaluation = MetastaticCancerEvaluator.isMetastatic(record, doidModel)

        return when (metastaticCancerEvaluation) {
            MetastaticCancerEvaluation.DATA_MISSING -> {
                EvaluationFactory.undetermined(labels.hasOligometastaticCancerUndeterminedMissing())
            }

            MetastaticCancerEvaluation.METASTATIC, MetastaticCancerEvaluation.UNDETERMINED -> {
                EvaluationFactory.undetermined(labels.hasOligometastaticCancerUndetermined())
            }

            MetastaticCancerEvaluation.NON_METASTATIC -> {
                EvaluationFactory.fail(labels.hasOligometastaticCancerFail(record.tumor.stage?.display()))
            }
        }
    }
}