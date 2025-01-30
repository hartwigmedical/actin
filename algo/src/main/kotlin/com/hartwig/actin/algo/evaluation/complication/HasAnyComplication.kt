package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAnyComplication: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return record.clinicalStatus.hasComplications?.let { hasComplications: Boolean ->
            if (hasComplications) {
                val complicationString =
                    concatLowercaseWithAnd(record.complications.map { it.name ?: "Unknown" })
                EvaluationFactory.pass("Has at least one cancer-related complication: $complicationString")
            } else {
                return EvaluationFactory.fail("No cancer-related complications present")
            }
        } ?: EvaluationFactory.undetermined("Undetermined whether patient has cancer-related complications")
    }
}