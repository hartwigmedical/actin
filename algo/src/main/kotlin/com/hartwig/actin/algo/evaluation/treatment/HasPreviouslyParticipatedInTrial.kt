package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasPreviouslyParticipatedInTrial(private val acronym: String? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val acronymString = acronym?.let { " $it" } ?: ""
        return if (record.oncologicalHistory.any { it.isTrial && (acronym == null || it.trialAcronym.equals(acronym, true)) }) {
            EvaluationFactory.pass("Has previously participated in trial$acronymString")
        } else {
            EvaluationFactory.fail("Has not participated in trial$acronymString")
        }
    }
}