package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.doid.DoidModel

class IsEligibleForLocalLiverTreatment(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasLiverLesions = record.tumor.hasLiverLesions
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, record.tumor.doids)
        val hasLiverCancer = DoidConstants.LIVER_CANCER_DOID in expandedDoidSet

        return when {
            hasLiverCancer && hasLiverLesions != true -> {
                EvaluationFactory.warn(
                    "Patient has liver cancer and is hence potentially eligible for local liver treatment",
                    "Liver cancer (hence potential eligibility for local liver treatment)"
                )
            }

            hasLiverLesions == false -> {
                EvaluationFactory.fail(
                    "Patient has no liver lesions and is hence not eligible for local liver treatment",
                    "No liver lesions (hence no eligibility for local liver treatment)"
                )
            }

            hasLiverLesions == true -> {
                EvaluationFactory.undetermined(
                    "Undetermined if liver lesions are eligible for local liver treatment",
                    "Undetermined eligibility for local liver treatment"
                )
            }

            else -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has liver lesions and therefore undetermined if patient is eligible for local liver treatment",
                    "Undetermined liver lesions and therefore undetermined eligibility for local liver treatment"
                )
            }
        }
    }
}