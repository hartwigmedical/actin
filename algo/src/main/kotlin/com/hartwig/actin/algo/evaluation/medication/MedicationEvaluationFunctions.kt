package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.algo.Evaluation

fun medicationNotProvided(labels: EvaluationLabels.Medication): Evaluation =
    EvaluationFactory.recoverableUndetermined(labels.medicationNotProvided())