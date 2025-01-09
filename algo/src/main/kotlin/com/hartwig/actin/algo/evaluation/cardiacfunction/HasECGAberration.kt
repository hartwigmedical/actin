package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasECGAberration internal constructor() : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ecg = record.clinicalStatus.ecg
            ?: return EvaluationFactory.fail("Assumed that there are no ECG abnormalities (ECG details missing)")
        return when {
            ecg.hasSigAberrationLatestECG && ecg.aberrationDescription != null -> {
                EvaluationFactory.pass("ECG abnormalities present (${ecg.aberrationDescription})")
            }

            ecg.hasSigAberrationLatestECG -> {
                EvaluationFactory.pass("ECG abnormalities present (details unknown)")
            }

            else ->
                EvaluationFactory.fail("No known ECG abnormalities")
        }
    }
}