package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.Medication

val MEDICATION_NOT_PROVIDED = EvaluationFactory.recoverableUndeterminedNoGeneral("No medication data provided")

fun medicationWhenProvidedEvaluation(
    record: PatientRecord,
    evaluationFunction: (medication: List<Medication>) -> Evaluation
): Evaluation {
    return record.medications?.let { evaluationFunction.invoke(it) } ?: MEDICATION_NOT_PROVIDED
}