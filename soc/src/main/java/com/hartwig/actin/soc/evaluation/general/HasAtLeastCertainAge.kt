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
        return when {
            age > minAge ->
                EvaluationFactory.pass("Patient is at least $minAge years old", "Adequate age")
            age == minAge ->
                EvaluationFactory.undetermined("Could not determine whether patient is at least $minAge years old", "Undetermined age")
            else ->
                EvaluationFactory.fail("Patient is younger than $minAge years old", "Inadequate age")
        }
    }
}