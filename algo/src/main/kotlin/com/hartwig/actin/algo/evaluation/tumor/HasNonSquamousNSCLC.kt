package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasNonSquamousNSCLC(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Undetermined non-squamous NSCLC tumor type")
        }

        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, tumorDoids)
        val isSquamousNSCLC = DoidConstants.LUNG_SQUAMOUS_CELL_CARCINOMA_DOID in expandedDoidSet
        val isAdenoSquamousNSCLC = DoidConstants.LUNG_ADENOSQUAMOUS_CARCINOMA_DOID in expandedDoidSet
        val isNonSquamousNSCLC = NON_SQUAMOUS_NSCLC_DOIDS.any { it in expandedDoidSet }
        val isNonSmallNSCLC = DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID in expandedDoidSet
        val isExactLungCarcinoma = DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.LUNG_CARCINOMA_DOID)
        val isExactLungCancer = DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.LUNG_CANCER_DOID)

        return when {
            isSquamousNSCLC || isAdenoSquamousNSCLC -> {
                EvaluationFactory.fail("Has no non-squamous NSCLC")
            }

            isNonSquamousNSCLC -> {
                EvaluationFactory.pass("Has non-squamous NSCLC")
            }

            isNonSmallNSCLC || isExactLungCarcinoma || isExactLungCancer -> {
                EvaluationFactory.undetermined("Undetermined if non-squamous NSCLC tumor type")
            }

            else -> {
                EvaluationFactory.fail("Has no non-squamous NSCLC")
            }
        }

    }

    companion object {
        val NON_SQUAMOUS_NSCLC_DOIDS = setOf(
            DoidConstants.LUNG_ADENOCARCINOMA_DOID,
            DoidConstants.LUNG_LARGE_CELL_CARCINOMA_DOID,
            DoidConstants.LUNG_NON_SQUAMOUS_NON_SMALL_CARCINOMA_DOID
        )
    }
}
