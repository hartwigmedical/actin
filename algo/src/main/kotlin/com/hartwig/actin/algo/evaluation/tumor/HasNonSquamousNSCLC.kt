package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasNonSquamousNSCLC internal constructor(private val doidModel: DoidModel) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical().tumor().doids()
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(
                "Could not determine whether patient non-squamous NSCLC tumor type",
                "Undetermined non-squamous NSCLC tumor type"
            )
        }

        val isSquamousNSCLC = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.LUNG_SQUAMOUS_CELL_CARCINOMA_DOID)
        val isAdenoSquamousNSCLC =
            DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.LUNG_ADENOSQUAMOUS_CARCINOMA_DOID)
        val isNonSquamousNSCLC = DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, NON_SQUAMOUS_NSCLC_DOIDS)
        val isNonSmallNSCLC = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
        val isExactLungCarcinoma = DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.LUNG_CARCINOMA_DOID)
        val isExactLungCancer = DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.LUNG_CANCER_DOID)

        if (isSquamousNSCLC || isAdenoSquamousNSCLC) {
            return EvaluationFactory.fail("Patient has no non-squamous non-small cell lung cancer", "No non-squamous NSCLC")
        } else if (isNonSquamousNSCLC) {
            return EvaluationFactory.pass(
                "Patient has non-squamous non-small cell lung cancer",
                "Has non-squamous NSCLC"
            )
        } else if (isNonSmallNSCLC || isExactLungCarcinoma || isExactLungCancer) {
            return EvaluationFactory.undetermined(
                "Undetermined if patient may have non-squamous non-small cell lung cancer",
                "Undetermined non-squamous NSCLC"
            )
        } else {
            return EvaluationFactory.fail("Patient has no non-squamous non-small cell lung cancer", "No non-squamous NSCLC")
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
