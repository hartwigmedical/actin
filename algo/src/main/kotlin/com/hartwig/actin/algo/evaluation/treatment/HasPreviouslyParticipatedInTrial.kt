package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasPreviouslyParticipatedInTrial(
    private val acronym: String,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val trialEntries = record.oncologicalHistory.filter { it.isTrial }
        val matchingTrial = trialEntries.filter { it.trialAcronym.equals(acronym, true) }

        return when {
            matchingTrial.isNotEmpty() -> {
                EvaluationFactory.pass(labels.hasPreviouslyParticipatedInTrialPass(acronym))
            }

            trialEntries.any { it.trialAcronym == null } -> {
                EvaluationFactory.undetermined(labels.hasPreviouslyParticipatedInTrialUndetermined(acronym))
            }

            else -> {
                EvaluationFactory.fail(labels.hasPreviouslyParticipatedInTrialFail(acronym))
            }
        }
    }
}