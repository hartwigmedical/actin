package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd

class HasAnyComplication: EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return record.clinicalStatus.hasComplications?.let { hasComplications: Boolean ->
            if (hasComplications) {
                val complicationString =
                    concatLowercaseWithAnd(record.complications?.map { it.name.ifEmpty { "Unknown" } } ?: emptyList())
                EvaluationFactory.pass(
                    "Patient has at least one cancer-related complication: $complicationString",
                    "Present complication(s): $complicationString"
                )
            } else {
                return EvaluationFactory.fail(
                    "Patient has no cancer-related complications", "No cancer-related complications present"
                )
            }
        } ?: EvaluationFactory.undetermined(
            "Undetermined whether patient has cancer-related complications", "Undetermined complication status"
        )
    }
}