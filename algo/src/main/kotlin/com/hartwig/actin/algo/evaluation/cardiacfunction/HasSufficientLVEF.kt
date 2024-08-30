package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSufficientLVEF(private val minLVEF: Double) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lvef = record.clinicalStatus.lvef

        return if (lvef == null) {
            EvaluationFactory.notEvaluated("No LVEF known", "LVEF unknown")
        } else if (lvef.compareTo(minLVEF) >= 0) {
            EvaluationFactory.pass("LVEF of $lvef exceeds minimum LVEF required ", "LVEF of $lvef exceeds $minLVEF")
        } else {
            EvaluationFactory.fail("LVEF of $lvef is below minimum LVEF of $minLVEF", "LVEF of $lvef below $minLVEF")
        }
    }
}