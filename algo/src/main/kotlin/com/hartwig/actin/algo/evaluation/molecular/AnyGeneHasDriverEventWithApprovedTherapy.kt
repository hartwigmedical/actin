package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.personalization.datamodel.TumorType

class AnyGeneHasDriverEventWithApprovedTherapy(private val genes: List<String>, val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val isLungCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_CANCER_DOID)

        return when {
            isLungCancer -> HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(genes.toSet(), emptySet()).evaluate(record)

            else -> {
                EvaluationFactory.undetermined(
                    "Driver events in genes with approved therapy are currently not determined",
                    "Undetermined if there are driver events with approved therapy"
                )
            }
        }
    }
}