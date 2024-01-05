package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class CurrentlyGetsBCRPInhibitingMedication() : EvaluationFunction {
    
    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Currently not determined if patient gets BCRP inhibiting/inducing medication",
            "BCRP medication requirements undetermined"
        )
    }
}