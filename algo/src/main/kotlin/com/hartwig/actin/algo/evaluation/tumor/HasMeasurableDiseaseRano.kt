package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasMeasurableDiseaseRano(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasMeasurableDisease = record.tumor.hasMeasurableDisease ?: return EvaluationFactory.recoverableUndetermined(
            "Data regarding measurable disease is missing, unknown if measurable by RANO", "Undetermined RANO measurable disease"
        )

        return if (hasMeasurableDisease && !DoidEvaluationFunctions.isOfDoidType(
                doidModel,
                record.tumor.doids,
                DoidConstants.CNS_CANCER_DOID
            )
        ) {
            EvaluationFactory.warn(
                "Patient has measurable disease, but given the patient's tumor type uncertain if this has been evaluated against RANO?",
                "Measurable disease by RANO unknown"
            )
        } else if (hasMeasurableDisease) {
            EvaluationFactory.recoverablePass("Patient has measurable disease", "Has measurable disease")
        } else {
            EvaluationFactory.recoverableFail("Patient has no measurable disease", "No measurable disease")
        }
    }
}