package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsBCRPInducingMedication : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED

        return if (medications.isEmpty()) EvaluationFactory.fail(
            "Patient does not get BCRP inducing medication",
            "No BCRP inducing medication"
        )
        else {
            EvaluationFactory.undetermined(
                "Currently not determined if patient gets BCRP inducing medication",
                "BCRP medication requirements undetermined"
            )
        }
    }
}