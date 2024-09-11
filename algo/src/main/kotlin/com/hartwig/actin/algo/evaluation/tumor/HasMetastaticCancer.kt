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

        val stage = record.tumor.stage?.display() ?: "(derived stage: ${record.tumor.derivedStages?.joinToString(" or ")})"
        return when (stageEvaluation.result) {
            EvaluationResult.PASS -> {
                EvaluationFactory.pass("Tumor stage $stage is considered metastatic", METASTATIC_CANCER)
            }

            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.undetermined(
                    "Could not be determined if tumor stage $stage is considered metastatic",
                    "Undetermined $METASTATIC_CANCER"
                )
            }

            else -> {
                val tumorDoids = record.tumor.doids
                if (DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, STAGE_II_POTENTIALLY_METASTATIC_CANCERS)) {
                    EvaluationFactory.warn(
                        "Could not be determined if tumor stage $stage is considered metastatic",
                        "Undetermined $METASTATIC_CANCER"
                    )
                } else {
                    failEvaluation(stage)
                }
            }
        }
    }

    companion object {
        val STAGE_II_POTENTIALLY_METASTATIC_CANCERS = setOf(DoidConstants.BRAIN_CANCER_DOID, DoidConstants.HEAD_AND_NECK_CANCER_DOID)
        private const val METASTATIC_CANCER: String = "Metastatic cancer"
        private const val NOT_METASTATIC_CANCER: String = "No metastatic cancer"

        private fun failEvaluation(stage: String): Evaluation {
            return EvaluationFactory.fail("Tumor stage $stage is not considered metastatic", NOT_METASTATIC_CANCER)
        }
    }
}