package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasSecondaryGlioblastoma(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(labels.hasSecondaryGlioblastomaUndetermined())
        }
        for (tumorDoid in tumorDoids ?: emptySet()) {
            if (doidModel.doidWithParents(tumorDoid).contains(DoidConstants.GLIOBLASTOMA_DOID)) {
                return EvaluationFactory.warn(labels.hasSecondaryGlioblastomaWarn(doidModel.resolveTermForDoid(tumorDoid)))
            }
        }
        return EvaluationFactory.fail(labels.hasSecondaryGlioblastomaFail())
    }
}