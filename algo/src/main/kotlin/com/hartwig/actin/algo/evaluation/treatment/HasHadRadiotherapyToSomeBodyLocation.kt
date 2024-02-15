package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasHadRadiotherapyToSomeBodyLocation(private val bodyLocation: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Undetermined if patient has received radiotherapy to body location $bodyLocation",
            "Undetermined if received radiotherapy to body location $bodyLocation "
        )
    }
}