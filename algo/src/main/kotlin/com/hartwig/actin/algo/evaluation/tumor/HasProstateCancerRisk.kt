package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasProstateCancerRisk(private val risks: List<String>, private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Prostate cancer undetermined (tumor doids missing)")
        }
        if (DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.PROSTATE_CANCER_DOID)) {
            return EvaluationFactory.undetermined(
                "Undetermined if patient has " +
                        "${Format.concatLowercaseWithCommaAndOr(risks)} risk prostate cancer"
            )
        }
        return EvaluationFactory.fail("No prostate cancer")
    }
}