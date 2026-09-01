package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasReceivedNonLiveVaccineWithinWeeks(private val minWeeks: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined("Non-live vaccine use within $minWeeks weeks undetermined based on provided history")
    }
}