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
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedParentsDoidTree(doidModel, record.tumor.doids)
        val hasLiverCancer = DoidConstants.LIVER_CANCER_DOID in expandedDoidSet

        return when {
            hasLiverCancer && hasLiverLesions != true -> {
                EvaluationFactory.undetermined("Liver cancer is present but undetermined if local liver treatment is available")
            }

            hasLiverLesions == false -> {
                EvaluationFactory.fail("No liver lesions hence local liver treatment is not available")
            }

            hasLiverLesions == true -> {
                EvaluationFactory.undetermined("Undetermined if local liver treatment is avaiable")
            }

            else -> {
                EvaluationFactory.undetermined("Liver lesions undetermined and therefore undetermined if local liver treatment is available")
            }
        }
    }
}