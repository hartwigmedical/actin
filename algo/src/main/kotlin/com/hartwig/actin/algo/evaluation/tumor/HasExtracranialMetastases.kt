package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasExtracranialMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasNonCnsMetastases = with(record.tumor) {
            hasBoneLesions == true || hasLungLesions == true || hasLiverLesions == true || hasLymphNodeLesions == true
        }
        val uncategorizedLesions = record.tumor.otherLesions

        return when {
            hasNonCnsMetastases -> {
                EvaluationFactory.pass("Patient has extracranial metastases", "Extracranial metastases present")
            }

            !uncategorizedLesions.isNullOrEmpty() || record.tumor.hasCnsLesions == true -> {
                EvaluationFactory.undetermined(
                    "Undetermined if extracranial metastases present",
                    "Undetermined if extracranial metastases present"
                )
            }

            else -> {
                EvaluationFactory.fail("Patient does not have extracranial metastases", "No extracranial metastases")
            }
        }
    }
}