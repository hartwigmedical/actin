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
            return EvaluationFactory.undetermined("Undetermined secondary glioblastoma (tumor location unknown)")
        }
        for (tumorDoid in tumorDoids ?: emptySet()) {
            if (doidModel.doidWithParents(tumorDoid).contains(DoidConstants.GLIOBLASTOMA_DOID)) {
                return EvaluationFactory.warn("Unclear if ${doidModel.resolveTermForDoid(tumorDoid)} is considered secondary glioblastoma")
            }
        }
        return EvaluationFactory.fail("No (secondary) glioblastoma")
    }
}