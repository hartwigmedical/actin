package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAtLeastCertainAge(
    private val referenceYear: Int,
    private val minAge: Int,
    private val labels: EvaluationLabels.General
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val age = referenceYear - record.patient.birthYear
        return when {
            age > minAge -> EvaluationFactory.pass(labels.hasAtLeastCertainAgePass(minAge))

            age == minAge -> EvaluationFactory.undetermined(
                labels.hasAtLeastCertainAgeUndetermined(record.patient.birthYear, minAge)
            )

            else -> EvaluationFactory.fail(labels.hasAtLeastCertainAgeFail(minAge))
        }
    }
}