package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSpecificComplication(private val termToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val complications = record.complications
            ?: return EvaluationFactory.recoverableUndetermined("Undetermined whether patient has cancer-related complications")

        val matchingComplications = complications.map { it.name }
            .filter { it.lowercase().contains(termToFind.lowercase()) }

        if (matchingComplications.isNotEmpty()) {
            return EvaluationFactory.pass("Present complications(s): " + concat(matchingComplications))
        }
        return if (hasComplicationsWithoutNames(record)) {
            EvaluationFactory.undetermined("Complications present but unknown if belonging to $termToFind")
        } else
            EvaluationFactory.fail("Complication $termToFind not present")
    }

    companion object {
        private fun hasComplicationsWithoutNames(record: PatientRecord): Boolean {
            return record.clinicalStatus.hasComplications == true
                    && record.complications?.any { ComplicationFunctions.isYesInputComplication(it) } ?: false
        }
    }
}