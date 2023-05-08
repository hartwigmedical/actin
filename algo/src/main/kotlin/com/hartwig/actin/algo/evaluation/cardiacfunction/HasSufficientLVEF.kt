package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasSufficientLVEF internal constructor(private val minLVEF: Double, private val passIfUnknown: Boolean) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val lvef = record.clinical().clinicalStatus().lvef() ?: return if (passIfUnknown) {
            EvaluationFactory.pass("No LVEF known", "LVEF unknown")
        } else {
            EvaluationFactory.undetermined("No LVEF known", "LVEF unknown")
        }
        return if (lvef.compareTo(minLVEF) >= 0) {
            EvaluationFactory.pass("LVEF of $lvef exceeds minimum LVEF required ", "LVEF of $lvef exceeds $minLVEF")
        } else {
            EvaluationFactory.fail("LVEF of $lvef is below minimum LVEF of $minLVEF", "LVEF of $lvef below $minLVEF")
        }
    }
}