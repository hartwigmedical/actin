package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsPGPInhibitingMedication : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED

        return if (medications.isEmpty()) EvaluationFactory.fail(
            "Patient does not get PGP inhibiting medication",
            "No PGP inhibiting medication"
        )
        else {
            EvaluationFactory.undetermined(
                "Currently not determined if patient gets PGP inhibiting medication",
                "PGP medication requirements undetermined"
            )
        }
    }
}