package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasOvarianCancerWithMucinousComponent(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(labels.hasOvarianCancerWithMucinousComponentUndetermined())
        }
        val isOvarianMucinousType = DoidEvaluationFunctions.isOfAtLeastOneDoidType(
            doidModel, tumorDoids, OVARIAN_MUCINOUS_DOIDS
        )
        val hasSpecificOvarianMucinousCombination = DoidEvaluationFunctions.isOfDoidCombinationType(tumorDoids, OVARIAN_MUCINOUS_DOID_SET)
        return if (isOvarianMucinousType || hasSpecificOvarianMucinousCombination) {
            EvaluationFactory.pass(labels.hasOvarianCancerWithMucinousComponentPass())
        } else
            EvaluationFactory.fail(labels.hasOvarianCancerWithMucinousComponentFail())
    }

    companion object {
        val OVARIAN_MUCINOUS_DOIDS = setOf(
            DoidConstants.OVARIAN_MUCINOUS_MALIGNANT_ADENOFIBROMA_DOID,
            DoidConstants.OVARIAN_MUCINOUS_CYSTADENOFIBROMA_DOID,
            DoidConstants.MUCINOUS_OVARIAN_CYSTADENOMA_DOID,
            DoidConstants.OVARIAN_MUCINOUS_CYSTADENOCARCINOMA_DOID,
            DoidConstants.OVARIAN_MUCINOUS_ADENOCARCINOMA_DOID,
            DoidConstants.OVARIAN_MUCINOUS_NEOPLASM_DOID,
            DoidConstants.OVARIAN_MUCINOUS_ADENOFIBROMA_DOID,
            DoidConstants.OVARIAN_SEROMUCINOUS_CARCINOMA_DOID
        )
        val OVARIAN_MUCINOUS_DOID_SET = setOf(DoidConstants.MUCINOUS_ADENOCARCINOMA_DOID, DoidConstants.OVARIAN_CANCER_DOID)
    }
}