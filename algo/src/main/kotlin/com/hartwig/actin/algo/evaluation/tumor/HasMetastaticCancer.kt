package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasMetastaticCancer(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val metastaticCancerEvaluation = MetastaticCancerEvaluator.isMetastatic(record, doidModel)
        val stageDisplay = record.tumor.stage?.display()

        return when (metastaticCancerEvaluation) {
            MetastaticCancerEvaluation.DATA_MISSING -> {
                EvaluationFactory.undetermined("Undetermined if metastatic cancer (tumor stage missing)")
            }

            MetastaticCancerEvaluation.METASTATIC -> EvaluationFactory.pass("Stage $stageDisplay is considered metastatic")

            MetastaticCancerEvaluation.UNDETERMINED -> {
                EvaluationFactory.undetermined("Undetermined if stage $stageDisplay is considered metastatic")
            }

            MetastaticCancerEvaluation.NON_METASTATIC -> EvaluationFactory.fail("Stage $stageDisplay is not considered metastatic")
        }
    }
}