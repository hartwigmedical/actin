package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat

class HasSpecificComplication internal constructor(private val termToFind: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val complications = record.complications ?: return EvaluationFactory.undetermined(
            "Undetermined whether patient has cancer-related complications",
            "Undetermined complication status"
        )

        val matchingComplications = complications.map { it.name }
            .filter { it.lowercase().contains(termToFind.lowercase()) }

        if (matchingComplications.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Patient has complication " + concat(matchingComplications),
                "Present " + concat(matchingComplications)
            )
        }
        return if (hasComplicationsWithoutNames(record)) {
            EvaluationFactory.undetermined(
                "Patient has complications but type of complications unknown. Undetermined if belonging to $termToFind",
                "Complications present, unknown if belonging to $termToFind"
            )
        } else
            EvaluationFactory.fail(
                "Patient does not have complication $termToFind",
                "Complication $termToFind not present"
            )
    }

    companion object {
        private fun hasComplicationsWithoutNames(record: PatientRecord): Boolean {
            return record.clinicalStatus.hasComplications == true
                    && record.complications?.any { ComplicationFunctions.isYesInputComplication(it) } ?: false
        }
    }
}