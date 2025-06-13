package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.doid.DoidModel

class HasMetastaticCancer(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage ?: return EvaluationFactory.undetermined("Undetermined if metastatic cancer (tumor stage missing)")
        val stageMessage =
            record.tumor.stage?.display() ?: "(derived stage: ${record.tumor.derivedStages?.joinToString(" or ") { it.display() }})"
        val hasPotentiallyMetastaticStageIICancerType =
            DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, record.tumor.doids, STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS)

        return when {
            (stage.category ?: stage) in setOf(TumorStage.III, TumorStage.IV) -> {
                EvaluationFactory.pass("Stage $stageMessage is considered metastatic")
            }

            (stage.category ?: stage) == TumorStage.II && hasPotentiallyMetastaticStageIICancerType -> {
                EvaluationFactory.undetermined("Undetermined if stage $stageMessage is considered metastatic")
            }

            else -> {
                EvaluationFactory.fail("Stage $stageMessage is not considered metastatic")
            }
        }
    }

    companion object {
        val STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS = setOf(DoidConstants.BRAIN_CANCER_DOID, DoidConstants.HEAD_AND_NECK_CANCER_DOID)
    }
}