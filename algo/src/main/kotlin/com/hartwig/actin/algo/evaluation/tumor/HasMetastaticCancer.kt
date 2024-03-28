package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.doid.DoidModel

class HasMetastaticCancer (private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val stage = record.tumor.stage
            ?: return EvaluationFactory.undetermined(
                "Tumor stage details are missing, if cancer is metastatic cannot be determined",
                "Undetermined metastatic cancer"
            )

        return if (isStageMatch(stage, TumorStage.III) || isStageMatch(stage, TumorStage.IV)) {
            EvaluationFactory.pass("Tumor stage $stage is considered metastatic", METASTATIC_CANCER)
        } else if (isStageMatch(stage, TumorStage.II)) {
            val tumorDoids = record.tumor.doids
            if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
                EvaluationFactory.undetermined(
                    "Could not be determined if tumor stage $stage is considered metastatic",
                    "Undetermined $METASTATIC_CANCER"
                )
            } else if (DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, STAGE_II_POTENTIALLY_METASTATIC_CANCERS)) {
                EvaluationFactory.warn(
                    "Could not be determined if tumor stage $stage is considered metastatic",
                    "Undetermined $METASTATIC_CANCER"
                )
            } else {
                failEvaluation(stage)
            }
        } else {
            failEvaluation(stage)
        }
    }

    companion object {
        val STAGE_II_POTENTIALLY_METASTATIC_CANCERS = setOf(DoidConstants.BRAIN_CANCER_DOID, DoidConstants.HEAD_AND_NECK_CANCER_DOID)
        private const val METASTATIC_CANCER: String = "Metastatic cancer"
        private const val NOT_METASTATIC_CANCER: String = "No metastatic cancer"

        private fun isStageMatch(stage: TumorStage, stageToMatch: TumorStage): Boolean {
            return stage == stageToMatch || stage.category == stageToMatch
        }

        private fun failEvaluation(stage: TumorStage): Evaluation {
            return EvaluationFactory.fail("Tumor stage $stage is not considered metastatic", NOT_METASTATIC_CANCER)
        }
    }
}