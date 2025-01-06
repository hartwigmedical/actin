package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatItems
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

//TODO (CB): only 1 message
class HasHadSomeSpecificTreatments(private val treatments: List<Treatment>, private val minTreatmentLines: Int) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val namesToMatch = treatments.map { it.name.lowercase() }.toSet()
        val matchTreatments = record.oncologicalHistory
            .filter { it.allTreatments().any { treatment -> treatment.name.lowercase() in namesToMatch } }
        val allowTrialMatches =
            treatments.any { it.categories().isEmpty() || it.categories().any(TrialFunctions::categoryAllowsTrialMatches) }
        val trialMatchCount = if (allowTrialMatches) {
            record.oncologicalHistory.count { it.isTrial && it.treatments.isEmpty() }
        } else 0

        val treatmentListing = concatItems(treatments)

        return when {
            matchTreatments.size >= minTreatmentLines -> {

                EvaluationFactory.pass("Has received ${matchTreatments.size} prior line(s) of $treatmentListing ")
            }

            matchTreatments.size + trialMatchCount >= minTreatmentLines -> {
                EvaluationFactory.undetermined("Undetermined if received $minTreatmentLines prior line(s) of $treatmentListing")
            }

            else -> {
                EvaluationFactory.fail("Has not received $minTreatmentLines prior line(s) of $treatmentListing")
            }
        }
    }
}