package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.doid.DoidModel

enum class MetastaticCancerEvaluation {
    METASTATIC,
    NON_METASTATIC,
    UNDETERMINED,
    DATA_MISSING;
}

object MetastaticCancerEvaluator {

    val STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS = setOf(DoidConstants.BRAIN_CANCER_DOID, DoidConstants.HEAD_AND_NECK_CANCER_DOID)

    fun isMetastatic(record: PatientRecord, doidModel: DoidModel): MetastaticCancerEvaluation {
        return record.tumor.stage?.let {
            when {
                TumorEvaluationFunctions.isStageMatch(it, setOf(TumorStage.III, TumorStage.IV)) -> MetastaticCancerEvaluation.METASTATIC

                TumorEvaluationFunctions.isStageMatch(it, setOf(TumorStage.II)) && DoidEvaluationFunctions.isOfAtLeastOneDoidType(
                    doidModel,
                    record.tumor.doids,
                    STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS
                ) -> {
                    MetastaticCancerEvaluation.UNDETERMINED
                }

                else -> MetastaticCancerEvaluation.NON_METASTATIC
            }
        } ?: MetastaticCancerEvaluation.DATA_MISSING
    }
}