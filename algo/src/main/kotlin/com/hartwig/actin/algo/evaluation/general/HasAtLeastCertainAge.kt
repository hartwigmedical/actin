package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAtLeastCertainAge(private val referenceYear: Int, private val minAge: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val age = referenceYear - record.patient.birthYear
        return when {
            age > minAge ->
                EvaluationFactory.pass("Patient is at least $minAge years old")

            age == minAge -> EvaluationFactory.undetermined(
                "Undetermined if patient with birth year ${record.patient.birthYear})"
                        + " is above $minAge years old"
            )

            else ->
                EvaluationFactory.fail("Patient is younger than $minAge years old")
        }
    }
}