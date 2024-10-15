package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsOATP1B3SubstrateMedication : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED

        return if (medications.isEmpty()) EvaluationFactory.fail(
            "Patient does not get OATP1B3 substrate medication",
            "No OATP1B3 substrate medication"
        )
        else {
            EvaluationFactory.undetermined(
                "Currently not determined if patient gets OATP1B3 substrate medication",
                "OATP1B3 medication requirements undetermined"
            )
        }
    }
}