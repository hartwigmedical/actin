package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasOligometastaticCancer(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val metastaticCancerEvaluation = MetastaticCancerEvaluator.isMetastatic(record, doidModel)

        return when (metastaticCancerEvaluation) {
            MetastaticCancerEvaluation.DATA_MISSING -> {
                EvaluationFactory.undetermined("Undetermined if oligometastatic cancer (tumor stage missing)")
            }

            MetastaticCancerEvaluation.METASTATIC, MetastaticCancerEvaluation.UNDETERMINED -> {
                EvaluationFactory.undetermined("Undetermined if oligometastatic cancer")
            }

            MetastaticCancerEvaluation.NON_METASTATIC -> {
                EvaluationFactory.fail("No oligometastatic cancer (stage ${record.tumor.stage?.display()})")
            }
        }
    }
}