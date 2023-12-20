package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.TumorDetails

class IsEligibleForOnLabelTreatment : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val tumor = record.clinical().tumor()
        return if (tumorIsCUP(tumor)) {
            EvaluationFactory.undetermined(
                "Tumor type is CUP and eligibility for on-label treatment is therefore undetermined",
                "Tumor type CUP - Eligibility for on-label treatment undetermined"
            )
        } else if (record.clinical().oncologicalHistory().isEmpty()) {
            EvaluationFactory.undetermined(
                "Patient has not had any prior cancer treatments and therefore undetermined eligibility for on-label treatment",
                "Undetermined eligibility for on-label treatment"
            )
        } else {
            EvaluationFactory.notEvaluated(
                "Assumed no eligibility for on-label treatment since patient has had prior cancer treatment",
                "Assumed no eligibility for on-label treatment"
            )
        }
    }

    private fun tumorIsCUP(tumor: TumorDetails): Boolean {
        val location = tumor.primaryTumorLocation()
        val subLocation = tumor.primaryTumorSubLocation()
        return location != null && subLocation != null && location == "Unknown" && subLocation == "CUP"
    }
}