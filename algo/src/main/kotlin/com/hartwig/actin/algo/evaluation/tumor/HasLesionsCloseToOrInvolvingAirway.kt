package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidType
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasLesionsCloseToOrInvolvingAirway(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val isMajorAirwayCancer = isOfAtLeastOneDoidType(doidModel, record.tumor.doids, MAJOR_AIRWAYS_CANCER_DOIDS)
        val isLungCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID)

        with(record.tumor) {
            val noLesionsCloseToAirway =
                !isMajorAirwayCancer && otherLesions.isNullOrEmpty() && otherSuspectedLesions.isNullOrEmpty() && hasLungLesions == false

            return when {
                isMajorAirwayCancer -> {
                    EvaluationFactory.pass(labels.hasLesionsCloseToOrInvolvingAirwayPass())
                }

                isLungCancer || hasLungLesions == true || hasSuspectedLungLesions == true -> {
                    val message = if (hasLungLesions != true && hasSuspectedLungLesions == true) {
                        labels.hasLesionsCloseToOrInvolvingAirwaySubjectSuspectedLung()
                    } else labels.hasLesionsCloseToOrInvolvingAirwaySubjectLung()

                    EvaluationFactory.warn(labels.hasLesionsCloseToOrInvolvingAirwayWarn(message))
                }

                noLesionsCloseToAirway -> {
                    EvaluationFactory.fail(labels.hasLesionsCloseToOrInvolvingAirwayFail())
                }

                else -> {
                    EvaluationFactory.undetermined(labels.hasLesionsCloseToOrInvolvingAirwayUndetermined())
                }
            }
        }
    }

    companion object {
        val MAJOR_AIRWAYS_CANCER_DOIDS = setOf(
            DoidConstants.BRONCHUS_CANCER_DOID,
            DoidConstants.MAIN_BRONCHUS_CANCER_DOID,
            DoidConstants.LUNG_HILUM_CANCER_DOID,
            DoidConstants.TRACHEAL_CANCER_DOID
        )
    }
}