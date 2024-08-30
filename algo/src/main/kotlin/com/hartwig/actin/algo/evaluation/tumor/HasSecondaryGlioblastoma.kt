package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasSecondaryGlioblastoma(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(
                "No tumor location/type configured for patient, unknown if patient has secondary glioblastoma",
                "Undetermined secondary glioblastoma"
            )
        }
        for (tumorDoid in tumorDoids ?: emptySet()) {
            if (doidModel.doidWithParents(tumorDoid).contains(DoidConstants.GLIOBLASTOMA_DOID)) {
                return EvaluationFactory.warn(
                    "Patient has " + doidModel.resolveTermForDoid(tumorDoid) + ", belonging to " +
                            doidModel.resolveTermForDoid(DoidConstants.GLIOBLASTOMA_DOID) +
                            ", unclear if this is considered secondary glioblastoma", "Unclear if considered secondary glioblastoma"
                )
            }
        }
        return EvaluationFactory.fail(
            "Patient has no (secondary) glioblastoma",
            "Tumor type"
        )
    }
}