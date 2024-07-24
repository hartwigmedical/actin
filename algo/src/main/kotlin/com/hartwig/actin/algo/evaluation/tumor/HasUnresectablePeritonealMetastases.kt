package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasUnresectablePeritonealMetastases : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return when (TumorTypeEvaluationFunctions.hasPeritonealMetastases(record.tumor)) {
            null -> {
                EvaluationFactory.undetermined("Missing metastases data")
            }

            true -> {
                EvaluationFactory.warn("Undetermined if peritoneal metastases are unresectable")
            }

            else -> {
                EvaluationFactory.fail("Patient has no unresectable peritoneal metastases", "No unresectable peritoneal metastases")
            }
        }
    }
}