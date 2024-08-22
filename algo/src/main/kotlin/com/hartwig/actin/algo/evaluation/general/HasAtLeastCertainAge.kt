package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasAtLeastCertainAge(private val referenceYear: Int, private val minAge: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val age = referenceYear - record.patient.birthYear
        return when {
            age > minAge ->
                EvaluationFactory.pass("Patient is at least $minAge years old", "Age above $minAge")

            age == minAge ->
                EvaluationFactory.undetermined(
                    "Patient birth year is " + record.patient.birthYear +
                            ", could not determine whether patient is at least $minAge years old", "Undetermined if age is above $minAge"
                )

            else ->
                EvaluationFactory.fail("Patient is younger than $minAge years old", "Age below $minAge")
        }
    }
}