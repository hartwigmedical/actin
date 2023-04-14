package com.hartwig.actin.soc.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.ImmutableEvaluation
import com.hartwig.actin.soc.evaluation.EvaluationFactory
import com.hartwig.actin.soc.evaluation.EvaluationFunction

class HasAtLeastCertainAge internal constructor(private val referenceYear: Int, private val minAge: Int) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val age = referenceYear - record.clinical().patient().birthYear()
        val result: EvaluationResult = if (age > minAge) {
            EvaluationResult.PASS
        } else if (age == minAge) {
            EvaluationResult.UNDETERMINED
        } else {
            EvaluationResult.FAIL
        }
        val builder: ImmutableEvaluation.Builder = EvaluationFactory.unrecoverable().result(result)
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Patient is younger than $minAge years old")
            builder.addFailGeneralMessages("Inadequate age")
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages("Could not determine whether patient is at least $minAge years old")
            builder.addUndeterminedGeneralMessages("Undetermined age")
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Patient is at least $minAge years old")
            builder.addPassGeneralMessages("Adequate age")
        }
        return builder.build()
    }
}