package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasLesionsCloseToOrInvolvingAirway(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, record.tumor.doids)
        val isMajorAirwayCancer = MAJOR_AIRWAYS_CANCER.any { it in expandedDoidSet }
        val isLungCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID)

        with(record.tumor) {
            val noLesionsCloseToAirway =
                !isMajorAirwayCancer && otherLesions.isNullOrEmpty() && otherSuspectedLesions.isNullOrEmpty() && hasLungLesions == false

            return when {
                isMajorAirwayCancer -> {
                    EvaluationFactory.pass("Has lesions close to or involving airway")
                }

                hasLungLesions == true || isLungCancer || hasSuspectedLungLesions == true -> {
                    val message = if (hasLungLesions != true && hasSuspectedLungLesions == true) {
                        "Suspected lung"
                    } else "Lung"

                    EvaluationFactory.warn("$message lesions which may be close to or involving airway")
                }

                noLesionsCloseToAirway -> {
                    EvaluationFactory.fail("No lesions close to or involving airway")
                }

                else -> {
                    EvaluationFactory.undetermined("Undetermined lesions close to or involving airway")
                }
            }
        }
    }

    companion object {
        val MAJOR_AIRWAYS_CANCER = setOf(
            DoidConstants.BRONCHUS_CANCER,
            DoidConstants.MAIN_BRONCHUS_CANCER,
            DoidConstants.LUNG_HILUM_CANCER,
            DoidConstants.TRACHEAL_CANCER
        )
    }
}