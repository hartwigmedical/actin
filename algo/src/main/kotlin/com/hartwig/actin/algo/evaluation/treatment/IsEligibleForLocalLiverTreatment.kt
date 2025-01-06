package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class IsEligibleForLocalLiverTreatment(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverLesions = record.tumor.hasLiverLesions
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, record.tumor.doids)
        val hasLiverCancer = DoidConstants.LIVER_CANCER_DOID in expandedDoidSet

        return when {
            hasLiverCancer && hasLiverLesions != true -> {
                EvaluationFactory.warn("Liver cancer (hence potential eligibility for local liver treatment)")
            }

            hasLiverLesions == false -> {
                EvaluationFactory.fail("No liver lesions (hence no eligibility for local liver treatment)")
            }

            hasLiverLesions == true -> {
                EvaluationFactory.undetermined("Eligibility for local liver treatment undetermined")
            }

            else -> {
                EvaluationFactory.undetermined("Undetermined liver lesions and therefore undetermined eligibility for local liver treatment")
            }
        }
    }
}