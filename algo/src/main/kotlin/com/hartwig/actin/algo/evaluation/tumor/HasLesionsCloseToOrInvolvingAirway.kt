package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.DoidModel

class HasLesionsCloseToOrInvolvingAirway(private val doidModel: DoidModel) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasRespiratorySystemCancer =
            DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.RESPIRATORY_SYSTEM_CANCER)
        val hasLungMetastases = TumorMetastasisEvaluator.evaluate(record.tumor.hasLungLesions, "lung").result == EvaluationResult.PASS

        return when {
            hasLungMetastases || hasRespiratorySystemCancer -> {
                EvaluationFactory.pass("Patient has lesions close to or involving airway", "Lesions close to or involving airway")
            }

            else -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has lesions close to or involving airway",
                    "Undetermined lesions close to or involving airway"
                )
            }
        }
    }
}