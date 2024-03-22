package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.Medication

fun medicationWhenProvidedEvaluation(
    record: PatientRecord,
    evaluationFunction: (medication: List<Medication>) -> Evaluation
): Evaluation {
    return record.medications?.let { evaluationFunction.invoke(it) }
        ?: EvaluationFactory.recoverableUndeterminedNoGeneral("No medication data provided")
}