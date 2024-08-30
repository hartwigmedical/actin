package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasPreviouslyParticipatedInTrial(private val acronym: String? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val acronymString = acronym?.let { " $it" } ?: ""
        val trialEntries = record.oncologicalHistory.filter { it.isTrial }
        return when {
            (acronym == null && trialEntries.isNotEmpty()) || trialEntries.any { it.trialAcronym.equals(acronym, true) } -> {
                EvaluationFactory.pass("Has previously participated in trial$acronymString")
            }

            trialEntries.any { it.trialAcronym == null } -> {
                EvaluationFactory.undetermined("Previous trial participation but unknown if trial$acronymString")
            }
           
            else -> {
                EvaluationFactory.fail("Has not participated in trial$acronymString")
            }
        }
    }
}