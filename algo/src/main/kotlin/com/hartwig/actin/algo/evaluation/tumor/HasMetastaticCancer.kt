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
        val stageEvaluation = HasTumorStage(setOf(TumorStage.III, TumorStage.IV)).evaluate(record)
        val tumorDoids = record.tumor.doids
        val stage = record.tumor.stage?.display() ?: "(derived stage: ${record.tumor.derivedStages?.joinToString(" or ")})"
        val potentiallyMetastaticAtStageII =
            DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, STAGE_II_POTENTIALLY_METASTATIC_CANCERS)
        val isNotStageI = (record.tumor.stage != TumorStage.I) && (record.tumor.derivedStages?.contains(TumorStage.I) != true)

        val undeterminedGeneralMessage = "Undetermined if $METASTATIC_CANCER"
        val undeterminedSpecificMessage = "Could not be determined if tumor stage $stage is considered metastatic"

        return when {
            stageEvaluation.result == EvaluationResult.PASS -> {
                EvaluationFactory.pass("Tumor stage $stage is considered metastatic", METASTATIC_CANCER)
            }

            (isNotStageI && potentiallyMetastaticAtStageII) || stageEvaluation.result == EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.undetermined(undeterminedSpecificMessage, undeterminedGeneralMessage)
            }

            else -> {
                EvaluationFactory.fail("Tumor stage $stage is not considered metastatic", NOT_METASTATIC_CANCER)
            }
        }
    }

    companion object {
        val STAGE_II_POTENTIALLY_METASTATIC_CANCERS = setOf(DoidConstants.BRAIN_CANCER_DOID, DoidConstants.HEAD_AND_NECK_CANCER_DOID)
        private const val METASTATIC_CANCER: String = "Metastatic cancer"
        private const val NOT_METASTATIC_CANCER: String = "No metastatic cancer"
    }
}