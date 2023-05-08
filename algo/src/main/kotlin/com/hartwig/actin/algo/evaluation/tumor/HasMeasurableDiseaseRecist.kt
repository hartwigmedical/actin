package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasMeasurableDiseaseRecist internal constructor(private val doidModel: DoidModel) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasMeasurableDisease = record.clinical().tumor().hasMeasurableDisease() ?: return EvaluationFactory.undetermined(
            "Data regarding measurable disease is missing, unknown if measurable by RECIST", "Undetermined RECIST measurable disease"
        )

        return if (hasMeasurableDisease && DoidEvaluationFunctions.isOfAtLeastOneDoidType(
                doidModel,
                record.clinical().tumor().doids(),
                NON_RECIST_TUMOR_DOIDS
            )
        ) {
            EvaluationFactory.warn(
                "Patient has measurable disease, but given the patient's tumor type uncertain if this has been evaluated against RECIST?",
                "Measurable disease by RECIST unknown"
            )
        } else if (hasMeasurableDisease) {
            EvaluationFactory.pass("Patient has measurable disease", "Measurable disease")
        } else {
            EvaluationFactory.fail("Patient has no measurable disease", "No measurable disease")
        }
    }

    companion object {
        val NON_RECIST_TUMOR_DOIDS = setOf(
            DoidConstants.HEMATOLOGIC_CANCER_DOID,
            DoidConstants.BRAIN_CANCER_DOID,
            DoidConstants.LYMPHOMA_DOID,
            DoidConstants.MULTIPLE_MYELOMA_DOID
        )
    }
}