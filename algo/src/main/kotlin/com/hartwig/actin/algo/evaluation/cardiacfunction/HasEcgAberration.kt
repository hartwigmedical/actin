package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasEcgAberration internal constructor() : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return if (record.ecgs.isEmpty()) {
            EvaluationFactory.recoverableFail("Missing ECG details - assumed no ECG abnormalities")
        } else {
            val aberrations = Format.concat(record.ecgs.map { it.name ?: "details unknown" })
            EvaluationFactory.recoverablePass("ECG abnormalities present ($aberrations)")
        }
    }
}