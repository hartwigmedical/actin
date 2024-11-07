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
        val hasLungLesions = record.tumor.hasLungLesions
        val hasSuspectedLungLesions = record.tumor.hasSuspectedLungLesions
        val hasOtherLesions = !record.tumor.otherLesions.isNullOrEmpty()
        val hasSuspectedOtherLesions = !record.tumor.otherSuspectedLesions.isNullOrEmpty()
        val noLesionsCloseToAirway =
            !isMajorAirwayCancer && !hasOtherLesions && !hasSuspectedOtherLesions && hasLungLesions == false

        return when {
            isMajorAirwayCancer -> {
                EvaluationFactory.pass("Patient has lesions close to or involving airway", "Lesions close to or involving airway")
            }

            hasLungLesions == true || isLungCancer || hasSuspectedLungLesions == true -> {
                val specificMessage = if (hasSuspectedLungLesions == true) " suspected" else ""
                val generalMessage = if (hasSuspectedLungLesions == true) "Suspected lung" else "Lung"
                EvaluationFactory.warn(
                    "Patient has$specificMessage lung lesions which may be close to or involving airways",
                    "$generalMessage lesions which may be close to or involving airway"
                )
            }

            noLesionsCloseToAirway -> {
                EvaluationFactory.fail(
                    "Patient does not have lesions close to or involving airway",
                    "Lesions not close to or involving airway"
                )
            }

            else -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has lesions close to or involving airway",
                    "Undetermined lesions close to or involving airway"
                )
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