package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class IsLegallyInstitutionalized internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return unrecoverable()
            .result(EvaluationResult.NOT_EVALUATED)
            .addPassSpecificMessages("Currently assumed that patient is not legally institutionalized")
            .addPassGeneralMessages("Assumed patient is not legally institutionalized")
            .build()
    }
}