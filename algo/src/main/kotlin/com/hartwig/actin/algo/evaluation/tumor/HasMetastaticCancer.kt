package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.doid.DoidModel

class HasMetastaticCancer(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val metastaticStageEvaluation = HasTumorStage(setOf(TumorStage.III, TumorStage.IV)).evaluate(record)
        val tumorDoids = record.tumor.doids
        val stageMessage = record.tumor.stage?.display() ?: "(derived stage: ${record.tumor.derivedStages?.joinToString(" or ")})"
        val potentiallyMetastaticAtStageII =
            DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, STAGE_II_POTENTIALLY_METASTATIC_CANCERS)
        val isNotStageI = (record.tumor.stage != TumorStage.I) && (record.tumor.derivedStages?.contains(TumorStage.I) != true)

        return when {
            metastaticStageEvaluation.result == EvaluationResult.PASS -> {
                EvaluationFactory.pass("Stage $stageMessage is considered metastatic")
            }

            (isNotStageI && potentiallyMetastaticAtStageII) || metastaticStageEvaluation.result == EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.undetermined("Undetermined if stage $stageMessage is considered metastatic")
            }

            else -> {
                EvaluationFactory.fail("Stage $stageMessage is not considered metastatic")
            }
        }
    }

    companion object {
        val STAGE_II_POTENTIALLY_METASTATIC_CANCERS = setOf(DoidConstants.BRAIN_CANCER_DOID, DoidConstants.HEAD_AND_NECK_CANCER_DOID)
    }
}